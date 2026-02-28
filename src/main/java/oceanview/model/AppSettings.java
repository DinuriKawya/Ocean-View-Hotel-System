package oceanview.model;

import java.util.Map;

/**
 * Static holder for application-wide settings loaded from the DB at startup.
 * Updated live when admin saves settings via SettingsServlet.
 */
public class AppSettings {

    private static volatile String currency    = "LKR";
    private static volatile String hotelName   = "OceanView Hotel";
    private static volatile String hotelAddress = "123 Coastal Avenue, Seaside City";
    private static volatile String hotelPhone  = "+94 11 234 5678";
    private static volatile double taxRate     = 0.0;

    private AppSettings() {}

    /** Called by SettingsListener at startup and by SettingsServlet after save. */
    public static synchronized void load(Map<String, String> settings) {
        if (settings.containsKey("currency"))
            currency = settings.get("currency");
        if (settings.containsKey("hotel_name"))
            hotelName = settings.get("hotel_name");
        if (settings.containsKey("hotel_address"))
            hotelAddress = settings.get("hotel_address");
        if (settings.containsKey("hotel_phone"))
            hotelPhone = settings.get("hotel_phone");
        if (settings.containsKey("tax_rate")) {
            try { taxRate = Double.parseDouble(settings.get("tax_rate")); }
            catch (NumberFormatException ignored) {}
        }
    }

    public static String getCurrency()    { return currency; }
    public static String getHotelName()   { return hotelName; }
    public static String getHotelAddress(){ return hotelAddress; }
    public static String getHotelPhone()  { return hotelPhone; }
    public static double getTaxRate()     { return taxRate; }
}
