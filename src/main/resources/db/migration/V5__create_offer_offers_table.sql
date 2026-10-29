CREATE TABLE IF NOT EXISTS offer_offers(
    id BIGSERIAL PRIMARY KEY, 
    advertiser_id BIGINT NOT NULL,
    title VARCHAR(100)  NOT NULL, 
    description TEXT, 
    landing_url VARCHAR(500) NOT NULL, 
    niche VARCHAR(20)  NOT NULL 
          CHECK (niche IN ('BEAUTY', 'TECH', 'GAMING', 'TRAVEL', 'LIFESTYLE', 'FOOD', 'SPORT')), 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
); 
CREATE INDEX idx_offer_offers_advertiser ON offer_offers (advertiser_id);
CREATE INDEX idx_offer_offers_niche      ON offer_offers (niche);