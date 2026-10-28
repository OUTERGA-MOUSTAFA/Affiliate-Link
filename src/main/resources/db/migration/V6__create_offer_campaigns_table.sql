CREATE Table IF NOT EXISTS offer_campaigns(
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL REFERENCES offer_offers(id),
    name VARCHAR(100) NOT NULL, 
    start_date DATE NOT NULL, 
    end_date DATE NOT NULL, 
    budget Numeric(12, 2) NOT NULL CHECK (budget > 0), 
    payout_model VARCHAR(20) NOT NULL CHECK (payout_model IN ('CPC', 'CPL', 'CPA_FIXED', 'CPA_PERCENT')), 
    payout_value Numeric(12, 2) NOT NULL CHECK (payout_value > 0), 
    status VARCHAR(40)  NOT NULL 
          CHECK (status IN ('DRAFT','ACTIVE','PAUSED','ENDED')) DEFAULT 'DRAFT',

    CONSTRAINT chk_campaign_dates  CHECK (end_date > start_date),
    CONSTRAINT chk_campaign_percent CHECK (payout_model <> 'CPA_PERCENT' OR payout_value <= 100)
); 
CREATE INDEX idx_offer_campaigns_offer  ON offer_campaigns (offer_id);
CREATE INDEX idx_offer_campaigns_status ON offer_campaigns (status);

-- Une seule campagne active par offre : activer une campagne met
-- automatiquement en pause celle qui était active sur la même offre
CREATE UNIQUE INDEX uq_offer_one_active_campaign
    ON offer_campaigns (offer_id) WHERE status = 'ACTIVE';