package oceanview.service;

import oceanview.dao.SettingsDAO;
import oceanview.model.AppSettings;

import java.sql.SQLException;
import java.util.Map;

public class SettingsService {

    private final SettingsDAO settingsDAO = new SettingsDAO();

    public Map<String, String> getAll() throws SettingsException {
        try {
            return settingsDAO.findAll();
        } catch (SQLException e) {
            throw new SettingsException("Database error: " + e.getMessage());
        }
    }

    public void save(Map<String, String> settings) throws SettingsException {
        try {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                settingsDAO.update(entry.getKey(), entry.getValue());
            }
            AppSettings.load(settings);
        } catch (SQLException e) {
            throw new SettingsException("Database error: " + e.getMessage());
        }
    }

    public static class SettingsException extends Exception {
        public SettingsException(String message) { super(message); }
    }
}
