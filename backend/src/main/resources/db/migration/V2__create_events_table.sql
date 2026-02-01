CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(500) NOT NULL,
    capacity INTEGER NOT NULL,
    available_capacity INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity_positive CHECK (capacity >= 0),
    CONSTRAINT chk_available_capacity_non_negative CHECK (available_capacity >= 0),
    CONSTRAINT chk_available_capacity_valid CHECK ( available_capacity <= capacity ),
    CONSTRAINT chk_name_not_empty CHECK ( length(trim(name)) > 0)
);

CREATE INDEX idx_events_date ON events(event_date);

CREATE INDEX idx_events_location ON events(location);

CREATE INDEX idx_events_available_capacity ON events(available_capacity);

CREATE INDEX idx_events_date_capacity ON events(event_date, available_capacity)
    WHERE available_capacity > 0;

CREATE INDEX idx_events_sold_out ON events(id, name, capacity)
    WHERE available_capacity = 0;

CREATE TRIGGER update_events_updated_at
    BEFORE UPDATE ON events
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE FUNCTION increment_version()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER increment_events_version
    BEFORE UPDATE ON events
    FOR EACH ROW
EXECUTE FUNCTION increment_version();

INSERT INTO events (name, event_date, location, capacity, available_capacity, description)
VALUES (
           'Spring Boot Mastery Workshop',
           CURRENT_TIMESTAMP + INTERVAL '30 days',
           'Tech Hub Convention Center, Room 301',
           100,
           100,
           'Deep dive into Spring Boot 3 with hands-on exercises covering Security, Data JPA, and Cloud Native development.'
       ) ON CONFLICT DO NOTHING;

INSERT INTO events (name, event_date, location, capacity, available_capacity, description)
VALUES (
           'Jazz Night Under the Stars',
           CURRENT_TIMESTAMP + INTERVAL '15 days',
           'Central Park Amphitheater',
           500,
           50,  -- Almost sold out
           'An evening of smooth jazz featuring world-renowned artists in an open-air venue.'
       ) ON CONFLICT DO NOTHING;

INSERT INTO events (name, event_date, location, capacity, available_capacity, description)
VALUES (
           'Startup Pitch Competition 2026',
           CURRENT_TIMESTAMP + INTERVAL '7 days',
           'Innovation Center Auditorium',
           200,
           0,  -- Sold out
           'Watch innovative startups pitch to top VCs and angel investors. Networking reception included.'
       ) ON CONFLICT DO NOTHING;

INSERT INTO events (name, event_date, location, capacity, available_capacity, description)
VALUES (
           'React Advanced Patterns Workshop',
           CURRENT_TIMESTAMP - INTERVAL '5 days',
           'Developer Academy, Floor 5',
           75,
           15,
           'Advanced React patterns including hooks, context, performance optimization, and testing strategies.'
       ) ON CONFLICT DO NOTHING;

COMMENT ON TABLE events IS 'Event information with capacity management and optimistic locking';
COMMENT ON COLUMN events.id IS 'Primary key (UUID v4)';
COMMENT ON COLUMN events.version IS 'Optimistic locking version (auto-incremented on update)';
COMMENT ON COLUMN events.name IS 'Event name';
COMMENT ON COLUMN events.event_date IS 'Event date and time (UTC)';
COMMENT ON COLUMN events.location IS 'Event location/venue';
COMMENT ON COLUMN events.capacity IS 'Total event capacity (immutable)';
COMMENT ON COLUMN events.available_capacity IS 'Remaining available capacity (decremented on ticket purchase)';
COMMENT ON COLUMN events.description IS 'Event description (optional)';
COMMENT ON COLUMN events.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN events.updated_at IS 'Last update timestamp (auto-updated)';

CREATE OR REPLACE VIEW active_events AS
SELECT
    id,
    name,
    event_date,
    location,
    capacity,
    available_capacity,
    description,
    (capacity - available_capacity) as tickets_sold,
    ROUND(((capacity - available_capacity)::NUMERIC / capacity * 100), 2) as occupancy_rate
FROM events
WHERE event_date > CURRENT_TIMESTAMP
ORDER BY event_date ASC;

COMMENT ON VIEW active_events IS 'View of future events with calculated metrics';