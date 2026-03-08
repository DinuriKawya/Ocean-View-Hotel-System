# OceanView Hotel Management System Overview

A full-featured hotel management web application built with Java Servlets, JSP, and MySQL. Designed for hotel staff and administrators to manage reservations, rooms, billing, payments, and operations from a single dashboard.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Arhcitecture](#architecture)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Default Login Credentials](#default-login-credentials)
- [Module Overview](#module-overview)
- [Role-Based Access](#role-based-access)
- [Email Configuration](#email-configuration)
- [Design Patterns](#design-patterns)
- [Screenshots](#screenshots)

---

## Features

### 🔐 Authentication & Security
- Secure login/logout with HTTP session management
- SHA-256 password hashing stored in the database
- `AuthFilter` servlet filter that guards all routes and redirects unauthenticated users to `/login`
- Role-based permissions enforced at the servlet level

### 👥 Role-Based Access Control
- Two roles: **Admin** and **Staff**
- Admins have full access to all modules
- Staff are restricted from sensitive administrative features (rooms, users, audit, settings)

### 📅 Reservations
- Full CRUD: create, view, edit, and cancel reservations
- Status lifecycle: `PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT / CANCELLED / NO_SHOW`
- Fields include guest name, email, phone, room assignment, dates, guest count, and special requests
- Date validation enforced at both DB constraint and service layer

### 🛏️ Room Management
- Manage room inventory: number, type, floor, pricing, and description
- Room types: `STANDARD`, `DELUXE`, `SUITE`, `FAMILY`, `PENTHOUSE`
- Room statuses: `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`, `OUT_OF_ORDER`
- Prevents assigning rooms that are not available

### ✅ Check-In / Check-Out
- One-click check-in for confirmed reservations
- Check-out wizard to finalize the stay, review charges, and generate the final bill
- Extra charges (room service, minibar, etc.) can be added during checkout

### 💰 Billing
- Generate detailed invoices and folios per reservation
- Revenue dashboard with summary statistics
- Tax rate configurable from the Settings panel and automatically applied to invoices

### 💳 Payments
- Multi-method payment support: **Cash**, **Card**, **Bank Transfer**
- Card payments capture last 4 digits of card
- Bank transfers link to a managed bank list and capture a reference number
- Multiple partial payments supported per reservation
- Comments field for payment notes

### 📊 Reports
- Staff performance reports
- Room occupancy reports
- Payment summary reports
- Export to **PDF** and **Excel** formats

### 🏦 Banks
- Manage the list of banks available in payment method dropdowns
- Activate/deactivate banks without deleting them

### 👤 User Management *(Admin only)*
- Create, edit, and deactivate system user accounts
- Assign roles (Admin / Staff)
- Prevents deletion of active users to preserve audit integrity

### 🗂️ Audit Logs *(Admin only)*
- Full action trail of all system events (create, update, delete, login, logout)
- Stores action type, table name, record ID, performing user, and IP address
- Filterable by user, action type, and date range
- Indexed for fast querying

### ⚙️ System Settings *(Admin only)*
- Configure hotel name, address, phone, currency, and tax rate from the UI
- Settings are stored in the `system_settings` table and loaded at application startup via `SettingsListener`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Runtime | Apache Tomcat 11.0 |
| Servlet API | Jakarta EE 5.0 |
| Database | MySQL 8.x |
| JDBC Driver | mysql-connector-j 9.6.0 |
| Frontend | Bootstrap 5.3.3, HTML5, Vanilla JS |
| IDE | Eclipse IDE with WTP (Web Tools Platform) |

---

## Project Structure

```
oceanview/
├── build/                          # Compiled bytecode (git-ignored)
└── src/
    └── main/
        ├── java/oceanview/
        │   ├── Audit/              # AuditLogger — append-only action trail for all state changes
        │   ├── dao/                # Data Access Objects — all SQL via PreparedStatement
        │   ├── database/           # DBConnection — JDBC connection singleton/factory
        │   ├── factory/            # Factory pattern — object creation abstraction
        │   ├── filter/             # AuthFilter — Jakarta EE servlet filter, session guard
        │   ├── FilterPattern/      # Additional filter pattern implementations
        │   ├── listener/           # SettingsListener — loads system_settings at app startup
        │   ├── model/              # POJO entity classes (User, Reservation, Room, Payment, ...)
        │   ├── service/            # Business logic and validation layer
        │   ├── servlet/            # HTTP request handlers (one Front Controller per module)
        │   ├── strategy/           # Strategy pattern — swappable business logic (e.g. payment methods)
        │   ├── test/               # Unit / integration tests
        │   └── email.properties    # Email configuration properties
        └── webapp/
            ├── META-INF/
            └── WEB-INF/
                ├── lib/            # mysql-connector-j JAR (bundled)
                ├── sql/            # Database creation and seed scripts
                └── views/          # JSP views, organized by feature module
                    ├── audit/
                    ├── banks/
                    ├── billing/
                    ├── checkin/
                    ├── checkout/
                    ├── billing/
                    ├── banks/
                    ├── users/
                    ├── audit/
                    ├── reports/
                    └── settings/

---
## Database Setup

### 1. Create the database

```sql
CREATE DATABASE oceanview_db;
USE oceanview_db;
```

### 2. Create tables

Run the following queries in order:

#### users
```sql
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('STAFF', 'ADMIN') NOT NULL DEFAULT 'STAFF',
    status        ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### rooms
```sql
CREATE TABLE IF NOT EXISTS rooms (
    room_id         INT AUTO_INCREMENT PRIMARY KEY,
    room_number     INT          NOT NULL UNIQUE,
    room_type       ENUM('STANDARD','DELUXE','SUITE','FAMILY','PENTHOUSE') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    status          ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','OUT_OF_ORDER') NOT NULL DEFAULT 'AVAILABLE',
    floor           INT          NOT NULL DEFAULT 1,
    description     TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### reservations
```sql
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id   INT AUTO_INCREMENT PRIMARY KEY,
    guest_name       VARCHAR(100) NOT NULL,
    guest_email      VARCHAR(100) NOT NULL,
    guest_phone      VARCHAR(20),
    room_number      INT          NOT NULL,
    room_type        ENUM('STANDARD','DELUXE','SUITE','FAMILY','PENTHOUSE') NOT NULL,
    check_in_date    DATE         NOT NULL,
    check_out_date   DATE         NOT NULL,
    number_of_guests INT          NOT NULL DEFAULT 1,
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status           ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'PENDING',
    special_requests TEXT,
    created_by       VARCHAR(50),
    created_at       DATE         NOT NULL,
    CONSTRAINT chk_dates  CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_guests CHECK (number_of_guests >= 1)
);
```

#### banks
```sql
CREATE TABLE IF NOT EXISTS banks (
    bank_id    INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    is_active  TINYINT(1)   NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### payments
```sql
CREATE TABLE IF NOT EXISTS payments (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT           NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    method         ENUM('CASH','CARD','TRANSFER') NOT NULL,
    bank_id        INT,
    bank_name      VARCHAR(100),
    card_last4     CHAR(4),
    reference_no   VARCHAR(100),
    comment        TEXT,
    created_by     VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    FOREIGN KEY (bank_id)        REFERENCES banks(bank_id) ON DELETE SET NULL
);
```

#### extra_charges
```sql
CREATE TABLE IF NOT EXISTS extra_charges (
    charge_id      INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT           NOT NULL,
    charge_type    VARCHAR(50)   NOT NULL DEFAULT 'Other',
    description    VARCHAR(255),
    amount         DECIMAL(10,2) NOT NULL,
    added_by       VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);
```

#### audit_log
```sql
CREATE TABLE IF NOT EXISTS audit_log (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    action       VARCHAR(50)  NOT NULL,
    table_name   VARCHAR(50)  NOT NULL DEFAULT '',
    record_id    INT          NOT NULL DEFAULT 0,
    performed_by VARCHAR(50)  NOT NULL DEFAULT '',
    ip_address   VARCHAR(45)  NOT NULL DEFAULT '',
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_action       (action),
    INDEX idx_audit_performed_by (performed_by),
    INDEX idx_audit_created_at   (created_at)
);
```

#### system_settings
```sql
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key   VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(500) NOT NULL,
    description   VARCHAR(255),
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO system_settings VALUES
    ('currency',      'LKR',                             'Currency code shown in the UI',   NOW()),
    ('hotel_name',    'OceanView Hotel',                  'Hotel display name',              NOW()),
    ('hotel_address', '123 Coastal Avenue, Seaside City', 'Hotel address for invoices',      NOW()),
    ('hotel_phone',   '+94 11 234 5678',                  'Hotel phone number for invoices', NOW()),
    ('tax_rate',      '0',                                'Tax % applied on invoices/bills', NOW());
```

---

## Configuration

### Database credentials

Edit `src/main/java/oceanview/database/DBConnection.java`:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/oceanview_db";
private static final String USER = "root";
private static final String PASS = "your_password";
```

> Update `USER` and `PASS` to match your local MySQL credentials.

### System settings

After the app starts, log in as admin and go to **Settings** (`/settings`) to configure:

| Setting | Default Value |
|---------|--------------|
| Hotel Name | OceanView Hotel |
| Hotel Address | 123 Coastal Avenue, Seaside City |
| Hotel Phone | +94 11 234 5678 |
| Currency | LKR |
| Tax Rate | 0% |

These are stored in the `system_settings` table and loaded at startup by `SettingsListener`.

---

## Running the Application

### Prerequisites

- Java 21 JDK
- Apache Tomcat 11.0
- MySQL 8.x
- Eclipse IDE with WTP (or any servlet container)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/ishira-web/ovr_reservation_system.git
   ```

2. **Import into Eclipse**
   - File → Import → Existing Projects into Workspace
   - Select the cloned folder

3. **Set up the database**
   - Create `oceanview_db` in MySQL
   - Run all SQL scripts from `src/main/webapp/WEB-INF/sql/`

4. **Update DB credentials**
   - Edit `DBConnection.java` with your MySQL username and password

5. **Add Tomcat to Eclipse**
   - Window → Preferences → Server → Runtime Environments → Add → Apache Tomcat 11.0

6. **Deploy and run**
   - Right-click project → Run As → Run on Server
   - Access at: `http://localhost:8080/oceanview`

---

## Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Staff | `staff1` | `staff123` |

> Passwords are hashed using SHA-256 in the database.

---

## Module Overview

| URL | Module | Description |
|-----|--------|-------------|
| `/login` | Login | Authenticate users |
| `/dashboard` | Dashboard | Role-aware home screen |
| `/reservations` | Reservations | Create, view, edit, cancel bookings |
| `/rooms` | Rooms | Manage room types, pricing, status |
| `/checkin` | Check-In | Mark reservation as checked in |
| `/checkout` | Check-Out | Process departure, finalize bill |
| `/billing` | Billing | Invoices, folios, revenue dashboard |
| `/reports` | Reports | Staff/room/payment reports, export |
| `/banks` | Banks | Manage banks for payment methods |
| `/users` | Users | User account management |
| `/audit` | Audit Logs | View system action history |
| `/settings` | Settings | Configure hotel and app settings |

---

## Role-Based Access

| Feature | Admin | Staff |
|---------|-------|-------|
| View Dashboard | ✅ | ✅ |
| Manage Reservations | ✅ | ✅ |
| Check-In / Check-Out | ✅ | ✅ |
| View Billing | ✅ | ✅ |
| Manage Rooms | ✅ | ❌ |
| Manage Banks | ✅ | ❌ |
| Manage Users | ✅ | ❌ |
| View Audit Logs | ✅ | ❌ |
| System Settings | ✅ | ❌ |
| View Reports | ✅ | ✅ |

---

## Architecture

The application follows a strict **3-tier Layered architecture**:

```
Request → AuthFilter → Servlet → Service → DAO → MySQL
                          ↓
                         JSP (View)
```

- **Filter** — `AuthFilter` protects all routes, redirects unauthenticated users to `/login`
- **Servlet** — Handles HTTP requests, validates roles, calls services, forwards to JSP
- **Service** — Contains business logic and validation rules
- **DAO** — Executes SQL using `PreparedStatement`, maps `ResultSet` to model objects
- **JSP** — Renders HTML using data set as request attributes by the servlet

---


## Email Configuration

Email credentials are kept in a gitignored email.properties file for security. Use email.properties.example as a template. You must use a Gmail App Password if using Gmail SMTP.

<img width="1890" height="822" alt="image" src="https://github.com/user-attachments/assets/1b5cd160-00cf-4ad9-b633-63a2d5d3f7a1" />


## Design Patterns

PatternApplied ToPurposeFilter PatternReservation filteringFilters reservations by criteria such as guest name, status, and dateSingleton PatternAuthenticationEnsures a single shared session/connection instance is used across the entire applicationStrategy PatternPayment methodsSwappable payment processing logic — Cash, Card, and Bank Transfer each have their own strategyDAO PatternEntire projectAll database access is handled through dedicated Data Access Objects, keeping SQL isolated from business logicFactory PatternRoom typesCreates the correct room object (Standard, Deluxe, Suite, Family, Penthouse) based on the selected type

## Screenshots

ADMIN DASHBOARD
<img width="1902" height="837" alt="image" src="https://github.com/user-attachments/assets/4d98bd9b-dbc9-4147-b9ef-54b5410ee26b" />

MANAGE RESERVATION
<img width="1896" height="893" alt="image" src="https://github.com/user-attachments/assets/e8bd37f5-eaf2-485e-b002-0bdecc392c3b" />

MANAGE ROOM
<img width="1883" height="909" alt="image" src="https://github.com/user-attachments/assets/53c64dab-a738-4490-ad3c-b8809a91295d" />

GUEST CHECKIN
<img width="1911" height="819" alt="image" src="https://github.com/user-attachments/assets/052aab04-86bf-40ae-8dab-6bf4a567accf" />

GUEST CHECKOUT
<img width="1916" height="793" alt="image" src="https://github.com/user-attachments/assets/cb059f58-23e5-4a0c-8ba6-4a72f20b9a65" />

ADD EXTRA CHARGES
<img width="1599" height="465" alt="image" src="https://github.com/user-attachments/assets/32545f20-2ed0-4940-880b-1cb413ad0133" />

VIEW AUDIT LOGS
<img width="1901" height="918" alt="image" src="https://github.com/user-attachments/assets/008dce74-a564-4068-a117-5639aed9d1c6" />

MANAGE SYSTEM SETTINGS
<img width="1903" height="911" alt="image" src="https://github.com/user-attachments/assets/3dce9b95-a1e1-42e3-aff9-acd199836b5c" />

MANAGE BANKS
<img width="1909" height="902" alt="image" src="https://github.com/user-attachments/assets/ac0192cb-9374-4486-8859-8405ddc9eb9f" />

VIEW BILLINGS
<img width="1893" height="921" alt="image" src="https://github.com/user-attachments/assets/c01490db-3fd5-4648-b1e5-5385455e3529" />

MONTHLY REVENUE DASHBOARD
<img width="1904" height="905" alt="image" src="https://github.com/user-attachments/assets/90d588f3-a25f-4165-999c-b2161329ff74" />

VIEW AND GENERATE REPORTS
<img width="1914" height="882" alt="image" src="https://github.com/user-attachments/assets/094dba45-a188-40b0-916f-3dc692b24f38" />

STAFF DASHBOARD
<img width="1886" height="569" alt="image" src="https://github.com/user-attachments/assets/1eff2b70-245b-4dd8-9cc0-b9b39dc87204" />


## License

This project is developed for academic and educational purposes.
