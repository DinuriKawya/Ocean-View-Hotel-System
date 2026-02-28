<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Reservation,
                 oceanview.model.ReservationStatus, oceanview.model.AppSettings,
                 java.time.format.DateTimeFormatter" %>
<%
    User user              = (User) session.getAttribute("loggedInUser");
    Reservation r          = (Reservation) request.getAttribute("reservation");
    ReservationStatus[] statuses = (ReservationStatus[]) request.getAttribute("statuses");
    String msg             = request.getParameter("msg") != null ? request.getParameter("msg") : "";
    DateTimeFormatter fmt  = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    String ctx             = request.getContextPath();
    String statusName      = r.getStatus() != null ? r.getStatus().name() : "";
    boolean editable       = r.getStatus() != ReservationStatus.CANCELLED
                          && r.getStatus() != ReservationStatus.CHECKED_OUT;
    String currency        = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reservation #<%= r.getReservationId() %> &mdash; OceanView Hotel</title>
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

        .breadcrumb { font-size: 0.82rem; color: #666; margin-bottom: 16px; }
        .breadcrumb a { color: #0077b6; text-decoration: none; }

        .alert { padding: 10px 16px; border-radius: 6px; margin-bottom: 16px; font-size: 0.88rem; }
        .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        /* Header card */
        .view-header {
            background: #fff; border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 24px 28px; margin-bottom: 20px;
            display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px;
        }
        .view-header .id-block h2 { font-size: 1.5rem; color: #023e8a; }
        .view-header .id-block p  { color: #666; font-size: 0.85rem; margin-top: 4px; }

        /* Status badge */
        .badge {
            display: inline-block; padding: 5px 14px; border-radius: 20px;
            font-size: 0.82rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;
        }
        .badge-PENDING     { background: #fff3cd; color: #856404; }
        .badge-CONFIRMED   { background: #d1ecf1; color: #0c5460; }
        .badge-CHECKED_IN  { background: #d4edda; color: #155724; }
        .badge-CHECKED_OUT { background: #e2e3e5; color: #383d41; }
        .badge-CANCELLED   { background: #f8d7da; color: #721c24; }
        .badge-NO_SHOW     { background: #fde8e8; color: #7b2d2d; }

        /* Action buttons */
        .action-bar { display: flex; gap: 8px; flex-wrap: wrap; }
        .btn {
            padding: 8px 18px; border-radius: 6px; font-size: 0.88rem;
            text-decoration: none; border: none; cursor: pointer; display: inline-block;
        }
        .btn-primary { background: #0077b6; color: #fff; }
        .btn-warn    { background: #f0ad4e; color: #fff; }
        .btn-danger  { background: #dc3545; color: #fff; }
        .btn-secondary { background: #e2e8f0; color: #333; }
        .btn:hover { opacity: 0.88; }

        /* Detail grid */
        .detail-grid {
            display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
        }
        .detail-card {
            background: #fff; border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            padding: 22px 26px;
        }
        .detail-card h3 {
            font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.08em;
            color: #888; border-bottom: 1px solid #e8ecf0; padding-bottom: 8px; margin-bottom: 14px;
        }
        .detail-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 0.9rem; }
        .detail-row .lbl { color: #666; flex-shrink: 0; margin-right: 12px; }
        .detail-row .val { font-weight: 500; text-align: right; }

        /* Special requests */
        .special-box {
            background: #f8fafc; border: 1px solid #e8ecf0; border-radius: 6px;
            padding: 10px 14px; font-size: 0.88rem; color: #555; margin-top: 4px;
        }

        /* Admin status change */
        .status-form {
            display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-top: 10px;
        }
        .status-form select {
            padding: 7px 12px; border: 1px solid #ccc; border-radius: 6px; font-size: 0.88rem;
        }
        .status-form button {
            padding: 7px 16px; background: #023e8a; color: #fff;
            border: none; border-radius: 6px; font-size: 0.88rem; cursor: pointer;
        }
        .status-form button:hover { background: #0077b6; }
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
    <div class="user-info" style="display:flex;align-items:center;gap:12px;font-size:0.9rem">
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
        <p class="breadcrumb">
            <a href="<%= ctx %>/reservations">Reservations</a>
            &rsaquo; Reservation #<%= r.getReservationId() %>
        </p>

        <!-- Header -->
        <div class="view-header">
            <div class="id-block">
                <h2>Reservation #<%= r.getReservationId() %></h2>
                <p>Booked by <strong><%= r.getCreatedBy() %></strong>
                   on <%= r.getCreatedAt() != null ? r.getCreatedAt().format(fmt) : "—" %></p>
            </div>
            <div>
                <div style="margin-bottom:10px">
                    <span class="badge badge-<%= statusName %>">
                        <%= r.getStatus() != null ? r.getStatus().getDisplayName() : "—" %>
                    </span>
                </div>
                <div class="action-bar">
                    <!-- Check-In button: CONFIRMED only -->
                    <% if (r.getStatus() == ReservationStatus.CONFIRMED) { %>
                        <a href="<%= ctx %>/checkin?id=<%= r.getReservationId() %>"
                           class="btn" style="background:#198754;color:#fff">&#128100; Check In</a>
                    <% } %>
                    <!-- Check-Out button: CHECKED_IN only -->
                    <% if (r.getStatus() == ReservationStatus.CHECKED_IN) { %>
                        <a href="<%= ctx %>/checkout?id=<%= r.getReservationId() %>"
                           class="btn" style="background:#0dcaf0;color:#000">&#128198; Check Out</a>
                    <% } %>
                    <% if (editable) { %>
                        <a href="<%= ctx %>/reservations?action=edit&id=<%= r.getReservationId() %>"
                           class="btn btn-primary">Edit</a>

                        <form method="post" action="<%= ctx %>/reservations" style="display:inline"
                              onsubmit="return confirm('Cancel this reservation?')">
                            <input type="hidden" name="action" value="cancel">
                            <input type="hidden" name="id" value="<%= r.getReservationId() %>">
                            <button type="submit" class="btn btn-warn">Cancel Reservation</button>
                        </form>
                    <% } %>

                    <% if (user.isAdmin()) { %>
                        <form method="post" action="<%= ctx %>/reservations" style="display:inline"
                              onsubmit="return confirm('Permanently delete this reservation?')">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= r.getReservationId() %>">
                            <button type="submit" class="btn btn-danger">Delete</button>
                        </form>
                    <% } %>

                    <a href="<%= ctx %>/reservations" class="btn btn-secondary">&#8592; Back</a>
                </div>
            </div>
        </div>

        <!-- Detail cards -->
        <div class="detail-grid">

            <!-- Guest -->
            <div class="detail-card">
                <h3>&#128100; Guest Information</h3>
                <div class="detail-row"><span class="lbl">Full Name</span><span class="val"><%= r.getGuestName() %></span></div>
                <div class="detail-row"><span class="lbl">Email</span><span class="val"><%= r.getGuestEmail() %></span></div>
                <div class="detail-row"><span class="lbl">Phone</span>
                    <span class="val"><%= r.getGuestPhone() != null && !r.getGuestPhone().isBlank() ? r.getGuestPhone() : "—" %></span></div>
                <div class="detail-row"><span class="lbl">Guests</span><span class="val"><%= r.getNumberOfGuests() %></span></div>
            </div>

            <!-- Room & Dates -->
            <div class="detail-card">
                <h3>&#127963; Room &amp; Dates</h3>
                <div class="detail-row"><span class="lbl">Room Number</span><span class="val"><%= r.getRoomNumber() %></span></div>
                <div class="detail-row"><span class="lbl">Room Type</span>
                    <span class="val"><%= r.getRoomType() != null ? r.getRoomType().getDisplayName() : "—" %></span></div>
                <div class="detail-row"><span class="lbl">Check-In</span>
                    <span class="val"><%= r.getCheckInDate()  != null ? r.getCheckInDate().format(fmt)  : "—" %></span></div>
                <div class="detail-row"><span class="lbl">Check-Out</span>
                    <span class="val"><%= r.getCheckOutDate() != null ? r.getCheckOutDate().format(fmt) : "—" %></span></div>
                <div class="detail-row"><span class="lbl">Nights</span><span class="val"><%= r.getNights() %></span></div>
            </div>

            <!-- Billing -->
            <div class="detail-card">
                <h3>&#128179; Billing</h3>
                <div class="detail-row">
                    <span class="lbl">Total Amount</span>
                    <span class="val" style="font-size:1.1rem;color:#023e8a">
                        <%= currency %> <%= String.format("%.2f", r.getTotalAmount()) %>
                    </span>
                </div>
                <% if (r.getSpecialRequests() != null && !r.getSpecialRequests().isBlank()) { %>
                <div style="margin-top:10px">
                    <span class="lbl" style="display:block;margin-bottom:4px">Special Requests</span>
                    <div class="special-box"><%= r.getSpecialRequests() %></div>
                </div>
                <% } %>
            </div>

            <!-- Status management (Admin only) -->
            <% if (user.isAdmin() && statuses != null) { %>
            <div class="detail-card">
                <h3>&#9881; Change Status (Admin)</h3>
                <p style="font-size:0.85rem;color:#666;margin-bottom:10px">
                    Current: <span class="badge badge-<%= statusName %>" style="font-size:0.75rem">
                        <%= r.getStatus() != null ? r.getStatus().getDisplayName() : "—" %>
                    </span>
                </p>
                <form method="post" action="<%= ctx %>/reservations" class="status-form">
                    <input type="hidden" name="action" value="status">
                    <input type="hidden" name="id" value="<%= r.getReservationId() %>">
                    <select name="newStatus">
                        <% for (ReservationStatus s : statuses) { %>
                            <option value="<%= s.name() %>" <%= s == r.getStatus() ? "selected" : "" %>>
                                <%= s.getDisplayName() %>
                            </option>
                        <% } %>
                    </select>
                    <button type="submit">Update</button>
                </form>
            </div>
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
