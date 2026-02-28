<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User" %>
<%
    User user = (User) session.getAttribute("loggedInUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard &mdash; OceanView Hotel</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', sans-serif; background: #f0f4f8; color: #333; }

        /* ---- Top nav ---- */
        nav {
            background: #1b263b;
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 14px 28px;
        }
        nav .brand { font-size: 1.2rem; font-weight: 600; }
        nav .user-info { font-size: 0.9rem; }
        nav .user-info span { margin-right: 12px; }
        nav a.logout {
            color: #fff;
            text-decoration: none;
            background: rgba(255,255,255,0.2);
            padding: 6px 14px;
            border-radius: 4px;
            font-size: 0.85rem;
        }
        nav a.logout:hover { background: rgba(255,255,255,0.35); }

        /* ---- Role badge ---- */
        .badge {
            display: inline-block;
            background: #e63946;
            color: #fff;
            font-size: 0.72rem;
            font-weight: 700;
            padding: 2px 8px;
            border-radius: 20px;
            letter-spacing: 0.05em;
            text-transform: uppercase;
            margin-left: 6px;
        }

        /* ---- Layout ---- */
        .wrapper { display: flex; min-height: calc(100vh - 52px); }

        /* ---- Sidebar ---- */
        aside {
            width: 220px;
            background: #0d1b2a;
            padding: 24px 0;
            flex-shrink: 0;
        }
        aside h3 {
            color: #778da9;
            font-size: 0.72rem;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            padding: 0 20px 10px;
        }
        aside a {
            display: block;
            color: #e0e1dd;
            text-decoration: none;
            padding: 11px 20px;
            font-size: 0.9rem;
            transition: background 0.15s;
        }
        aside a:hover { background: #1b263b; }
        aside .section { margin-top: 18px; }

        /* ---- Main content ---- */
        main { flex: 1; padding: 30px 36px; }
        main h2 { font-size: 1.4rem; margin-bottom: 6px; color: #1b263b; }
        main p.sub { color: #666; font-size: 0.88rem; margin-bottom: 28px; }

        /* ---- Cards ---- */
        .cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 18px; }
        .card {
            background: #fff;
            border-radius: 10px;
            padding: 24px 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            text-align: center;
        }
        .card .icon { font-size: 2rem; margin-bottom: 10px; }
        .card h4 { font-size: 0.95rem; color: #1b263b; }
        .card.admin-only { border-top: 3px solid #e63946; }
        .card.admin-only h4 { color: #e63946; }
    </style>
</head>
<body>

<nav>
    <span class="brand">OceanView Hotel &mdash; Admin</span>
    <div class="user-info">
        <span>Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge"><%= user.getRole() %></span>
        </span>
        <a class="logout" href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<div class="wrapper">
    <aside>
        <!-- Staff operations (also visible to admin) -->
        <h3>Operations</h3>
        <a href="${pageContext.request.contextPath}/reservations">&#128722; Reservations</a>
        <a href="${pageContext.request.contextPath}/rooms">&#127963; Room Status</a>
        <a href="${pageContext.request.contextPath}/checkin">&#128100; Check-In</a>
        <a href="${pageContext.request.contextPath}/checkout">&#128198; Check-Out</a>

        <!-- Admin-only section -->
        <div class="section">
            <h3>&#128274; Admin Only</h3>
            <a href="${pageContext.request.contextPath}/users">&#128100; Manage Users</a>
            <a href="${pageContext.request.contextPath}/banks">&#127974; Bank Management</a>
            <a href="${pageContext.request.contextPath}/reports">&#128202; Reports &amp; Analytics</a>
            <a href="${pageContext.request.contextPath}/rooms">&#128188; Room Management</a>
            <a href="${pageContext.request.contextPath}/billing">&#128179; Billing &amp; Revenue</a>
            <a href="${pageContext.request.contextPath}/audit">&#128196; Audit Logs</a>
            <a href="${pageContext.request.contextPath}/settings">&#9881; System Settings</a>
        </div>
    </aside>

    <main>
        <h2>Admin Dashboard</h2>
        <p class="sub">You have full administrative access. Manage all hotel operations and settings below.</p>

        <div class="cards">
            <!-- Shared with staff -->
            <div class="card">
                <div class="icon">&#128722;</div>
                <h4>Reservations</h4>
            </div>
            <div class="card">
                <div class="icon">&#127963;</div>
                <h4>Room Status</h4>
            </div>
            <div class="card">
                <div class="icon">&#128106;</div>
                <h4>Check-In / Out</h4>
            </div>

            <!-- Admin-only cards -->
            <div class="card admin-only">
                <div class="icon">&#128100;</div>
                <h4>Manage Users</h4>
            </div>
            <div class="card admin-only">
                <div class="icon">&#128202;</div>
                <h4>Reports</h4>
            </div>
            <div class="card admin-only" onclick="location.href='${pageContext.request.contextPath}/billing'" style="cursor:pointer">
                <div class="icon">&#128179;</div>
                <h4>Billing</h4>
            </div>
            <div class="card admin-only">
                <div class="icon">&#128196;</div>
                <h4>Audit Logs</h4>
            </div>
            <div class="card admin-only" onclick="location.href='${pageContext.request.contextPath}/settings'" style="cursor:pointer">
                <div class="icon">&#9881;</div>
                <h4>Settings</h4>
            </div>
        </div>
    </main>
</div>

</body>
</html>
