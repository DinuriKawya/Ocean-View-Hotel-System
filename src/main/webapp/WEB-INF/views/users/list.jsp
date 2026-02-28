<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*, java.util.List" %>
<%
    User currentUser       = (User) session.getAttribute("loggedInUser");
    List<User> users       = (List<User>) request.getAttribute("users");
    Integer activeCount    = (Integer) request.getAttribute("activeCount");
    Integer adminCount     = (Integer) request.getAttribute("adminCount");
    Integer staffCount     = (Integer) request.getAttribute("staffCount");
    String msg             = (String)  request.getAttribute("msg");
    String errMsg          = (String)  request.getAttribute("errorMessage");
    String ctx             = request.getContextPath();

    int total  = (users != null) ? users.size() : 0;
    int active = (activeCount != null) ? activeCount : 0;
    int admins = (adminCount  != null) ? adminCount  : 0;
    int staff  = (staffCount  != null) ? staffCount  : 0;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Manage Users &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #778da9; padding: 16px 16px 4px; }
        .sidebar .nav-link { color: #e0e1dd; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #1b263b; color: #fff; }
        .topnav { background: #1b263b; }
        .stat-card { border-radius: 12px; padding: 20px 24px; color: #fff; }
        .avatar { width: 36px; height: 36px; border-radius: 50%; display: inline-flex;
                  align-items: center; justify-content: center; font-weight: 700;
                  font-size: .85rem; flex-shrink: 0; }
    </style>
</head>
<body>
<nav class="navbar topnav px-3">
    <span class="navbar-brand text-white fw-semibold">OceanView Hotel &mdash; Admin</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">Welcome, <strong><%= currentUser.getFullName() %></strong>
            <span class="badge bg-danger ms-1"><%= currentUser.getRole() %></span></span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>
<div class="d-flex">
    <div class="sidebar">
        <div class="lbl">Operations</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Rooms</a>
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link">&#128198; Check-Out</a>
        <div class="lbl mt-2">Admin</div>
        <a href="<%= ctx %>/users"        class="nav-link active">&#128100; Users</a>
        <a href="<%= ctx %>/banks"        class="nav-link">&#127974; Banks</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#128188; Room Mgmt</a>
    </div>

    <div class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="mb-0 fw-bold" style="color:#1b263b">&#128100; Manage Users</h4>
            <a href="<%= ctx %>/users?action=new" class="btn btn-primary">
                &#43; Add New User
            </a>
        </div>

        <% if (msg != null && !msg.isBlank()) { %>
        <div class="alert alert-success alert-dismissible fade show">
            &#10003; <%= msg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>
        <% if (errMsg != null) { %>
        <div class="alert alert-danger alert-dismissible fade show">
            <strong>Error:</strong> <%= errMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <!-- Stats row -->
        <div class="row g-3 mb-4">
            <div class="col-6 col-md-3">
                <div class="stat-card" style="background:#1b263b">
                    <div style="font-size:.75rem;opacity:.7">Total Users</div>
                    <div style="font-size:2rem;font-weight:700"><%= total %></div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-card" style="background:#198754">
                    <div style="font-size:.75rem;opacity:.8">Active</div>
                    <div style="font-size:2rem;font-weight:700"><%= active %></div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-card" style="background:#e63946">
                    <div style="font-size:.75rem;opacity:.8">Admins</div>
                    <div style="font-size:2rem;font-weight:700"><%= admins %></div>
                </div>
            </div>
            <div class="col-6 col-md-3">
                <div class="stat-card" style="background:#0077b6">
                    <div style="font-size:.75rem;opacity:.8">Staff</div>
                    <div style="font-size:2rem;font-weight:700"><%= staff %></div>
                </div>
            </div>
        </div>

        <!-- Search + filter -->
        <div class="card shadow-sm border-0 mb-0">
            <div class="card-header bg-light d-flex flex-wrap gap-2 align-items-center justify-content-between py-2">
                <div class="input-group" style="max-width:320px">
                    <span class="input-group-text bg-white border-end-0">&#128269;</span>
                    <input type="text" id="searchInput" class="form-control border-start-0"
                           placeholder="Search by name or username&hellip;" oninput="filterTable()">
                </div>
                <div class="d-flex gap-2">
                    <div class="btn-group btn-group-sm" id="roleFilter">
                        <button class="btn btn-outline-secondary active" onclick="setFilter('role','all',this)">All Roles</button>
                        <button class="btn btn-outline-danger"           onclick="setFilter('role','ADMIN',this)">Admin</button>
                        <button class="btn btn-outline-primary"          onclick="setFilter('role','STAFF',this)">Staff</button>
                    </div>
                    <div class="btn-group btn-group-sm" id="statusFilter">
                        <button class="btn btn-outline-secondary active" onclick="setFilter('status','all',this)">All Status</button>
                        <button class="btn btn-outline-success"          onclick="setFilter('status','ACTIVE',this)">Active</button>
                        <button class="btn btn-outline-warning"          onclick="setFilter('status','INACTIVE',this)">Inactive</button>
                    </div>
                </div>
            </div>

            <!-- Users table -->
            <div class="card-body p-0">
                <table class="table table-hover align-middle mb-0" id="usersTable">
                    <thead class="table-light">
                        <tr>
                            <th style="width:40px">#</th>
                            <th>User</th>
                            <th>Username</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th style="width:200px">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (users == null || users.isEmpty()) { %>
                    <tr><td colspan="6" class="text-center text-muted py-4">No users found.</td></tr>
                    <% } else { for (User u : users) {
                        String initials = u.getFullName().trim().length() >= 2
                            ? String.valueOf(u.getFullName().trim().charAt(0)).toUpperCase()
                              + String.valueOf(u.getFullName().trim().split("\\s+")[u.getFullName().trim().split("\\s+").length-1].charAt(0)).toUpperCase()
                            : u.getFullName().trim().substring(0,1).toUpperCase();
                        String avatarBg = u.isAdmin() ? "#e63946" : "#0077b6";
                        boolean isSelf = (u.getUserId() == currentUser.getUserId());
                    %>
                    <tr data-role="<%= u.getRole().name() %>"
                        data-status="<%= u.getStatus().name() %>"
                        data-search="<%= u.getFullName().toLowerCase() %> <%= u.getUsername().toLowerCase() %>">
                        <td class="text-muted small"><%= u.getUserId() %></td>
                        <td>
                            <div class="d-flex align-items-center gap-2">
                                <div class="avatar" style="background:<%= avatarBg %>; color:#fff">
                                    <%= initials %>
                                </div>
                                <div>
                                    <div class="fw-semibold"><%= u.getFullName() %>
                                        <% if (isSelf) { %>
                                        <span class="badge bg-secondary ms-1" style="font-size:.65rem">You</span>
                                        <% } %>
                                    </div>
                                </div>
                            </div>
                        </td>
                        <td class="text-muted small">@<%= u.getUsername() %></td>
                        <td>
                            <% if (u.getRole() == Role.ADMIN) { %>
                                <span class="badge bg-danger">&#128274; Admin</span>
                            <% } else { %>
                                <span class="badge bg-primary">&#128100; Staff</span>
                            <% } %>
                        </td>
                        <td>
                            <% if (u.getStatus() == UserStatus.ACTIVE) { %>
                                <span class="badge bg-success">&#9679; Active</span>
                            <% } else { %>
                                <span class="badge bg-secondary">&#9675; Inactive</span>
                            <% } %>
                        </td>
                        <td>
                            <div class="d-flex gap-1 flex-wrap">
                                <!-- Edit -->
                                <a href="<%= ctx %>/users?action=edit&id=<%= u.getUserId() %>"
                                   class="btn btn-outline-primary btn-sm" title="Edit">
                                    &#9998;
                                </a>

                                <!-- Change Password -->
                                <button type="button" class="btn btn-outline-warning btn-sm"
                                        title="Change Password"
                                        onclick="openPwModal(<%= u.getUserId() %>, '<%= u.getFullName().replace("'","\\'") %>')">
                                    &#128273;
                                </button>

                                <!-- Toggle Status -->
                                <% if (!isSelf) { %>
                                <form method="post" action="<%= ctx %>/users" class="d-inline"
                                      onsubmit="return confirm('<%= u.getStatus() == UserStatus.ACTIVE ? "Deactivate" : "Activate" %> <%= u.getFullName().replace("'","\\'") %>?')">
                                    <input type="hidden" name="action"  value="toggleStatus">
                                    <input type="hidden" name="userId"  value="<%= u.getUserId() %>">
                                    <input type="hidden" name="status"
                                           value="<%= u.getStatus() == UserStatus.ACTIVE ? "INACTIVE" : "ACTIVE" %>">
                                    <button type="submit"
                                            class="btn btn-sm <%= u.getStatus() == UserStatus.ACTIVE ? "btn-outline-secondary" : "btn-outline-success" %>"
                                            title="<%= u.getStatus() == UserStatus.ACTIVE ? "Deactivate" : "Activate" %>">
                                        <%= u.getStatus() == UserStatus.ACTIVE ? "&#128683;" : "&#9989;" %>
                                    </button>
                                </form>

                                <!-- Delete -->
                                <form method="post" action="<%= ctx %>/users" class="d-inline"
                                      onsubmit="return confirm('Permanently delete <%= u.getFullName().replace("'","\\'") %>? This cannot be undone.')">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="userId" value="<%= u.getUserId() %>">
                                    <button type="submit" class="btn btn-outline-danger btn-sm" title="Delete">
                                        &#128465;
                                    </button>
                                </form>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <% } } %>
                    </tbody>
                </table>
            </div>
            <div class="card-footer text-muted small py-2">
                Showing <span id="visibleCount"><%= total %></span> of <%= total %> users
            </div>
        </div>
    </div>
</div>

<!-- Change Password Modal -->
<div class="modal fade" id="pwModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered" style="max-width:400px">
        <div class="modal-content">
            <div class="modal-header bg-warning bg-opacity-25">
                <h5 class="modal-title fw-bold">&#128273; Change Password</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="post" action="<%= ctx %>/users" id="pwForm">
                <div class="modal-body">
                    <p class="text-muted small mb-3">Changing password for: <strong id="pwUserName"></strong></p>
                    <input type="hidden" name="action" value="changePassword">
                    <input type="hidden" name="userId" id="pwUserId">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">New Password</label>
                        <input type="password" name="newPassword" id="pwNew"
                               class="form-control" placeholder="Min. 6 characters" required minlength="6">
                    </div>
                    <div class="mb-1">
                        <label class="form-label fw-semibold">Confirm Password</label>
                        <input type="password" name="confirmPassword" id="pwConfirm"
                               class="form-control" placeholder="Re-enter password" required>
                        <div class="form-text text-danger d-none" id="pwMismatch">Passwords do not match.</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-warning" id="pwSaveBtn">Save Password</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
// ---- Change Password Modal ----
function openPwModal(userId, fullName) {
    document.getElementById('pwUserId').value   = userId;
    document.getElementById('pwUserName').textContent = fullName;
    document.getElementById('pwNew').value      = '';
    document.getElementById('pwConfirm').value  = '';
    document.getElementById('pwMismatch').classList.add('d-none');
    new bootstrap.Modal(document.getElementById('pwModal')).show();
}
document.getElementById('pwForm').addEventListener('submit', function(e) {
    var pw  = document.getElementById('pwNew').value;
    var cpw = document.getElementById('pwConfirm').value;
    if (pw !== cpw) {
        e.preventDefault();
        document.getElementById('pwMismatch').classList.remove('d-none');
    }
});
document.getElementById('pwConfirm').addEventListener('input', function() {
    var match = this.value === document.getElementById('pwNew').value;
    document.getElementById('pwMismatch').classList.toggle('d-none', match);
});

// ---- Search + Filter ----
var activeRoleFilter   = 'all';
var activeStatusFilter = 'all';

function setFilter(type, value, btn) {
    if (type === 'role')   { activeRoleFilter   = value; document.querySelectorAll('#roleFilter   .btn').forEach(b => b.classList.remove('active')); }
    if (type === 'status') { activeStatusFilter = value; document.querySelectorAll('#statusFilter .btn').forEach(b => b.classList.remove('active')); }
    btn.classList.add('active');
    filterTable();
}

function filterTable() {
    var q    = document.getElementById('searchInput').value.toLowerCase().trim();
    var rows = document.querySelectorAll('#usersTable tbody tr[data-search]');
    var vis  = 0;
    rows.forEach(function(row) {
        var searchMatch  = !q || row.dataset.search.includes(q);
        var roleMatch    = activeRoleFilter   === 'all' || row.dataset.role   === activeRoleFilter;
        var statusMatch  = activeStatusFilter === 'all' || row.dataset.status === activeStatusFilter;
        var show = searchMatch && roleMatch && statusMatch;
        row.style.display = show ? '' : 'none';
        if (show) vis++;
    });
    document.getElementById('visibleCount').textContent = vis;
}
</script>
</body>
</html>
