package services.discovery.components.analyzer

import java.util.UUID

import org.apache.jena.rdf.model.{Model, ModelFactory, Resource}
import services.discovery.model._
import services.discovery.model.components._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LinksetBasedUnion extends UnionInstance {

    case class LinksetBasedUnionState(subjectsClass: String, objectsClass: String) extends ComponentState

    val linksPortName = "LINKS"
    val dataSource1PortName = "DS1"
    val dataSource2PortName = "DS2"

    val linksPort = Port(linksPortName, 1)
    val source1Port = Port(dataSource1PortName, 2)
    val source2Port = Port(dataSource2PortName, 2)

    val linksDescriptor = SelectQuery(
        """
          |PREFIX void: <http://rdfs.org/ns/void#>
          |
          |SELECT ?subjectsClass ?objectsClass
          |WHERE {
          |    ?ls a void:Linkset ;
          |        void:subjectsTarget ?subjectsDs ;
          |        void:objectsTarget ?objectsDs .
          |    ?subjectsDs void:class ?subjectsClass .
          |    ?objectsDs void:class ?objectsClass .
          |}
        """.stripMargin
    )

    def classDescriptor(isClass: String) = AskQuery(
        s"""
           |PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>
           |
           |ASK {
           |   ?x a <$isClass> .
           |}
        """.stripMargin
    )

    override val getInputPorts: Seq[Port] = Seq(linksPort, source1Port, source2Port)

    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = port match {
        case Port(`linksPortName`, _) => checkLinks(state, outputDataSample, discoveryId, iterationNumber)
        case Port(`dataSource1PortName`, _) => checkClassPresence(s => s.subjectsClass, state, outputDataSample, discoveryId, iterationNumber)
        case Port(`dataSource2PortName`, _) => checkClassPresence(s => s.objectsClass, state, outputDataSample, discoveryId, iterationNumber)
    }

    def checkLinks(state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = state match {
        case None => outputDataSample.executeSelect(linksDescriptor, discoveryId, iterationNumber).map { resultSet =>
            resultSet.hasNext match {
                case false => PortCheckResult(PortCheckResult.Status.Failure)
                case true => {
                    val result = resultSet.next()
                    val subjects = result.get("subjectsClass").asResource().getURI
                    val objects = result.get("objectsClass").asResource().getURI
                    val newState = LinksetBasedUnionState(subjects, objects)
                    PortCheckResult(PortCheckResult.Status.Success, Some(newState))
                }
            }
        }
        case _ => Future.successful(PortCheckResult(PortCheckResult.Status.Failure, state))
    }


    def checkClassPresence(classUriSelector: LinksetBasedUnionState => String, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = state match {
        case None => Future.successful(PortCheckResult(PortCheckResult.Status.Failure, state))
        case Some(s) => {
            val classUri = classUriSelector(s.asInstanceOf[LinksetBasedUnionState])
            outputDataSample.executeAsk(classDescriptor(classUri), discoveryId, iterationNumber).map { bool =>
                PortCheckResult(bool, state)
            }
        }
    }

    private def getSampleInstance(dataSample: DataSample, linkClassUri: String, discoveryId: UUID, iterationNumber: Int): Future[Resource] = {
        dataSample.executeSelect(SelectQuery(s"""SELECT ?x WHERE { ?x a <$linkClassUri> }""".stripMargin), discoveryId, iterationNumber).map { rs =>
            rs.hasNext match {
                case true => rs.next().getResource("x")
                case _ => throw new Exception
            }
        }
    }

    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
        state match {
            case None => Future.failed(new Exception)
            case Some(s) => generateOutputDataSample(s, dataSamples, discoveryId, iterationNumber)
        }
    }

    private def generateOutputDataSample(componentState: ComponentState, dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {

        val lbuState = componentState.asInstanceOf[LinksetBasedUnionState]
        val sample1 = getSampleInstance(dataSamples(source1Port), lbuState.subjectsClass, discoveryId, iterationNumber)
        val sample2 = getSampleInstance(dataSamples(source2Port), lbuState.objectsClass, discoveryId, iterationNumber)

        for {
            s1 <- sample1
            s2 <- sample2
            l <- generateLink(dataSamples, s1, s2, discoveryId, iterationNumber)
        } yield enrichDataSampleWithLink(l, dataSamples, discoveryId, iterationNumber)
    }

    private def enrichDataSampleWithLink(generatedLink: Model, dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): ModelDataSample = {
        val result = unionSamples(dataSamples, discoveryId, iterationNumber).add(generatedLink)
        ModelDataSample(result)
    }

    private def generateLink(dataSamples: Map[Port, DataSample], sample1: Resource, sample2: Resource, discoveryId: UUID, iterationNumber: Int): Future[Model] = {
        dataSamples(linksPort).executeConstruct(
            ConstructQuery(
                s"""
                   | PREFIX void: <http://rdfs.org/ns/void#>
                   |
                   | CONSTRUCT {
                   |   <${sample1.getURI}> ?p <${sample2.getURI}>
                   | } WHERE {
                   |   [] void:linkPredicate ?p .
                   | }
                   | """.stripMargin
            ),
            discoveryId,
            iterationNumber
        )
    }

    private def unionSamples(dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Model = {
        val models = dataSamples.values.map(_.getModel(discoveryId, iterationNumber))
        val result = ModelFactory.createDefaultModel()
        models.foreach(result.add)
        result
    }
}
