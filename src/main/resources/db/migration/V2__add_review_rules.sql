-- Create review_rules table
CREATE TABLE review_rules (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT REFERENCES repositories(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    rule_config JSONB NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    target_files TEXT,
    exclude_files TEXT,
    min_severity VARCHAR(20),
    custom_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_review_rules_repository_id ON review_rules(repository_id);
CREATE INDEX idx_review_rules_enabled ON review_rules(enabled);
CREATE INDEX idx_review_rules_priority ON review_rules(priority DESC);

-- Add some default global rules
INSERT INTO review_rules (name, description, rule_type, rule_config, priority, target_files)
VALUES
    ('보안: 하드코딩된 비밀번호 검사',
     '코드에 하드코딩된 비밀번호나 API 키가 있는지 검사합니다.',
     'code_pattern',
     '{"patterns": ["password\\s*=\\s*[\"''].*[\"'']", "api[_-]?key\\s*=\\s*[\"''].*[\"'']", "secret\\s*=\\s*[\"''].*[\"'']"], "severity": "error"}',
     100,
     '**/*'),

    ('성능: 불필요한 반복문 검사',
     'N+1 쿼리 문제나 비효율적인 반복문을 검사합니다.',
     'custom_prompt',
     '{"prompt": "Check for N+1 query problems and inefficient loops. Focus on database queries inside loops.", "severity": "warning"}',
     80,
     '**/*.java,**/*.ts,**/*.py'),

    ('코드 품질: TODO/FIXME 코멘트',
     'TODO나 FIXME 코멘트를 찾아 개선을 권장합니다.',
     'code_pattern',
     '{"patterns": ["//\\s*TODO", "//\\s*FIXME", "#\\s*TODO", "#\\s*FIXME"], "severity": "info"}',
     50,
     '**/*');
