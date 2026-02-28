-- ============================================================
--  OceanView Hotel  |  Rooms table
-- ============================================================

USE oceanview_db;

CREATE TABLE IF NOT EXISTS rooms (
    room_id        INT AUTO_INCREMENT PRIMARY KEY,
    room_number    INT          NOT NULL UNIQUE,
    room_type      ENUM('STANDARD','DELUXE','SUITE','FAMILY','PENTHOUSE') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    status         ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','OUT_OF_ORDER')
                   NOT NULL DEFAULT 'AVAILABLE',
    floor          INT          NOT NULL DEFAULT 1,
    description    TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--  Seed data — sample rooms for each type
-- ============================================================

INSERT INTO rooms (room_number, room_type, price_per_night, status, floor, description) VALUES
-- Floor 1 — Standard
(101, 'STANDARD', 3000.00, 'AVAILABLE', 1, 'Garden view, queen bed'),
(102, 'STANDARD', 3000.00, 'AVAILABLE', 1, 'Garden view, twin beds'),
(103, 'STANDARD', 3000.00, 'OCCUPIED',  1, 'Garden view, queen bed'),

-- Floor 2 — Deluxe
(201, 'DELUXE',   5000.00, 'AVAILABLE', 2, 'Sea view, king bed, mini bar'),
(202, 'DELUXE',   5000.00, 'AVAILABLE', 2, 'Sea view, king bed, mini bar'),
(203, 'DELUXE',   5500.00, 'MAINTENANCE', 2, 'Corner sea view, king bed'),

-- Floor 3 — Suite
(301, 'SUITE',   10000.00, 'AVAILABLE', 3, 'Ocean suite, king bed, living area, jacuzzi'),
(302, 'SUITE',   10000.00, 'AVAILABLE', 3, 'Ocean suite, king bed, living area'),

-- Floor 4 — Family
(401, 'FAMILY',   7000.00, 'AVAILABLE', 4, 'Family room, 2 queen beds, kids play area'),
(402, 'FAMILY',   7500.00, 'AVAILABLE', 4, 'Connecting family rooms, sea view'),

-- Floor 5 — Penthouse
(501, 'PENTHOUSE', 25000.00, 'AVAILABLE', 5, 'Full floor penthouse, panoramic ocean view, private pool'),
(502, 'PENTHOUSE', 22000.00, 'AVAILABLE', 5, 'Penthouse suite, terrace, butler service');

-- ============================================================
--  Verify
-- ============================================================
-- SELECT room_number, room_type, price_per_night, status, floor FROM rooms ORDER BY room_number;
