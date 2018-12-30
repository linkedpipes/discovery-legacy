package services

import java.io.{BufferedOutputStream, ByteArrayOutputStream}
import java.util.UUID
import java.util.zip.{ZipEntry, ZipOutputStream}

import controllers.dto.{CsvFile, CsvLine, CsvRequestData, PipelineGrouping}
import javax.inject.{Inject, Singleton}
import services.discovery.Discovery

import scala.collection.mutable
import scala.concurrent.ExecutionContext


@Singleton
class StatisticsService @Inject()(implicit executionContext: ExecutionContext) {
    private val csvRequests = new scala.collection.mutable.HashMap[UUID, Seq[CsvRequestData]]

    def addCsvRequest(indexes: Seq[CsvRequestData]) : UUID = {
        val id = UUID.randomUUID()
        csvRequests.put(id, indexes)
        id
    }

    def getCsvRequest(id: UUID): Option[Seq[CsvRequestData]] = csvRequests.get(id)

    def getZip(csvRequestData: Seq[CsvRequestData], discoveryService: DiscoveryService): ByteArrayOutputStream = {
        val outputByteStream = new ByteArrayOutputStream()
        val zip = new ZipOutputStream(new BufferedOutputStream(outputByteStream))

        getCsvFiles(csvRequestData, discoveryService).foreach { csvFile =>
            zip.putNextEntry(new ZipEntry(csvFile.name))
            zip.write(csvFile.content.getBytes("UTF-8"))
            zip.closeEntry()
        }
        zip.close()
        outputByteStream
    }

    def getCsvFiles(csvRequests: Seq[CsvRequestData], discoveryService: DiscoveryService) : Seq[CsvFile] = {

        val pipelineGroupingsById = new mutable.HashMap[String, PipelineGrouping]()
        csvRequests.foreach { i => pipelineGroupingsById.put(i.discoveryId, discoveryService.getPipelinesOfDiscovery(i.discoveryId).map(PipelineGrouping.create).get) }

        val pipelineGroupingsByIdMap = pipelineGroupingsById.toMap

        Seq(
            CsvFile("1.csv", getGlobalCsvStats(csvRequests, pipelineGroupingsByIdMap, discoveryService)),
            CsvFile("2.csv", getDataSourceExperimentCsvStats(csvRequests, pipelineGroupingsByIdMap, discoveryService)),
            CsvFile("3.csv", getApplicationExperimentCsvStats(csvRequests, pipelineGroupingsByIdMap, discoveryService)),
            CsvFile("4.csv", getDataSourceApplicationExperimentCsvStats(csvRequests, pipelineGroupingsByIdMap, discoveryService))
        ) ++ csvRequests.map {
            i => CsvFile(s"${i.discoveryId}.csv", getDetailedCsv(i.discoveryId, discoveryService))
        }
    }

    // 1.csv
    private def getGlobalCsvHeader = CsvLine(Seq(
        "Discovery ID",
        "Discovery URI",
        "Application group count",
        "Datasource group count",
        "Extractor group count",
        "Data sample group count",
        "Pipeline count",
        "Discovery duration",
        "Application count",
        "Data source count",
        "Transformer count"
    ))

    // 2.csv
    private def getDataSourceExperimentCsvHeader = CsvLine(Seq(
        "Discovery ID",
        "Discovery URI",
        "Datasource URI",
        "Datasource label",
        "Extractor group count",
        "Application count",
        "Pipeline count"
    ))

    // 3.csv
    private def getApplicationExperimentCsvHeader = CsvLine(Seq(
        "Discovery ID",
        "Discovery URI",
        "Application URI",
        "Application label",
        "Extractor group count",
        "Datasource count",
        "Pipeline count"
    ))

    // 4.csv
    private def getDataSourceApplicationExperimentCsvHeader = CsvLine(Seq(
        "Discovery ID",
        "Discovery URI",
        "Datasource URI",
        "Application URI",
        "Datasource label",
        "Application label",
        "Data sample group count",
        "Pipeline count"
    ))

