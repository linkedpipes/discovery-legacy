# Visualization discovery service

Demo no longer running

# How to run

## Building docker image

The following commands are used for building and running the image of the Discovery service:

```
$ docker build -t <image_tag> .
$ docker run -p <port>:9000 <image_tag> -Dplay.http.secret.key=<yourApplicationSecret>
```

For generating application secrets, you can take a look at: https://www.playframework.com/documentation/2.6.x/ApplicationSecret


## Expected workflows

```
start -> status -> ... -> status[isFinished = true] –> list -> export
```

```
start –> list -> export
```

(results are published continuously)
