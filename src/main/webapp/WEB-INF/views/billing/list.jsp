<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.BillingRow, oceanview.model.AppSettings,
                 java.util.List, java.time.format.DateTimeFormatter" %>
<%
    User user           = (User) session.getAttribute("loggedInUser");
    List<BillingRow> rows = (List<BillingRow>) request.getAttribute("billingRows");
    String fSearch      = (String) request.getAttribute("fSearch");
    String fDateFrom    = (String) request.getAttribute("fDateFrom");
    String fDateTo      = (String) request.getAttribute("fDateTo");
    String fStatus      = (String) request.getAttribute("fStatus");
    String errMsg       = (String) request.getAttribute("errorMessage");
    String ctx          = request.getContextPath();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    if (fSearch   == null) fSearch   = "";
    if (fDateFrom == null) fDateFrom = "";
    if (fDateTo   == null) fDateTo   = "";
    if (fStatus   == null) fStatus   = "";
    String currency = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Billing &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; font-family: 'Segoe UI', sans-serif; }

        /* Nav */
        .topnav { background: #1b263b; }
        .topnav .brand { color: #fff; font-weight: 600; font-size: 1.1rem; }
        .topnav .nav-right { color: rgba(255,255,255,.85); font-size: .88rem; }

        /* Sidebar */
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #778da9; padding: 18px 20px 6px; }
        .sidebar .nav-link { color: #e0e1dd; font-size: .9rem; padding: 10px 20px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #1b263b; color: #fff; }

        /* Badges */
        .badge-PAID     { background: #198754; color: #fff; }
        .badge-PARTIAL  { background: #fd7e14; color: #fff; }
        .badge-UNPAID   { background: #dc3545; color: #fff; }
        .badge-NA       { background: #6c757d; color: #fff; }
        .bill-badge {
            display: inline-block; padding: 3px 9px; border-radius: 20px;
            font-size: .73rem; font-weight: 700; text-transform: uppercase; letter-spacing: .04em;
        }

        /* Res status badges */
        .badge-status {
            display: inline-block; padding: 3px 9px; border-radius: 20px;
            font-size: .73rem; font-weight: 700; text-transform: uppercase; letter-spacing: .04em;
        }
        .rs-PENDING     { background: #fff3cd; color: #856404; }
        .rs-CONFIRMED   { background: #d1ecf1; color: #0c5460; }
        .rs-CHECKED_IN  { background: #d4edda; color: #155724; }
        .rs-CHECKED_OUT { background: #e2e3e5; color: #383d41; }
        .rs-CANCELLED   { background: #f8d7da; color: #721c24; }
        .rs-NO_SHOW     { background: #fde8e8; color: #7b2d2d; }

        @media print {
            .no-print { display: none !important; }
            .sidebar  { display: none !important; }
            .topnav   { display: none !important; }
            .d-flex   { display: block !important; }
            body      { background: white; }
        }
    </style>
</head>
<body>

<nav class="navbar topnav px-3 no-print">
    <span class="brand">OceanView Hotel &mdash; Admin</span>
    <div class="nav-right d-flex align-items-center gap-3">
        <span>Welcome, <strong><%= user.getFullName() %></strong>
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
        <a href="<%= ctx %>/billing"      class="nav-link active">&#128179; Billing</a>
        <a href="<%= ctx %>/billing?action=dashboard" class="nav-link">&#128200; Revenue Dashboard</a>
        <a href="<%= ctx %>/audit"        class="nav-link">&#128196; Audit Logs</a>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#127968; Dashboard</a>
    </div>

    <div class="flex-grow-1 p-4">

        <div class="d-flex align-items-center justify-content-between mb-3">
            <h4 class="mb-0" style="color:#1b263b">&#128179; Billing &amp; Revenue</h4>
            <div class="d-flex gap-2">
                <a href="<%= ctx %>/billing?action=dashboard" class="btn btn-sm btn-outline-primary">&#128200; Revenue Dashboard</a>
                <button class="btn btn-sm btn-outline-secondary no-print" onclick="window.print()">&#128438; Print</button>
            </div>
        </div>

        <% if (errMsg != null) { %>
        <div class="alert alert-danger"><%= errMsg %></div>
        <% } %>

        <!-- Filter bar -->
        <form method="get" action="<%= ctx %>/billing" class="card card-body mb-3 p-3 no-print">
            <div class="row g-2 align-items-end">
                <div class="col-md-3">
                    <label class="form-label small fw-semibold mb-1">Search Guest</label>
                    <input type="text" name="search" class="form-control form-control-sm"
                           placeholder="Guest name..." value="<%= fSearch %>">
                </div>
                <div class="col-md-2">
                    <label class="form-label small fw-semibold mb-1">Check-In From</label>
                    <input type="date" name="dateFrom" class="form-control form-control-sm" value="<%= fDateFrom %>">
                </div>
                <div class="col-md-2">
                    <label class="form-label small fw-semibold mb-1">Check-Out To</label>
                    <input type="date" name="dateTo" class="form-control form-control-sm" value="<%= fDateTo %>">
                </div>
                <div class="col-md-2">
                    <label class="form-label small fw-semibold mb-1">Status</label>
                    <select name="status" class="form-select form-select-sm">
                        <option value="">-- All Statuses --</option>
                        <option value="PENDING"     <%= "PENDING".equals(fStatus)     ? "selected" : "" %>>Pending</option>
                        <option value="CONFIRMED"   <%= "CONFIRMED".equals(fStatus)   ? "selected" : "" %>>Confirmed</option>
                        <option value="CHECKED_IN"  <%= "CHECKED_IN".equals(fStatus)  ? "selected" : "" %>>Checked In</option>
                        <option value="CHECKED_OUT" <%= "CHECKED_OUT".equals(fStatus) ? "selected" : "" %>>Checked Out</option>
                        <option value="CANCELLED"   <%= "CANCELLED".equals(fStatus)   ? "selected" : "" %>>Cancelled</option>
                        <option value="NO_SHOW"     <%= "NO_SHOW".equals(fStatus)     ? "selected" : "" %>>No Show</option>
                    </select>
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-sm btn-primary">&#128269; Search</button>
                    <a href="<%= ctx %>/billing" class="btn btn-sm btn-secondary ms-1">Clear</a>
                </div>
            </div>
        </form>

        <!-- Table -->
        <div class="card shadow-sm border-0">
            <% if (rows == null || rows.isEmpty()) { %>
            <div class="text-center text-muted py-5">No billing records found.</div>
            <% } else { %>
            <div class="table-responsive">
            <table class="table table-hover table-sm align-middle mb-0" style="font-size:.84rem">
                <thead style="background:#1b263b;color:#fff">
                    <tr>
                        <th>#</th>
                        <th>Guest</th>
                        <th>Room</th>
                        <th>Type</th>
                        <th>Check-In</th>
                        <th>Check-Out</th>
                        <th>Nights</th>
                        <th class="text-end">Room (<%= currency %>)</th>
                        <th class="text-end">Extra (<%= currency %>)</th>
                        <th class="text-end">Total Due</th>
                        <th class="text-end">Total Paid</th>
                        <th class="text-end">Balance</th>
                        <th>Res. Status</th>
                        <th>Billing</th>
                        <th class="no-print">Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% for (BillingRow r : rows) {
                    String bs = r.getBillingStatus() != null ? r.getBillingStatus() : "NA";
                    String badgeCls = "badge-" + ("N/A".equals(bs) ? "NA" : bs);
                    String rsCls = "rs-" + (r.getReservationStatus() != null ? r.getReservationStatus() : "");
                %>
                <tr>
                    <td><%= r.getReservationId() %></td>
                    <td>
                        <strong><%= r.getGuestName() %></strong><br>
                        <small class="text-muted"><%= r.getGuestEmail() != null ? r.getGuestEmail() : "" %></small>
                    </td>
                    <td><%= r.getRoomNumber() %></td>
                    <td><%= r.getRoomType() != null ? r.getRoomType() : "—" %></td>
                    <td><%= r.getCheckInDate()  != null ? r.getCheckInDate().format(fmt)  : "—" %></td>
                    <td><%= r.getCheckOutDate() != null ? r.getCheckOutDate().format(fmt) : "—" %></td>
                    <td><%= r.getNights() %></td>
                    <td class="text-end"><%= String.format("%,.2f", r.getRoomCharges()) %></td>
                    <td class="text-end"><%= String.format("%,.2f", r.getExtraCharges()) %></td>
                    <td class="text-end fw-semibold"><%= String.format("%,.2f", r.getTotalDue()) %></td>
                    <td class="text-end text-success"><%= String.format("%,.2f", r.getTotalPaid()) %></td>
                    <td class="text-end <%= r.getBalance() > 0.01 ? "text-danger fw-bold" : "text-success" %>">
                        <%= String.format("%,.2f", r.getBalance()) %>
                    </td>
                    <td><span class="badge-status <%= rsCls %>"><%= r.getReservationStatus() != null ? r.getReservationStatus() : "—" %></span></td>
                    <td><span class="bill-badge <%= badgeCls %>"><%= bs %></span></td>
                    <td class="no-print">
                        <div class="d-flex gap-1">
                            <a href="<%= ctx %>/billing?action=invoice&id=<%= r.getReservationId() %>"
                               class="btn btn-xs btn-outline-primary"
                               style="font-size:.75rem;padding:2px 8px">Invoice</a>
                            <a href="<%= ctx %>/billing?action=folio&id=<%= r.getReservationId() %>"
                               class="btn btn-xs btn-outline-secondary"
                               style="font-size:.75rem;padding:2px 8px">Folio</a>
                        </div>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
            </div>
            <% } %>
        </div>

    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
