<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.*, java.util.List,
                 java.time.LocalDate, java.time.LocalDateTime, java.time.format.DateTimeFormatter,
                 oceanview.model.AppSettings" %>
<%
    User user                      = (User) session.getAttribute("loggedInUser");
    Reservation res                = (Reservation) request.getAttribute("reservation");
    List<ExtraCharge> extraCharges = (List<ExtraCharge>) request.getAttribute("extraCharges");
    List<Bank>        banks        = (List<Bank>)        request.getAttribute("activeBanks");
    Double totalExtra              = (Double) request.getAttribute("totalExtra");
    String errMsg                  = (String) request.getAttribute("errorMessage");
    String ctx                     = request.getContextPath();
    DateTimeFormatter fmt          = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    double extraTotal  = (totalExtra != null) ? totalExtra : 0.0;
    double resTotal    = (res != null)        ? res.getTotalAmount() : 0.0;
    double grandTotal  = resTotal + extraTotal;
    double taxRate     = AppSettings.getTaxRate();
    double taxAmount   = grandTotal * taxRate / 100.0;
    double grandTotalWithTax = grandTotal + taxAmount;
    double balance     = grandTotalWithTax;

    String currency    = AppSettings.getCurrency();
    String today = DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(LocalDate.now());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Bill Preview &mdash; Reservation #<%= res != null ? res.getReservationId() : "" %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f0f4f8; }
        .sidebar { min-height: calc(100vh - 56px); background: #023e8a; width: 220px; flex-shrink: 0; }
        .sidebar .lbl { font-size: .7rem; text-transform: uppercase; letter-spacing: .1em; color: #90e0ef; padding: 16px 16px 4px; }
        .sidebar .nav-link { color: #caf0f8; font-size: .9rem; padding: 10px 16px; border-radius: 0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #0077b6; color: #fff; }
        .topnav { background: #0077b6; }

        /* Bill card styles */
        .bill-card { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,.1); overflow: hidden; }
        .bill-hotel-header { background: #023e8a; color: #fff; padding: 28px 32px 20px; }
        .bill-hotel-header h5 { font-size: 1.4rem; font-weight: 700; margin: 0; }
        .bill-hotel-header p  { font-size: .85rem; opacity: .8; margin: 4px 0 0; }
        .bill-receipt-label { background: #0077b6; color: #fff; text-align: center;
                              font-size: .75rem; letter-spacing: .18em; text-transform: uppercase;
                              font-weight: 700; padding: 6px; }
        .bill-guest-bar { background: #f8fafc; border-bottom: 1px solid #e8ecf0; padding: 16px 24px; }
        .bill-body { padding: 24px; }
        .bill-section-title { font-size: .72rem; text-transform: uppercase; letter-spacing: .12em;
                              color: #888; font-weight: 700; margin-bottom: 8px; margin-top: 18px; }
        .bill-table { width: 100%; border-collapse: collapse; }
        .bill-table td, .bill-table th { padding: 8px 12px; }
        .bill-table thead th { background: #f1f5f9; font-size: .8rem; font-weight: 600; color: #555; }
        .bill-table tbody tr:nth-child(even) { background: #fafbfc; }
        .bill-table tfoot td { border-top: 2px solid #dee2e6; font-weight: 700; }
        .bill-divider { border: none; border-top: 1px dashed #ccc; margin: 20px 0; }
        .bill-totals { background: #f8fafc; border-radius: 8px; padding: 16px 20px; }
        .bill-totals table { width: 100%; }
        .bill-totals td { padding: 5px 8px; font-size: .9rem; }
        .bill-grand { font-size: 1.1rem; font-weight: 700; color: #023e8a; border-top: 2px solid #dee2e6; padding-top: 8px; }
        .bill-balance { font-size: 1rem; color: #dc3545; font-weight: 700; }
        .bill-balance.zero { color: #198754; }

        /* Payment section */
        .payment-row { border: 1px solid #dee2e6; border-radius: 10px; padding: 14px; background: #fff; margin-bottom: 10px; position: relative; }
        .payment-row .row-num { position: absolute; top: -10px; left: 14px; background: #0077b6; color: #fff; border-radius: 20px; padding: 1px 10px; font-size: .72rem; font-weight: 700; }
        .summary-bar { border-radius: 8px; padding: 12px 16px; border: 2px solid #dee2e6; background: #fff; }
        .summary-bar.ok    { border-color: #198754; background: #f0fff4; }
        .summary-bar.over  { border-color: #dc3545; background: #fff5f5; }
        .summary-bar.under { border-color: #ffc107; background: #fffde7; }

        /* Print styles */
        @media print {
            .no-print { display: none !important; }
            .sidebar   { display: none !important; }
            .topnav    { display: none !important; }
            .d-flex    { display: block !important; }
            body       { background: white; }
            .bill-card { box-shadow: none; border: 1px solid #ccc; border-radius: 0; }
            .print-full { max-width: 100% !important; width: 100% !important; }
        }
    </style>
</head>
<body>
<nav class="navbar topnav px-3 no-print">
    <span class="navbar-brand text-white fw-semibold">OceanView Hotel</span>
    <div class="d-flex align-items-center gap-3">
        <span class="text-white small">Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge bg-info text-dark ms-1"><%= user.getRole() %></span></span>
        <a href="<%= ctx %>/logout" class="btn btn-sm btn-outline-light">Logout</a>
    </div>
</nav>
<div class="d-flex">
    <div class="sidebar no-print">
        <div class="lbl">Menu</div>
        <a href="<%= ctx %>/dashboard"    class="nav-link">&#128202; Dashboard</a>
        <a href="<%= ctx %>/reservations" class="nav-link">&#128722; Reservations</a>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Rooms</a>
        <a href="<%= ctx %>/banks"        class="nav-link">&#127974; Banks</a>
        <a href="<%= ctx %>/checkin"      class="nav-link">&#128100; Check-In</a>
        <a href="<%= ctx %>/checkout"     class="nav-link active">&#128198; Check-Out</a>
    </div>

    <div class="flex-grow-1 p-4">

        <% if (errMsg != null) { %>
        <div class="alert alert-danger alert-dismissible fade show no-print">
            <strong>Error:</strong> <%= errMsg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <% } %>

        <% if (res == null) { %>
        <div class="alert alert-warning">No reservation data found. <a href="<%= ctx %>/checkout">Go back</a>.</div>
        <% } else { %>

        <div class="row g-4 justify-content-center">

            <!-- ===================== BILL CARD ===================== -->
            <div class="col-lg-7 print-full">
                <div class="bill-card">

                    <!-- Hotel header -->
                    <div class="bill-hotel-header">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h5>&#127748; OceanView Hotel</h5>
                                <p>Official Guest Receipt &amp; Billing Statement</p>
                            </div>
                            <div class="text-end small" style="opacity:.85">
                                <div>Reservation #<strong><%= res.getReservationId() %></strong></div>
                                <div>Date: <%= today %></div>
                                <div>Issued by: <%= user.getFullName() %></div>
                            </div>
                        </div>
                    </div>
                    <div class="bill-receipt-label">Official Receipt</div>

                    <!-- Guest info bar -->
                    <div class="bill-guest-bar">
                        <div class="row g-2">
                            <div class="col-6 col-md-4">
                                <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Guest Name</div>
                                <div class="fw-bold"><%= res.getGuestName() %></div>
                                <div class="small text-muted"><%= res.getGuestEmail() %></div>
                            </div>
                            <div class="col-6 col-md-2">
                                <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Room</div>
                                <div class="fw-bold">Room <%= res.getRoomNumber() %></div>
                                <div class="small"><%= res.getRoomType() != null ? res.getRoomType().getDisplayName() : "" %></div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Check-In</div>
                                <div class="fw-bold small"><%= res.getCheckInDate() != null ? res.getCheckInDate().format(fmt) : "—" %></div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="text-muted" style="font-size:.72rem;text-transform:uppercase;letter-spacing:.06em">Check-Out</div>
                                <div class="fw-bold small"><%= res.getCheckOutDate() != null ? res.getCheckOutDate().format(fmt) : "—" %></div>
                            </div>
                        </div>
                    </div>

                    <div class="bill-body">

                        <!-- Room charges -->
                        <div class="bill-section-title">&#127963; Room Charges</div>
                        <table class="bill-table">
                            <thead><tr><th>Description</th><th>Nights</th><th class="text-end">Amount</th></tr></thead>
                            <tbody>
                                <tr>
                                    <td>Room <%= res.getRoomNumber() %>
                                        <% if (res.getRoomType() != null) { %>
                                            &mdash; <%= res.getRoomType().getDisplayName() %>
                                        <% } %>
                                    </td>
                                    <td><%= res.getNights() %></td>
                                    <td class="text-end fw-semibold"><%= currency %> <%= String.format("%,.2f", resTotal) %></td>
                                </tr>
                            </tbody>
                            <tfoot>
                                <tr><td colspan="2" class="text-end">Room Subtotal</td>
                                    <td class="text-end"><%= currency %> <%= String.format("%,.2f", resTotal) %></td></tr>
                            </tfoot>
                        </table>

                        <!-- Extra charges -->
                        <% if (extraCharges != null && !extraCharges.isEmpty()) { %>
                        <div class="bill-section-title">&#9888; Extra Charges</div>
                        <table class="bill-table">
                            <thead><tr><th>Type</th><th>Description</th><th class="text-end">Amount</th></tr></thead>
                            <tbody>
                            <% for (ExtraCharge ec : extraCharges) { %>
                                <tr>
                                    <td><span class="badge bg-warning text-dark" style="font-size:.75rem"><%= ec.getChargeType() %></span></td>
                                    <td class="small"><%= ec.getDescription() != null && !ec.getDescription().isBlank() ? ec.getDescription() : "—" %></td>
                                    <td class="text-end text-danger fw-semibold"><%= currency %> <%= String.format("%,.2f", ec.getAmount()) %></td>
                                </tr>
                            <% } %>
                            </tbody>
                            <tfoot>
                                <tr><td colspan="2" class="text-end">Extra Subtotal</td>
                                    <td class="text-end text-danger"><%= currency %> <%= String.format("%,.2f", extraTotal) %></td></tr>
                            </tfoot>
                        </table>
                        <% } %>

                        <hr class="bill-divider">

                        <!-- Totals -->
                        <div class="bill-totals">
                            <table>
                                <tr>
                                    <td class="text-muted">Room Charges</td>
                                    <td class="text-end"><%= currency %> <%= String.format("%,.2f", resTotal) %></td>
                                </tr>
                                <% if (extraTotal > 0) { %>
                                <tr>
                                    <td class="text-muted">Extra Charges</td>
                                    <td class="text-end text-danger"><%= currency %> <%= String.format("%,.2f", extraTotal) %></td>
                                </tr>
                                <% } %>
                                <% if (taxRate > 0) { %>
                                <tr>
                                    <td class="text-muted">Tax (<%= String.format("%.2f", taxRate) %>%)</td>
                                    <td class="text-end"><%= currency %> <%= String.format("%,.2f", taxAmount) %></td>
                                </tr>
                                <% } %>
                                <tr class="bill-grand">
                                    <td>Grand Total</td>
                                    <td class="text-end"><%= currency %> <%= String.format("%,.2f", grandTotalWithTax) %></td>
                                </tr>
                                <tr class="bill-balance" id="billBalanceRow">
                                    <td>Amount Due</td>
                                    <td class="text-end" id="billBalanceAmt"><%= currency %> <%= String.format("%,.2f", grandTotalWithTax) %></td>
                                </tr>
                                <%-- These rows are hidden until JS detects overpayment (cash change) --%>
                                <tr id="billTenderedRow" style="display:none">
                                    <td class="text-muted">Cash Tendered</td>
                                    <td class="text-end fw-semibold" id="billTenderedAmt"></td>
                                </tr>
                                <tr id="billChangeRow" style="display:none"
                                    class="fw-bold" style="color:#198754">
                                    <td style="color:#198754">&#8592; Change to Return</td>
                                    <td class="text-end" style="color:#198754" id="billChangeAmt"></td>
                                </tr>
                            </table>
                        </div>

                        <!-- Footer note -->
                        <div class="text-center text-muted mt-4" style="font-size:.78rem;border-top:1px solid #eee;padding-top:14px">
                            Thank you for staying at OceanView Hotel.<br>
                            This is an official receipt. Please keep for your records.
                        </div>
                    </div>
                </div><!-- /bill-card -->
            </div><!-- /col-lg-7 -->

            <!-- ===================== RIGHT PANEL ===================== -->
            <div class="col-lg-5 no-print">

                <!-- Payment section -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-primary text-white fw-semibold d-flex justify-content-between align-items-center">
                        <span>&#128179; Collect Payment</span>
                        <button type="button" class="btn btn-light btn-sm" onclick="addPaymentRow()">+ Add</button>
                    </div>
                    <div class="card-body">
                        <div class="alert alert-warning small mb-3 py-2">
                            Total due: <strong><%= currency %> <%= String.format("%,.2f", grandTotalWithTax) %></strong>
                            (room charges + extra charges<%= taxRate > 0 ? " + tax" : "" %>).
                        </div>
                        <input type="hidden" id="balanceDueHidden" value="<%= grandTotalWithTax %>">
                        <div id="paymentsContainer"></div>
                        <div class="summary-bar under mt-3" id="summaryBar">
                            <div class="row text-center g-1">
                                <div class="col-4">
                                    <div class="text-muted" style="font-size:.72rem">Total Due</div>
                                    <div class="fw-bold small"><%= currency %> <%= String.format("%,.2f", grandTotalWithTax) %></div>
                                </div>
                                <div class="col-4">
                                    <div class="text-muted" style="font-size:.72rem">Entered</div>
                                    <div class="fw-bold small" id="totalEnteredDisplay"><%= currency %> 0.00</div>
                                </div>
                                <div class="col-4">
                                    <div class="text-muted" style="font-size:.72rem" id="diffLabel">Remaining</div>
                                    <div class="fw-bold small" id="diffDisplay"><%= currency %> <%= String.format("%,.2f", grandTotalWithTax) %></div>
                                </div>
                            </div>
                            <div class="text-center mt-1" style="font-size:.8rem" id="summaryMsg">
                                <span class="text-warning fw-semibold">&#9888; Enter payment amount.</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Action buttons card -->
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-success text-white fw-semibold">&#128198; Confirm Check-Out</div>
                    <div class="card-body">
                        <form method="post" action="<%= ctx %>/checkout" id="confirmForm">
                            <input type="hidden" name="reservationId" value="<%= res.getReservationId() %>">
                            <input type="hidden" name="action" value="confirm">
                            <!-- Payment inputs mirrored here by JS before submit -->
                            <div id="paymentFormInputs"></div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-success btn-lg" id="confirmBtn" disabled>
                                    &#10003; Confirm Check-Out
                                </button>
                                <button type="button" class="btn btn-secondary"
                                        onclick="window.print()">
                                    &#128438; Print Bill
                                </button>
                                <a href="<%= ctx %>/checkout?id=<%= res.getReservationId() %>"
                                   class="btn btn-outline-secondary">
                                    &#8592; Back to Checkout
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

            </div><!-- /right panel -->
        </div><!-- /row -->

        <%-- Payment row template (no JSP EL in JS context — uses __RN__ placeholder) --%>
        <template id="paymentRowTemplate">
            <div class="payment-row" id="prow-__RN__">
                <span class="row-num">Payment __RN__</span>
                <div class="row g-2 align-items-start mt-1">
                    <div class="col-md-4">
                        <label class="form-label fw-semibold small">Method</label>
                        <select name="_pm[]" class="form-select form-select-sm" onchange="onMethodChange(this, __RN__)">
                            <option value="CASH">&#128181; Cash</option>
                            <option value="CARD">&#128179; Card</option>
                            <option value="TRANSFER">&#127974; Transfer</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-semibold small">Amount (<%= currency %>)</label>
                        <input type="number" name="_pa[]" id="pamt-__RN__"
                               class="form-control form-control-sm payment-amount"
                               min="0.01" step="0.01" placeholder="0.00" oninput="updatePaymentSummary()">
                    </div>
                    <div class="col-auto d-flex align-items-end">
                        <button type="button" class="btn btn-outline-danger btn-sm"
                                onclick="removePayRow(__RN__)">&#x2715;</button>
                    </div>
                    <div class="col-md-5 d-none" id="field-bank-__RN__">
                        <label class="form-label fw-semibold small">Bank</label>
                        <select name="_pb[]" id="pbank-__RN__" class="form-select form-select-sm">
                            <option value="">-- Select Bank --</option>
                        </select>
                    </div>
                    <div class="col-md-4 d-none" id="field-card-__RN__">
                        <label class="form-label fw-semibold small">Last 4 Digits</label>
                        <input type="text" name="_pc[]" id="plast4-__RN__"
                               class="form-control form-control-sm" maxlength="4"
                               pattern="\d{4}" placeholder="1234">
                    </div>
                    <div class="col-md-5 d-none" id="field-ref-__RN__">
                        <label class="form-label fw-semibold small">Reference No.</label>
                        <input type="text" name="_pr[]" id="pref-__RN__"
                               class="form-control form-control-sm" placeholder="REF-123">
                    </div>
                    <div class="col-12">
                        <input type="text" name="_pco[]" class="form-control form-control-sm"
                               placeholder="Comment (optional)">
                    </div>
                </div>
            </div>
        </template>

        <script>
        var currencyCode = '<%= currency %>';
        var activeBanks  = [
            <% if (banks != null) { for (int i = 0; i < banks.size(); i++) { Bank b = banks.get(i); %>
                {id: <%= b.getBankId() %>, name: "<%= b.getName().replace("\"","\\\"") %>"}
                <%= i < banks.size()-1 ? "," : "" %>
            <% } } %>
        ];
        var balanceDue   = <%= grandTotalWithTax %>;
        var paymentCount = 0;

        function addPaymentRow(prefill) {
            paymentCount++;
            var rn = paymentCount;

            var tmpl = document.getElementById('paymentRowTemplate');
            var html = tmpl.innerHTML.replace(/__RN__/g, rn);
            var wrap = document.createElement('div');
            wrap.innerHTML = html;
            document.getElementById('paymentsContainer').appendChild(wrap.firstElementChild);

            // Populate bank dropdown
            var bankSel = document.getElementById('pbank-' + rn);
            activeBanks.forEach(function(b) {
                var opt = document.createElement('option');
                opt.value = b.id; opt.textContent = b.name;
                bankSel.appendChild(opt);
            });

            if (prefill) {
                document.getElementById('pamt-' + rn).value = prefill.toFixed(2);
                updatePaymentSummary();
            }
        }

        function onMethodChange(sel, rn) {
            var m = sel.value;
            ['bank','card','ref'].forEach(function(f) {
                var el = document.getElementById('field-' + f + '-' + rn);
                if (el) {
                    el.classList.add('d-none');
                    el.querySelectorAll('input,select').forEach(function(i){ i.value=''; });
                }
            });
            if (m === 'CARD' || m === 'TRANSFER') document.getElementById('field-bank-' + rn).classList.remove('d-none');
            if (m === 'CARD')     document.getElementById('field-card-' + rn).classList.remove('d-none');
            if (m === 'TRANSFER') document.getElementById('field-ref-' + rn).classList.remove('d-none');
        }

        function removePayRow(rn) {
            var el = document.getElementById('prow-' + rn);
            if (el) el.remove();
            updatePaymentSummary();
        }

        function updatePaymentSummary() {
            var entered = 0;
            document.querySelectorAll('.payment-amount').forEach(function(inp) {
                entered += parseFloat(inp.value || 0);
            });

            document.getElementById('totalEnteredDisplay').textContent =
                currencyCode + ' ' + entered.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});

            var diff = balanceDue - entered;
            var bar  = document.getElementById('summaryBar');
            var btn  = document.getElementById('confirmBtn');

            // Hide tendered/change rows by default; shown only when there is change
            document.getElementById('billTenderedRow').style.display = 'none';
            document.getElementById('billChangeRow').style.display   = 'none';

            if (Math.abs(diff) < 0.01) {
                // Exact payment
                bar.className = 'summary-bar ok mt-3';
                document.getElementById('diffLabel').textContent   = 'Balance';
                document.getElementById('diffDisplay').textContent  = currencyCode + ' 0.00';
                document.getElementById('summaryMsg').innerHTML     =
                    '<span class="text-success fw-semibold">&#10003; Exact payment. Ready to confirm.</span>';
                btn.disabled = false;

            } else if (diff > 0) {
                // Underpayment — still need more
                bar.className = 'summary-bar under mt-3';
                document.getElementById('diffLabel').textContent   = 'Still Needed';
                document.getElementById('diffDisplay').textContent  =
                    currencyCode + ' ' + diff.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
                document.getElementById('summaryMsg').innerHTML     =
                    '<span class="text-warning fw-semibold">&#9888; Need ' + currencyCode + ' ' +
                    diff.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' more.</span>';
                btn.disabled = true;

            } else {
                // Overpayment — customer gave more; return change. This is valid.
                var change = Math.abs(diff);
                bar.className = 'summary-bar ok mt-3';
                document.getElementById('diffLabel').textContent   = 'Change';
                document.getElementById('diffDisplay').textContent  =
                    currencyCode + ' ' + change.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
                document.getElementById('summaryMsg').innerHTML     =
                    '<span class="text-success fw-semibold">&#10003; Return change of ' + currencyCode + ' ' +
                    change.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' to guest.</span>';
                btn.disabled = false;  // valid — allow confirm

                // Update the printed bill with tendered amount and change
                document.getElementById('billTenderedRow').style.display = '';
                document.getElementById('billTenderedAmt').textContent   =
                    currencyCode + ' ' + entered.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
                document.getElementById('billChangeRow').style.display   = '';
                document.getElementById('billChangeAmt').textContent     =
                    currencyCode + ' ' + change.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
            }

            syncPaymentInputsToForm();
        }

        // Mirror payment inputs to the confirm form with correct param names
        function syncPaymentInputsToForm() {
            var container = document.getElementById('paymentFormInputs');
            container.innerHTML = '';

            var rows = document.querySelectorAll('#paymentsContainer .payment-row');
            rows.forEach(function(row) {
                function copyVal(srcName, destName) {
                    var src = row.querySelector('[name="' + srcName + '"]');
                    if (!src) return;
                    var inp = document.createElement('input');
                    inp.type = 'hidden';
                    inp.name = destName;
                    inp.value = src.value;
                    container.appendChild(inp);
                }
                copyVal('_pm[]',  'paymentMethod[]');
                copyVal('_pa[]',  'paymentAmount[]');
                copyVal('_pb[]',  'paymentBankId[]');
                copyVal('_pc[]',  'paymentCardLast4[]');
                copyVal('_pr[]',  'paymentReference[]');
                copyVal('_pco[]', 'paymentComment[]');
            });
        }

        window.addEventListener('DOMContentLoaded', function() {
            addPaymentRow(balanceDue);
        });
        </script>

        <% } // end if res != null %>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
