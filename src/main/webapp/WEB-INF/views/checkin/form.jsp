<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*, oceanview.model.AppSettings, java.time.format.DateTimeFormatter" %>
<%
    User user       = (User) session.getAttribute("loggedInUser");
    Reservation res = (Reservation) request.getAttribute("reservation");
    String errMsg   = (String) request.getAttribute("errorMessage");
    String ctx      = request.getContextPath();
    String currency = AppSettings.getCurrency();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Guest Check-In &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #023e8a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #90e0ef; padding: 16px 16px 4px; }
        .sidebar .nav-link { color: #caf0f8; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #0077b6; color: #fff; }
        .topnav { background: #0077b6; }
    </style>
</head>
<body>
<nav class="navbar topnav px-3">
    <span class="navbar-brand text-white fw-semibold">OceanView Hotel</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge bg-info text-dark ms-1"><%= user.getRole() %></span></span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>
<div class="d-flex">
    <div class="sidebar">
        <div class="lbl">Menu</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Rooms</a>
        <a href="<%= ctx %>/banks"        class="nav-link">&#127974; Banks</a>
        <a href="<%= ctx %>/checkin"      class="nav-link active">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link">&#128198; Check-Out</a>
    </div>

    <div class="flex-grow-1 p-4">
        <h4 class="mb-4 text-primary fw-bold">&#128100; Guest Check-In</h4>

        <% if (errMsg != null) { %>
        <div class="alert alert-danger alert-dismissible fade show">
            <strong>Error:</strong> <%= errMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <!-- SEARCH — shown when no reservation is loaded -->
        <% if (res == null) { %>
        <div class="card shadow-sm border-0 mb-4" style="max-width:480px">
            <div class="card-body">
                <label class="form-label fw-semibold">Find Reservation by ID</label>
                <div class="input-group">
                    <input type="number" id="searchId" class="form-control" placeholder="Enter reservation #">
                    <button class="btn btn-primary" onclick="goToReservation()">Find &rarr;</button>
                </div>
                <div class="form-text">Reservation must have status: <strong>CONFIRMED</strong></div>
            </div>
        </div>
        <script>
            function goToReservation() {
                var id = document.getElementById('searchId').value.trim();
                if (id) window.location.href = '<%= ctx %>/checkin?id=' + id;
            }
            document.getElementById('searchId').addEventListener('keydown', function(e) {
                if (e.key === 'Enter') goToReservation();
            });
        </script>
        <% } else { %>

        <!-- Reservation details card -->
        <div class="card shadow-sm border-0 mb-4" style="max-width:680px">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                <span class="fw-semibold">Reservation #<%= res.getReservationId() %></span>
                <span class="badge bg-light text-primary"><%= res.getStatus().getDisplayName() %></span>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="text-muted small">Guest</div>
                        <div class="fw-bold"><%= res.getGuestName() %></div>
                        <div class="small text-muted"><%= res.getGuestEmail() %></div>
                    </div>
                    <div class="col-md-2">
                        <div class="text-muted small">Room</div>
                        <div class="fw-bold">Room <%= res.getRoomNumber() %></div>
                        <div class="small"><%= res.getRoomType() != null ? res.getRoomType().getDisplayName() : "" %></div>
                    </div>
                    <div class="col-md-3">
                        <div class="text-muted small">Check-In</div>
                        <div class="fw-bold"><%= res.getCheckInDate() != null ? res.getCheckInDate().format(fmt) : "—" %></div>
                    </div>
                    <div class="col-md-3">
                        <div class="text-muted small">Check-Out</div>
                        <div class="fw-bold"><%= res.getCheckOutDate() != null ? res.getCheckOutDate().format(fmt) : "—" %></div>
                    </div>
                </div>

                <hr class="my-3">

                <div class="row g-3 align-items-center">
                    <div class="col-md-5">
                        <div class="text-muted small">Total Amount</div>
                        <div class="fw-bold fs-5 text-primary"><%= currency %> <%= String.format("%,.2f", res.getTotalAmount()) %></div>
                        <div class="text-muted" style="font-size:.78rem">
                            &#128161; Payment will be collected at check-out.
                        </div>
                    </div>
                    <div class="col-md-7">
                        <div class="alert alert-info mb-0 py-2 small">
                            &#128100; Confirming check-in will mark the guest as <strong>checked in</strong>
                            and assign Room <strong><%= res.getRoomNumber() %></strong>.
                            No payment is required at this stage.
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Confirm form -->
        <form method="post" action="<%= ctx %>/checkin">
            <input type="hidden" name="reservationId" value="<%= res.getReservationId() %>">
            <button type="submit" class="btn btn-success btn-lg px-5">
                &#10003; Confirm Check-In
            </button>
            <a href="<%= ctx %>/reservations?action=view&id=<%= res.getReservationId() %>"
               class="btn btn-outline-secondary btn-lg ms-2">Cancel</a>
        </form>

        <% } %>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
