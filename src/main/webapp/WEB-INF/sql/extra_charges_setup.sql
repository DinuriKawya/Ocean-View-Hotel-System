-- ============================================================
--  OceanView Hotel  |  Extra Charges table
--  Run this AFTER payment_setup.sql (reservations table must exist)
-- ============================================================

USE oceanview_db;

-- Fresh install: create with all columns
CREATE TABLE IF NOT EXISTS extra_charges (
    charge_id      INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT           NOT NULL,
    charge_type    VARCHAR(50)   NOT NULL DEFAULT 'Other',
    description    VARCHAR(255),
    amount         DECIMAL(10,2) NOT NULL,
    added_by       VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- If table already exists from a previous install, add the charge_type column:
-- (Run only if you get "Unknown column 'charge_type'" errors)
-- ALTER TABLE extra_charges ADD COLUMN charge_type VARCHAR(50) NOT NULL DEFAULT 'Other' AFTER reservation_id;
-- ALTER TABLE extra_charges MODIFY COLUMN description VARCHAR(255);

-- Verify
-- SELECT * FROM extra_charges;
