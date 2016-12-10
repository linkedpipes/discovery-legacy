package services.discovery.components.application

class TimeInstantsApplication extends SimpleApplication {

    override protected val prefixes: String = "PREFIX time: <http://www.w3.org/2006/time#>"

    override protected val whereClause: String = "[] time:inXSDDateTime ?ed ."
}
