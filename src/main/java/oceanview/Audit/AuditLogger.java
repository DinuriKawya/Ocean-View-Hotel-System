package oceanview.Audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oceanview.database.DBConnection;

public class AuditLogger {
	
	public static void log(String action, String tableName, int recordId,String performedBy, String ipAddress, String description) {
		String sql = "INSERT INTO audit_log (action, table_name, record_id, " +"performed_by, ip_address, description) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection();
	               PreparedStatement ps = conn.prepareStatement(sql)) {

	              ps.setString(1, action);
	              ps.setString(2, tableName);
	              ps.setInt(3, recordId);
	              ps.setString(4, performedBy);
	              ps.setString(5, ipAddress);
	              ps.setString(6, description);
	              ps.executeUpdate();

	          } catch (SQLException e) {
	              e.printStackTrace();
	          }
	}

}
