# Visualization discovery service

Demo running at: http://demo.visualization.linkedpipes.com:8080

# How to run
```
docker run -i -p 9000:9000 linkedpipes/discovery docker-play -Dplay.crypto.secret = yourRandomSecret
```

## Expected workflows
```
start -> status -> ... -> status[isFinished = true] –> list -> export
```

```
start –> list -> export
``` 
(results are published continuously)
