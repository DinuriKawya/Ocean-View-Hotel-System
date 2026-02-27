-- ============================================================
--  OceanView Hotel  |  Auth & RBAC  |  users table setup
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,          -- SHA-256 hex string
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('STAFF', 'ADMIN') NOT NULL DEFAULT 'STAFF',
    status        ENUM('ACTIVE', 'INACTIVE')  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--  Seed data  (password_hash = SHA2('password123', 256))
--  Change passwords immediately in production!
-- ============================================================

INSERT INTO users (username, password_hash, full_name, role, status) VALUES
  ('admin',    SHA2('admin123', 256),    'System Administrator', 'ADMIN', 'ACTIVE'),
  ('jdelacruz',SHA2('staff123', 256),    'Juan Dela Cruz',       'STAFF', 'ACTIVE'),
  ('mreyes',   SHA2('staff456', 256),    'Maria Reyes',          'STAFF', 'ACTIVE'),
  ('inactive', SHA2('test789',  256),    'Inactive User',        'STAFF', 'INACTIVE');

-- ============================================================
--  Verify
-- ============================================================
-- SELECT user_id, username, full_name, role, status FROM users;
