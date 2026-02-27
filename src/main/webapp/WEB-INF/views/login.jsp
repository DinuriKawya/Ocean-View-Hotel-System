<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OceanView Hotel &mdash; Login</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #0077b6, #00b4d8);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .card {
            background: #fff;
            border-radius: 12px;
            padding: 40px 36px;
            width: 100%;
            max-width: 400px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.2);
        }
        .logo {
            text-align: center;
            margin-bottom: 28px;
        }
        .logo h1 { color: #0077b6; font-size: 1.8rem; }
        .logo p  { color: #666; font-size: 0.85rem; margin-top: 4px; }
        label { display: block; font-size: 0.85rem; color: #444; margin-bottom: 4px; }
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 0.95rem;
            margin-bottom: 16px;
            transition: border-color 0.2s;
        }
        input:focus { outline: none; border-color: #0077b6; }
        .btn-login {
            width: 100%;
            padding: 12px;
            background: #0077b6;
            color: #fff;
            border: none;
            border-radius: 6px;
            font-size: 1rem;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-login:hover { background: #005f99; }
        .error {
            background: #fde8e8;
            color: #c0392b;
            border: 1px solid #e74c3c;
            border-radius: 6px;
            padding: 10px 14px;
            font-size: 0.88rem;
            margin-bottom: 16px;
        }
    </style>
</head>
<body>
<div class="card">
    <div class="logo">
        <h1>OceanView Hotel</h1>
        <p>Staff Portal &mdash; Please sign in</p>
    </div>

    <% String error = (String) request.getAttribute("errorMessage"); %>
    <% if (error != null) { %>
        <div class="error"><%= error %></div>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <label for="username">Username</label>
        <input type="text" id="username" name="username"
               value=""
               placeholder="Enter your username" required autofocus>

        <label for="password">Password</label>
        <input type="password" id="password" name="password"
               placeholder="Enter your password" required>

        <button type="submit" class="btn-login">Sign In</button>
    </form>
</div>
</body>
</html>
