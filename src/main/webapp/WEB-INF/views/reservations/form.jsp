<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User, oceanview.model.Reservation,
                 oceanview.model.Room, oceanview.model.ReservationStatus,
                 oceanview.model.AppSettings, java.util.List" %>
<%
    User user          = (User)       session.getAttribute("loggedInUser");
    Reservation res    = (Reservation) request.getAttribute("reservation");
    List<Room>  rooms  = (List<Room>) request.getAttribute("rooms");
    String errMsg      = (String)     request.getAttribute("errorMessage");
    boolean isEdit     = (res != null);
    String pageTitle   = isEdit ? "Edit Reservation #" + res.getReservationId() : "New Reservation";
    String ctx         = request.getContextPath();

    // Pre-fill values for edit mode
    String vGuestName  = isEdit ? res.getGuestName()    : "";
    String vGuestEmail = isEdit ? res.getGuestEmail()   : "";
    String vGuestPhone = isEdit && res.getGuestPhone()  != null ? res.getGuestPhone() : "";
    String vGuests     = isEdit ? String.valueOf(res.getNumberOfGuests()) : "1";
    String vAmount     = isEdit ? String.format("%.2f", res.getTotalAmount()) : "0.00";
    String vSpecial    = isEdit && res.getSpecialRequests() != null ? res.getSpecialRequests() : "";
    String vCheckIn    = isEdit && res.getCheckInDate()  != null ? res.getCheckInDate().toString()  : "";
    String vCheckOut   = isEdit && res.getCheckOutDate() != null ? res.getCheckOutDate().toString() : "";
    int    vRoomNumber = isEdit ? res.getRoomNumber() : 0;
    String vRoomType   = isEdit && res.getRoomType() != null ? res.getRoomType().name() : "";
    String currency    = AppSettings.getCurrency();
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
        .sidebar .nav-label { font-size:.7rem; text-transform:uppercase; letter-spacing:.1em; color:#90e0ef; padding:16px 16px 4px; }
        .sidebar .nav-link  { color:#caf0f8; font-size:.9rem; padding:10px 16px; border-radius:0; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background:#0077b6; color:#fff; }
        .topnav { background: #0077b6; }
        .room-info-box { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 8px; padding: 14px 16px; }
        .price-highlight { font-size: 1.5rem; font-weight: 700; color: #0077b6; }
        .total-box { background: #e8f4fd; border: 2px solid #0077b6; border-radius: 8px; padding: 14px 16px; }
        .total-box .total-amount { font-size: 1.8rem; font-weight: 700; color: #023e8a; }
        .section-title { font-size:.72rem; text-transform:uppercase; letter-spacing:.08em;
                         color:#888; padding-bottom:6px; border-bottom:1px solid #dee2e6; margin-bottom:14px; }
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
        <a href="<%= ctx %>/reservations" class="nav-link active">&#128722; Reservations</a>
        <% if (user.isAdmin()) { %>
        <a href="<%= ctx %>/rooms"        class="nav-link">&#127963; Room Management</a>
        <a href="#"                        class="nav-link">&#128100; Manage Users</a>
        <% } %>
    </div>

    <div class="flex-grow-1 p-4">
        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="<%= ctx %>/reservations">Reservations</a></li>
                <li class="breadcrumb-item active"><%= pageTitle %></li>
            </ol>
        </nav>

        <% if (errMsg != null) { %>
            <div class="alert alert-danger alert-dismissible fade show">
                <strong>Error:</strong> <%= errMsg %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>

        <form method="post" action="<%= ctx %>/reservations">
            <% if (isEdit) { %>
                <input type="hidden" name="action"        value="update">
                <input type="hidden" name="reservationId" value="<%= res.getReservationId() %>">
            <% } else { %>
                <input type="hidden" name="action" value="create">
            <% } %>
            <!-- Hidden fields populated by JavaScript when room is selected -->
            <input type="hidden" id="roomNumber" name="roomNumber" value="<%= vRoomNumber %>">
            <input type="hidden" id="roomType"   name="roomType"   value="<%= vRoomType %>">

            <div class="row g-4">

                <!-- LEFT COLUMN -->
                <div class="col-lg-7">

                    <!-- Guest Information -->
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-body">
                            <div class="section-title">&#128100; Guest Information</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                                    <input type="text" name="guestName" class="form-control"
                                           value="<%= vGuestName %>" required placeholder="e.g. Maria Santos">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Email Address <span class="text-danger">*</span></label>
                                    <input type="email" name="guestEmail" class="form-control"
                                           value="<%= vGuestEmail %>" required placeholder="guest@email.com">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Phone Number</label>
                                    <input type="tel" name="guestPhone" class="form-control"
                                           value="<%= vGuestPhone %>" placeholder="09XXXXXXXXX">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Number of Guests <span class="text-danger">*</span></label>
                                    <input type="number" name="numberOfGuests" id="numberOfGuests"
                                           class="form-control" value="<%= vGuests %>"
                                           min="1" max="10" required>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Room & Dates -->
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-body">
                            <div class="section-title">&#127963; Room &amp; Dates</div>

                            <!-- Room Selector -->
                            <div class="mb-3">
                                <label class="form-label fw-semibold">
                                    Select Room <span class="text-danger">*</span>
                                </label>
                                <% if (rooms == null || rooms.isEmpty()) { %>
                                    <div class="alert alert-warning py-2 mb-0">
                                        No available rooms found.
                                        <% if (user.isAdmin()) { %>
                                            <a href="<%= ctx %>/rooms?action=new">Add rooms here.</a>
                                        <% } %>
                                    </div>
                                <% } else { %>
                                <select class="form-select" id="roomSelect" onchange="onRoomChange(this)" required>
                                    <option value="">-- Select a Room --</option>
                                    <% for (Room r : rooms) { %>
                                        <option value="<%= r.getRoomId() %>"
                                                data-number="<%= r.getRoomNumber() %>"
                                                data-type="<%= r.getRoomType() != null ? r.getRoomType().name() : "" %>"
                                                data-type-display="<%= r.getRoomType() != null ? r.getRoomType().getDisplayName() : "" %>"
                                                data-price="<%= r.getPricePerNight() %>"
                                                data-floor="<%= r.getFloor() %>"
                                                data-max-guests="<%= r.getRoomType() != null ? r.getRoomType().getMaxGuests() : 10 %>"
                                                <%= (vRoomNumber > 0 && r.getRoomNumber() == vRoomNumber) ? "selected" : "" %>>
                                            Room <%= r.getRoomNumber() %>
                                            &mdash; <%= r.getRoomType() != null ? r.getRoomType().getDisplayName() : "" %>
                                            | Floor <%= r.getFloor() %>
                                            | <%= currency %> <%= String.format("%,.2f", r.getPricePerNight()) %>/night
                                        </option>
                                    <% } %>
                                </select>
                                <% } %>
                            </div>

                            <!-- Room Info display (shown after room is selected) -->
                            <div id="roomInfoBox" class="room-info-box mb-3"
                                 style="display:<%= (vRoomNumber > 0) ? "block" : "none" %>">
                                <div class="row text-center g-2">
                                    <div class="col-4">
                                        <div class="text-muted small">Room Type</div>
                                        <div class="fw-bold" id="roomTypeDisplay">
                                            <%= (isEdit && res.getRoomType() != null) ? res.getRoomType().getDisplayName() : "" %>
                                        </div>
                                    </div>
                                    <div class="col-4">
                                        <div class="text-muted small">Price / Night</div>
                                        <div class="fw-bold text-success" id="priceDisplay">
                                            <%= isEdit ? currency + " " + String.format("%,.2f", res.getTotalAmount() / Math.max(res.getNights(),1)) : "" %>
                                        </div>
                                    </div>
                                    <div class="col-4">
                                        <div class="text-muted small">Floor</div>
                                        <div class="fw-bold" id="floorDisplay"></div>
                                    </div>
                                </div>
                            </div>

                            <!-- Dates -->
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Check-In Date <span class="text-danger">*</span></label>
                                    <input type="date" name="checkInDate" id="checkInDate"
                                           class="form-control" value="<%= vCheckIn %>" required
                                           onchange="calculateTotal()">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Check-Out Date <span class="text-danger">*</span></label>
                                    <input type="date" name="checkOutDate" id="checkOutDate"
                                           class="form-control" value="<%= vCheckOut %>" required
                                           onchange="calculateTotal()">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Special Requests -->
                    <div class="card shadow-sm border-0">
                        <div class="card-body">
                            <div class="section-title">&#128203; Special Requests</div>
                            <textarea name="specialRequests" class="form-control" rows="3"
                                      placeholder="e.g. Non-smoking room, late check-in, extra bed..."><%= vSpecial %></textarea>
                        </div>
                    </div>
                </div>

                <!-- RIGHT COLUMN — pricing summary -->
                <div class="col-lg-5">
                    <div class="card shadow-sm border-0 sticky-top" style="top:16px">
                        <div class="card-header bg-primary text-white fw-semibold">
                            &#128179; Booking Summary
                        </div>
                        <div class="card-body">
                            <table class="table table-sm table-borderless mb-3">
                                <tr>
                                    <td class="text-muted">Room</td>
                                    <td class="text-end fw-semibold" id="summaryRoom">—</td>
                                </tr>
                                <tr>
                                    <td class="text-muted">Type</td>
                                    <td class="text-end" id="summaryType">—</td>
                                </tr>
                                <tr>
                                    <td class="text-muted">Price / Night</td>
                                    <td class="text-end text-success fw-semibold" id="summaryPrice">—</td>
                                </tr>
                                <tr>
                                    <td class="text-muted">Nights</td>
                                    <td class="text-end" id="summaryNights">—</td>
                                </tr>
                            </table>

                            <div class="total-box text-center mb-3">
                                <div class="text-muted small mb-1">Total Amount</div>
                                <div class="total-amount" id="summaryTotal">
                                    <%= currency %> <%= String.format("%,.2f", isEdit ? res.getTotalAmount() : 0.00) %>
                                </div>
                            </div>

                            <!-- Hidden total amount submitted with form -->
                            <input type="hidden" name="totalAmount" id="totalAmountHidden"
                                   value="<%= vAmount %>">

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">
                                    <%= isEdit ? "&#10003; Update Reservation" : "&#10003; Confirm Reservation" %>
                                </button>
                                <a href="<%= ctx %>/reservations" class="btn btn-outline-secondary">Cancel</a>
                            </div>
                        </div>
                    </div>
                </div>

            </div><!-- /row -->
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    var currentPrice = 0;
    var currencyCode = '<%= currency %>';

    // Called when room dropdown changes
    function onRoomChange(sel) {
        var opt = sel.options[sel.selectedIndex];
        if (!opt.value) { resetRoomInfo(); return; }

        var number    = opt.dataset.number      || '';
        var type      = opt.dataset.type        || '';
        var typeDisp  = opt.dataset.typeDisplay || '';
        var price     = parseFloat(opt.dataset.price || 0);
        var floor     = opt.dataset.floor       || '';
        var maxGuests = parseInt(opt.dataset.maxGuests || 10);

        currentPrice = price;

        // Populate hidden inputs (these get submitted with the form)
        document.getElementById('roomNumber').value = number;
        document.getElementById('roomType').value   = type;

        // Update display info box
        document.getElementById('roomTypeDisplay').textContent = typeDisp;
        document.getElementById('priceDisplay').textContent    = currencyCode + ' ' + price.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
        document.getElementById('floorDisplay').textContent    = 'Floor ' + floor;
        document.getElementById('roomInfoBox').style.display   = 'block';

        // Update summary panel
        document.getElementById('summaryRoom').textContent  = 'Room ' + number;
        document.getElementById('summaryType').textContent  = typeDisp;
        document.getElementById('summaryPrice').textContent = currencyCode + ' ' + price.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});

        // Update max guests hint
        var guestsInput = document.getElementById('numberOfGuests');
        guestsInput.max = maxGuests;
        if (parseInt(guestsInput.value) > maxGuests) guestsInput.value = maxGuests;

        calculateTotal();
    }

    function resetRoomInfo() {
        currentPrice = 0;
        document.getElementById('roomNumber').value = '';
        document.getElementById('roomType').value   = '';
        document.getElementById('roomInfoBox').style.display = 'none';
        document.getElementById('summaryRoom').textContent  = '—';
        document.getElementById('summaryType').textContent  = '—';
        document.getElementById('summaryPrice').textContent = '—';
        document.getElementById('summaryNights').textContent= '—';
        document.getElementById('summaryTotal').textContent = currencyCode + ' 0.00';
        document.getElementById('totalAmountHidden').value  = '0.00';
    }

    // Called when dates change
    function calculateTotal() {
        var checkIn  = document.getElementById('checkInDate').value;
        var checkOut = document.getElementById('checkOutDate').value;

        if (!checkIn || !checkOut || currentPrice <= 0) {
            document.getElementById('summaryNights').textContent = '—';
            document.getElementById('summaryTotal').textContent  = currencyCode + ' 0.00';
            document.getElementById('totalAmountHidden').value   = '0.00';
            return;
        }

        var nights = Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000);
        if (nights <= 0) {
            document.getElementById('summaryNights').textContent = 'Invalid dates';
            return;
        }

        var total = nights * currentPrice;
        document.getElementById('summaryNights').textContent = nights + (nights === 1 ? ' night' : ' nights');
        document.getElementById('summaryTotal').textContent  = currencyCode + ' ' + total.toLocaleString('en', {minimumFractionDigits:2, maximumFractionDigits:2});
        document.getElementById('totalAmountHidden').value   = total.toFixed(2);
    }

    // Date constraints
    var today = new Date().toISOString().split('T')[0];
    document.getElementById('checkInDate').min = today;
    document.getElementById('checkInDate').addEventListener('change', function () {
        document.getElementById('checkOutDate').min = this.value;
        if (document.getElementById('checkOutDate').value &&
            document.getElementById('checkOutDate').value <= this.value) {
            document.getElementById('checkOutDate').value = '';
        }
        calculateTotal();
    });

    // On page load: if editing, trigger room select to restore summary
    window.addEventListener('DOMContentLoaded', function () {
        var sel = document.getElementById('roomSelect');
        if (sel && sel.value) onRoomChange(sel);
        else calculateTotal();
    });
</script>

</body>
</html>
