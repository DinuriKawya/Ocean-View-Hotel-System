<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*, oceanview.model.AppSettings, java.util.List,
                 java.time.LocalDate, java.time.format.DateTimeFormatter" %>
<%
    User user               = (User) session.getAttribute("loggedInUser");
    Reservation res         = (Reservation) request.getAttribute("reservation");
    List<Payment> payments  = (List<Payment>) request.getAttribute("payments");
    List<ExtraCharge> extra = (List<ExtraCharge>) request.getAttribute("extraCharges");
    Double extraTotal       = (Double) request.getAttribute("extraTotal");
    Double totalDue         = (Double) request.getAttribute("totalDue");
    Double totalPaid        = (Double) request.getAttribute("totalPaid");
    Double balance          = (Double) request.getAttribute("balance");
    String errMsg           = (String) request.getAttribute("errorMessage");
    String ctx              = request.getContextPath();
    DateTimeFormatter fmt   = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    String today            = DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(LocalDate.now());

    double xt  = extraTotal != null ? extraTotal : 0.0;
    double td  = totalDue   != null ? totalDue   : 0.0;
    double tp  = totalPaid  != null ? totalPaid  : 0.0;
    double bal = balance    != null ? balance     : 0.0;
    double taxRate   = AppSettings.getTaxRate();
    double taxAmount = td * taxRate / 100.0;
    String currency  = AppSettings.getCurrency();
    String hotelAddress = AppSettings.getHotelAddress();
    String hotelPhone   = AppSettings.getHotelPhone();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Invoice #<%= res != null ? res.getReservationId() : "" %> &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .topnav  { background: #1b263b; }
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #778da9; padding: 18px 20px 6px; }
        .sidebar .nav-link { color: #e0e1dd; font-size: .9rem; padding: 10px 20px; border-radius: 0; }
        .sidebar .nav-link:hover { background: #1b263b; color: #fff; }

        /* Invoice card */
        .inv-card { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,.1); overflow: hidden; max-width: 780px; margin: 0 auto; }
        .inv-header { background: #1b263b; color: #fff; padding: 28px 32px 20px; }
        .inv-header h5 { font-size: 1.4rem; font-weight: 700; margin: 0; }
        .inv-header p  { font-size: .85rem; opacity: .8; margin: 4px 0 0; }
        .inv-label { background: #0d1b2a; color: #fff; text-align: center;
                     font-size: .72rem; letter-spacing: .18em; text-transform: uppercase;
                     font-weight: 700; padding: 6px; }
        .inv-guest-bar { background: #f8fafc; border-bottom: 1px solid #e8ecf0; padding: 16px 28px; }
        .inv-body { padding: 28px; }
        .section-title { font-size: .72rem; text-transform: uppercase; letter-spacing: .12em;
                         color: #888; font-weight: 700; margin: 18px 0 8px; }
        .inv-table { width: 100%; border-collapse: collapse; }
        .inv-table thead th { background: #f1f5f9; font-size: .8rem; font-weight: 600; color: #555; padding: 8px 12px; }
        .inv-table tbody td { padding: 8px 12px; border-bottom: 1px solid #f0f4f8; font-size: .88rem; }
        .inv-table tfoot td { border-top: 2px solid #dee2e6; font-weight: 700; padding: 8px 12px; }
        .totals-box { background: #f8fafc; border-radius: 8px; padding: 16px 20px; }
        .totals-box table { width: 100%; }
        .totals-box td { padding: 5px 8px; font-size: .9rem; }
        .grand-row td { font-size: 1.05rem; font-weight: 700; color: #1b263b; border-top: 2px solid #dee2e6; padding-top: 10px; }
        .balance-row td { font-size: 1rem; font-weight: 700; }
        .balance-due  { color: #dc3545; }
        .balance-zero { color: #198754; }

        @media print {
            .no-print, .sidebar, .topnav { display: none !important; }
            .d-flex  { display: block !important; }
            body     { background: white; }
            .inv-card { box-shadow: none; border: 1px solid #ccc; border-radius: 0; max-width: 100%; }
        }
    </style>
</head>
<body>

<nav class="navbar topnav px-3 no-print">
    <span class="text-white fw-semibold">OceanView Hotel &mdash; Admin</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge bg-danger ms-1"><%= user.getRole() %></span></span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>

<div class="d-flex">
    <div class="sidebar no-print">
        <div class="lbl">Operations</div>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Room Status</a>
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link">&#128198; Check-Out</a>
        <div class="lbl">&#128274; Admin Only</div>
        <a href="<%= ctx %>/users"        class="nav-link">&#128100; Manage Users</a>
        <a href="<%= ctx %>/banks"        class="nav-link">&#127974; Bank Management</a>
        <a href="<%= ctx %>/reports"      class="nav-link">&#128202; Reports</a>
        <a href="<%= ctx %>/billing"      class="nav-link">&#128179; Billing</a>
        <a href="<%= ctx %>/billing?action=dashboard" class="nav-link">&#128200; Revenue Dashboard</a>
        <a href="<%= ctx %>/audit"        class="nav-link">&#128196; Audit Logs</a>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#127968; Dashboard</a>
    </div>

    <div class="flex-grow-1 p-4">

        <% if (errMsg != null) { %>
        <div class="alert alert-danger no-print"><%= errMsg %></div>
        <% } %>

        <% if (res == null) { %>
        <div class="alert alert-warning">Reservation not found. <a href="<%= ctx %>/billing">Back to Billing</a></div>
        <% } else { %>

        <div class="inv-card">

            <!-- Hotel header -->
            <div class="inv-header">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <h5>&#127748; OceanView Hotel</h5>
                        <p><%= hotelAddress %> &bull; Tel: <%= hotelPhone %></p>
                    </div>
                    <div class="text-end small" style="opacity:.85">
                        <div>Invoice #<strong><%= res.getReservationId() %></strong></div>
                        <div>Date Issued: <%= today %></div>
                        <div>Prepared by: <%= user.getFullName() %></div>
                    </div>
                </div>
            </div>
            <div class="inv-label">Official Invoice</div>

            <!-- Guest info -->
            <div class="inv-guest-bar">
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Bill To</div>
                        <div class="fw-bold"><%= res.getGuestName() %></div>
                        <div class="small text-muted"><%= res.getGuestEmail() != null ? res.getGuestEmail() : "" %></div>
                        <div class="small text-muted"><%= res.getGuestPhone() != null ? res.getGuestPhone() : "" %></div>
                    </div>
                    <div class="col-md-2">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Room</div>
                        <div class="fw-bold">Room <%= res.getRoomNumber() %></div>
                        <div class="small"><%= res.getRoomType() != null ? res.getRoomType().getDisplayName() : "" %></div>
                    </div>
                    <div class="col-md-3">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Check-In</div>
                        <div class="fw-bold"><%= res.getCheckInDate()  != null ? res.getCheckInDate().format(fmt)  : "—" %></div>
                    </div>
                    <div class="col-md-3">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Check-Out</div>
                        <div class="fw-bold"><%= res.getCheckOutDate() != null ? res.getCheckOutDate().format(fmt) : "—" %></div>
                    </div>
                </div>
            </div>

            <div class="inv-body">

                <!-- Room charges -->
                <div class="section-title">&#127963; Room Charges</div>
                <table class="inv-table">
                    <thead><tr><th>Description</th><th class="text-center">Nights</th><th class="text-end">Amount (<%= currency %>)</th></tr></thead>
                    <tbody>
                        <tr>
                            <td>Room <%= res.getRoomNumber() %>
                                <% if (res.getRoomType() != null) { %>— <%= res.getRoomType().getDisplayName() %><% } %>
                            </td>
                            <td class="text-center"><%= res.getNights() %></td>
                            <td class="text-end fw-semibold"><%= String.format("%,.2f", res.getTotalAmount()) %></td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="2" class="text-end text-muted">Room Subtotal</td>
                            <td class="text-end"><%= String.format("%,.2f", res.getTotalAmount()) %></td>
                        </tr>
                    </tfoot>
                </table>

                <!-- Extra charges -->
                <% if (extra != null && !extra.isEmpty()) { %>
                <div class="section-title">&#9888; Extra Charges</div>
                <table class="inv-table">
                    <thead><tr><th>Type</th><th>Description</th><th class="text-end">Amount (<%= currency %>)</th></tr></thead>
                    <tbody>
                    <% for (ExtraCharge ec : extra) { %>
                        <tr>
                            <td><span class="badge bg-warning text-dark" style="font-size:.75rem"><%= ec.getChargeType() %></span></td>
                            <td class="small"><%= ec.getDescription() != null && !ec.getDescription().isBlank() ? ec.getDescription() : "—" %></td>
                            <td class="text-end text-danger fw-semibold"><%= String.format("%,.2f", ec.getAmount()) %></td>
                        </tr>
                    <% } %>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="2" class="text-end text-muted">Extra Subtotal</td>
                            <td class="text-end text-danger"><%= String.format("%,.2f", xt) %></td>
                        </tr>
                    </tfoot>
                </table>
                <% } %>

                <hr style="border-top:1px dashed #ccc;margin:20px 0">

                <!-- Totals -->
                <div class="totals-box">
                    <table>
                        <tr>
                            <td class="text-muted">Room Charges</td>
                            <td class="text-end"><%= currency %> <%= String.format("%,.2f", res.getTotalAmount()) %></td>
                        </tr>
                        <% if (xt > 0) { %>
                        <tr>
                            <td class="text-muted">Extra Charges</td>
                            <td class="text-end text-danger"><%= currency %> <%= String.format("%,.2f", xt) %></td>
                        </tr>
                        <% } %>
                        <% if (taxRate > 0) { %>
                        <tr>
                            <td class="text-muted">Tax (<%= String.format("%.2f", taxRate) %>%)</td>
                            <td class="text-end"><%= currency %> <%= String.format("%,.2f", taxAmount) %></td>
                        </tr>
                        <% } %>
                        <tr class="grand-row">
                            <td>Grand Total</td>
                            <td class="text-end"><%= currency %> <%= String.format("%,.2f", taxRate > 0 ? td + taxAmount : td) %></td>
                        </tr>
                    </table>
                </div>

                <!-- Payments received -->
                <% if (payments != null && !payments.isEmpty()) { %>
                <div class="section-title mt-4">&#128179; Payments Received</div>
                <table class="inv-table">
                    <thead><tr><th>Date</th><th>Method</th><th>Reference / Bank / Card</th><th class="text-end">Amount (<%= currency %>)</th></tr></thead>
                    <tbody>
                    <% for (Payment p : payments) { %>
                        <tr>
                            <td><%= p.getCreatedAt() != null ? p.getCreatedAt().format(dtFmt) : "—" %></td>
                            <td><span class="badge bg-secondary"><%= p.getMethod() %></span></td>
                            <td class="small">
                                <% if (p.getBankName() != null) { %><%= p.getBankName() %><% } %>
                                <% if (p.getCardLast4() != null) { %> &bull; ****<%= p.getCardLast4() %><% } %>
                                <% if (p.getReferenceNo() != null) { %> &bull; Ref: <%= p.getReferenceNo() %><% } %>
                                <% if (p.getBankName() == null && p.getCardLast4() == null && p.getReferenceNo() == null) { %>—<% } %>
                            </td>
                            <td class="text-end text-success fw-semibold">+<%= String.format("%,.2f", p.getAmount()) %></td>
                        </tr>
                    <% } %>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" class="text-end text-muted">Total Paid</td>
                            <td class="text-end text-success"><%= currency %> <%= String.format("%,.2f", tp) %></td>
                        </tr>
                    </tfoot>
                </table>
                <% } %>

                <!-- Balance -->
                <div class="mt-3">
                    <div class="totals-box">
                        <table>
                            <tr>
                                <td class="text-muted">Grand Total Due</td>
                                <td class="text-end"><%= currency %> <%= String.format("%,.2f", td) %></td>
                            </tr>
                            <tr>
                                <td class="text-muted">Total Paid</td>
                                <td class="text-end text-success"><%= currency %> <%= String.format("%,.2f", tp) %></td>
                            </tr>
                            <tr class="balance-row">
                                <td class="<%= bal > 0.01 ? "balance-due" : "balance-zero" %>">
                                    <%= bal > 0.01 ? "&#9888; Outstanding Balance" : "&#10003; Fully Paid" %>
                                </td>
                                <td class="text-end <%= bal > 0.01 ? "balance-due" : "balance-zero" %>">
                                    <%= currency %> <%= String.format("%,.2f", bal) %>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

                <!-- Footer note -->
                <div class="text-center text-muted mt-4" style="font-size:.78rem;border-top:1px solid #eee;padding-top:14px">
                    Thank you for staying at OceanView Hotel.<br>
                    This is an official invoice. Please retain for your records.
                </div>

                <!-- Action buttons -->
                <div class="d-flex gap-2 justify-content-end mt-4 no-print">
                    <button onclick="window.print()" class="btn btn-outline-primary btn-sm">&#128438; Print Invoice</button>
                    <a href="<%= ctx %>/billing?action=folio&id=<%= res.getReservationId() %>"
                       class="btn btn-outline-secondary btn-sm">&#128196; View Folio</a>
                    <a href="<%= ctx %>/billing" class="btn btn-secondary btn-sm">&#8592; Back to Billing</a>
                </div>

            </div>
        </div><!-- /inv-card -->

        <% } %>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
