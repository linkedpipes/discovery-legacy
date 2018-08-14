FROM hseeberger/scala-sbt

ENV PROJECT_HOME /usr/src
RUN mkdir -p $PROJECT_HOME/app

ENV PATH $PROJECT_WORKPLACE/build/target/universal/stage/bin:$PATH
COPY ./src $PROJECT_HOME/app
WORKDIR $PROJECT_HOME/app
EXPOSE 9000
RUN sbt run