CREATE TABLE IF NOT EXISTS tickets
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    event_id          UUID         NOT NULL,
    user_id           UUID         NOT NULL,
    participant_name  VARCHAR(255) NOT NULL,
    participant_email VARCHAR(255) NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    purchase_date     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmation_code VARCHAR(10)  NOT NULL UNIQUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_event
        FOREIGN KEY (event_id)
            REFERENCES events (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT chk_status_valid
        CHECK (status IN ('ACTIVE', 'CANCELLED', 'USED')),
    CONSTRAINT chk_participant_name_not_empty
        CHECK (length(trim(participant_name)) > 0),
    CONSTRAINT chk_participant_email_format
        CHECK (participant_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);
-- Partial unique index to enforce only one ACTIVE ticket per user per event
CREATE UNIQUE INDEX idx_unique_active_ticket_per_user_event
    ON tickets (event_id, user_id)
    WHERE status = 'ACTIVE';
CREATE INDEX idx_tickets_event_id ON tickets (event_id);
CREATE INDEX idx_tickets_user_id ON tickets (user_id);
CREATE INDEX idx_tickets_confirmation_code ON tickets (confirmation_code);
CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_user_status ON tickets (user_id, status);
CREATE INDEX idx_tickets_event_status ON tickets (event_id, status);
CREATE TRIGGER update_tickets_updated_at
    BEFORE UPDATE
    ON tickets
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
CREATE OR REPLACE FUNCTION generate_confirmation_code()
    RETURNS TEXT AS
$$
DECLARE
    characters TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    result     TEXT := '';
    i          INTEGER;
BEGIN
    FOR i IN 1..6
        LOOP
            result := result || substr(characters, floor(random() * length(characters) + 1)::INTEGER, 1);
        END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION set_confirmation_code()
    RETURNS TRIGGER AS
$$
DECLARE
    new_code    TEXT;
    code_exists BOOLEAN;
BEGIN
    LOOP
        new_code := generate_confirmation_code();
        SELECT EXISTS(SELECT 1 FROM tickets WHERE confirmation_code = new_code) INTO code_exists;
        EXIT WHEN NOT code_exists;
    END LOOP;
    NEW.confirmation_code := new_code;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER set_ticket_confirmation_code
    BEFORE INSERT
    ON tickets
    FOR EACH ROW
    WHEN (NEW.confirmation_code IS NULL OR NEW.confirmation_code = '')
EXECUTE FUNCTION set_confirmation_code();
CREATE OR REPLACE FUNCTION decrement_event_capacity()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.status = 'ACTIVE' THEN
        UPDATE events
        SET available_capacity = available_capacity - 1,
            version            = version + 1
        WHERE id = NEW.event_id
          AND available_capacity > 0;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'No available capacity for event %', NEW.event_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER decrement_capacity_on_ticket
    AFTER INSERT
    ON tickets
    FOR EACH ROW
EXECUTE FUNCTION decrement_event_capacity();
CREATE OR REPLACE FUNCTION restore_event_capacity()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.status = 'ACTIVE' AND NEW.status = 'CANCELLED' THEN
        UPDATE events
        SET available_capacity = available_capacity + 1,
            version            = version + 1
        WHERE id = NEW.event_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER restore_capacity_on_cancel
    AFTER UPDATE
    ON tickets
    FOR EACH ROW
EXECUTE FUNCTION restore_event_capacity();
CREATE OR REPLACE FUNCTION prevent_ticket_event_change()
    RETURNS TRIGGER AS
$$
BEGIN
    IF OLD.event_id != NEW.event_id THEN
        RAISE EXCEPTION 'Cannot change event_id of a ticket';
    END IF;
    IF OLD.user_id != NEW.user_id THEN
        RAISE EXCEPTION 'Cannot change user_id of a ticket';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER prevent_ticket_modification
    BEFORE UPDATE
    ON tickets
    FOR EACH ROW
EXECUTE FUNCTION prevent_ticket_event_change();
COMMENT ON TABLE tickets IS 'Stores ticket purchases for events';
COMMENT ON COLUMN tickets.id IS 'Primary key (UUID V4)';
COMMENT ON COLUMN tickets.event_id IS 'Reference to the event';
COMMENT ON COLUMN tickets.user_id IS 'Reference to the user who purchased';
COMMENT ON COLUMN tickets.participant_name IS 'Name of the person attending';
COMMENT ON COLUMN tickets.participant_email IS 'Email for ticket confirmation';
COMMENT ON COLUMN tickets.status IS 'Ticket status: ACTIVE, CANCELLED, or USED';
COMMENT ON COLUMN tickets.purchase_date IS 'When the ticket was purchased';
COMMENT ON COLUMN tickets.confirmation_code IS 'Unique 6-character confirmation code';