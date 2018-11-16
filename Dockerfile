FROM openjdk:8-jre-alpine

RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./src/run_jar.sh ./src/app-assembly.jar ./

EXPOSE 9000

ENTRYPOINT ["sh", "./run_jar.sh"]