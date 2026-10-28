CREATE TABLE IF NOT EXISTS offer_offer_tags(
    offer_id BIGINT NOT NULL REFERENCES offer_offers(id),
    tag_id BIGINT NOT NULL REFERENCES offer_tags(id)
);