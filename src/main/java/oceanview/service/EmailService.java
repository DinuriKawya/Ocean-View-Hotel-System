package oceanview.service;

import oceanview.model.Reservation;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;


import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

public class EmailService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final String smtpHost;
    private final int    smtpPort;
    private final String senderEmail;
    private final String senderPassword;

    private static EmailService instance;

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    private EmailService() {
        Properties config = loadConfig();
        this.smtpHost       = config.getProperty("email.smtp.host",  "smtp.gmail.com");
        this.smtpPort       = Integer.parseInt(config.getProperty("email.smtp.port", "587"));
        this.senderEmail    = config.getProperty("email.sender");
        this.senderPassword = config.getProperty("email.password");
    }

    public void sendConfirmationEmail(Reservation r) {
        if (!canSend(r)) return;
        try {
            send(
                r.getGuestEmail(),
                "Reservation Confirmed - OceanView Hotel #" + r.getReservationId(),
                buildConfirmedBody(r)
            );
            System.out.println("[EmailService] Confirmation sent to: " + r.getGuestEmail());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("[EmailService] Confirmation failed: " + e.getMessage());
        }
    }

    public void sendCancellationEmail(Reservation r) {
        if (!canSend(r)) return;
        try {
            send(
                r.getGuestEmail(),
                "Reservation Cancelled - OceanView Hotel #" + r.getReservationId(),
                buildCancelledBody(r)
            );
            System.out.println("[EmailService] Cancellation sent to: " + r.getGuestEmail());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            System.err.println("[EmailService] Cancellation failed: " + e.getMessage());
        }
    }

    private void send(String toEmail, String subject, String htmlBody)
            throws MessagingException, java.io.UnsupportedEncodingException {

        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth",            "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host",            smtpHost);
        smtpProps.put("mail.smtp.port",            String.valueOf(smtpPort));

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(senderEmail, "OceanView Hotel"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject(subject);
        msg.setContent(htmlBody, "text/html; charset=utf-8");

        Transport.send(msg);
    }

    private boolean canSend(Reservation r) {
        if (senderEmail == null || senderPassword == null) {
            System.err.println("[EmailService] email.properties not configured — skipping.");
            return false;
        }
        if (r.getGuestEmail() == null || r.getGuestEmail().isBlank()) {
            System.out.println("[EmailService] No guest email on reservation #"
                    + r.getReservationId() + " — skipping.");
            return false;
        }
        return true;
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                                        .getResourceAsStream("email.properties")) {
            if (in == null) {
                System.err.println("[EmailService] email.properties not found.");
                return props;
            }
            props.load(in);
        } catch (IOException e) {
            System.err.println("[EmailService] Could not read email.properties: " + e.getMessage());
        }
        return props;
    }

    private String buildConfirmedBody(Reservation r) {
        return template(
            "#0077b6", "BOOKING CONFIRMATION",
            "&#10003; Your Reservation is Confirmed!", "#d4edda", "#155724",
            "Dear <strong>" + r.getGuestName() + "</strong>,<br><br>" +
            "Great news! Your reservation at <strong>OceanView Hotel</strong> " +
            "has been <strong style='color:#0077b6;'>confirmed</strong>. " +
            "We look forward to welcoming you!",
            buildDetailsTable(r),
            "If you need to make any changes, please reply to this email."
        );
    }

    private String buildCancelledBody(Reservation r) {
        return template(
            "#721c24", "RESERVATION UPDATE",
            "&#10007; Reservation Cancelled", "#f8d7da", "#721c24",
            "Dear <strong>" + r.getGuestName() + "</strong>,<br><br>" +
            "We're sorry to inform you that your reservation at " +
            "<strong>OceanView Hotel</strong> has been " +
            "<strong style='color:#c0392b;'>cancelled</strong>. " +
            "Details of the cancelled reservation are shown below.",
            buildDetailsTable(r),
            "If this was a mistake or you would like to rebook, please contact us."
        );
    }

    private String buildDetailsTable(Reservation r) {
        String nights = r.getNights() + " night" + (r.getNights() == 1 ? "" : "s");
        return
            "<table style='width:100%;border-collapse:collapse;margin:20px 0;font-size:14px;'>" +
            row("Reservation ID",   "#" + r.getReservationId(),  false) +
            row("Guest Name",        r.getGuestName(),           true)  +
            row("Email",             r.getGuestEmail(),          false) +
            row("Phone",
                (r.getGuestPhone() != null && !r.getGuestPhone().isBlank())
                    ? r.getGuestPhone() : "—",                   true)  +
            row("Room Number",       String.valueOf(r.getRoomNumber()), false) +
            row("Room Type",
                r.getRoomType() != null ? r.getRoomType().getDisplayName() : "—", true) +
            row("Check-In",
                r.getCheckInDate()  != null ? r.getCheckInDate().format(FMT)  : "—", false) +
            row("Check-Out",
                r.getCheckOutDate() != null ? r.getCheckOutDate().format(FMT) : "—", true)  +
            row("Nights",            nights,                     false) +
            row("Number of Guests",  String.valueOf(r.getNumberOfGuests()), true) +
            row("Total Amount",      String.format("LKR %.2f", r.getTotalAmount()), false) +
            (r.getSpecialRequests() != null && !r.getSpecialRequests().isBlank()
                ? row("Special Requests", r.getSpecialRequests(), true) : "") +
            "</table>";
    }

    private String row(String label, String value, boolean shaded) {
        String bg = shaded ? "background:#f0f7ff;" : "";
        return "<tr style='" + bg + "'>"
             + "<td style='padding:10px 12px;border:1px solid #dce3e8;"
             +      "font-weight:600;width:38%;color:#555;'>" + label + "</td>"
             + "<td style='padding:10px 12px;border:1px solid #dce3e8;color:#333;'>"
             + (value != null ? value : "—") + "</td>"
             + "</tr>";
    }

    private String template(String bannerBg, String bannerLabel,
                            String heading,  String headingBg,  String headingClr,
                            String intro,    String details,    String footerNote) {
        return
        "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
        "<body style='margin:0;padding:0;background:#f0f4f8;font-family:Arial,sans-serif;'>" +
        "<div style='max-width:620px;margin:30px auto;background:#fff;" +
        "border:1px solid #dce3e8;border-radius:10px;overflow:hidden;" +
        "box-shadow:0 4px 12px rgba(0,0,0,0.08);'>" +

        "<div style='background:" + bannerBg + ";padding:28px 24px;text-align:center;'>" +
        "<h1 style='color:#fff;margin:0;font-size:22px;'>OceanView Hotel</h1>" +
        "<p style='color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:12px;" +
        "letter-spacing:2px;'>" + bannerLabel + "</p>" +
        "</div>" +

        "<div style='padding:32px 28px;'>" +
        "<div style='background:" + headingBg + ";border-left:4px solid " + headingClr + ";" +
        "padding:12px 16px;border-radius:4px;margin-bottom:20px;'>" +
        "<span style='font-size:16px;font-weight:700;color:" + headingClr + ";'>" +
        heading + "</span></div>" +
        "<p style='font-size:15px;line-height:1.7;color:#444;'>" + intro + "</p>" +
        details +
        "<p style='font-size:13px;color:#666;line-height:1.6;margin-top:16px;'>" +
        footerNote + "</p>" +
        "<p style='margin-top:28px;font-size:14px;color:#555;'>" +
        "Warm regards,<br><strong>OceanView Hotel Team</strong></p>" +
        "</div>" +

        "<div style='background:#f5f5f5;padding:14px;text-align:center;" +
        "font-size:12px;color:#999;border-top:1px solid #e0e0e0;'>" +
        "© 2025 OceanView Hotel. All rights reserved." +
        "</div>" +

        "</div></body></html>";
    }
}