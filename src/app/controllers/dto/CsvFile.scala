package controllers.dto

case class CsvFile(name: String, lines: Seq[CsvLine]) {
    def content = lines.map(_.content).mkString("\n")
}