    private def getDetailedCsvHeader = CsvLine(Seq(
        "Discovery ID",
        "Application group",
        "Datasource group",
        "Extractor group",
        "Data sample group",
        "Datasources",
        "Transformer count",
        "Extractors",
        "Transformers",
        "Application",
        "Iteration number",
        "Execute URL"
    ))

    // 1.csv
    private def getGlobalCsvStats(csvRequests: Seq[CsvRequestData], pipelineGroupings: Map[String, PipelineGrouping], discoveryService: DiscoveryService) : Seq[CsvLine] = {
        Seq(getGlobalCsvHeader) ++ withCsvRequests(csvRequests, pipelineGroupings, discoveryService) { case (csvRequest, discovery, grouping) =>
            grouping.map { g =>
                CsvLine(Seq(
                    csvRequest.discoveryId,
                    csvRequest.inputIri,
                    g.applicationGroups.size,
                    g.applicationGroups.map(ag => ag.dataSourceGroups.size).sum,
                    g.applicationGroups.map(ag => ag.dataSourceGroups.map(ds => ds.extractorGroups.size).sum).sum,
                    g.applicationGroups.map(ag => ag.dataSourceGroups.map(ds => ds.extractorGroups.map(eg => eg.dataSampleGroups.size).sum).sum).sum,
                    g.pipelines.size,
                    discovery.timer.duration.get,
                    discovery.input.applications.size,
                    discovery.input.dataSets.size,
                    discovery.input.processors.size
                ))
            }.toSeq
        }
    }

    // 2.csv
    private def getDataSourceExperimentCsvStats(csvRequests: Seq[CsvRequestData], pipelineGroupings: Map[String, PipelineGrouping], discoveryService: DiscoveryService) : Seq[CsvLine] = {
        Seq(getDataSourceExperimentCsvHeader) ++ withCsvRequests(csvRequests, pipelineGroupings, discoveryService) { case (csvRequest, discovery, grouping) =>
            val dataSources = discovery.input.dataSets.map(ds => ds.dataSourceInstance)
            dataSources.map { dataSource =>
                CsvLine(Seq(
                    csvRequest.discoveryId,
                    csvRequest.inputIri,
                    dataSource.iri,
                    dataSource.label,
                    grouping.map { g =>
                        g.applicationGroups.map(_.dataSourceGroups.filter(_.dataSourceInstances.contains(dataSource)).map(_.extractorGroups).size).sum
                    }.get,
                    grouping.map { g =>
                        g.applicationGroups.count(_.dataSourceGroups.exists(_.dataSourceInstances.contains(dataSource)))
                    }.get,
                    discoveryService.getPipelinesOfDiscovery(csvRequest.discoveryId).map(pipelines => pipelines.count(p => p._2.components.exists(c => c.componentInstance == dataSource))).get
                ))
            }
        }
    }

    // 3.csv
    private def getApplicationExperimentCsvStats(csvRequests: Seq[CsvRequestData], pipelineGroupings: Map[String, PipelineGrouping], discoveryService: DiscoveryService) : Seq[CsvLine] = {
        Seq(getApplicationExperimentCsvHeader) ++ withCsvRequests(csvRequests, pipelineGroupings, discoveryService) { case (csvRequest, discovery, grouping) =>
            discovery.input.applications.map { a =>
                CsvLine(Seq(
                    csvRequest.discoveryId,
                    csvRequest.inputIri,
                    a.iri,
                    a.label,
                    grouping.map { g =>
                        g.applicationGroups.filter(_.applicationInstance == a).map(_.dataSourceGroups.map(_.extractorGroups).size).sum
                    }.get,
                    grouping.map { g =>
                        g.applicationGroups.filter(_.applicationInstance == a).flatMap(_.dataSourceGroups.map(_.dataSourceInstances)).distinct.size
                    }.get,
                    discoveryService.getPipelinesOfDiscovery(csvRequest.discoveryId).map(pipelines => pipelines.count(p => p._2.components.exists(c => c.componentInstance == a))).get
                ))
            }
        }
    }

