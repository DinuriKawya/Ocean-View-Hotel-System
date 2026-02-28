<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="oceanview.model.User" %>
<%
    User user = (User) session.getAttribute("loggedInUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Staff Dashboard &mdash; OceanView Hotel</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', sans-serif; background: #f0f4f8; color: #333; }

        /* ---- Top nav ---- */
        nav {
            background: #0077b6;
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
            background: #48cae4;
            color: #003049;
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
            background: #023e8a;
            padding: 24px 0;
            flex-shrink: 0;
        }
        aside h3 {
            color: #90e0ef;
            font-size: 0.72rem;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            padding: 0 20px 10px;
        }
        aside a {
            display: block;
            color: #caf0f8;
            text-decoration: none;
            padding: 11px 20px;
            font-size: 0.9rem;
            transition: background 0.15s;
        }
        aside a:hover { background: #0077b6; }

        /* ---- Main content ---- */
        main { flex: 1; padding: 30px 36px; }
        main h2 { font-size: 1.4rem; margin-bottom: 6px; color: #023e8a; }
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
        .card h4 { font-size: 0.95rem; color: #0077b6; }
    </style>
</head>
<body>

<nav>
    <span class="brand">OceanView Hotel</span>
    <div class="user-info">
        <span>Welcome, <strong><%= user.getFullName() %></strong>
            <span class="badge"><%= user.getRole() %></span>
        </span>
        <a class="logout" href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<div class="wrapper">
    <aside>
        <h3>Staff Menu</h3>
        <a href="${pageContext.request.contextPath}/reservations">&#128722; Reservations</a>
        <a href="#">&#127963; Room Status</a>
        <a href="${pageContext.request.contextPath}/checkin">&#128100; Guest Check-In</a>
        <a href="${pageContext.request.contextPath}/checkout">&#128198; Guest Check-Out</a>
        <a href="#">&#128205; Housekeeping</a>
        <a href="#">&#128222; Front Desk</a>
    </aside>

    <main>
        <h2>Staff Dashboard</h2>
        <p class="sub">You are logged in as Staff. Manage daily hotel operations below.</p>

        <div class="cards">
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
            <div class="card">
                <div class="icon">&#128205;</div>
                <h4>Housekeeping</h4>
            </div>
        </div>
    </main>
</div>

</body>
</html>
