import play.api.libs.json.Json

import scala.io.Source
import better.files._
import java.io.{File => JFile}


val transformersData = Source.fromFile("gen/transformers.json").getLines().mkString("\n")
val transformers = Json.parse(transformersData)
