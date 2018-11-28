import better.files._

import scala.io.Source

val baseFolder = "D:\\mff-experiments"
val experiments = baseFolder.toFile.list(f => f.isDirectory)
val header1csv = "\"Discovery ID\",\"Discovery URI\",\"Application group count\",\"Datasource group count\",\"Extractor group count\",\"Data sample group count\",\"Pipeline count\",\"Discovery duration\",\"Application count\",\"Data source count\",\"Transformer count\""


val allLines = for {
    i <- 0 to 47
} yield experiments.map { e =>
    val discoveryPath = s"{$baseFolder}\\dis-${"%03d".format(i)}\\1.csv"
    val lines = Source.fromFile(discoveryPath).getLines().toSeq
    lines.last
}

val content = Seq(header1csv) ++ allLines.flatten

s"$baseFolder\\1.csv".toFile.createFileIfNotExists().writeText(content.mkString("\n"))