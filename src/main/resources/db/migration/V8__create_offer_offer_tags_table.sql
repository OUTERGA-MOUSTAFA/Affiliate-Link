CREATE TABLE IF NOT EXISTS offer_offer_tags(
    offer_id BIGINT NOT NULL REFERENCES offer_offers(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES offer_tags(id),
    PRIMARY KEY(offer_id, tag_id)
);

CREATE INDEX idx_offer_offer_tags_tag ON offer_offer_tags (tag_id);