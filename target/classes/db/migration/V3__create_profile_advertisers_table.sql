CREATE Table profile_advertisers(
    id BIGINT PRIMARY KEY REFERENCES profile_profiles(id),
    company_name VARCHAR(60) NOT NULL,
    sector VARCHAR(40) NOT NULL,
    website VARCHAR(100)
);