package dicom

import akka.actor.Actor
import akka.actor.Props

class ArchiveProcessorActor extends Actor {
  def receive: Receive = {
    case root: String => SonoArchiveService.processCanonArchive(root)
  }
}

object ArchiveProcessorActor {
  def props: Props = Props[ArchiveProcessorActor]
}




