CREATE TABLE ai_summary_cache(
 id UUID PRIMARY KEY,prompt_type VARCHAR(30) NOT NULL,input_hash CHAR(64) NOT NULL,payload TEXT NOT NULL,source VARCHAR(30) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),UNIQUE(prompt_type,input_hash)
);
