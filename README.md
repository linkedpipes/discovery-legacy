# Visualization discovery service

Demo running at: http://demo.visualization.linkedpipes.com:8080

# How to run

### Prerequisites

Make sure to have `JAVA_HOME` to point to `jdk1.8`. This is due to the fact that `sbt assembly` might not be compatible with higher versions.

### Building docker image

```
$ ./dockerize.sh {Your docker tag}
$ docker run -i -p 9000:9000 {Your docker tag} -Dplay.crypto.secret=yourRandomSecret
```

## Expected workflows

```
start -> status -> ... -> status[isFinished = true] –> list -> export
```

```
start –> list -> export
```

(results are published continuously)
