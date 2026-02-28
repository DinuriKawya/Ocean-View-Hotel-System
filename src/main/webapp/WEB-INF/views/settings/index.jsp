<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, java.util.Map" %>
<%
    User user = (User) session.getAttribute("loggedInUser");
    Map<String, String> settings = (Map<String, String>) request.getAttribute("settings");
    String msg    = request.getAttribute("msg")          != null ? (String) request.getAttribute("msg") : "";
    String errMsg = (String) request.getAttribute("errorMessage");
    String ctx    = request.getContextPath();

    String vCurrency    = settings != null && settings.containsKey("currency")      ? settings.get("currency")      : "LKR";
    String vHotelName   = settings != null && settings.containsKey("hotel_name")    ? settings.get("hotel_name")    : "OceanView Hotel";
    String vHotelAddr   = settings != null && settings.containsKey("hotel_address") ? settings.get("hotel_address") : "";
    String vHotelPhone  = settings != null && settings.containsKey("hotel_phone")   ? settings.get("hotel_phone")   : "";
    String vTaxRate     = settings != null && settings.containsKey("tax_rate")      ? settings.get("tax_rate")      : "0";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>System Settings &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', sans-serif; background: #f0f4f8; color: #333; }

        nav {
            background: #1b263b; color: #fff;
            display: flex; align-items: center; justify-content: space-between;
            padding: 14px 28px;
        }
        nav .brand { font-size: 1.2rem; font-weight: 600; }
        nav .user-info { font-size: 0.9rem; }
        nav .user-info span { margin-right: 12px; }
        nav a.logout {
            color: #fff; text-decoration: none;
            background: rgba(255,255,255,0.2);
            padding: 6px 14px; border-radius: 4px; font-size: 0.85rem;
        }
        nav a.logout:hover { background: rgba(255,255,255,0.35); }

        .badge-role {
            display: inline-block; background: #e63946; color: #fff;
            font-size: 0.72rem; font-weight: 700; padding: 2px 8px;
            border-radius: 20px; letter-spacing: 0.05em; text-transform: uppercase; margin-left: 6px;
        }

        .wrapper { display: flex; min-height: calc(100vh - 52px); }

        aside {
            width: 220px; background: #0d1b2a; padding: 24px 0; flex-shrink: 0;
        }
        aside h3 {
            color: #778da9; font-size: 0.72rem; text-transform: uppercase;
            letter-spacing: 0.1em; padding: 0 20px 10px;
        }
        aside a {
            display: block; color: #e0e1dd; text-decoration: none;
            padding: 11px 20px; font-size: 0.9rem; transition: background 0.15s;
        }
        aside a:hover { background: #1b263b; }
        aside a.active { background: #1b263b; color: #fff; }
        aside .section { margin-top: 18px; }

        main { flex: 1; padding: 30px 36px; }
        main h2 { font-size: 1.4rem; margin-bottom: 6px; color: #1b263b; }
        main p.sub { color: #666; font-size: 0.88rem; margin-bottom: 28px; }

        .settings-card {
            background: #fff; border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 28px 32px; max-width: 680px;
        }
        .form-label { font-weight: 600; margin-bottom: 4px; }
        .form-text  { font-size: 0.8rem; color: #888; margin-top: 3px; }
    </style>
</head>
<body>

<!-- Toast container -->
<div class="toast-container position-fixed top-0 end-0 p-3" style="z-index:9999">
    <% if (msg != null && !msg.isEmpty()) { %>
    <div id="msgToast" class="toast align-items-center text-bg-success border-0"
         role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body fw-semibold">&#10003; <%= msg %></div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto"
                    data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    </div>
    <% } %>
</div>

<nav>
    <span class="brand">OceanView Hotel &mdash; Admin</span>
    <div class="user-info">
        <span>Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge-role"><%= user.getRole() %></span>
        </span>
        <a class="logout" href="<%= ctx %>/logout">Logout</a>
    </div>
</nav>

<div class="wrapper">
    <aside>
        <h3>Operations</h3>
        <a href="<%= ctx %>/reservations">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms">&#127963; Room Status</a>
        <a href="<%= ctx %>/checkin">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout">&#128198; Check-Out</a>

        <div class="section">
            <h3>&#128274; Admin Only</h3>
            <a href="<%= ctx %>/users">&#128100; Manage Users</a>
            <a href="<%= ctx %>/banks">&#127974; Bank Management</a>
            <a href="<%= ctx %>/reports">&#128202; Reports &amp; Analytics</a>
            <a href="<%= ctx %>/rooms">&#128188; Room Management</a>
            <a href="<%= ctx %>/billing">&#128179; Billing &amp; Revenue</a>
            <a href="<%= ctx %>/audit">&#128196; Audit Logs</a>
            <a href="<%= ctx %>/settings" class="active">&#9881; System Settings</a>
        </div>
    </aside>

    <main>
        <h2>&#9881; System Settings</h2>
        <p class="sub">Configure hotel-wide settings. Changes take effect immediately.</p>

        <div class="settings-card">
            <% if (errMsg != null) { %>
            <div class="alert alert-danger mb-3"><%= errMsg %></div>
            <% } %>

            <form method="post" action="<%= ctx %>/settings">
                <div class="mb-3">
                    <label class="form-label">Currency Code</label>
                    <input type="text" name="currency" class="form-control"
                           value="<%= vCurrency %>" maxlength="10" required placeholder="e.g. LKR">
                    <div class="form-text">3-letter ISO code shown in all price displays (e.g. LKR, USD, EUR).</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Hotel Name</label>
                    <input type="text" name="hotel_name" class="form-control"
                           value="<%= vHotelName %>" maxlength="200" required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Hotel Address</label>
                    <input type="text" name="hotel_address" class="form-control"
                           value="<%= vHotelAddr %>" maxlength="500">
                    <div class="form-text">Shown on invoices and bills.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Hotel Phone</label>
                    <input type="text" name="hotel_phone" class="form-control"
                           value="<%= vHotelPhone %>" maxlength="50">
                    <div class="form-text">Shown on invoices and bills.</div>
                </div>

                <div class="mb-4">
                    <label class="form-label">Tax Rate (%)</label>
                    <input type="number" name="tax_rate" class="form-control"
                           value="<%= vTaxRate %>" min="0" step="0.01" required>
                    <div class="form-text">Applied on invoices and bills. Set to 0 to disable tax.</div>
                </div>

                <div class="d-flex gap-3 align-items-center">
                    <button type="submit" class="btn btn-primary px-4">&#10003; Save Settings</button>
                    <a href="<%= ctx %>/dashboard" class="text-decoration-none text-secondary"
                       style="font-size:0.9rem">&#8592; Back to Dashboard</a>
                </div>
            </form>
        </div>
    </main>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        var el = document.getElementById('msgToast');
        if (el) new bootstrap.Toast(el, { delay: 4000 }).show();
    });
</script>
</body>
</html>
