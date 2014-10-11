package moccos.info.actor

import akka.actor.{ ActorRef, Props, Actor }
import scala.collection.mutable.Map
import moccos.info.{ Report, Run, Finished, StartAll }

class ManagerActor extends Actor {
  var not_finished = 0
  var initiator: ActorRef = _
  var thread_history: Map[Int, List[Report]] = Map.empty
  var actor_history: Map[Int, List[Report]] = Map.empty
  println()
  println("=== [Report] ===")

  private[this] def report(name: String, collection: Map[Int, List[Report]]) = {
    println()
    println(s"=== [Group by $name] ===")

    val keys = collection.keys.toList.sorted
    keys.foreach(key => {
      println(s" # $name $key")
      collection(key).foreach(println)
    })
  }

  private[this] def addReport(r: Report, i: Int, collection: Map[Int, List[Report]]) = {
    collection.get(i) match {
      case None => collection + (i -> List(r))
      case Some(list) =>
        collection(i) = list :+ r
        collection
    }
  }

  override def receive = {
    case StartAll(nr_actor, nr_iter, weight) =>
      not_finished = nr_actor
      initiator = context.sender()

      val actors = Range(0, nr_actor).map(actor_no =>
        context.actorOf(Props(classOf[WorkerActor], actor_no, nr_iter, weight, System.currentTimeMillis()))
      )
      actors.foreach(actor =>
        actor ! Run
      )

    case r: Report =>
      println(r)
      thread_history = addReport(r, r.thread_no, thread_history)
      actor_history = addReport(r, r.actor_no, actor_history)

    case Finished =>
      not_finished -= 1
      if (not_finished == 0) {
        report("Thread", thread_history)
        report("Actor", actor_history)
        initiator ! Finished
      }
  }

}
