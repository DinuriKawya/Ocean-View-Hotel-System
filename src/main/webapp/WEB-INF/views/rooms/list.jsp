<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Room,
                 oceanview.model.RoomStatus, oceanview.model.AppSettings, java.util.List" %>
<%
    User user         = (User) session.getAttribute("loggedInUser");
    List<Room> rooms  = (List<Room>) request.getAttribute("rooms");
    String msg        = request.getParameter("msg")  != null ? request.getParameter("msg")  : "";
    String errMsg     = (String) request.getAttribute("errorMessage");
    String ctx        = request.getContextPath();
    String currency   = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Room Management &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #023e8a; width: 220px; flex-shrink: 0; }
        .sidebar .nav-label { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #90e0ef; padding: 16px 16px 4px; }
        .sidebar .nav-link  { color: #caf0f8; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #0077b6; color: #fff; }
        .topnav { background: #0077b6; }
        .badge-AVAILABLE   { background-color: #198754 !important; }
        .badge-OCCUPIED    { background-color: #dc3545 !important; }
        .badge-MAINTENANCE { background-color: #ffc107 !important; color: #000 !important; }
        .badge-OUT_OF_ORDER{ background-color: #6c757d !important; }
    </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar topnav px-3">
    <span class="navbar-brand text-white fw-semibold">OceanView Hotel</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">
            Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge bg-info text-dark ms-1"><%= user.getRole() %></span>
        </span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>

<div class="d-flex">
    <!-- Sidebar -->
    <div class="sidebar">
        <div class="nav-label">Menu</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <% if (user.isAdmin()) { %>
        <a href="<%= ctx %>/rooms"        class="nav-link active">&#127963; Room Management</a>
        <a href="#"                        class="nav-link">&#128100; Manage Users</a>
        <a href="#"                        class="nav-link">&#128196; Audit Logs</a>
        <% } %>
    </div>

    <!-- Main -->
    <div class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="mb-0 text-primary fw-bold">&#127963; Room Management</h4>
            <% if (user.isAdmin()) { %>
            <a href="<%= ctx %>/rooms?action=new" class="btn btn-primary">
                + Add New Room
            </a>
            <% } %>
        </div>

        <% if (!msg.isEmpty()) { %>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <%= msg %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>
        <% if (errMsg != null) { %>
            <div class="alert alert-danger"><%= errMsg %></div>
        <% } %>

        <!-- Summary cards -->
        <div class="row g-3 mb-4">
            <%
                long available = rooms == null ? 0 : rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
                long occupied  = rooms == null ? 0 : rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
                long maint     = rooms == null ? 0 : rooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE || r.getStatus() == RoomStatus.OUT_OF_ORDER).count();
                long total     = rooms == null ? 0 : rooms.size();
            %>
            <div class="col-6 col-md-3">
                <div class="card border-0 shadow-sm text-center py-3">
                    <div class="fs-2 fw-bold text-primary"><%= total %></div>
                    <div class="text-muted small">Total Rooms</div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="card border-0 shadow-sm text-center py-3">
                    <div class="fs-2 fw-bold text-success"><%= available %></div>
                    <div class="text-muted small">Available</div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="card border-0 shadow-sm text-center py-3">
                    <div class="fs-2 fw-bold text-danger"><%= occupied %></div>
                    <div class="text-muted small">Occupied</div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="card border-0 shadow-sm text-center py-3">
                    <div class="fs-2 fw-bold text-warning"><%= maint %></div>
                    <div class="text-muted small">Maintenance</div>
                </div>
            </div>
        </div>

        <!-- Table -->
        <div class="card shadow-sm border-0">
            <div class="card-body p-0">
                <% if (rooms == null || rooms.isEmpty()) { %>
                    <p class="text-center text-muted py-5">No rooms found. Add your first room.</p>
                <% } else { %>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-dark">
                            <tr>
                                <th>Room #</th>
                                <th>Type</th>
                                <th>Floor</th>
                                <th>Price / Night</th>
                                <th>Max Guests</th>
                                <th>Status</th>
                                <th>Description</th>
                                <% if (user.isAdmin()) { %><th class="text-center">Actions</th><% } %>
                            </tr>
                        </thead>
                        <tbody>
                        <% for (Room r : rooms) { %>
                            <tr>
                                <td><strong><%= r.getRoomNumber() %></strong></td>
                                <td><%= r.getRoomType() != null ? r.getRoomType().getDisplayName() : "—" %></td>
                                <td><%= r.getFloor() %></td>
                                <td class="text-success fw-semibold"><%= currency %> <%= String.format("%,.2f", r.getPricePerNight()) %></td>
                                <td><%= r.getRoomType() != null ? r.getRoomType().getMaxGuests() : "—" %></td>
                                <td>
                                    <span class="badge badge-<%= r.getStatus().name() %>">
                                        <%= r.getStatus().getDisplayName() %>
                                    </span>
                                </td>
                                <td><small class="text-muted">
                                    <%= r.getDescription() != null && !r.getDescription().isBlank() ? r.getDescription() : "—" %>
                                </small></td>
                                <% if (user.isAdmin()) { %>
                                <td class="text-center">
                                    <a href="<%= ctx %>/rooms?action=edit&id=<%= r.getRoomId() %>"
                                       class="btn btn-sm btn-outline-warning me-1">Edit</a>
                                    <form method="post" action="<%= ctx %>/rooms" class="d-inline"
                                          onsubmit="return confirm('Delete Room <%= r.getRoomNumber() %>?')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id"     value="<%= r.getRoomId() %>">
                                        <button class="btn btn-sm btn-outline-danger">Delete</button>
                                    </form>
                                </td>
                                <% } %>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <% } %>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
