# DC schema

# --- !Ups


CREATE TABLE ExecutionResult (
    id varchar(255) NOT NULL PRIMARY KEY,
    discovery_id varchar(255) NOT NULL,
    pipeline_id varchar(255) NOT NULL,
    graph_iri text NOT NULL
);


# --- !Downs

DROP TABLE ExecutionResult;