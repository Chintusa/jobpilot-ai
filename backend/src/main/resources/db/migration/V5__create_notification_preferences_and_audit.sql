-- V5: Notification Preferences, Deduplication, and Audit Enhancement
CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notify_high_match_jobs BOOLEAN NOT NULL DEFAULT TRUE,
    high_match_min_score INTEGER NOT NULL DEFAULT 85,
    notify_interventions BOOLEAN NOT NULL DEFAULT TRUE,
    notify_submissions BOOLEAN NOT NULL DEFAULT TRUE,
    notify_failures BOOLEAN NOT NULL DEFAULT TRUE,
    notify_interviews BOOLEAN NOT NULL DEFAULT TRUE,
    notify_deadlines BOOLEAN NOT NULL DEFAULT TRUE,
    digest_frequency VARCHAR(50) NOT NULL DEFAULT 'INSTANT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_prefs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS category VARCHAR(100) DEFAULT 'GENERAL';
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS dedup_key VARCHAR(500);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS channel VARCHAR(50) DEFAULT 'IN_APP';
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS email_delivered BOOLEAN DEFAULT FALSE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS read_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS metadata TEXT DEFAULT '{}';
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS reference_id UUID;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS reference_type VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_dedup ON notifications(user_id, dedup_key);
CREATE INDEX IF NOT EXISTS idx_notifications_category ON notifications(user_id, category);
CREATE INDEX IF NOT EXISTS idx_notif_prefs_user ON notification_preferences(user_id);
