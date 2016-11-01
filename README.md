# Visualization discovery service

```
POST         /discovery/start
GET          /discovery/$id<[^/]+>
GET          /discovery/$id<[^/]+>/pipelines
GET          /discovery/$id<[^/]+>/pipelines/$pipelineId<[^/]+>
GET          /discovery/$id<[^/]+>/stop
```

## start
```
DiscoverySettings(
  combineExistingDataSources: Boolean = false,
  sparqlEndpoints: Seq[SparqlEndpoint] = Seq(),
  dumpUrls: Seq[String] = Seq(),
  descriptorIris: Seq[String] = Seq()
)

SparqlEndpoint(
  url: String,
  defaultGraphIris: Seq[String] = Seq(),
  label: String = "",
  isLarge: Boolean = true,
  isLinkset: Boolean = false
)
```
