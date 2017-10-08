# DC schema

# --- !Ups


CREATE TABLE DiscoveryResult (
    id varchar(255) NOT NULL PRIMARY KEY,
    data text NOT NULL
);


# --- !Downs

DROP TABLE DiscoveryResult;