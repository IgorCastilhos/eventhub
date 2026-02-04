-- Create events table
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(500) NOT NULL,
    capacity INTEGER NOT NULL,
    available_capacity INTEGER NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    image_url VARCHAR(2048),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity_positive CHECK (capacity >= 0),
    CONSTRAINT chk_available_capacity_non_negative CHECK (available_capacity >= 0),
    CONSTRAINT chk_available_capacity_valid CHECK (available_capacity <= capacity),
    CONSTRAINT chk_name_not_empty CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_price_non_negative CHECK (price >= 0),
    CONSTRAINT chk_status_valid CHECK (status IN ('SCHEDULED', 'ONGOING', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_events_date ON events(event_date);

CREATE INDEX idx_events_location ON events(location);

CREATE INDEX idx_events_available_capacity ON events(available_capacity);

CREATE INDEX idx_events_status ON events(status);

CREATE INDEX idx_events_date_capacity ON events(event_date, available_capacity)
    WHERE available_capacity > 0;

CREATE INDEX idx_events_sold_out ON events(id, name, capacity)
    WHERE available_capacity = 0;

CREATE INDEX idx_events_active ON events(status, event_date)
    WHERE status IN ('SCHEDULED', 'ONGOING');

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

INSERT INTO events (name, event_date, location, capacity, available_capacity, description, price, image_url)
VALUES (
           'Campeonato Estadual de Futebol - Final',
           CURRENT_TIMESTAMP + INTERVAL '30 days',
           'Estádio Municipal Arena Verde, Porto Alegre - RS',
           50000,
           15000,
           'Grande final do Campeonato Gaúcho 2026. Emoção garantida com as duas melhores equipes do estado disputando o título em um jogo épico.',
           85.00,
           'https://images.unsplash.com/photo-1459865264687-595d652de67e?w=800&h=600&fit=crop'
       ) ON CONFLICT DO NOTHING;

INSERT INTO events (name, event_date, location, capacity, available_capacity, description, price, image_url)
VALUES (
           'Maratona Internacional de São Paulo',
           CURRENT_TIMESTAMP + INTERVAL '15 days',
           'Avenida Paulista, São Paulo - SP',
           10000,
           500,
           'Participe da maior maratona da América Latina! Percurso de 42km pelos principais pontos turísticos da cidade. Inscrições limitadas!',
           120.00,
           'https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?w=800&h=600&fit=crop'
       ) ON CONFLICT DO NOTHING;

INSERT INTO events (name, event_date, location, capacity, available_capacity, description, price, image_url)
VALUES (
           'Torneio Aberto de Vôlei de Praia',
           CURRENT_TIMESTAMP + INTERVAL '7 days',
           'Praia de Copacabana, Rio de Janeiro - RJ',
           2000,
           0,
           'Competição profissional de vôlei de praia com participação de atletas olímpicos. Arena montada na orla de Copacabana. Evento esgotado!',
           0.00,
           'https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?w=800&h=600&fit=crop'
       ) ON CONFLICT DO NOTHING;

INSERT INTO events (name, event_date, location, capacity, available_capacity, description, price, image_url, status)
VALUES (
           'Copa Regional de Basquete Sub-21',
           CURRENT_TIMESTAMP - INTERVAL '5 days',
           'Ginásio Poliesportivo Municipal, Curitiba - PR',
           3000,
           800,
           'Competição regional com as melhores equipes de base do Sul do Brasil. Revelação de novos talentos do basquete nacional.',
           45.00,
           'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800&h=600&fit=crop',
           'COMPLETED'
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
COMMENT ON COLUMN events.price IS 'Event ticket price in BRL (0.00 = free event)';
COMMENT ON COLUMN events.image_url IS 'URL to event image/banner (optional)';
COMMENT ON COLUMN events.status IS 'Event status (SCHEDULED, ONGOING, COMPLETED, CANCELLED)';
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
    price,
    image_url,
    status,
    (capacity - available_capacity) as tickets_sold,
    ROUND(((capacity - available_capacity)::NUMERIC / capacity * 100), 2) as occupancy_rate
FROM events
WHERE event_date > CURRENT_TIMESTAMP
  AND status IN ('SCHEDULED', 'ONGOING')
ORDER BY event_date;

COMMENT ON VIEW active_events IS 'View of future events with calculated metrics';