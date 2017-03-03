# Visualization discovery service

Demo running at: http://demo.visualization.linkedpipes.com:8080

```
start:          POST         /discovery/start?uri={$inputUri}
status:         GET          /discovery/$id<[^/]+>
list:           GET          /discovery/$id<[^/]+>/pipelines
export:         GET          /discovery/$id<[^/]+>/pipelines/$pipelineId<[^/]+>
```
## Expected workflows
```start -> status -> ... -> status[isFinished = true] –> list -> export```

```start –> list -> export``` (results are published continuously)
