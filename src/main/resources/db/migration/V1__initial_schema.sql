-- Create repositories table
CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT UNIQUE NOT NULL,
    owner VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    installation_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_repositories_github_id ON repositories(github_id);
CREATE INDEX idx_repositories_owner_name ON repositories(owner, name);

-- Create pull_requests table
CREATE TABLE pull_requests (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    pr_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    author VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'open',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(repository_id, pr_number)
);

CREATE INDEX idx_pull_requests_repository_id ON pull_requests(repository_id);
CREATE INDEX idx_pull_requests_status ON pull_requests(status);

-- Create reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    pull_request_id BIGINT NOT NULL REFERENCES pull_requests(id) ON DELETE CASCADE,
    commit_sha VARCHAR(40) NOT NULL,
    review_status VARCHAR(50) DEFAULT 'pending',
    total_comments INTEGER DEFAULT 0,
    severity_counts JSONB,
    ai_model VARCHAR(50),
    tokens_used INTEGER,
    processing_time_ms INTEGER,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_pull_request_id ON reviews(pull_request_id);
CREATE INDEX idx_reviews_status ON reviews(review_status);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- Create comments table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    file_path TEXT NOT NULL,
    line_number INTEGER,
    severity VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    suggestion TEXT,
    code_example TEXT,
    github_comment_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_review_id ON comments(review_id);
CREATE INDEX idx_comments_severity ON comments(severity);
CREATE INDEX idx_comments_category ON comments(category);

-- Create review_settings table
CREATE TABLE review_settings (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT true,
    min_severity VARCHAR(20) DEFAULT 'low',
    excluded_files TEXT[],
    custom_rules JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(repository_id)
);

CREATE INDEX idx_review_settings_repository_id ON review_settings(repository_id);

-- Create webhook_events table for audit trail
CREATE TABLE webhook_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    repository_id BIGINT,
    pr_number INTEGER,
    payload JSONB NOT NULL,
    processed BOOLEAN DEFAULT false,
    processed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_events_processed ON webhook_events(processed);
CREATE INDEX idx_webhook_events_created_at ON webhook_events(created_at);
