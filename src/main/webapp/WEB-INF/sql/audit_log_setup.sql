-- Audit Log table
-- Run this script once to create the audit_log table.
-- If the table already exists (created by AuditLogger at runtime), this is a no-op.

CREATE TABLE IF NOT EXISTS audit_log (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    action       VARCHAR(50)  NOT NULL,
    table_name   VARCHAR(50)  NOT NULL DEFAULT '',
    record_id    INT          NOT NULL DEFAULT 0,
    performed_by VARCHAR(50)  NOT NULL DEFAULT '',
    ip_address   VARCHAR(45)  NOT NULL DEFAULT '',
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_audit_action       (action),
    INDEX idx_audit_performed_by (performed_by),
    INDEX idx_audit_created_at   (created_at)
);
