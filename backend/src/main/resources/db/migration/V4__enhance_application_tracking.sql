-- V4: Enhance Application Tracking and Lifecycle Timeline
ALTER TABLE applications ADD COLUMN IF NOT EXISTS source_name VARCHAR(100) DEFAULT 'JOBPILOT_DIRECT';
ALTER TABLE applications ADD COLUMN IF NOT EXISTS submission_result TEXT;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS failure_reason TEXT;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS withdrawn_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS interview_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS offer_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE application_events ADD COLUMN IF NOT EXISTS from_status VARCHAR(50);
ALTER TABLE application_events ADD COLUMN IF NOT EXISTS to_status VARCHAR(50);
ALTER TABLE application_events ADD COLUMN IF NOT EXISTS source VARCHAR(100) DEFAULT 'AI_AGENT';

CREATE INDEX IF NOT EXISTS idx_applications_user_status ON applications(user_id, status);
CREATE INDEX IF NOT EXISTS idx_application_events_app_created ON application_events(application_id, created_at ASC);
