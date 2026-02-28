<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.ExtraCharge, oceanview.model.Reservation,
                 oceanview.model.ReservationStatus, oceanview.model.User,
                 oceanview.model.AppSettings, java.util.List, java.time.format.DateTimeFormatter" %>
<%
    User user                     = (User) session.getAttribute("loggedInUser");
    Reservation res               = (Reservation) request.getAttribute("reservation");
    List<ExtraCharge> savedCharges = (List<ExtraCharge>) request.getAttribute("extraCharges");
    Double totalExtra             = (Double) request.getAttribute("totalExtra");
    String errMsg                 = (String) request.getAttribute("errorMessage");
    String ctx                    = request.getContextPath();
    DateTimeFormatter fmt         = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    double savedExtra = (totalExtra != null) ? totalExtra : 0.0;
    String currency   = AppSettings.getCurrency();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Guest Check-Out &mdash; OceanView Hotel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #023e8a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #90e0ef; padding: 16px 16px 4px; }
        .sidebar .nav-link { color: #caf0f8; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #0077b6; color: #fff; }
        .topnav { background: #0077b6; }
        .charge-row { background: #fff8f0; border: 1px solid #ffc107; border-radius: 8px; padding: 10px 14px; margin-bottom: 8px; }
        .badge-type { font-size: .75rem; font-weight: 600; }
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
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link active">&#128198; Check-Out</a>
    </div>

    <div class="flex-grow-1 p-4">
        <h4 class="mb-4 text-primary fw-bold">&#128198; Guest Check-Out</h4>

        <% if (errMsg != null) { %>
        <div class="alert alert-danger alert-dismissible fade show">
            <strong>Error:</strong> <%= errMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <!-- Search form when no reservation -->
        <% if (res == null) { %>
        <div class="card shadow-sm border-0" style="max-width:480px">
            <div class="card-body">
                <label class="form-label fw-semibold">Find Reservation by ID</label>
                <div class="input-group">
                    <input type="number" id="searchId" class="form-control" placeholder="Enter reservation #">
                    <button class="btn btn-primary" onclick="goSearch()">Find &rarr;</button>
                </div>
                <div class="form-text">Reservation must have status: <strong>CHECKED IN</strong></div>
            </div>
        </div>
        <script>
            function goSearch() {
                var id = document.getElementById('searchId').value.trim();
                if (id) window.location.href = '<%= ctx %>/checkout?id=' + id;
            }
            document.getElementById('searchId').addEventListener('keydown', function(e) {
                if (e.key === 'Enter') goSearch();
            });
        </script>

        <% } else { %>

        <!-- Main form -->
        <form method="post" action="<%= ctx %>/checkout" id="mainForm">
            <input type="hidden" name="reservationId" value="<%= res.getReservationId() %>">
            <input type="hidden" name="action" value="generateBill">
            <!-- Hidden charge inputs are injected here by JS -->
            <div id="chargeInputsHidden"></div>

            <div class="row g-4">
                <!-- LEFT: Reservation summary + payments + extra charges -->
                <div class="col-lg-8">

                    <!-- Reservation summary -->
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                            <span class="fw-semibold">Reservation #<%= res.getReservationId() %></span>
                            <span class="badge bg-light text-primary"><%= res.getStatus().getDisplayName() %></span>
                        </div>
                        <div class="card-body">
                            <div class="row g-2">
                                <div class="col-md-4">
                                    <div class="text-muted small">Guest</div>
                                    <div class="fw-bold"><%= res.getGuestName() %></div>
                                    <div class="small text-muted"><%= res.getGuestEmail() %></div>
                                </div>
                                <div class="col-md-3">
                                    <div class="text-muted small">Room</div>
                                    <div class="fw-bold">Room <%= res.getRoomNumber() %></div>
                                    <div class="small"><%= res.getRoomType() != null ? res.getRoomType().getDisplayName() : "" %></div>
                                </div>
                                <div class="col-md-2">
                                    <div class="text-muted small">Check-In</div>
                                    <div class="fw-bold small"><%= res.getCheckInDate() != null ? res.getCheckInDate().format(fmt) : "—" %></div>
                                </div>
                                <div class="col-md-3">
                                    <div class="text-muted small">Room Total</div>
                                    <div class="fw-bold text-primary"><%= currency %> <%= String.format("%,.2f", res.getTotalAmount()) %></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Already-saved extra charges (from a previous Generate Bill) -->
                    <% if (savedCharges != null && !savedCharges.isEmpty()) { %>
                    <div class="card shadow-sm border-0 border-warning mb-4">
                        <div class="card-header bg-warning bg-opacity-25 fw-semibold small">
                            &#9888; Extra Charges Already Added
                            <span class="badge bg-danger ms-2"><%= currency %> <%= String.format("%,.2f", savedExtra) %></span>
                        </div>
                        <div class="card-body p-0">
                            <table class="table table-sm align-middle mb-0">
                                <thead class="table-light">
                                    <tr><th>Type</th><th>Description</th><th class="text-end">Amount</th></tr>
                                </thead>
                                <tbody>
                                <% for (ExtraCharge ec : savedCharges) { %>
                                <tr>
                                    <td><span class="badge bg-warning text-dark badge-type"><%= ec.getChargeType() %></span></td>
                                    <td class="small"><%= ec.getDescription() != null && !ec.getDescription().isBlank() ? ec.getDescription() : "—" %></td>
                                    <td class="text-end text-danger fw-semibold"><%= currency %> <%= String.format("%,.2f", ec.getAmount()) %></td>
                                </tr>
                                <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <% } %>
1
                    <!-- ADD NEW EXTRA CHARGES -->
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header fw-semibold d-flex justify-content-between align-items-center"
                             style="background:#fff3cd">
                            <span>&#10010; Add Extra Charges</span>
                            <span class="text-muted small fw-normal">damage, room service, minibar, etc.</span>
                        </div>
                        <div class="card-body">
                            <!-- Input row -->
                            <div class="row g-2 align-items-end mb-3">
                                <div class="col-md-3">
                                    <label class="form-label fw-semibold small">Type</label>
                                    <select id="newChargeType" class="form-select form-select-sm">
                                        <option value="Damage/Breakage">&#9889; Damage / Breakage</option>
                                        <option value="Room Service">&#127869; Room Service</option>
                                        <option value="Minibar">&#127864; Minibar</option>
                                        <option value="Laundry">&#128086; Laundry</option>
                                        <option value="Parking">&#128663; Parking</option>
                                        <option value="Phone / Internet">&#128222; Phone / Internet</option>
                                        <option value="Miscellaneous">&#128203; Miscellaneous</option>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label fw-semibold small">Description <span class="text-muted fw-normal">(optional)</span></label>
                                    <input type="text" id="newChargeDesc" class="form-control form-control-sm"
                                           placeholder="e.g. Broken mirror, Extra towels">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label fw-semibold small">Amount (<%= currency %>)</label>
                                    <input type="number" id="newChargeAmt" class="form-control form-control-sm"
                                           min="0.01" step="0.01" placeholder="0.00">
                                </div>
                                <div class="col-md-2">
                                    <button type="button" class="btn btn-warning btn-sm w-100"
                                            onclick="addCharge()">&#43; Add</button>
                                </div>
                            </div>

                            <!-- Pending charges table -->
                            <div id="pendingSection" class="d-none">
                                <div class="table-responsive">
                                    <table class="table table-sm table-bordered align-middle mb-2">
                                        <thead class="table-warning">
                                            <tr>
                                                <th>Type</th>
                                                <th>Description</th>
                                                <th class="text-end">Amount</th>
                                                <th style="width:50px"></th>
                                            </tr>
                                        </thead>
                                        <tbody id="pendingBody"></tbody>
                                        <tfoot>
                                            <tr class="table-light fw-bold">
                                                <td colspan="2" class="text-end">New Charges Total:</td>
                                                <td class="text-end text-danger" id="pendingTotal"><%= currency %> 0.00</td>
                                                <td></td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </div>
                            <p class="text-muted small mb-0" id="noChargesMsg">
                                No extra charges added yet. Use the form above to add any additional fees.
                            </p>
                        </div>
                    </div>

                </div><!-- /col-lg-8 -->

                <!-- RIGHT: Bill summary + Generate Bill button -->
                <div class="col-lg-4">
                    <div class="card shadow-sm border-0 sticky-top" style="top:16px">
                        <div class="card-header bg-dark text-white fw-semibold">&#129534; Bill Summary</div>
                        <div class="card-body">
                            <table class="table table-sm table-borderless mb-0">
                                <tr>
                                    <td class="text-muted">Room Charges</td>
                                    <td class="text-end"><%= currency %> <%= String.format("%,.2f", res.getTotalAmount()) %></td>
                                </tr>
                                <% if (savedExtra > 0) { %>
                                <tr>
                                    <td class="text-muted">Saved Extra Charges</td>
                                    <td class="text-end text-danger"><%= currency %> <%= String.format("%,.2f", savedExtra) %></td>
                                </tr>
                                <% } %>
                                <tr>
                                    <td class="text-muted">New Charges (pending)</td>
                                    <td class="text-end text-danger" id="summaryNewExtra"><%= currency %> 0.00</td>
                                </tr>
                                <tr class="border-top fw-bold">
                                    <td>Grand Total</td>
                                    <td class="text-end text-primary" id="summaryGrand"><%= currency %> <%= String.format("%,.2f", res.getTotalAmount() + savedExtra) %></td>
                                </tr>
                                <tr class="fw-semibold">
                                    <td>Amount Due at Check-Out</td>
                                    <td class="text-end text-danger" id="summaryBalance"><%= currency %> <%= String.format("%,.2f", res.getTotalAmount() + savedExtra) %></td>
                                </tr>
                            </table>
                        </div>
                        <div class="card-footer d-grid gap-2">
                            <button type="submit" class="btn btn-success btn-lg">
                                &#128196; Generate Bill Preview &rarr;
                            </button>
                            <a href="<%= ctx %>/reservations?action=view&id=<%= res.getReservationId() %>"
                               class="btn btn-outline-secondary">Cancel</a>
                        </div>
                    </div>
                </div>
            </div><!-- /row -->
        </form>

        <script>
        var currencyCode = '<%= currency %>';
        var chargeCount = 0;
        var resTotal    = <%= res.getTotalAmount() %>;
        var savedExtra  = <%= savedExtra %>;

        function addCharge() {
            var type = document.getElementById('newChargeType').value;
            var desc = document.getElementById('newChargeDesc').value.trim();
            var amt  = parseFloat(document.getElementById('newChargeAmt').value);

            if (!amt || amt <= 0) {
                alert('Please enter a valid amount greater than 0.');
                document.getElementById('newChargeAmt').focus();
                return;
            }

            chargeCount++;
            var rn = chargeCount;

            // Add hidden inputs to the form
            var hidden = document.getElementById('chargeInputsHidden');
            hidden.insertAdjacentHTML('beforeend',
                '<input type="hidden" name="chargeType[]"   id="ht-' + rn + '" value="' + escHtml(type) + '">' +
                '<input type="hidden" name="chargeDesc[]"   id="hd-' + rn + '" value="' + escHtml(desc) + '">' +
                '<input type="hidden" name="chargeAmount[]" id="ha-' + rn + '" value="' + amt + '">');

            // Add row to preview table
            var tr = document.createElement('tr');
            tr.id = 'cr-' + rn;
            tr.innerHTML =
                '<td><span class="badge bg-warning text-dark" style="font-size:.75rem">' + escHtml(type) + '</span></td>' +
                '<td class="small">' + (desc ? escHtml(desc) : '<span class="text-muted">—</span>') + '</td>' +
                '<td class="text-end text-danger fw-semibold">currencyCode + ' ' + amt.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2}) + '</td>' +
                '<td class="text-center">' +
                  '<button type="button" class="btn btn-outline-danger btn-sm py-0 px-1" onclick="removeCharge(' + rn + ')" title="Remove">&#x2715;</button>' +
                '</td>';
            document.getElementById('pendingBody').appendChild(tr);

            // Show table, hide empty msg
            document.getElementById('pendingSection').classList.remove('d-none');
            document.getElementById('noChargesMsg').classList.add('d-none');

            // Clear inputs
            document.getElementById('newChargeDesc').value = '';
            document.getElementById('newChargeAmt').value  = '';

            updateSummary();
        }

        function removeCharge(rn) {
            var row = document.getElementById('cr-' + rn);
            if (row) row.remove();
            ['ht', 'hd', 'ha'].forEach(function(p) {
                var el = document.getElementById(p + '-' + rn);
                if (el) el.remove();
            });
            if (document.querySelectorAll('#pendingBody tr').length === 0) {
                document.getElementById('pendingSection').classList.add('d-none');
                document.getElementById('noChargesMsg').classList.remove('d-none');
            }
            updateSummary();
        }

        function getNewExtraTotal() {
            var total = 0;
            document.querySelectorAll('[id^="ha-"]').forEach(function(inp) {
                total += parseFloat(inp.value || 0);
            });
            return total;
        }

        function updateSummary() {
            var newExtra   = getNewExtraTotal();
            var grandTotal = resTotal + savedExtra + newExtra;

            // Update pending total in table footer
            document.getElementById('pendingTotal').textContent =
                currencyCode + ' ' + newExtra.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});

            // Update right-side summary card
            document.getElementById('summaryNewExtra').textContent =
                currencyCode + ' ' + newExtra.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
            document.getElementById('summaryGrand').textContent =
                currencyCode + ' ' + grandTotal.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
            document.getElementById('summaryBalance').textContent =
                currencyCode + ' ' + grandTotal.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
        }

        function escHtml(s) {
            return String(s)
                .replace(/&/g,'&amp;').replace(/</g,'&lt;')
                .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
        }
        </script>

        <% } // end if res != null %>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
