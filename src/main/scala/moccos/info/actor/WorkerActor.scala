package moccos.info.actor

import akka.actor.Actor
import moccos.info._

class WorkerActor(actor_no: Int, nr_iter: Int, weight: Int, base_time: Long) extends Actor {
  // println(s"I am actor #$actor_no. iter: $nr_iter")

  def sendReport(seq_no: Int, start_time: Long) = {
    val end_time = System.currentTimeMillis() - base_time
    val msg = Report(actor_no, seq_no, Thread.currentThread().getId().toInt, start_time, end_time)
    context.parent ! msg
  }

  def doHeavyTask(weight: Int): Unit = {
    def fib(n: Int): Int = n match {
      case 0 => 1
      case 1 => 1
      case _ => fib(n - 2) + fib(n - 1)
    }
    fib(weight)
  }

  override def receive = {
    case Run =>
      self ! Next(0)
    case Next(seq_no) =>
      val start_time = System.currentTimeMillis() - base_time
      doHeavyTask(weight)
      sendReport(seq_no, start_time)
      (nr_iter > seq_no + 1) match {
        case true => self ! Next(seq_no + 1)
        case false => context.parent ! Finished
      }
  }

}
