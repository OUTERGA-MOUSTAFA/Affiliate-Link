CREATE TABLE IF NOT EXISTS event_publication (
    id                UUID NOT NULL,
    listener_id       VARCHAR(512) NOT NULL,
    event_type        VARCHAR(512) NOT NULL,
    serialized_event  VARCHAR(4000) NOT NULL,
    publication_date  TIMESTAMP(9) WITH TIME ZONE NOT NULL,
    completion_date   TIMESTAMP(9) WITH TIME ZONE,
    PRIMARY KEY (id)
);