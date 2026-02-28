<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Reservation,
                 oceanview.model.ReservationStatus, oceanview.model.AppSettings,
                 java.util.List, java.time.format.DateTimeFormatter" %>
<%
    User user       = (User) session.getAttribute("loggedInUser");
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    ReservationStatus[] statuses   = (ReservationStatus[]) request.getAttribute("statuses");
    String search     = request.getParameter("search")     != null ? request.getParameter("search")     : "";
    String statusFilt = request.getParameter("status")     != null ? request.getParameter("status")     : "";
    String msg        = request.getParameter("msg")        != null ? request.getParameter("msg")        : "";
    String errMsg     = (String) request.getAttribute("errorMessage");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    String ctx = request.getContextPath();
    String currency = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reservations &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', sans-serif; background: #f0f4f8; color: #333; }

        nav {
            background: #0077b6; color: #fff;
            display: flex; align-items: center; justify-content: space-between;
            padding: 14px 28px;
        }
        nav .brand { font-size: 1.2rem; font-weight: 600; }
        nav .user-info { font-size: 0.9rem; }
        nav .user-info span { margin-right: 12px; }
        nav a { color: #fff; text-decoration: none; background: rgba(255,255,255,0.2);
                padding: 6px 14px; border-radius: 4px; font-size: 0.85rem; }
        nav a:hover { background: rgba(255,255,255,0.35); }
        .badge-role {
            display: inline-block; background: #48cae4; color: #003049;
            font-size: 0.72rem; font-weight: 700; padding: 2px 8px;
            border-radius: 20px; text-transform: uppercase; margin-left: 6px;
        }

        .wrapper { display: flex; min-height: calc(100vh - 52px); }

        aside {
            width: 220px; background: #023e8a; padding: 24px 0; flex-shrink: 0;
        }
        aside h3 {
            color: #90e0ef; font-size: 0.72rem; text-transform: uppercase;
            letter-spacing: 0.1em; padding: 0 20px 10px;
        }
        aside a {
            display: block; color: #caf0f8; text-decoration: none;
            padding: 11px 20px; font-size: 0.9rem; transition: background 0.15s;
        }
        aside a:hover, aside a.active { background: #0077b6; }

        main { flex: 1; padding: 28px 32px; }

        .page-header {
            display: flex; align-items: center; justify-content: space-between;
            margin-bottom: 20px;
        }
        .page-header h2 { font-size: 1.4rem; color: #023e8a; }
        .btn-new {
            background: #0077b6; color: #fff; border: none;
            padding: 9px 20px; border-radius: 6px; font-size: 0.9rem;
            cursor: pointer; text-decoration: none; display: inline-block;
        }
        .btn-new:hover { background: #005f99; }

        /* Alerts */
        .alert { padding: 10px 16px; border-radius: 6px; margin-bottom: 16px; font-size: 0.88rem; }
        .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .alert-error   { background: #fde8e8; color: #c0392b; border: 1px solid #e74c3c; }

        /* Filter bar */
        .filter-bar {
            display: flex; gap: 10px; margin-bottom: 18px; flex-wrap: wrap;
        }
        .filter-bar input, .filter-bar select {
            padding: 8px 12px; border: 1px solid #ccc; border-radius: 6px;
            font-size: 0.88rem; background: #fff;
        }
        .filter-bar input { flex: 1; min-width: 200px; }
        .btn-filter {
            background: #023e8a; color: #fff; border: none;
            padding: 8px 18px; border-radius: 6px; font-size: 0.88rem; cursor: pointer;
        }
        .btn-filter:hover { background: #0077b6; }
        .btn-reset {
            background: #6c757d; color: #fff; border: none;
            padding: 8px 14px; border-radius: 6px; font-size: 0.88rem;
            cursor: pointer; text-decoration: none; display: inline-block;
        }

        /* Table */
        .table-wrap { background: #fff; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
        th { background: #023e8a; color: #fff; padding: 12px 14px; text-align: left; font-weight: 600; }
        td { padding: 11px 14px; border-bottom: 1px solid #e8ecf0; vertical-align: middle; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: #f4f8fc; }

        /* Status badges */
        .badge {
            display: inline-block; padding: 3px 9px; border-radius: 20px;
            font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em;
        }
        .badge-PENDING     { background: #fff3cd; color: #856404; }
        .badge-CONFIRMED   { background: #d1ecf1; color: #0c5460; }
        .badge-CHECKED_IN  { background: #d4edda; color: #155724; }
        .badge-CHECKED_OUT { background: #e2e3e5; color: #383d41; }
        .badge-CANCELLED   { background: #f8d7da; color: #721c24; }
        .badge-NO_SHOW     { background: #fde8e8; color: #7b2d2d; }

        /* Action links */
        .actions { display: flex; gap: 6px; flex-wrap: nowrap; }
        .btn-sm {
            padding: 4px 10px; border-radius: 4px; font-size: 0.78rem;
            text-decoration: none; border: none; cursor: pointer; display: inline-block;
        }
        .btn-view   { background: #e7f1ff; color: #0077b6; }
        .btn-edit   { background: #fff3cd; color: #856404; }
        .btn-cancel { background: #f8d7da; color: #721c24; }
        .btn-delete { background: #dc3545; color: #fff; }
        .btn-sm:hover { opacity: 0.85; }

        .empty { text-align: center; padding: 40px; color: #888; font-size: 0.95rem; }
    </style>
</head>
<body>

<!-- Toast container -->
<div class="toast-container position-fixed top-0 end-0 p-3" style="z-index:9999">
    <% if (!msg.isEmpty()) { %>
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
    <span class="brand">OceanView Hotel</span>
    <div class="user-info">
        <span>Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge-role"><%= user.getRole() %></span>
        </span>
        <a href="<%= ctx %>/logout">Logout</a>
    </div>
</nav>

<div class="wrapper">
    <aside>
        <h3>Menu</h3>
        <a href="<%= ctx %>/dashboard">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="active">&#128722; Reservations</a>
        <% if (user.isAdmin()) { %>
        <a href="#">&#128100; Manage Users</a>
        <a href="#">&#128196; Audit Logs</a>
        <% } %>
    </aside>

    <main>
        <div class="page-header">
            <h2>&#128722; Reservations</h2>
            <a href="<%= ctx %>/reservations?action=new" class="btn-new">+ New Reservation</a>
        </div>

        <% if (errMsg != null) { %>
            <div class="alert alert-error"><%= errMsg %></div>
        <% } %>

        <!-- Filter bar -->
        <form method="get" action="<%= ctx %>/reservations" class="filter-bar">
            <input type="text" name="search" placeholder="Search by guest name..."
                   value="<%= search %>">
            <select name="status">
                <option value="">-- All Statuses --</option>
                <% for (ReservationStatus s : statuses) { %>
                    <option value="<%= s.name() %>" <%= s.name().equals(statusFilt) ? "selected" : "" %>>
                        <%= s.getDisplayName() %>
                    </option>
                <% } %>
            </select>
            <button type="submit" class="btn-filter">Search</button>
            <a href="<%= ctx %>/reservations" class="btn-reset">Clear</a>
        </form>

        <div class="table-wrap">
            <% if (reservations == null || reservations.isEmpty()) { %>
                <p class="empty">No reservations found.</p>
            <% } else { %>
            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Guest</th>
                        <th>Room</th>
                        <th>Type</th>
                        <th>Check-In</th>
                        <th>Check-Out</th>
                        <th>Nights</th>
                        <th>Total (<%= currency %>)</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Reservation r : reservations) {
                    String statusName = r.getStatus() != null ? r.getStatus().name() : ""; %>
                    <tr>
                        <td><%= r.getReservationId() %></td>
                        <td>
                            <strong><%= r.getGuestName() %></strong><br>
                            <small style="color:#888"><%= r.getGuestEmail() %></small>
                        </td>
                        <td><%= r.getRoomNumber() %></td>
                        <td><%= r.getRoomType() != null ? r.getRoomType().getDisplayName() : "—" %></td>
                        <td><%= r.getCheckInDate()  != null ? r.getCheckInDate().format(fmt)  : "—" %></td>
                        <td><%= r.getCheckOutDate() != null ? r.getCheckOutDate().format(fmt) : "—" %></td>
                        <td><%= r.getNights() %></td>
                        <td><%= String.format("%.2f", r.getTotalAmount()) %></td>
                        <td><span class="badge badge-<%= statusName %>">
                            <%= r.getStatus() != null ? r.getStatus().getDisplayName() : "—" %>
                        </span></td>
                        <td>
                            <div class="actions">
                                <a href="<%= ctx %>/reservations?action=view&id=<%= r.getReservationId() %>"
                                   class="btn-sm btn-view">View</a>

                                <% if (r.getStatus() != ReservationStatus.CANCELLED
                                    && r.getStatus() != ReservationStatus.CHECKED_OUT) { %>
                                    <a href="<%= ctx %>/reservations?action=edit&id=<%= r.getReservationId() %>"
                                       class="btn-sm btn-edit">Edit</a>

                                    <form method="post" action="<%= ctx %>/reservations" style="display:inline"
                                          onsubmit="return confirm('Cancel this reservation?')">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="id" value="<%= r.getReservationId() %>">
                                        <button type="submit" class="btn-sm btn-cancel">Cancel</button>
                                    </form>
                                <% } %>

                                <% if (user.isAdmin()) { %>
                                    <form method="post" action="<%= ctx %>/reservations" style="display:inline"
                                          onsubmit="return confirm('Permanently delete this reservation?')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="<%= r.getReservationId() %>">
                                        <button type="submit" class="btn-sm btn-delete">Delete</button>
                                    </form>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
            <% } %>
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
