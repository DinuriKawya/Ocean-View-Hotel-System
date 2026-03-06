<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>System Help - OceanView Hotel</title>

<style>
body{
    font-family:Segoe UI;
    background:#f4f6f9;
    padding:40px;
}

.container{
    max-width:900px;
    margin:auto;
    background:white;
    padding:30px;
    border-radius:8px;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
}

h1{
    color:#1b263b;
    margin-bottom:20px;
}

.rule{
    margin-bottom:18px;
}

.rule h3{
    color:#1b263b;
}

.rule p{
    color:#555;
}
</style>

</head>
<body>

<div class="container">

<h1>System Help & Staff Rules</h1>

<div class="rule">
<h3>Reservations</h3>
<p>Staff must verify guest information before creating a reservation.</p>
</div>

<div class="rule">
<h3>Check-In</h3>
<p>Only reservations with CONFIRMED status can be checked in.</p>
</div>

<div class="rule">
<h3>Check-Out</h3>
<p>Ensure all charges are added before completing checkout.</p>
</div>

<div class="rule">
<h3>Room Management</h3>
<p>Rooms marked as MAINTENANCE cannot be reserved.</p>
</div>

<div class="rule">
<h3>Billing</h3>
<p>All invoices must be verified before printing or sending to guests.</p>
</div>

<div class="rule">
<h3>Security</h3>
<p>Staff accounts must never be shared. All actions are logged in the audit system.</p>
</div>

</div>

</body>
</html>