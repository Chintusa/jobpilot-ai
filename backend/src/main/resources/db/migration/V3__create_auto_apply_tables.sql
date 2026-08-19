-- V3: Create AutoApplyPolicy and AutoApplyDecision tables
CREATE TABLE IF NOT EXISTS auto_apply_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    minimum_score INTEGER NOT NULL DEFAULT 85,
    require_approval BOOLEAN NOT NULL DEFAULT TRUE,
    max_applications_per_day INTEGER NOT NULL DEFAULT 5,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auto_apply_decisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    score INTEGER NOT NULL,
    policy_values TEXT NOT NULL,
    passed_conditions TEXT NOT NULL DEFAULT '[]',
    failed_conditions TEXT NOT NULL DEFAULT '[]',
    decision VARCHAR(100) NOT NULL,
    decision_reason TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auto_apply_decisions_user ON auto_apply_decisions(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_auto_apply_decisions_job ON auto_apply_decisions(job_id);
