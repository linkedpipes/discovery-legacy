import better.files._

import scala.io.Source

val baseFolder = "D:\\mff-experiments\\"
val experiments = baseFolder.toFile.children.toList
val header1csv = "\"Experiment\",\"Discovery ID\",\"Discovery URI\",\"Application group count\",\"Datasource group count\",\"Extractor group count\",\"Data sample group count\",\"Pipeline count\",\"Discovery duration\",\"Application count\",\"Data source count\",\"Transformer count\""


val allLines = for {
    i <- 0 to 47
} yield experiments.map { e =>
    val discoveryPath = s"$baseFolder\\${e.name}\\dis-${"%03d".format(i)}\\1.csv"
    if (discoveryPath.toFile.exists){
        val lines = Source.fromFile(discoveryPath).getLines().toSeq
        s"${e.name},${lines.last}"
    } else {
        ""
    }
}

val content = Seq(header1csv) ++ allLines.flatten.filterNot(l => l.isEmpty)

s"$baseFolder\\1.csv".toFile.createFileIfNotExists().writeText(content.mkString("\n"))