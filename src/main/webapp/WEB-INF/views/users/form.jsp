<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*" %>
<%
    User   currentUser = (User) session.getAttribute("loggedInUser");
    User   editUser    = (User) request.getAttribute("editUser");
    String errMsg      = (String) request.getAttribute("errorMessage");
    String ctx         = request.getContextPath();
    boolean isEdit     = (editUser != null);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title><%= isEdit ? "Edit User" : "New User" %> &mdash; OceanView Hotel Resort</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #778da9; padding: 16px 16px 4px; }
        .sidebar .nav-link { color: #e0e1dd; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #1b263b; color: #fff; }
        .topnav { background: #1b263b; }
        .pw-strength { height: 4px; border-radius: 2px; transition: all .3s; }
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
        <!-- Breadcrumb -->
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="<%= ctx %>/users" class="text-decoration-none">Users</a></li>
                <li class="breadcrumb-item active"><%= isEdit ? "Edit: " + editUser.getFullName() : "New User" %></li>
            </ol>
        </nav>

        <% if (errMsg != null) { %>
        <div class="alert alert-danger alert-dismissible fade show">
            <strong>Error:</strong> <%= errMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <div class="row justify-content-center">
            <div class="col-lg-6">
                <div class="card shadow-sm border-0">
                    <div class="card-header fw-semibold py-3"
                         style="background:<%= isEdit ? "#1b263b" : "#0077b6" %>;color:#fff">
                        <% if (isEdit) { %>
                            &#9998; Edit User &mdash; <%= editUser.getFullName() %>
                        <% } else { %>
                            &#43; Create New User
                        <% } %>
                    </div>
                    <div class="card-body p-4">
                        <form method="post" action="<%= ctx %>/users" id="userForm" novalidate>
                            <input type="hidden" name="action"
                                   value="<%= isEdit ? "update" : "create" %>">
                            <% if (isEdit) { %>
                            <input type="hidden" name="userId" value="<%= editUser.getUserId() %>">
                            <% } %>

                            <!-- Username -->
                            <div class="mb-3">
                                <label class="form-label fw-semibold">
                                    Username <% if (!isEdit) { %><span class="text-danger">*</span><% } %>
                                </label>
                                <% if (isEdit) { %>
                                <input type="text" class="form-control bg-light"
                                       value="@<%= editUser.getUsername() %>" readonly>
                                <div class="form-text">Username cannot be changed after creation.</div>
                                <% } else { %>
                                <input type="text" name="username" class="form-control"
                                       placeholder="e.g. jdelacruz"
                                       value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>"
                                       required maxlength="50" autocomplete="off">
                                <div class="form-text">Lowercase, no spaces. Used for login.</div>
                                <% } %>
                            </div>

                            <!-- Full Name -->
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                                <input type="text" name="fullName" class="form-control"
                                       placeholder="e.g. Juan Dela Cruz"
                                       value="<%= isEdit ? editUser.getFullName() : (request.getParameter("fullName") != null ? request.getParameter("fullName") : "") %>"
                                       required maxlength="100">
                            </div>

                            <!-- Role -->
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Role <span class="text-danger">*</span></label>
                                <div class="d-flex gap-3">
                                    <div class="form-check">
                                        <input class="form-check-input" type="radio" name="role"
                                               id="roleStaff" value="STAFF"
                                               <%= (!isEdit || editUser.getRole() == Role.STAFF) ? "checked" : "" %>>
                                        <label class="form-check-label" for="roleStaff">
                                            <span class="badge bg-primary">Staff</span>
                                            <div class="text-muted" style="font-size:.78rem">
                                                Reservations, check-in/out access
                                            </div>
                                        </label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="radio" name="role"
                                               id="roleAdmin" value="ADMIN"
                                               <%= (isEdit && editUser.getRole() == Role.ADMIN) ? "checked" : "" %>>
                                        <label class="form-check-label" for="roleAdmin">
                                            <span class="badge bg-danger">Admin</span>
                                            <div class="text-muted" style="font-size:.78rem">
                                                Full access including user management
                                            </div>
                                        </label>
                                    </div>
                                </div>
                            </div>

                            <% if (!isEdit) { %>
                            <hr class="my-3">
                            <p class="fw-semibold mb-3">&#128274; Set Password</p>

                            <!-- Password -->
                            <div class="mb-2">
                                <label class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
                                <input type="password" name="password" id="pwField"
                                       class="form-control" placeholder="Min. 6 characters"
                                       required minlength="6" autocomplete="new-password"
                                       oninput="checkStrength(this.value)">
                                <div class="pw-strength mt-1" id="pwStrengthBar"
                                     style="width:0%;background:#dee2e6"></div>
                                <div class="form-text" id="pwStrengthLabel"></div>
                            </div>

                            <!-- Confirm Password -->
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Confirm Password <span class="text-danger">*</span></label>
                                <input type="password" name="confirmPassword" id="pwConfirmField"
                                       class="form-control" placeholder="Re-enter password"
                                       required oninput="checkMatch()">
                                <div class="form-text text-danger d-none" id="pwMismatch">
                                    &#9888; Passwords do not match.
                                </div>
                            </div>
                            <% } %>

                            <div class="d-flex gap-2 mt-4">
                                <button type="submit" class="btn btn-primary px-4">
                                    <%= isEdit ? "&#10003; Save Changes" : "&#43; Create User" %>
                                </button>
                                <a href="<%= ctx %>/users" class="btn btn-outline-secondary px-4">
                                    Cancel
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <% if (isEdit) { %>
                <!-- Info card for editing -->
                <div class="card shadow-sm border-0 mt-3 border-start border-4 border-warning">
                    <div class="card-body py-2 px-3">
                        <div class="small text-muted">
                            &#128161; To change this user's password, use the
                            <strong>&#128273; key icon</strong> on the Users list page.
                        </div>
                    </div>
                </div>
                <% } %>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
function checkStrength(val) {
    var bar   = document.getElementById('pwStrengthBar');
    var label = document.getElementById('pwStrengthLabel');
    var score = 0;
    if (val.length >= 6)  score++;
    if (val.length >= 10) score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    var colors = ['#dc3545','#ffc107','#ffc107','#198754','#198754'];
    var labels = ['Too short','Weak','Fair','Strong','Very strong'];
    var widths = ['20%','40%','60%','80%','100%'];
    bar.style.width      = val.length === 0 ? '0%' : widths[Math.min(score-1, 4)];
    bar.style.background = val.length === 0 ? '#dee2e6' : colors[Math.min(score-1, 4)];
    label.textContent    = val.length === 0 ? '' : labels[Math.min(score-1, 4)];
}

function checkMatch() {
    var pw  = document.getElementById('pwField').value;
    var cpw = document.getElementById('pwConfirmField').value;
    var msg = document.getElementById('pwMismatch');
    if (cpw.length > 0)
        msg.classList.toggle('d-none', pw === cpw);
}
</script>
</body>
</html>
