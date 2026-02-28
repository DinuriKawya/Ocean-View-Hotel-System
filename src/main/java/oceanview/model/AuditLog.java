package oceanview.model;

import java.time.LocalDateTime;

public class AuditLog {

    private int           logId;
    private String        action;
    private String        tableName;
    private int           recordId;
    private String        performedBy;
    private String        ipAddress;
    private String        description;
    private LocalDateTime createdAt;

    public int           getLogId()            { return logId; }
    public void          setLogId(int v)        { this.logId = v; }

    public String        getAction()            { return action; }
    public void          setAction(String v)    { this.action = v; }

    public String        getTableName()         { return tableName; }
    public void          setTableName(String v) { this.tableName = v; }

    public int           getRecordId()          { return recordId; }
    public void          setRecordId(int v)     { this.recordId = v; }

    public String        getPerformedBy()           { return performedBy; }
    public void          setPerformedBy(String v)   { this.performedBy = v; }

    public String        getIpAddress()             { return ipAddress; }
    public void          setIpAddress(String v)     { this.ipAddress = v; }

    public String        getDescription()           { return description; }
    public void          setDescription(String v)   { this.description = v; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
