<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.AppSettings, java.util.*, java.text.*" %>
<%!
    /* ---- Helper methods — compiled into the servlet class, not _jspService ---- */

    private String roomLabel(String code) {
        if (code == null) return "";
        switch (code) {
            case "STANDARD":  return "Standard";
            case "DELUXE":    return "Deluxe";
            case "SUITE":     return "Suite";
            case "FAMILY":    return "Family";
            case "PENTHOUSE": return "Penthouse";
            default:          return code;
        }
    }

    private String methodLabel(String code) {
        if (code == null) return "";
        switch (code) {
            case "CASH":     return "Cash";
            case "CARD":     return "Credit / Debit Card";
            case "TRANSFER": return "Bank Transfer";
            default:         return code;
        }
    }

    private String displayCell(String raw, int col, boolean isMoney,
                                String type, DecimalFormat fmt) {
        if (raw == null) raw = "0";
        if (isMoney) {
            try { return fmt.format(Double.parseDouble(raw)); }
            catch (Exception e) { return raw; }
        }
        if (col == 0 && "room".equals(type))    return roomLabel(raw);
        if (col == 0 && "payment".equals(type)) return methodLabel(raw);
        return raw;
    }

    private String jsStr(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\r","").replace("\n","\\n");
    }
%>
<%
    User   currentUser = (User)   session.getAttribute("loggedInUser");
    String ctx         = request.getContextPath();

    String type      = (String) request.getAttribute("type");
    String dateFrom  = (String) request.getAttribute("dateFrom");
    String dateTo    = (String) request.getAttribute("dateTo");
    String title     = (String) request.getAttribute("title");
    String errMsg    = (String) request.getAttribute("errorMessage");

    List     rowsRaw  = (List)     request.getAttribute("rows");
    String[] headers  = (String[]) request.getAttribute("headers");
    int[]    moneyCols = (int[])   request.getAttribute("moneyCols");

    // Safe casts
    List<String[]> rows = new ArrayList<String[]>();
    if (rowsRaw != null) {
        for (Object o : rowsRaw) rows.add((String[]) o);
    }

    boolean hasData = (!rows.isEmpty() && headers != null);

    // Set of money-column indices
    Set<Integer> moneySet = new HashSet<Integer>();
    if (moneyCols != null) for (int mc : moneyCols) moneySet.add(mc);

    // Column totals
    double[] colTotals = (headers != null) ? new double[headers.length] : new double[0];
    if (hasData && moneyCols != null) {
        for (String[] row : rows) {
            for (int mc : moneyCols) {
                if (mc < row.length) {
                    try { colTotals[mc] += Double.parseDouble(row[mc]); }
                    catch (Exception ignored) {}
                }
            }
        }
    }

    DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");
    String currency = AppSettings.getCurrency();

    // Build JSON arrays for JS export
    StringBuilder jsHeaders = new StringBuilder("[");
    StringBuilder jsRows    = new StringBuilder("[");
    if (hasData) {
        for (int h = 0; h < headers.length; h++) {
            if (h > 0) jsHeaders.append(",");
            jsHeaders.append("\"").append(jsStr(headers[h])).append("\"");
        }
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) jsRows.append(",");
            jsRows.append("[");
            String[] row = rows.get(r);
            for (int c = 0; c < row.length; c++) {
                if (c > 0) jsRows.append(",");
                String cell = displayCell(row[c], c, moneySet.contains(c), type, moneyFmt);
                jsRows.append("\"").append(jsStr(cell)).append("\"");
            }
            jsRows.append("]");
        }
    }
    jsHeaders.append("]");
    jsRows.append("]");

    // Money cols JS array
    StringBuilder jsMoneyCols = new StringBuilder("[");
    if (moneyCols != null) {
        for (int m = 0; m < moneyCols.length; m++) {
            if (m > 0) jsMoneyCols.append(",");
            jsMoneyCols.append(moneyCols[m]);
        }
    }
    jsMoneyCols.append("]");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Reports &amp; Analytics &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background:#f0f4f8; }
        .sidebar { min-height:calc(100vh - 56px); background:#0d1b2a; width:220px; flex-shrink:0; }
        .sidebar .lbl { font-size:.7rem; text-transform:uppercase; letter-spacing:.1em; color:#778da9; padding:16px 16px 4px; }
        .sidebar .nav-link { color:#e0e1dd; font-size:.9rem; padding:10px 16px; border-radius:0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background:#1b263b; color:#fff; }
        .topnav { background:#1b263b; }

        .report-table thead th { background:#1b263b; color:#fff; font-size:.82rem; vertical-align:middle; white-space:nowrap; }
        .report-table tbody td { font-size:.88rem; vertical-align:middle; }
        .report-table tfoot td { background:#eef2ff; font-weight:700; font-size:.88rem; border-top:2px solid #1b263b; }
        .report-table .money  { text-align:right; font-family:monospace; }
        .report-table .num    { text-align:center; }

        .export-bar { background:#fff; border:1px solid #dee2e6; border-radius:8px;
                      padding:12px 18px; display:flex; gap:10px; align-items:center;
                      flex-wrap:wrap; margin-bottom:18px; }
        .export-bar .lbl { font-weight:600; font-size:.85rem; color:#555; }

        @media print {
            .no-print  { display:none !important; }
            .sidebar   { display:none !important; }
            .topnav    { display:none !important; }
            .d-flex    { display:block !important; }
            body       { background:white; }
            .print-hdr { display:block !important; }
            .card      { box-shadow:none !important; }
        }
        .print-hdr { display:none; text-align:center; margin-bottom:16px; }
        .print-hdr h5 { font-weight:700; }
        .print-hdr p  { font-size:.85rem; color:#555; margin:2px 0; }
    </style>
</head>
<body>

<nav class="navbar topnav px-3 no-print">
    <span class="navbar-brand text-white fw-semibold">OceanView Hotel &mdash; Admin</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">Welcome, <strong><%= currentUser.getFullName() %></strong>
            <span class="badge bg-danger ms-1"><%= currentUser.getRole() %></span></span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>

<div class="d-flex">
    <div class="sidebar no-print">
        <div class="lbl">Operations</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Rooms</a>
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link">&#128198; Check-Out</a>
        <div class="lbl mt-2">Admin</div>
        <a href="<%= ctx %>/users"        class="nav-link">&#128100; Users</a>
        <a href="<%= ctx %>/banks"        class="nav-link">&#127974; Banks</a>
        <a href="<%= ctx %>/reports"      class="nav-link active">&#128202; Reports</a>
        <a href="<%= ctx %>/audit"        class="nav-link">&#128196; Audit Logs</a>
    </div>

    <div class="flex-grow-1 p-4">

        <!-- Print-only header -->
        <div class="print-hdr">
            <h5>&#127748; OceanView Hotel</h5>
            <% if (title != null) { %>
            <p><strong><%= title %></strong></p>
            <p>Period: <%= dateFrom %> &mdash; <%= dateTo %></p>
            <% } %>
        </div>

        <h5 class="fw-semibold mb-1 no-print">&#128202; Reports &amp; Analytics</h5>
        <p class="text-muted small mb-3 no-print">Select a report type and date range, then click Generate.</p>

        <% if (errMsg != null) { %>
        <div class="alert alert-danger alert-dismissible fade show no-print">
            <strong>Error:</strong> <%= errMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <!-- ---- Filter form ---- -->
        <div class="card border-0 shadow-sm mb-4 no-print">
            <div class="card-body">
                <form method="get" action="<%= ctx %>/reports" class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label class="form-label fw-semibold small">Report Type</label>
                        <select name="type" class="form-select form-select-sm" required>
                            <option value="" disabled <%= (type == null || type.isEmpty()) ? "selected" : "" %>>-- Select type --</option>
                            <option value="staff"   <%= "staff".equals(type)   ? "selected" : "" %>>&#128100; Staff-Wise Revenue</option>
                            <option value="room"    <%= "room".equals(type)    ? "selected" : "" %>>&#127963; Room Category Revenue</option>
                            <option value="payment" <%= "payment".equals(type) ? "selected" : "" %>>&#128179; Payment Method Breakdown</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-semibold small">Date From</label>
                        <input type="date" name="dateFrom" class="form-control form-control-sm"
                               value="<%= dateFrom != null ? dateFrom : "" %>" required>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-semibold small">Date To</label>
                        <input type="date" name="dateTo" class="form-control form-control-sm"
                               value="<%= dateTo != null ? dateTo : "" %>" required>
                    </div>
                    <div class="col-md-3">
                        <button type="submit" class="btn btn-primary btn-sm w-100">
                            &#9654; Generate Report
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- ---- Results ---- -->
        <% if (hasData) { %>

        <!-- Export bar -->
        <div class="export-bar no-print">
            <span class="lbl">Export as:</span>
            <button class="btn btn-outline-secondary btn-sm px-3" onclick="window.print()">
                &#128438; Print
            </button>
            <button class="btn btn-outline-danger btn-sm px-3" onclick="exportPDF()">
                &#128196; PDF
            </button>
            <button class="btn btn-outline-success btn-sm px-3" onclick="exportExcel()">
                &#128202; Excel
            </button>
        </div>

        <!-- Report header -->
        <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
                <h6 class="fw-bold mb-0"><%= title %></h6>
                <div class="text-muted small">
                    Period: <strong><%= dateFrom %></strong> to <strong><%= dateTo %></strong>
                    &mdash; <%= rows.size() %> row<%= rows.size() != 1 ? "s" : "" %>
                </div>
            </div>
        </div>

        <!-- Report table -->
        <div class="card border-0 shadow-sm">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-bordered table-hover report-table mb-0" id="reportTable">
                        <thead>
                            <tr>
                                <% for (int h = 0; h < headers.length; h++) { %>
                                <th class="<%= moneySet.contains(h) ? "money" : (h > 0 ? "num" : "") %>">
                                    <%= headers[h] %>
                                </th>
                                <% } %>
                            </tr>
                        </thead>
                        <tbody>
                        <% if (rows.isEmpty()) { %>
                            <tr>
                                <td colspan="<%= headers.length %>" class="text-center text-muted py-4">
                                    No data found for the selected date range.
                                </td>
                            </tr>
                        <% } else {
                               for (String[] row : rows) { %>
                            <tr>
                                <% for (int c = 0; c < row.length; c++) {
                                       boolean isMoney = moneySet.contains(c);
                                       String cell = displayCell(row[c], c, isMoney, type, moneyFmt);
                                %>
                                <td class="<%= isMoney ? "money" : (c > 0 ? "num" : "fw-semibold") %>">
                                    <%= isMoney ? currency + " " + cell : cell %>
                                </td>
                                <% } %>
                            </tr>
                        <% } } %>
                        </tbody>
                        <% if (!rows.isEmpty()) { %>
                        <tfoot>
                            <tr>
                                <% for (int c = 0; c < headers.length; c++) { %>
                                <td class="<%= moneySet.contains(c) ? "money" : (c > 0 ? "num" : "") %>">
                                    <% if (c == 0) { %>TOTAL
                                    <% } else if (moneySet.contains(c)) { %><%= currency %> <%= moneyFmt.format(colTotals[c]) %>
                                    <% } else { %>&mdash;<% } %>
                                </td>
                                <% } %>
                            </tr>
                        </tfoot>
                        <% } %>
                    </table>
                </div>
            </div>
        </div>

        <!-- JS export data -->
        <script>
        var REPORT_TITLE  = "<%= jsStr(title) %>";
        var REPORT_PERIOD = "Period: <%= dateFrom %> to <%= dateTo %>";
        var RPT_HEADERS   = <%= jsHeaders.toString() %>;
        var RPT_ROWS      = <%= jsRows.toString() %>;
        var MONEY_COLS    = <%= jsMoneyCols.toString() %>;

        // Build totals row
        var totalsRow = RPT_HEADERS.map(function(h, i) { return i === 0 ? 'TOTAL' : ''; });
        MONEY_COLS.forEach(function(ci) {
            var sum = 0;
            RPT_ROWS.forEach(function(r) {
                var n = parseFloat(String(r[ci]).replace(/,/g, ''));
                if (!isNaN(n)) sum += n;
            });
            totalsRow[ci] = sum.toLocaleString('en-PH', {minimumFractionDigits:2, maximumFractionDigits:2});
        });

        // ---- PDF via jsPDF + AutoTable ----
        function exportPDF() {
            var jsPDF = window.jspdf.jsPDF;
            var doc = new jsPDF({ orientation:'landscape', unit:'mm', format:'a4' });

            doc.setFontSize(14); doc.setFont(undefined,'bold');
            doc.text('OceanView Hotel', 14, 14);
            doc.setFontSize(11);
            doc.text(REPORT_TITLE, 14, 21);
            doc.setFontSize(9); doc.setFont(undefined,'normal');
            doc.text(REPORT_PERIOD, 14, 27);
            doc.text('Generated: ' + new Date().toLocaleString(), 14, 32);

            var colStyles = {};
            RPT_HEADERS.forEach(function(h, i) {
                if (MONEY_COLS.indexOf(i) >= 0 || i > 0) colStyles[i] = { halign:'right' };
            });

            doc.autoTable({
                startY: 37,
                head:  [RPT_HEADERS],
                body:  RPT_ROWS,
                foot:  [totalsRow],
                theme: 'striped',
                headStyles: { fillColor:[27,38,59], fontSize:8, fontStyle:'bold' },
                footStyles: { fillColor:[238,242,255], fontSize:8, fontStyle:'bold', textColor:[27,38,59] },
                bodyStyles: { fontSize:8 },
                columnStyles: colStyles,
                showFoot: 'lastPage'
            });

            doc.save('OceanView_' + REPORT_TITLE.replace(/\s+/g,'_') + '_<%= dateFrom %>.pdf');
        }

        // ---- Excel via SheetJS ----
        function exportExcel() {
            var wb = XLSX.utils.book_new();
            var data = [];
            data.push(['OceanView Hotel']);
            data.push([REPORT_TITLE]);
            data.push([REPORT_PERIOD]);
            data.push(['Generated: ' + new Date().toLocaleString()]);
            data.push([]);
            data.push(RPT_HEADERS);

            RPT_ROWS.forEach(function(r) {
                var row = r.map(function(cell, ci) {
                    if (MONEY_COLS.indexOf(ci) >= 0) {
                        var n = parseFloat(String(cell).replace(/,/g,''));
                        return isNaN(n) ? cell : n;
                    }
                    return cell;
                });
                data.push(row);
            });

            // Totals row as numbers
            var exTotals = totalsRow.map(function(cell, ci) {
                if (MONEY_COLS.indexOf(ci) >= 0) {
                    var n = parseFloat(String(cell).replace(/,/g,''));
                    return isNaN(n) ? cell : n;
                }
                return cell;
            });
            data.push(exTotals);

            var ws = XLSX.utils.aoa_to_sheet(data);
            ws['!cols'] = RPT_HEADERS.map(function(h) {
                return { wch: Math.max(h.length + 4, 20) };
            });

            XLSX.utils.book_append_sheet(wb, ws, 'Report');
            XLSX.writeFile(wb, 'OceanView_' + REPORT_TITLE.replace(/\s+/g,'_') + '_<%= dateFrom %>.xlsx');
        }
        </script>

        <% } // end hasData %>

    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<% if (hasData) { %>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.8.2/jspdf.plugin.autotable.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
<% } %>
</body>
</html>
