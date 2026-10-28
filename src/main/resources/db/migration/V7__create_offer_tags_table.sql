CREATE Table IF NOT EXISTS offer_tags(
    id BIGSERIAL PRIMARY KEY, 
    name  VARCHAR(40) UNIQUE NOT NULL CHECK (name = lower(name))
);