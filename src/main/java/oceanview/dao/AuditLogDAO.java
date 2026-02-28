package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    private static final String SELECT_COLS =
        "SELECT log_id, action, table_name, record_id, performed_by, " +
        "       ip_address, description, created_at " +
        "FROM audit_log";

    // -----------------------------------------------------------------------
    // Filtered + paginated query
    // -----------------------------------------------------------------------

    public List<AuditLog> findFiltered(String action, String performedBy,
                                        String dateFrom, String dateTo,
                                        String search,
                                        int page, int pageSize) throws SQLException {

        StringBuilder sb = new StringBuilder(SELECT_COLS);
        List<Object> params = buildWhere(sb, action, performedBy, dateFrom, dateTo, search);
        sb.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<AuditLog> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = prepare(c, sb.toString(), params);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int countFiltered(String action, String performedBy,
                              String dateFrom, String dateTo,
                              String search) throws SQLException {

        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM audit_log");
        List<Object> params = buildWhere(sb, action, performedBy, dateFrom, dateTo, search);

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = prepare(c, sb.toString(), params);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // -----------------------------------------------------------------------
    // Distinct values for filter dropdowns
    // -----------------------------------------------------------------------

    public List<String> getDistinctActions() throws SQLException {
        return getDistinct("SELECT DISTINCT action FROM audit_log ORDER BY action");
    }

    public List<String> getDistinctPerformedBy() throws SQLException {
        return getDistinct("SELECT DISTINCT performed_by FROM audit_log ORDER BY performed_by");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Appends WHERE clauses for the active filters and returns the bind values. */
    private List<Object> buildWhere(StringBuilder sb, String action, String performedBy,
                                     String dateFrom, String dateTo, String search) {
        List<Object> params = new ArrayList<>();
        List<String> clauses = new ArrayList<>();

        if (notBlank(action))      { clauses.add("action = ?");          params.add(action); }
        if (notBlank(performedBy)) { clauses.add("performed_by = ?");    params.add(performedBy); }
        if (notBlank(dateFrom))    { clauses.add("DATE(created_at) >= ?"); params.add(dateFrom); }
        if (notBlank(dateTo))      { clauses.add("DATE(created_at) <= ?"); params.add(dateTo); }
        if (notBlank(search)) {
            clauses.add("(description LIKE ? OR table_name LIKE ? OR CAST(record_id AS CHAR) LIKE ?)");
            String like = "%" + search + "%";
            params.add(like); params.add(like); params.add(like);
        }

        if (!clauses.isEmpty()) {
            sb.append(" WHERE ").append(String.join(" AND ", clauses));
        }
        return params;
    }

    private PreparedStatement prepare(Connection c, String sql, List<Object> params)
            throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
        return ps;
    }

    private List<String> getDistinct(String sql) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));
        log.setAction(rs.getString("action"));
        log.setTableName(rs.getString("table_name"));
        log.setRecordId(rs.getInt("record_id"));
        log.setPerformedBy(rs.getString("performed_by"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
