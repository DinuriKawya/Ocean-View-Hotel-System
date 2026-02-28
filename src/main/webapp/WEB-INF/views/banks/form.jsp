<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Bank" %>
<%
    User user  = (User) session.getAttribute("loggedInUser");
    Bank bank  = (Bank) request.getAttribute("bank");
    boolean isEdit = (bank != null);
    String ctx = request.getContextPath();
    String errMsg = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title><%= isEdit ? "Edit Bank" : "Add Bank" %> &mdash; OceanView</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body{background:#f0f4f8}
        .sidebar{min-height:calc(100vh-56px);background:#023e8a;width:220px;flex-shrink:0}
        .sidebar .lbl{font-size:.7rem;text-transform:uppercase;letter-spacing:.1em;color:#90e0ef;padding:16px 16px 4px}
        .sidebar .nav-link{color:#caf0f8;font-size:.9rem;padding:10px 16px;border-radius:0}
        .sidebar .nav-link:hover,.sidebar .nav-link.active{background:#0077b6;color:#fff}
        .topnav{background:#0077b6}
    </style>
</head>
<body>
<nav class="navbar topnav px-3">
    <span class="navbar-brand text-white fw-semibold">OceanView Hotel</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">Welcome, <strong><%= user.getFullName() %></strong></span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>
<div class="d-flex">
    <div class="sidebar">
        <div class="lbl">Menu</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Room Management</a>
        <a href="<%= ctx %>/banks"        class="nav-link active">&#127974; Bank Management</a>
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link">&#128198; Check-Out</a>
    </div>
    <div class="flex-grow-1 p-4">
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="<%= ctx %>/banks">Banks</a></li>
                <li class="breadcrumb-item active"><%= isEdit ? "Edit Bank" : "Add Bank" %></li>
            </ol>
        </nav>
        <div class="card shadow-sm border-0" style="max-width:480px">
            <div class="card-header bg-primary text-white fw-semibold">
                &#127974; <%= isEdit ? "Edit Bank" : "Add New Bank" %>
            </div>
            <div class="card-body">
                <% if (errMsg != null) { %>
                    <div class="alert alert-danger"><%= errMsg %></div>
                <% } %>
                <form method="post" action="<%= ctx %>/banks">
                    <% if (isEdit) { %>
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="bankId" value="<%= bank.getBankId() %>">
                    <% } else { %>
                        <input type="hidden" name="action" value="create">
                    <% } %>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Bank Name <span class="text-danger">*</span></label>
                        <input type="text" name="name" class="form-control"
                               value="<%= isEdit ? bank.getName() : "" %>"
                               required placeholder="e.g. BDO Unibank, BPI, Metrobank">
                    </div>
                    <% if (isEdit) { %>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Status</label>
                        <select name="isActive" class="form-select">
                            <option value="1" <%= bank.isActive() ? "selected" : "" %>>Active (shows in payment form)</option>
                            <option value="0" <%= !bank.isActive() ? "selected" : "" %>>Inactive (hidden from payment form)</option>
                        </select>
                    </div>
                    <% } %>
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary"><%= isEdit ? "Update Bank" : "Add Bank" %></button>
                        <a href="<%= ctx %>/banks" class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
