-- ============================================================
--  OceanView Hotel  |  Banks + Payments tables
-- ============================================================

USE oceanview_db;

-- Banks (managed by admin, shown in Card/Transfer payment dropdowns)
CREATE TABLE IF NOT EXISTS banks (
    bank_id    INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    is_active  TINYINT(1)   NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payments (one row per payment method per transaction)
CREATE TABLE IF NOT EXISTS payments (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT          NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    method         ENUM('CASH','CARD','TRANSFER') NOT NULL,
    bank_id        INT,                          -- FK to banks; NULL for CASH
    bank_name      VARCHAR(100),                 -- denormalised for history
    card_last4     CHAR(4),                      -- CARD only: last 4 digits
    reference_no   VARCHAR(100),                 -- TRANSFER only
    comment        TEXT,
    created_by     VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    FOREIGN KEY (bank_id)        REFERENCES banks(bank_id) ON DELETE SET NULL
);

-- ============================================================
--  Seed banks
-- ============================================================

INSERT INTO banks (name, is_active) VALUES
  ('BDO Unibank',      1),
  ('Bank of the Philippine Islands (BPI)', 1),
  ('Metrobank',        1),
  ('UnionBank',        1),
  ('Landbank',         1),
  ('Security Bank',    1),
  ('RCBC',             1),
  ('PNB',              1),
  ('Eastwest Bank',    1),
  ('Maya Bank',        1);

-- ============================================================
--  Verify
-- ============================================================
-- SELECT * FROM banks;
-- SELECT * FROM payments;
