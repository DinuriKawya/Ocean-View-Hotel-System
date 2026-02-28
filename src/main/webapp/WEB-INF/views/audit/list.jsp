<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*,oceanview.service.AuditLogService,java.util.*,java.time.format.*" %>
<%
    User currentUser = (User) session.getAttribute("loggedInUser");
    String ctx       = request.getContextPath();

    List<AuditLog>  logs       = (List<AuditLog>)  request.getAttribute("logs");
    List<String>    actions    = (List<String>)     request.getAttribute("actions");
    List<String>    performers = (List<String>)     request.getAttribute("performers");

    int totalPages  = (Integer) request.getAttribute("totalPages");
    int totalCount  = (Integer) request.getAttribute("totalCount");
    int currentPage = (Integer) request.getAttribute("currentPage");

    String fAction      = (String) request.getAttribute("fAction");
    String fPerformedBy = (String) request.getAttribute("fPerformedBy");
    String fDateFrom    = (String) request.getAttribute("fDateFrom");
    String fDateTo      = (String) request.getAttribute("fDateTo");
    String fSearch      = (String) request.getAttribute("fSearch");

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Audit Logs &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #0d1b2a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size:.7rem; text-transform:uppercase; letter-spacing:.1em; color:#778da9; padding:16px 16px 4px; }
        .sidebar .nav-link { color:#e0e1dd; font-size:.9rem; padding:10px 16px; border-radius:0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background:#1b263b; color:#fff; }
        .topnav { background:#1b263b; }

        /* Action color badges */
        .badge-action { font-size:.72rem; font-weight:700; padding:3px 9px; border-radius:12px; }
        .act-CREATE        { background:#0d6efd22; color:#0d6efd; border:1px solid #0d6efd55; }
        .act-UPDATE        { background:#ffc10722; color:#b07800; border:1px solid #ffc10788; }
        .act-DELETE        { background:#dc354522; color:#dc3545; border:1px solid #dc354555; }
        .act-CHECK_IN      { background:#19875422; color:#198754; border:1px solid #19875455; }
        .act-CHECK_OUT     { background:#20c99722; color:#0d8a63; border:1px solid #20c99766; }
        .act-CHANGE_PASSWORD { background:#6f42c122; color:#6f42c1; border:1px solid #6f42c155; }
        .act-STATUS        { background:#fd7e1422; color:#c25a00; border:1px solid #fd7e1455; }
        .act-EXTRA_CHARGES { background:#e8390022; color:#e83900; border:1px solid #e8390055; }
        .act-default       { background:#6c757d22; color:#6c757d; border:1px solid #6c757d55; }

        .table-sm td, .table-sm th { vertical-align:middle; font-size:.875rem; }
        .description-cell { max-width:340px; word-break:break-word; }
        .filter-card { border:0; box-shadow:0 1px 6px rgba(0,0,0,.08); }
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
        <a href="<%= ctx %>/users"        class="nav-link">&#128100; Users</a>
        <a href="<%= ctx %>/banks"        class="nav-link">&#127974; Banks</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#128188; Room Mgmt</a>
        <a href="<%= ctx %>/audit"        class="nav-link active">&#128196; Audit Logs</a>
    </div>

    <div class="flex-grow-1 p-4">

        <!-- Page header -->
        <div class="d-flex align-items-center justify-content-between mb-3">
            <div>
                <h5 class="mb-0 fw-semibold text-dark">&#128196; Audit Logs</h5>
                <div class="text-muted" style="font-size:.82rem">
                    Showing <%= totalCount %> record<%= totalCount != 1 ? "s" : "" %>
                    <% if (!fAction.isEmpty() || !fPerformedBy.isEmpty() || !fDateFrom.isEmpty() || !fDateTo.isEmpty() || !fSearch.isEmpty()) { %>
                        &mdash; <span class="text-primary">filters active</span>
                    <% } %>
                </div>
            </div>
        </div>

        <!-- Filter card -->
        <div class="card filter-card mb-3">
            <div class="card-body py-3">
                <form method="get" action="<%= ctx %>/audit" id="filterForm" class="row g-2 align-items-end">
                    <input type="hidden" name="page" value="1">

                    <!-- Action dropdown -->
                    <div class="col-md-2">
                        <label class="form-label mb-1 small fw-semibold">Action</label>
                        <select name="action" class="form-select form-select-sm">
                            <option value="">All Actions</option>
                            <% for (String a : actions) { %>
                            <option value="<%= a %>" <%= a.equals(fAction) ? "selected" : "" %>><%= a %></option>
                            <% } %>
                        </select>
                    </div>

                    <!-- Performed By dropdown -->
                    <div class="col-md-2">
                        <label class="form-label mb-1 small fw-semibold">Performed By</label>
                        <select name="performedBy" class="form-select form-select-sm">
                            <option value="">All Users</option>
                            <% for (String p : performers) { %>
                            <option value="<%= p %>" <%= p.equals(fPerformedBy) ? "selected" : "" %>><%= p %></option>
                            <% } %>
                        </select>
                    </div>

                    <!-- Date From -->
                    <div class="col-md-2">
                        <label class="form-label mb-1 small fw-semibold">Date From</label>
                        <input type="date" name="dateFrom" class="form-control form-control-sm"
                               value="<%= fDateFrom %>">
                    </div>

                    <!-- Date To -->
                    <div class="col-md-2">
                        <label class="form-label mb-1 small fw-semibold">Date To</label>
                        <input type="date" name="dateTo" class="form-control form-control-sm"
                               value="<%= fDateTo %>">
                    </div>

                    <!-- Search -->
                    <div class="col-md-3">
                        <label class="form-label mb-1 small fw-semibold">Search</label>
                        <input type="text" name="search" class="form-control form-control-sm"
                               placeholder="Description, table, record ID&hellip;"
                               value="<%= fSearch %>">
                    </div>

                    <!-- Buttons -->
                    <div class="col-md-1 d-flex gap-1">
                        <button type="submit" class="btn btn-primary btn-sm px-3">Filter</button>
                        <a href="<%= ctx %>/audit" class="btn btn-outline-secondary btn-sm px-2" title="Clear filters">&#10005;</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Logs table -->
        <div class="card border-0 shadow-sm">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-sm table-hover mb-0">
                        <thead class="table-dark">
                            <tr>
                                <th style="width:50px">#</th>
                                <th style="width:130px">Date &amp; Time</th>
                                <th style="width:120px">Action</th>
                                <th style="width:110px">Table</th>
                                <th style="width:70px">Record</th>
                                <th style="width:110px">Performed By</th>
                                <th style="width:110px">IP Address</th>
                                <th>Description</th>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (logs.isEmpty()) { %>
                            <tr>
                                <td colspan="8" class="text-center text-muted py-5">
                                    &#128269; No audit log entries found.
                                </td>
                            </tr>
                        <% } else { %>
                            <% for (AuditLog log : logs) { %>
                            <tr>
                                <td class="text-muted small"><%= log.getLogId() %></td>
                                <td class="small text-nowrap">
                                    <%= log.getCreatedAt() != null ? log.getCreatedAt().format(dtf) : "" %>
                                </td>
                                <td>
                                    <%
                                        String act = log.getAction() != null ? log.getAction() : "";
                                        String cssAct;
                                        switch (act) {
                                            case "CREATE":          cssAct = "act-CREATE";          break;
                                            case "UPDATE":          cssAct = "act-UPDATE";          break;
                                            case "DELETE":          cssAct = "act-DELETE";          break;
                                            case "CHECK_IN":        cssAct = "act-CHECK_IN";        break;
                                            case "CHECK_OUT":       cssAct = "act-CHECK_OUT";       break;
                                            case "CHANGE_PASSWORD": cssAct = "act-CHANGE_PASSWORD"; break;
                                            case "STATUS":          cssAct = "act-STATUS";          break;
                                            case "EXTRA_CHARGES":   cssAct = "act-EXTRA_CHARGES";   break;
                                            default:                cssAct = "act-default";
                                        }
                                    %>
                                    <span class="badge-action <%= cssAct %>"><%= act %></span>
                                </td>
                                <td class="small text-muted font-monospace"><%= log.getTableName() != null ? log.getTableName() : "" %></td>
                                <td class="small text-center text-muted"><%= log.getRecordId() > 0 ? log.getRecordId() : "&mdash;" %></td>
                                <td class="small fw-semibold"><%= log.getPerformedBy() != null ? log.getPerformedBy() : "" %></td>
                                <td class="small text-muted font-monospace"><%= log.getIpAddress() != null ? log.getIpAddress() : "" %></td>
                                <td class="description-cell small text-muted"><%= log.getDescription() != null ? log.getDescription() : "" %></td>
                            </tr>
                            <% } %>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Pagination -->
        <% if (totalPages > 1) { %>
        <nav class="mt-3">
            <ul class="pagination pagination-sm mb-0 justify-content-center">

                <!-- Previous -->
                <li class="page-item <%= currentPage <= 1 ? "disabled" : "" %>">
                    <a class="page-link" href="<%= buildUrl(ctx, fAction, fPerformedBy, fDateFrom, fDateTo, fSearch, currentPage - 1) %>">
                        &laquo; Prev
                    </a>
                </li>

                <!-- Page numbers (window of 5 around current) -->
                <%
                    int startPg = Math.max(1, currentPage - 2);
                    int endPg   = Math.min(totalPages, startPg + 4);
                    startPg     = Math.max(1, endPg - 4);

                    if (startPg > 1) {
                %>
                <li class="page-item"><a class="page-link" href="<%= buildUrl(ctx, fAction, fPerformedBy, fDateFrom, fDateTo, fSearch, 1) %>">1</a></li>
                <% if (startPg > 2) { %><li class="page-item disabled"><span class="page-link">&hellip;</span></li><% } %>
                <% } %>

                <% for (int pg = startPg; pg <= endPg; pg++) { %>
                <li class="page-item <%= pg == currentPage ? "active" : "" %>">
                    <a class="page-link" href="<%= buildUrl(ctx, fAction, fPerformedBy, fDateFrom, fDateTo, fSearch, pg) %>"><%= pg %></a>
                </li>
                <% } %>

                <% if (endPg < totalPages) {
                    if (endPg < totalPages - 1) { %><li class="page-item disabled"><span class="page-link">&hellip;</span></li><% }
                %>
                <li class="page-item"><a class="page-link" href="<%= buildUrl(ctx, fAction, fPerformedBy, fDateFrom, fDateTo, fSearch, totalPages) %>"><%= totalPages %></a></li>
                <% } %>

                <!-- Next -->
                <li class="page-item <%= currentPage >= totalPages ? "disabled" : "" %>">
                    <a class="page-link" href="<%= buildUrl(ctx, fAction, fPerformedBy, fDateFrom, fDateTo, fSearch, currentPage + 1) %>">
                        Next &raquo;
                    </a>
                </li>

            </ul>
        </nav>
        <% } %>

    </div><!-- /flex-grow-1 -->
</div><!-- /d-flex -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
<%!
    /** Build a URL preserving all active filters, updating only page number. */
    private String buildUrl(String ctx, String action, String performedBy,
                             String dateFrom, String dateTo, String search, int page)
            throws java.io.UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(ctx).append("/audit?page=").append(page);
        if (action      != null && !action.isBlank())      sb.append("&action=").append(java.net.URLEncoder.encode(action, "UTF-8"));
        if (performedBy != null && !performedBy.isBlank()) sb.append("&performedBy=").append(java.net.URLEncoder.encode(performedBy, "UTF-8"));
        if (dateFrom    != null && !dateFrom.isBlank())    sb.append("&dateFrom=").append(dateFrom);
        if (dateTo      != null && !dateTo.isBlank())      sb.append("&dateTo=").append(dateTo);
        if (search      != null && !search.isBlank())      sb.append("&search=").append(java.net.URLEncoder.encode(search, "UTF-8"));
        return sb.toString();
    }
%>
