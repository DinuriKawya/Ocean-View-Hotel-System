<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Bank, java.util.List" %>
<%
    User user      = (User) session.getAttribute("loggedInUser");
    List<Bank> banks = (List<Bank>) request.getAttribute("banks");
    String msg     = (String) request.getAttribute("msg");
    String errMsg  = (String) request.getAttribute("errorMessage");
    String ctx     = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Bank Management &mdash; OceanView</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body{background:#f0f4f8}
        .sidebar{min-height:calc(100vh - 56px);background:#023e8a;width:220px;flex-shrink:0}
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
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Room Management</a>
        <a href="<%= ctx %>/banks"        class="nav-link active">&#127974; Bank Management</a>
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link">&#128198; Check-Out</a>
    </div>
    <div class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="mb-0 text-primary fw-bold">&#127974; Bank Management</h4>
            <a href="<%= ctx %>/banks?action=new" class="btn btn-primary">+ Add Bank</a>
        </div>
        <% if (msg != null && !msg.isBlank()) { %>
            <div class="alert alert-success alert-dismissible fade show">
                <%= msg %><button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>
        <% if (errMsg != null) { %>
            <div class="alert alert-danger"><%= errMsg %></div>
        <% } %>
        <div class="card shadow-sm border-0">
            <div class="card-body p-0">
                <% if (banks == null || banks.isEmpty()) { %>
                    <p class="text-center text-muted py-5">No banks added yet. Banks added here will appear in Card and Transfer payment options.</p>
                <% } else { %>
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-dark">
                        <tr><th>Bank Name</th><th>Status</th><th class="text-center">Actions</th></tr>
                    </thead>
                    <tbody>
                    <% for (Bank b : banks) { %>
                        <tr>
                            <td class="fw-semibold"><%= b.getName() %></td>
                            <td>
                                <% if (b.isActive()) { %>
                                    <span class="badge bg-success">Active</span>
                                <% } else { %>
                                    <span class="badge bg-secondary">Inactive</span>
                                <% } %>
                            </td>
                            <td class="text-center">
                                <a href="<%= ctx %>/banks?action=edit&id=<%= b.getBankId() %>"
                                   class="btn btn-sm btn-outline-warning me-1">Edit</a>
                                <form method="post" action="<%= ctx %>/banks" class="d-inline"
                                      onsubmit="return confirm('Delete <%= b.getName() %>?')">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="<%= b.getBankId() %>">
                                    <button class="btn btn-sm btn-outline-danger">Delete</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
                <% } %>
            </div>
        </div>
        <p class="text-muted small mt-3">
            &#8505; Only <strong>Active</strong> banks appear in the payment form Bank dropdown for Card and Transfer payments.
        </p>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
