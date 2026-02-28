<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*, oceanview.model.AppSettings, java.util.List, java.util.ArrayList,
                 java.time.LocalDate, java.time.LocalDateTime, java.time.format.DateTimeFormatter" %>
<%
    User user               = (User) session.getAttribute("loggedInUser");
    Reservation res         = (Reservation) request.getAttribute("reservation");
    List<Payment> payments  = (List<Payment>) request.getAttribute("payments");
    List<ExtraCharge> extra = (List<ExtraCharge>) request.getAttribute("extraCharges");
    Double totalDue         = (Double) request.getAttribute("totalDue");
    Double totalPaid        = (Double) request.getAttribute("totalPaid");
    Double balance          = (Double) request.getAttribute("balance");
    String errMsg           = (String) request.getAttribute("errorMessage");
    String ctx              = request.getContextPath();
    DateTimeFormatter fmt   = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    double td  = totalDue  != null ? totalDue  : 0.0;
    double tp  = totalPaid != null ? totalPaid : 0.0;
    double bal = balance   != null ? balance   : 0.0;
    String currency = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Folio #<%= res != null ? res.getReservationId() : "" %> &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .topnav  { background: #1b263b; }
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #778da9; padding: 18px 20px 6px; }
        .sidebar .nav-link { color: #e0e1dd; font-size: .9rem; padding: 10px 20px; border-radius: 0; }
        .sidebar .nav-link:hover { background: #1b263b; color: #fff; }

        .folio-card { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,.1); overflow: hidden; max-width: 860px; margin: 0 auto; }
        .folio-header { background: #1b263b; color: #fff; padding: 22px 28px; }
        .folio-header h5 { font-size: 1.3rem; font-weight: 700; margin: 0; }
        .folio-subhead { background: #0d1b2a; color: #9db2cc; text-align: center;
                         font-size: .72rem; letter-spacing: .16em; text-transform: uppercase;
                         font-weight: 700; padding: 5px; }
        .folio-guest-bar { background: #f8fafc; border-bottom: 1px solid #e8ecf0; padding: 14px 28px; }
        .folio-body { padding: 24px 28px; }

        .folio-table { width: 100%; border-collapse: collapse; font-size: .87rem; }
        .folio-table thead th { background: #1b263b; color: #fff; padding: 10px 12px; font-weight: 600; }
        .folio-table tbody td { padding: 9px 12px; border-bottom: 1px solid #f0f4f8; vertical-align: middle; }
        .folio-table tfoot td { border-top: 2px solid #dee2e6; font-weight: 700; padding: 9px 12px; }
        .debit  { color: #dc3545; }
        .credit { color: #198754; }
        .running-balance { font-weight: 600; }

        @media print {
            .no-print, .sidebar, .topnav { display: none !important; }
            .d-flex  { display: block !important; }
            body     { background: white; }
            .folio-card { box-shadow: none; border: 1px solid #ccc; border-radius: 0; max-width: 100%; }
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

        <div class="folio-card">

            <!-- Header -->
            <div class="folio-header">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <h5>&#127748; OceanView Hotel &mdash; Guest Folio</h5>
                        <p class="mb-0 small" style="opacity:.8">Chronological Ledger &bull; Reservation #<strong><%= res.getReservationId() %></strong></p>
                    </div>
                    <div class="text-end small" style="opacity:.85">
                        <div>Generated: <%= DateTimeFormatter.ofPattern("MMM dd, yyyy").format(LocalDate.now()) %></div>
                        <div>By: <%= user.getFullName() %></div>
                    </div>
                </div>
            </div>
            <div class="folio-subhead">Guest Ledger</div>

            <!-- Guest info -->
            <div class="folio-guest-bar">
                <div class="row g-2">
                    <div class="col-md-3">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Guest</div>
                        <div class="fw-bold small"><%= res.getGuestName() %></div>
                        <div class="small text-muted"><%= res.getGuestEmail() != null ? res.getGuestEmail() : "" %></div>
                    </div>
                    <div class="col-md-2">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Room</div>
                        <div class="fw-bold small">Room <%= res.getRoomNumber() %></div>
                        <div class="small"><%= res.getRoomType() != null ? res.getRoomType().getDisplayName() : "" %></div>
                    </div>
                    <div class="col-md-2">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Check-In</div>
                        <div class="fw-bold small"><%= res.getCheckInDate()  != null ? res.getCheckInDate().format(fmt)  : "—" %></div>
                    </div>
                    <div class="col-md-2">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Check-Out</div>
                        <div class="fw-bold small"><%= res.getCheckOutDate() != null ? res.getCheckOutDate().format(fmt) : "—" %></div>
                    </div>
                    <div class="col-md-3">
                        <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Status</div>
                        <div class="fw-bold small"><%= res.getStatus() != null ? res.getStatus().getDisplayName() : "—" %></div>
                    </div>
                </div>
            </div>

            <div class="folio-body">
                <table class="folio-table">
                    <thead>
                        <tr>
                            <th>Date / Time</th>
                            <th>Description</th>
                            <th class="text-end">Debit (<%= currency %>)</th>
                            <th class="text-end">Credit (<%= currency %>)</th>
                            <th class="text-end">Running Balance</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        /* -------------------------------------------------------
                         * Build a unified chronological ledger:
                         *   1) Room charge at check-in (debit)
                         *   2) Extra charges in order (debit)
                         *   3) Payments in order (credit)
                         * Running balance is computed in the JSP loop.
                         * ------------------------------------------------------- */
                        double runningBalance = 0.0;

                        // 1. Room charge
                        runningBalance += res.getTotalAmount();
                        String checkInLabel = res.getCheckInDate() != null
                            ? res.getCheckInDate().format(fmt) : "—";
                    %>
                        <tr>
                            <td><%= checkInLabel %></td>
                            <td>Room <%= res.getRoomNumber() %>
                                <% if (res.getRoomType() != null) { %>— <%= res.getRoomType().getDisplayName() %><% } %>
                                (<%= res.getNights() %> night<%= res.getNights() != 1 ? "s" : "" %>)
                            </td>
                            <td class="text-end debit"><%= String.format("%,.2f", res.getTotalAmount()) %></td>
                            <td class="text-end">—</td>
                            <td class="text-end running-balance <%= runningBalance > 0.01 ? "debit" : "credit" %>">
                                <%= String.format("%,.2f", runningBalance) %>
                            </td>
                        </tr>

                    <%  // 2. Extra charges
                        if (extra != null) {
                            for (ExtraCharge ec : extra) {
                                runningBalance += ec.getAmount();
                    %>
                        <tr>
                            <td><%= ec.getCreatedAt() != null ? ec.getCreatedAt().format(dtFmt) : "—" %></td>
                            <td>
                                <span class="badge bg-warning text-dark" style="font-size:.72rem"><%= ec.getChargeType() %></span>
                                <% if (ec.getDescription() != null && !ec.getDescription().isBlank()) { %>
                                    &nbsp;<small class="text-muted"><%= ec.getDescription() %></small>
                                <% } %>
                            </td>
                            <td class="text-end debit"><%= String.format("%,.2f", ec.getAmount()) %></td>
                            <td class="text-end">—</td>
                            <td class="text-end running-balance <%= runningBalance > 0.01 ? "debit" : "credit" %>">
                                <%= String.format("%,.2f", runningBalance) %>
                            </td>
                        </tr>
                    <%      }
                        } %>

                    <%  // 3. Payments
                        if (payments != null) {
                            for (Payment p : payments) {
                                runningBalance -= p.getAmount();
                    %>
                        <tr>
                            <td><%= p.getCreatedAt() != null ? p.getCreatedAt().format(dtFmt) : "—" %></td>
                            <td>
                                Payment &mdash; <span class="badge bg-secondary" style="font-size:.72rem"><%= p.getMethod() %></span>
                                <% if (p.getBankName() != null) { %>
                                    <small class="text-muted">&nbsp;<%= p.getBankName() %></small>
                                <% } %>
                                <% if (p.getCardLast4() != null) { %>
                                    <small class="text-muted">&nbsp;****<%= p.getCardLast4() %></small>
                                <% } %>
                                <% if (p.getReferenceNo() != null) { %>
                                    <small class="text-muted">&nbsp;Ref: <%= p.getReferenceNo() %></small>
                                <% } %>
                            </td>
                            <td class="text-end">—</td>
                            <td class="text-end credit"><%= String.format("%,.2f", p.getAmount()) %></td>
                            <td class="text-end running-balance <%= runningBalance > 0.01 ? "debit" : "credit" %>">
                                <%= String.format("%,.2f", runningBalance) %>
                            </td>
                        </tr>
                    <%      }
                        } %>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="2" class="text-end">Totals</td>
                            <td class="text-end debit"><%= String.format("%,.2f", td) %></td>
                            <td class="text-end credit"><%= String.format("%,.2f", tp) %></td>
                            <td class="text-end <%= bal > 0.01 ? "debit" : "credit" %>">
                                <strong><%= currency %> <%= String.format("%,.2f", bal) %></strong>
                                &nbsp;<small><%= bal > 0.01 ? "OUTSTANDING" : "SETTLED" %></small>
                            </td>
                        </tr>
                    </tfoot>
                </table>

                <!-- Footer note -->
                <div class="text-center text-muted mt-4" style="font-size:.78rem;border-top:1px solid #eee;padding-top:14px">
                    Thank you for staying at OceanView Hotel. &bull; This folio is an official record of charges and payments.
                </div>

                <!-- Action buttons -->
                <div class="d-flex gap-2 justify-content-end mt-4 no-print">
                    <button onclick="window.print()" class="btn btn-outline-primary btn-sm">&#128438; Print Folio</button>
                    <a href="<%= ctx %>/billing?action=invoice&id=<%= res.getReservationId() %>"
                       class="btn btn-outline-secondary btn-sm">&#128196; View Invoice</a>
                    <a href="<%= ctx %>/billing" class="btn btn-secondary btn-sm">&#8592; Back to Billing</a>
                </div>

            </div>
        </div><!-- /folio-card -->

        <% } %>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
