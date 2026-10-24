CREATE TABLE profile_publishers (
    id BIGINT PRIMARY KEY REFERENCES profile_profiles(id),
    first_name      VARCHAR(40) NOT NULL UNIQUE,
    last_name     VARCHAR(40)  NOT NULL,
    niche VARCHAR(20)  NOT NULL 
          CHECK (niche IN ('BEAUTY', 'TECH', 'GAMING', 'TRAVEL', 'LIFESTYLE', 'FOOD', 'SPORT')),
    bio VARCHAR(500)  NOT NULL,
    country VARCHAR(100)  NOT NULL
);