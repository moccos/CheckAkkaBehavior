package moccos.info

import akka.actor._
import java.util.concurrent.{ TimeoutException, TimeUnit }
import com.typesafe.config.{ ConfigFactory, Config }
import moccos.info.actor.ManagerActor
import scala.util.control.Exception._

// import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

case class Report(actor_no: Int, seq_no: Int, thread_no: Int, start_time: Long, end_time: Long) {
  override def toString() = f"[$actor_no% 2d: #$seq_no%03d at $thread_no% 4d] $start_time% 7d -> $end_time% 7d"
}
case object Run
case class Next(seq_no: Int)
case object Finished
case class StartAll(nr_actor: Int, nr_iter: Int, weight: Int)

object Behaviorcheck extends App {
  // default settings
  var nr_actor = 3
  var nr_message = 5
  var computation_weight = 39

  def printSystemInfo() = {
    println("=== [System Information] ===")
    println("Processors: " + Runtime.getRuntime().availableProcessors())
    println("Thread active: " + Thread.activeCount)
    println("Current Thread: " + Thread.currentThread().getId())
  }

  def printConfiguration(conf: Config) = {
    println("=== [Akka Configuration] ===")
    val conf_akka = conf.getConfig("akka.actor")
    val conf_dispatcher = conf_akka.getConfig("default-dispatcher")

    val throughput = conf_dispatcher.getInt("throughput")
    val deadline = conf_dispatcher.getString("throughput-deadline-time")
    println(s"throughput: $throughput, deadline: $deadline")

    val factor = conf_dispatcher.getDouble("fork-join-executor.parallelism-factor")
    val min = conf_dispatcher.getDouble("fork-join-executor.parallelism-min")
    val max = conf_dispatcher.getDouble("fork-join-executor.parallelism-max")

    println(s"factor: $factor [Min $min -> $max Max]")
  }

  def printUsage() = {
    println()
    println("Optional arguments:")
    println("\t-a #    : number of actors")
    println("\t-n #    : number of messages (per actor)")
    println("\t-w #    : weight of computation")
    println()
    println("Optional arguments (Akka default-dispatcher):")
    println("\t-at #   : <akka> throughput")
    println("\t-atd #  : <akka> throughput-deadline-time")
    println("\t-apf #  : <akka> parallelism-factor")
    println("\t-apmin #: <akka> parallelism-min")
    println("\t-apmax #: <akka> parallelism-max")
    println()
    println("To run this app without sbt, execute \"sbt stage\". Then run target/start(.bat)")
    println()
  }

  def overwriteConfig(param: String, value: String, conf: Config) = {
    ConfigFactory.parseString(s"akka.actor.default-dispatcher.$param = $value").withFallback(conf)
  }

  def parseOption(args: List[String], conf: Config): (List[String], Config) = args match {
    case ("-a" | "--actor") :: n :: xs =>
      allCatch opt n.toInt match {
        case Some(x) if x > 0 => nr_actor = x
        case _ => parseOption(xs, conf)
      }
      parseOption(xs, conf)
    case ("-n") :: n :: xs =>
      allCatch opt n.toInt match {
        case Some(x) if x > 0 => nr_message = x
        case _ => parseOption(xs, conf)
      }
      parseOption(xs, conf)
    case ("-w") :: n :: xs =>
      allCatch opt n.toInt match {
        case Some(x) if x > 0 => computation_weight = x
        case _ => parseOption(xs, conf)
      }
      parseOption(xs, conf)
    case ("-at") :: n :: xs =>
      allCatch opt n.toInt match {
        case Some(x) if x > 0 =>
          parseOption(xs, overwriteConfig("throughput", n, conf))
        case _ => parseOption(xs, conf)
      }
    case ("-atd") :: n :: xs =>
      parseOption(xs, overwriteConfig("throughput-deadline-time", n, conf))
    case ("-apf") :: n :: xs =>
      allCatch opt n.toFloat match {
        case Some(x) if x > 0.0 =>
          parseOption(xs, overwriteConfig("fork-join-executor.parallelism-factor", n, conf))
        case _ => parseOption(xs, conf)
      }
    case ("-apmin") :: n :: xs =>
      allCatch opt n.toInt match {
        case Some(x) if x > 0 =>
          parseOption(xs, overwriteConfig("fork-join-executor.parallelism-min", n, conf))
        case _ => parseOption(xs, conf)
      }
    case ("-apmax") :: n :: xs =>
      allCatch opt n.toInt match {
        case Some(x) if x > 0 =>
          parseOption(xs, overwriteConfig("fork-join-executor.parallelism-max", n, conf))
        case None => parseOption(xs, conf)
      }
    case ("-h" | "--help") :: _ =>
      printUsage()
      sys.exit(0)
    case Nil => (args, conf)
    case _ =>
      println("Failed to parse argument(s): " + args)
      sys.exit(0)
  }

  val (_, config) = parseOption(args.toList, ConfigFactory.load())
  val system = ActorSystem("Behaviorcheck", config)
  printConfiguration(config)
  printSystemInfo()
  val manager = system.actorOf(Props[ManagerActor])

  val inbox = Inbox.create(system)
  inbox.send(manager, StartAll(nr_actor, nr_message, computation_weight))
  val timeout = FiniteDuration(1, TimeUnit.MINUTES)

  try {
    val msg = inbox.receive(timeout)
    println("Finished all tasks.")
  } catch {
    case _: TimeoutException => println("TimeoutException: " + timeout)
  }

  system.shutdown()
}
