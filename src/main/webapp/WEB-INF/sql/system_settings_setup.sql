CREATE TABLE IF NOT EXISTS system_settings (
  setting_key   VARCHAR(100) PRIMARY KEY,
  setting_value VARCHAR(500) NOT NULL,
  description   VARCHAR(255),
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO system_settings VALUES
  ('currency',      'LKR',                             'Currency code shown in the UI',       NOW()),
  ('hotel_name',    'OceanView Hotel',                  'Hotel display name',                  NOW()),
  ('hotel_address', '123 Coastal Avenue, Seaside City', 'Hotel address for invoices',          NOW()),
  ('hotel_phone',   '+94 11 234 5678',                  'Hotel phone number for invoices',     NOW()),
  ('tax_rate',      '0',                                'Tax % applied on invoices/bills',     NOW());
