package controllers.dto

case class CsvLine(cells: Seq[Any]) {
    def content = cells.map(i => "\"" + i.toString.replace("\"","\"\"") + "\"").mkString(",")
}