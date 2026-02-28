package oceanview.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import oceanview.model.AppSettings;
import oceanview.service.SettingsService;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(SettingsListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SettingsService service = new SettingsService();
            Map<String, String> settings = service.getAll();
            AppSettings.load(settings);
            log.info("AppSettings loaded from DB: currency=" + AppSettings.getCurrency());
        } catch (Exception e) {
            log.log(Level.WARNING,
                "Could not load system settings from DB — using defaults. " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }
}