    // 4.csv
    private def getDataSourceApplicationExperimentCsvStats(csvRequests: Seq[CsvRequestData], pipelineGroupings: Map[String, PipelineGrouping], discoveryService: DiscoveryService) : Seq[CsvLine] = {
        Seq(getDataSourceApplicationExperimentCsvHeader) ++ withCsvRequests(csvRequests, pipelineGroupings, discoveryService) { case (csvRequest, discovery, grouping) =>
            discovery.input.applications.flatMap { applicationInstance =>
                val dataSources = discovery.input.dataSets.map(ds => ds.dataSourceInstance)
                dataSources.map { datasourceInstance =>
                    CsvLine(Seq(
                        csvRequest.discoveryId,
                        csvRequest.inputIri,
                        datasourceInstance.iri,
                        applicationInstance.iri,
                        datasourceInstance.label,
                        applicationInstance.label,
                        grouping.map { g =>
                            g.applicationGroups.filter(ag => ag.applicationInstance == applicationInstance)
                                .flatMap(applicationGroup => applicationGroup.dataSourceGroups)
                                .filter(_.dataSourceInstances.contains(datasourceInstance))
                                .map(_.extractorGroups.map(_.dataSampleGroups.size).sum)
                                .sum
                        }.get,
                        discoveryService.getPipelinesOfDiscovery(csvRequest.discoveryId).map(pipelines => pipelines.count { p =>
                            p._2.components.exists(c => c.componentInstance == applicationInstance) && p._2.components.exists(c => c.componentInstance == datasourceInstance)
                        }).get
                    ))
                }
            }
        }
    }

    def getDetailedCsv(id: String, discoveryService: DiscoveryService) : Seq[CsvLine] = {
        val maybeGrouping = discoveryService.getPipelinesOfDiscovery(id).map { pipelineMap => PipelineGrouping.create(pipelineMap) }
        val body = maybeGrouping.map { grouping =>
            grouping.applicationGroups.flatMap { applicationGroup =>
                applicationGroup.dataSourceGroups.flatMap { dataSourceGroup =>
                    dataSourceGroup.extractorGroups.flatMap { extractorGroup =>
                        extractorGroup.dataSampleGroups.sortBy(g => g.minimalIteration).flatMap { dataSampleGroup =>
                            val pipelines = dataSampleGroup.pipelines.toSeq.sortBy(p => p._2.lastComponent.discoveryIteration)

                            pipelines.map { p =>

                                val dataSourcesString = p._2.typedDatasources.map(_.label).mkString(",")
                                val extractorsString = p._2.typedExtractors.map(_.getClass.getSimpleName).mkString(",")
                                val transformersString = p._2.typedProcessors.map(_.iri).mkString(",")
                                val transformersCount = p._2.typedProcessors.size
                                val app = p._2.typedApplications.map(_.label).mkString(",")
                                val iterationNumber = p._2.lastComponent.discoveryIteration

                                CsvLine(Seq(
                                    id,
                                    "appGroup?",
                                    "dataSourcesGroup?",
                                    "extractorsGroup?",
                                    "dataSampleGroup?",
                                    dataSourcesString,
                                    transformersCount,
                                    extractorsString,
                                    transformersString,
                                    app,
                                    iterationNumber,
                                    s"/discovery/$id/execute/${p._1}"
                                ))
                            }
                        }
                    }
                }
            }
        }.toSeq.flatten

        Seq(getDetailedCsvHeader) ++ body
    }


    private def withCsvRequests(csvRequests: Seq[CsvRequestData], pipelineGroupings: Map[String, PipelineGrouping], discoveryService: DiscoveryService)
                               (action: (CsvRequestData, Discovery, Option[PipelineGrouping]) => Seq[CsvLine])
    : Seq[CsvLine] = {
        csvRequests.flatMap { csvRequest =>
            discoveryService.withDiscovery(csvRequest.discoveryId) { discovery =>
                action(csvRequest, discovery, pipelineGroupings.get(csvRequest.discoveryId))
            }.get
        }
    }
}
