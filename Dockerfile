# Fix the version?
FROM hseeberger/scala-sbt as builder

# Copy files
ADD ./src /app
WORKDIR /app

# Create tarball
RUN sbt universal:packageZipTarball

FROM openjdk:8-jre-slim

COPY --from=builder /app/target/universal/linkedpipes-discovery-1.0-SNAPSHOT.tgz /tmp
RUN mkdir -p /app && \
    tar -C /app -xvzf /tmp/linkedpipes-discovery-1.0-SNAPSHOT.tgz && \
    chmod +x /app/linkedpipes-discovery-1.0-SNAPSHOT/bin/linkedpipes-discovery

WORKDIR /app/linkedpipes-discovery-1.0-SNAPSHOT/bin

# Expose port 9000
EXPOSE 9000

ENTRYPOINT ["./linkedpipes-discovery"]