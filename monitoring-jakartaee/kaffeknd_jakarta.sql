DROP DATABASE IF EXISTS kaffeknd_jakarta;
CREATE DATABASE kaffeknd_jakarta;
USE kaffeknd_jakarta;

CREATE TABLE IF NOT EXISTS machine_status (
    macchinetta_code VARCHAR(50) PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    last_heartbeat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    latitude DOUBLE,
    longitude DOUBLE
);


INSERT INTO machine_status (macchinetta_code, status, latitude, longitude) VALUES
('FE716XW', 'ACTIVE',      38.10543, 13.35086),
('DR529QP', 'MAINTENANCE', 38.10488, 13.34960),
('AB123CD', 'OFFLINE',     38.10705, 13.35031), 
('GH890LM', 'ERROR',       38.10419, 13.34863);