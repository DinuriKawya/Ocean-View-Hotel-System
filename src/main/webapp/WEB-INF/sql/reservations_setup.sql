-- ============================================================
--  OceanView Hotel  |  Reservations table
-- ============================================================

USE oceanview_db;

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id   INT AUTO_INCREMENT PRIMARY KEY,
    guest_name       VARCHAR(100) NOT NULL,
    guest_email      VARCHAR(100) NOT NULL,
    guest_phone      VARCHAR(20),
    room_number      INT          NOT NULL,
    room_type        ENUM('STANDARD','DELUXE','SUITE','FAMILY','PENTHOUSE') NOT NULL,
    check_in_date    DATE         NOT NULL,
    check_out_date   DATE         NOT NULL,
    number_of_guests INT          NOT NULL DEFAULT 1,
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status           ENUM('PENDING','CONFIRMED','CHECKED_IN',
                          'CHECKED_OUT','CANCELLED','NO_SHOW')
                     NOT NULL DEFAULT 'PENDING',
    special_requests TEXT,
    created_by       VARCHAR(50),                          -- FK to users.username
    created_at       DATE         NOT NULL,

    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_guests CHECK (number_of_guests >= 1)
);

-- ============================================================
--  Sample data
-- ============================================================

INSERT INTO reservations
  (guest_name, guest_email, guest_phone, room_number, room_type,
   check_in_date, check_out_date, number_of_guests, total_amount,
   status, special_requests, created_by, created_at)
VALUES
  ('Maria Santos',  'maria@email.com', '09171234567', 101, 'STANDARD',
   '2026-03-01', '2026-03-04', 2, 9000.00,  'CONFIRMED', 'Non-smoking room', 'jdelacruz', CURDATE()),

  ('Pedro Reyes',   'pedro@email.com', '09289876543', 205, 'DELUXE',
   '2026-03-05', '2026-03-08', 1, 15000.00, 'PENDING',   NULL,               'jdelacruz', CURDATE()),

  ('Ana Lim',       'ana@email.com',   '09391122334', 310, 'SUITE',
   '2026-03-10', '2026-03-15', 3, 50000.00, 'CONFIRMED', 'Anniversary setup', 'admin',     CURDATE());

-- ============================================================
--  Verify
-- ============================================================
-- SELECT reservation_id, guest_name, room_type, check_in_date,
--        check_out_date, status FROM reservations;
