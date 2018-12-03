package services.discovery

class Timer {

    private var startTime : Option[Long] = None

    private var endTime : Option[Long] = None

    def start = {
        startTime = Some(now)
    }

    def stop = {
        endTime = Some(now)
    }

    def duration: Option[Long] = {
        startTime match {
            case Some(s) => {
                endTime match {
                    case Some(e) => Some(diff(s,e))
                    case _ => Some(diff(s, now))
                }
            }
            case _ => None
        }
    }

    private def now = System.nanoTime()

    private def diff(start: Long, end: Long) = {
        (end - start) / (1000 * 1000) // ns -> ms
    }
}
