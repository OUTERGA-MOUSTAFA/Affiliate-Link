CREATE TABLE event_publication(
    id BIGINT PRIMARY KEY REFERENCES event_events(id),
    publisher_id BIGINT NOT NULL REFERENCES profile_publishers(id),
    advertiser_id BIGINT NOT NULL REFERENCES profile_advertisers(id),
    publication_date TIMESTAMP NOT NULL,
    publication_status VARCHAR(20) NOT NULL
          CHECK (publication_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);