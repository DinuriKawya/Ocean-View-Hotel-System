<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Room,
                 oceanview.model.RoomType, oceanview.model.RoomStatus, oceanview.model.AppSettings" %>
<%
    User       user     = (User)       session.getAttribute("loggedInUser");
    Room       room     = (Room)       request.getAttribute("room");
    RoomType[] types    = (RoomType[]) request.getAttribute("roomTypes");
    RoomStatus[] stats  = (RoomStatus[]) request.getAttribute("roomStatuses");
    String errMsg       = (String)     request.getAttribute("errorMessage");
    boolean isEdit      = (room != null);
    String pageTitle    = isEdit ? "Edit Room " + room.getRoomNumber() : "Add New Room";
    String ctx          = request.getContextPath();

    String vNumber  = isEdit ? String.valueOf(room.getRoomNumber())       : "";
    String vType    = isEdit && room.getRoomType()   != null ? room.getRoomType().name()   : "";
    String vPrice   = isEdit ? String.format("%.2f", room.getPricePerNight()) : "";
    String vFloor   = isEdit ? String.valueOf(room.getFloor())            : "";
    String vStatus  = isEdit && room.getStatus()     != null ? room.getStatus().name()     : "AVAILABLE";
    String vDesc    = isEdit && room.getDescription()!= null ? room.getDescription()       : "";
    String currency = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= pageTitle %> &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #023e8a; width: 220px; flex-shrink: 0; }
        .sidebar .nav-label { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #90e0ef; padding: 16px 16px 4px; }
        .sidebar .nav-link  { color: #caf0f8; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #0077b6; color: #fff; }
        .topnav { background: #0077b6; }
    </style>
</head>
<body>

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
    <div class="sidebar">
        <div class="nav-label">Menu</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <% if (user.isAdmin()) { %>
        <a href="<%= ctx %>/rooms"        class="nav-link active">&#127963; Room Management</a>
        <a href="#"                        class="nav-link">&#128100; Manage Users</a>
        <% } %>
    </div>

    <div class="flex-grow-1 p-4">
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="<%= ctx %>/rooms">Rooms</a></li>
                <li class="breadcrumb-item active"><%= pageTitle %></li>
            </ol>
        </nav>

        <div class="card shadow-sm border-0" style="max-width:640px">
            <div class="card-header bg-primary text-white fw-semibold">
                &#127963; <%= pageTitle %>
            </div>
            <div class="card-body">

                <% if (errMsg != null) { %>
                    <div class="alert alert-danger"><%= errMsg %></div>
                <% } %>

                <form method="post" action="<%= ctx %>/rooms">
                    <% if (isEdit) { %>
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="roomId" value="<%= room.getRoomId() %>">
                    <% } else { %>
                        <input type="hidden" name="action" value="create">
                    <% } %>

                    <div class="row g-3">
                        <!-- Room Number -->
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Room Number <span class="text-danger">*</span></label>
                            <input type="number" name="roomNumber" class="form-control"
                                   value="<%= vNumber %>" min="1" required placeholder="e.g. 101">
                        </div>

                        <!-- Floor -->
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Floor <span class="text-danger">*</span></label>
                            <input type="number" name="floor" class="form-control"
                                   value="<%= vFloor %>" min="1" required placeholder="e.g. 1">
                        </div>

                        <!-- Room Type -->
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Room Type <span class="text-danger">*</span></label>
                            <select name="roomType" class="form-select" required onchange="updatePrice(this)">
                                <option value="">-- Select Type --</option>
                                <% for (RoomType t : types) { %>
                                    <option value="<%= t.name() %>"
                                            data-default-price="<%= getDefaultPrice(t) %>"
                                            <%= t.name().equals(vType) ? "selected" : "" %>>
                                        <%= t.getDisplayName() %>
                                        (max <%= t.getMaxGuests() %> guests)
                                    </option>
                                <% } %>
                            </select>
                        </div>

                        <!-- Price per Night -->
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Price per Night (<%= currency %>) <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <span class="input-group-text"><%= currency %></span>
                                <input type="number" name="pricePerNight" id="priceInput" class="form-control"
                                       value="<%= vPrice %>" min="0.01" step="0.01" required placeholder="0.00">
                            </div>
                        </div>

                        <!-- Status -->
                        <div class="col-md-6">
                            <label class="form-label fw-semibold">Status <span class="text-danger">*</span></label>
                            <select name="status" class="form-select" required>
                                <% for (RoomStatus s : stats) { %>
                                    <option value="<%= s.name() %>" <%= s.name().equals(vStatus) ? "selected" : "" %>>
                                        <%= s.getDisplayName() %>
                                    </option>
                                <% } %>
                            </select>
                        </div>

                        <!-- Description -->
                        <div class="col-12">
                            <label class="form-label fw-semibold">Description</label>
                            <textarea name="description" class="form-control" rows="3"
                                      placeholder="e.g. Ocean view, king bed, balcony..."><%= vDesc %></textarea>
                        </div>
                    </div>

                    <hr class="my-3">
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary px-4">
                            <%= isEdit ? "Update Room" : "Add Room" %>
                        </button>
                        <a href="<%= ctx %>/rooms" class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Suggest a default price when room type is selected (only if price field is empty)
    function updatePrice(sel) {
        var priceInput = document.getElementById('priceInput');
        if (!priceInput.value) {
            var defaultPrice = sel.options[sel.selectedIndex].getAttribute('data-default-price');
            if (defaultPrice) priceInput.value = defaultPrice;
        }
    }
</script>
</body>
</html>

<%!
    // Default suggested prices per room type (helper method in JSP)
    private String getDefaultPrice(oceanview.model.RoomType t) {
        return switch (t) {
            case STANDARD  -> "3000.00";
            case DELUXE    -> "5000.00";
            case SUITE     -> "10000.00";
            case FAMILY    -> "7000.00";
            case PENTHOUSE -> "25000.00";
        };
    }
%>
