<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.AppSettings, java.util.List" %>
<%
    User user                     = (User) session.getAttribute("loggedInUser");
    Double revenueToday           = (Double)       request.getAttribute("revenueToday");
    Double revenueMonth           = (Double)       request.getAttribute("revenueMonth");
    Double revenueYear            = (Double)       request.getAttribute("revenueYear");
    Integer checkoutsMonth        = (Integer)      request.getAttribute("checkoutsMonth");
    List<String[]> revenueByMethod = (List<String[]>) request.getAttribute("revenueByMethod");
    List<String[]> revenueByType   = (List<String[]>) request.getAttribute("revenueByType");
    List<String[]> dailyRevenue    = (List<String[]>) request.getAttribute("dailyRevenue");
    List<String[]> recentPayments  = (List<String[]>) request.getAttribute("recentPayments");
    String errMsg                 = (String)       request.getAttribute("errorMessage");
    String ctx                    = request.getContextPath();

    double today  = revenueToday    != null ? revenueToday    : 0.0;
    double month  = revenueMonth    != null ? revenueMonth    : 0.0;
    double year   = revenueYear     != null ? revenueYear     : 0.0;
    int    coMonth = checkoutsMonth != null ? checkoutsMonth  : 0;
    String currency = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Revenue Dashboard &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
    <style>
        body { background: #f0f4f8; font-family: 'Segoe UI', sans-serif; }
        .topnav  { background: #1b263b; }
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #778da9; padding: 18px 20px 6px; }
        .sidebar .nav-link { color: #e0e1dd; font-size: .9rem; padding: 10px 20px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #1b263b; color: #fff; }

        /* KPI cards */
        .kpi-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,.08); padding: 22px 24px; }
        .kpi-card .kpi-label { font-size: .78rem; text-transform: uppercase; letter-spacing: .08em; color: #778da9; }
        .kpi-card .kpi-value { font-size: 1.6rem; font-weight: 700; color: #1b263b; }
        .kpi-card .kpi-icon  { font-size: 2rem; }

        /* Chart containers */
        .chart-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,.08); padding: 20px; }
        .chart-card h6 { color: #1b263b; font-size: .88rem; font-weight: 700; text-transform: uppercase; letter-spacing: .06em; margin-bottom: 14px; }

        @media print {
            .no-print, .sidebar, .topnav { display: none !important; }
            .d-flex  { display: block !important; }
            body     { background: white; }
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
        <a href="<%= ctx %>/billing"      class="nav-link">&#128179; Billing List</a>
        <a href="<%= ctx %>/billing?action=dashboard" class="nav-link active">&#128200; Revenue Dashboard</a>
        <a href="<%= ctx %>/audit"        class="nav-link">&#128196; Audit Logs</a>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#127968; Dashboard</a>
    </div>

    <div class="flex-grow-1 p-4">

        <div class="d-flex align-items-center justify-content-between mb-4">
            <h4 class="mb-0" style="color:#1b263b">&#128200; Revenue Dashboard</h4>
            <a href="<%= ctx %>/billing" class="btn btn-sm btn-outline-secondary no-print">&#128179; Billing List</a>
        </div>

        <% if (errMsg != null) { %>
        <div class="alert alert-danger no-print"><%= errMsg %></div>
        <% } %>

        <!-- KPI Row -->
        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="kpi-card d-flex align-items-center gap-3">
                    <div class="kpi-icon">&#9728;</div>
                    <div>
                        <div class="kpi-label">Today's Revenue</div>
                        <div class="kpi-value"><%= currency %> <%= String.format("%,.0f", today) %></div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="kpi-card d-flex align-items-center gap-3">
                    <div class="kpi-icon">&#128197;</div>
                    <div>
                        <div class="kpi-label">This Month</div>
                        <div class="kpi-value"><%= currency %> <%= String.format("%,.0f", month) %></div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="kpi-card d-flex align-items-center gap-3">
                    <div class="kpi-icon">&#128200;</div>
                    <div>
                        <div class="kpi-label">This Year</div>
                        <div class="kpi-value"><%= currency %> <%= String.format("%,.0f", year) %></div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="kpi-card d-flex align-items-center gap-3">
                    <div class="kpi-icon">&#128198;</div>
                    <div>
                        <div class="kpi-label">Month Check-Outs</div>
                        <div class="kpi-value"><%= coMonth %></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Charts Row 1 -->
        <div class="row g-3 mb-4">
            <div class="col-md-8">
                <div class="chart-card">
                    <h6>Daily Revenue &mdash; Last 30 Days</h6>
                    <canvas id="dailyChart" height="120"></canvas>
                </div>
            </div>
            <div class="col-md-4">
                <div class="chart-card">
                    <h6>Revenue by Payment Method (This Month)</h6>
                    <canvas id="methodChart" height="200"></canvas>
                </div>
            </div>
        </div>

        <!-- Charts Row 2 -->
        <div class="row g-3">
            <div class="col-md-6">
                <div class="chart-card">
                    <h6>Revenue by Room Type (This Year)</h6>
                    <canvas id="typeChart" height="180"></canvas>
                </div>
            </div>
            <div class="col-md-6">
                <div class="chart-card">
                    <h6>Recent Payments (Last 10)</h6>
                    <% if (recentPayments == null || recentPayments.isEmpty()) { %>
                    <p class="text-muted text-center py-4">No recent payments.</p>
                    <% } else { %>
                    <table class="table table-sm table-hover align-middle mb-0" style="font-size:.84rem">
                        <thead style="background:#f1f5f9"><tr><th>Date</th><th>Guest</th><th>Method</th><th class="text-end">Amount (<%= currency %>)</th></tr></thead>
                        <tbody>
                        <% for (String[] rp : recentPayments) { %>
                        <tr>
                            <td><%= rp[0] != null ? rp[0] : "—" %></td>
                            <td><%= rp[1] != null ? rp[1] : "—" %></td>
                            <td><span class="badge bg-secondary" style="font-size:.72rem"><%= rp[2] != null ? rp[2] : "—" %></span></td>
                            <td class="text-end text-success fw-semibold"><%= rp[3] != null ? String.format("%,.2f", Double.parseDouble(rp[3])) : "0.00" %></td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                    <% } %>
                </div>
            </div>
        </div>

    </div>
</div>

<script>
<%-- Daily Revenue line chart --%>
(function() {
    var labels  = [
        <%
        if (dailyRevenue != null) {
            for (int i = 0; i < dailyRevenue.size(); i++) {
                String[] dr = dailyRevenue.get(i); %>
                "<%= dr[0] != null ? dr[0] : "" %>"<%= i < dailyRevenue.size()-1 ? "," : "" %>
        <%  }
        } %>
    ];
    var amounts = [
        <%
        if (dailyRevenue != null) {
            for (int i = 0; i < dailyRevenue.size(); i++) {
                String[] dr = dailyRevenue.get(i); %>
                <%= dr[1] != null ? dr[1] : "0" %><%= i < dailyRevenue.size()-1 ? "," : "" %>
        <%  }
        } %>
    ];
    new Chart(document.getElementById('dailyChart'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Revenue (' + '<%= currency %>' + ')',
                data: amounts,
                borderColor: '#1b263b',
                backgroundColor: 'rgba(27,38,59,.1)',
                fill: true,
                tension: 0.3,
                pointRadius: 3
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true } }
        }
    });
})();

<%-- Revenue by Method doughnut chart --%>
(function() {
    var mLabels = [
        <%
        if (revenueByMethod != null) {
            for (int i = 0; i < revenueByMethod.size(); i++) {
                String[] m = revenueByMethod.get(i); %>
                "<%= m[0] != null ? m[0] : "" %>"<%= i < revenueByMethod.size()-1 ? "," : "" %>
        <%  }
        } %>
    ];
    var mAmounts = [
        <%
        if (revenueByMethod != null) {
            for (int i = 0; i < revenueByMethod.size(); i++) {
                String[] m = revenueByMethod.get(i); %>
                <%= m[1] != null ? m[1] : "0" %><%= i < revenueByMethod.size()-1 ? "," : "" %>
        <%  }
        } %>
    ];
    new Chart(document.getElementById('methodChart'), {
        type: 'doughnut',
        data: {
            labels: mLabels,
            datasets: [{
                data: mAmounts,
                backgroundColor: ['#1b263b','#415a77','#778da9','#e0e1dd','#0d1b2a']
            }]
        },
        options: {
            plugins: { legend: { position: 'bottom', labels: { font: { size: 11 } } } }
        }
    });
})();

<%-- Revenue by Room Type bar chart --%>
(function() {
    var tLabels = [
        <%
        if (revenueByType != null) {
            for (int i = 0; i < revenueByType.size(); i++) {
                String[] t = revenueByType.get(i); %>
                "<%= t[0] != null ? t[0] : "" %>"<%= i < revenueByType.size()-1 ? "," : "" %>
        <%  }
        } %>
    ];
    var tAmounts = [
        <%
        if (revenueByType != null) {
            for (int i = 0; i < revenueByType.size(); i++) {
                String[] t = revenueByType.get(i); %>
                <%= t[1] != null ? t[1] : "0" %><%= i < revenueByType.size()-1 ? "," : "" %>
        <%  }
        } %>
    ];
    new Chart(document.getElementById('typeChart'), {
        type: 'bar',
        data: {
            labels: tLabels,
            datasets: [{
                label: 'Revenue (' + '<%= currency %>' + ')',
                data: tAmounts,
                backgroundColor: '#415a77',
                borderRadius: 4
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true } }
        }
    });
})();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
