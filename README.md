# Visualization discovery service

Demo running at: http://demo.visualization.linkedpipes.com:8080

```
start:          POST         /discovery/start
status:         GET          /discovery/$id<[^/]+>
list:           GET          /discovery/$id<[^/]+>/pipelines
export:         GET          /discovery/$id<[^/]+>/pipelines/$pipelineId<[^/]+>
stop:           GET          /discovery/$id<[^/]+>/stop
```
## Expected workflows
```start -> status -> ... -> status[isFinished = true] –> list -> export```

```start –> list -> export``` (results are published continuously)

## start: DiscoverySettings => {id: UUID}
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

Request example:
```
{
	"sparqlEndpoints": [
		{"url": "http://linked.opendata.cz/sparql"}
	]
}
```

Response example:
```
{
  "id": "079068ba-e5bf-4e66-bab4-3636245e1500"
}
```

