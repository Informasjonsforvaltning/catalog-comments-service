CREATE TABLE users (
    id       VARCHAR(255) PRIMARY KEY,
    name     VARCHAR(500),
    email    VARCHAR(500)
);

CREATE TABLE comments (
    id                VARCHAR(255) PRIMARY KEY,
    created_date      TIMESTAMP NOT NULL,
    last_changed_date TIMESTAMP,
    topic_id          VARCHAR(255),
    org_number        VARCHAR(255),
    user_id           VARCHAR(255),
    comment           TEXT
);

CREATE INDEX idx_comments_org_number ON comments (org_number);
CREATE INDEX idx_comments_org_topic ON comments (org_number, topic_id);
CREATE INDEX idx_comments_topic_id ON comments (topic_id);
