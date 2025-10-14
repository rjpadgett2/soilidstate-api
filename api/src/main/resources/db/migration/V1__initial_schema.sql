
-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       oauth_subject VARCHAR(255) NOT NULL UNIQUE,
                       oauth_provider VARCHAR(50),
                       display_name VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_oauth_sub ON users(oauth_subject);

-- Phidget Connections table
CREATE TABLE phidget_connections (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     server_address VARCHAR(255) NOT NULL,
                                     server_port INTEGER NOT NULL,
                                     phidget_port INTEGER,
                                     is_active BOOLEAN DEFAULT false,
                                     connected_at TIMESTAMP,
                                     disconnected_at TIMESTAMP,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_connection_user ON phidget_connections(user_id);
CREATE INDEX idx_connection_active ON phidget_connections(is_active);

-- Registered Sensors table
CREATE TABLE registered_sensors (
                                    id BIGSERIAL PRIMARY KEY,
                                    connection_id BIGINT NOT NULL REFERENCES phidget_connections(id) ON DELETE CASCADE,
                                    phidget_sensor_id VARCHAR(255) NOT NULL,
                                    sensor_type VARCHAR(50) NOT NULL,
                                    sensor_name VARCHAR(255) NOT NULL,
                                    hub_port INTEGER NOT NULL,
                                    channel_number INTEGER NOT NULL,
                                    serial_number INTEGER,
                                    is_attached BOOLEAN DEFAULT false,
                                    last_value DOUBLE PRECISION,
                                    unit VARCHAR(20),
                                    last_reading_at TIMESTAMP,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sensor_connection ON registered_sensors(connection_id);
CREATE INDEX idx_sensor_phidget_id ON registered_sensors(phidget_sensor_id);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_connections_updated_at BEFORE UPDATE ON phidget_connections
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sensors_updated_at BEFORE UPDATE ON registered_sensors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE users IS 'OAuth2 authenticated users';
COMMENT ON TABLE phidget_connections IS 'User-specific Phidget SBC4 connections';
COMMENT ON TABLE registered_sensors IS 'Sensors registered to connections';

COMMENT ON COLUMN users.oauth_subject IS 'OAuth2 sub claim (unique identifier)';
COMMENT ON COLUMN users.oauth_provider IS 'OAuth2 provider (auth0, google, okta, etc.)';
COMMENT ON COLUMN phidget_connections.is_active IS 'Whether this connection is currently active';
COMMENT ON COLUMN registered_sensors.phidget_sensor_id IS 'Runtime UUID from Phidget library';