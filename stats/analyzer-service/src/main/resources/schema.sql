CREATE TABLE IF NOT EXISTS user_actions (
    user_id BIGINT,
    event_id BIGINT,
    action_type VARCHAR,
    created TIMESTAMP,
    CONSTRAINT pk_user_actions PRIMARY KEY (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS event_actions (
    event_a BIGINT,
    event_b BIGINT,
    scope_event DOUBLE PRECISION,
    created TIMESTAMP,
    CONSTRAINT pk_event_actions PRIMARY KEY (event_a, event_b)
);