# Hotel Booking System — Technical Documentation

**Version:** 1.0.0  
**Language:** Java 8+  
**Architecture:** MVC with DAO Pattern  
**Database:** MySQL (via JDBC)  
**GUI Framework:** Java Swing (FlatLaf 3.4)

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture](#2-architecture)
3. [Project Structure](#3-project-structure)
4. [Database Schema](#4-database-schema)
5. [Model Layer](#5-model-layer)
6. [Data Access Layer (DAO)](#6-data-access-layer-dao)
7. [Session Management](#7-session-management)
8. [User Interface Layer](#8-user-interface-layer)
9. [Threading Model](#9-threading-model)
10. [PDF Generation](#10-pdf-generation)
11. [Build and Deployment](#11-build-and-deployment)
12. [User Accounts](#12-user-accounts)
13. [Known Constraints](#13-known-constraints)

---

## 1. System Overview

The Hotel Booking System is a desktop application designed to manage room reservations, customer accounts, and booking administration for a mid-scale hotel operation.

The system supports three distinct user roles, each with a dedicated interface and set of permissions:

- **Admin** — Full system control: room management, booking oversight, statistical reporting.
- **Receptionist** — Operational duties: guest check-in, check-out, booking status updates.
- **Customer** — Self-service: browse available rooms, filter by type/price/floor, submit bookings.

Authentication is centralized. All three roles enter through the same login screen. After credential verification against the database, the system instantiates the correct user subtype and routes the session to the appropriate dashboard.

---

## 2. Architecture

The system follows a layered architecture:

```
Presentation Layer   → Java Swing JFrames and JDialogs
                       (admin_dashboard, Receptionist_Dashboard, Custmor_Dashboard)

Business Logic       → Model classes enforce rules via OOP
                       (User hierarchy, IReservable interface)

Data Access Layer    → DAO classes isolate all SQL from the UI
                       (UserDAO, RoomDAO, BookingDAO)

Persistence Layer    → MySQL database via JDBC
                       (DatabaseConnection singleton)

Utility Layer        → Cross-cutting concerns
                       (PDFGenerator, Session)
```

Design Patterns applied:
- **DAO Pattern** — All database operations go through typed DAO classes.
- **Singleton Pattern** — `DatabaseConnection` returns one shared connection.
- **SwingWorker Pattern** — Background threads for all DB calls, preventing UI freeze.

---

## 3. Project Structure

```
HotelBookingSystem\
│
├── src\
│   ├── hotel\
│   │   ├── dao\
│   │   │   ├── BookingDAO.java
│   │   │   ├── RoomDAO.java
│   │   │   └── UserDAO.java
│   │   ├── database\
│   │   │   └── DatabaseConnection.java
│   │   ├── exception\
│   │   │   └── DatabaseException.java
│   │   ├── model\
│   │   │   ├── Admin.java
│   │   │   ├── Booking.java
│   │   │   ├── Customer.java
│   │   │   ├── IReservable.java
│   │   │   ├── Receptionist.java
│   │   │   ├── Room.java
│   │   │   └── User.java
│   │   └── utils\
│   │       └── PDFGenerator.java
│   │
│   └── hotelbookingsystem\
│       ├── HotelBookingSystem.java     (main entry point)
│       ├── Session.java
│       ├── IndexPage.java
│       ├── LoginPage.java
│       ├── RegistPage.java
│       ├── admin_dashboard.java
│       ├── Receptionist_Dashboard.java
│       ├── Custmor_Dashboard.java
│       ├── RoomPickerDialog.java
│       └── InvoiceDialog.java
│
├── libs\
│   ├── AbsoluteLayout.jar
│   ├── flatlaf-3.4.jar
│   ├── mysql-connector-j-9.7.0.jar
│   └── pdfbox-app-2.0.30.jar
│
├── build\classes\                      (compiled .class files)
├── compile3.ps1
└── run2.ps1
```

---

## 4. Database Schema

### 4.1 users

```sql
CREATE TABLE users (
    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    email         TEXT    UNIQUE NOT NULL,
    password      TEXT    NOT NULL,
    role          TEXT    NOT NULL,       -- 'admin', 'receptionist', 'customer'
    phone_number  TEXT
);
```

### 4.2 rooms

```sql
CREATE TABLE rooms (
    room_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    room_number   TEXT    NOT NULL,
    type          TEXT    NOT NULL,       -- 'Single', 'Double', 'Triple', 'Suite'
    price         REAL    NOT NULL,
    is_available  INTEGER NOT NULL        -- 1 = available, 0 = booked
);
```

### 4.3 bookings

```sql
CREATE TABLE bookings (
    booking_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id    INTEGER,
    room_id        INTEGER,
    check_in_date  TEXT,
    check_out_date TEXT,
    amount         REAL,
    status         TEXT,                  -- 'Confirmed','Cancelled','CheckedIn','CheckedOut'
    FOREIGN KEY(customer_id) REFERENCES users(user_id),
    FOREIGN KEY(room_id)     REFERENCES rooms(room_id)
);
```

### 4.4 Room Availability Logic

`is_available` is set to 0 on booking confirmation and restored to 1 on cancellation, checkout, or deletion. All transitions go through `RoomDAO.updateRoomStatus()`.

---

## 5. Model Layer

### 5.1 Inheritance Hierarchy

```
User  (base)
  - userID, name, email, password, role
  + login(), logout(), getDetails()

  Admin extends User
    + addRoom(), deleteRoom(), viewAllBookings()

  Receptionist extends User
    + checkIn(bookingId), checkOut(bookingId)

  Customer extends User
    - phoneNumber
    + searchRoom(), makeBooking()
```

### 5.2 Booking

```
Booking implements IReservable
  - bookingId, customerId (FK), roomId (FK)
  - checkInDate, checkOutDate (TEXT)
  - amount, status
```

### 5.3 Room

```
Room
  - roomId, roomNumber, type, price, isAvailable
```

Room number convention: first digit = floor (e.g., "201" = Floor 2).

### 5.4 IReservable Interface

```java
interface IReservable {
    boolean reserve();
    boolean cancelReservation();
}
```

---

## 6. Data Access Layer (DAO)

Every method opens and closes its own JDBC connection using try-with-resources.

### UserDAO

| Method | Returns | Description |
|---|---|---|
| loginUser(email, password) | User | Polymorphic: returns Admin, Receptionist, or Customer |
| registerCustomer(customer) | boolean | Inserts new customer account |

### RoomDAO

| Method | Returns | Description |
|---|---|---|
| getAllRooms() | List<Room> | All rooms |
| getAvailableRooms() | List<Room> | is_available = 1 only |
| addRoom(room) | boolean | Insert new room |
| updateRoomStatus(roomId, bool) | boolean | Toggle availability |
| updateRoom(roomId, type, price, avail) | boolean | Full room update |
| deleteRoom(roomNumber) | boolean | Delete by room number |

### BookingDAO

| Method | Returns | Description |
|---|---|---|
| createBooking(booking) | boolean | Insert + mark room unavailable |
| getBookingsByCustomer(customerId) | List<Booking> | Customer history |
| getAllBookings() | List<Booking> | Raw booking list |
| getAllBookingsDetailed() | List<Object[]> | JOIN with users + rooms |
| cancelBooking(bookingId, roomId) | boolean | Status = Cancelled, free room |
| deleteBooking(bookingId, roomId) | boolean | Delete record, free room |
| updateBooking(id, checkIn, checkOut, status) | boolean | Edit booking |
| checkInBooking(bookingId) | boolean | Confirmed → CheckedIn |
| checkOutBooking(bookingId, roomId) | boolean | CheckedIn → CheckedOut |
| calculateRevenue() | double | Sum of nights × price |

---

## 7. Session Management

```java
// Set after successful login
Session.setCurrentUser(user);

// Read from any dashboard
Session.getCurrentUser();

// Clear on logout
Session.logout();

// Check authentication state
Session.isLoggedIn();
```

---

## 8. User Interface Layer

### Navigation Flow

```
main()
  └── IndexPage
        ├── LoginPage ──── success ──► Admin Dashboard
        │                         ──► Receptionist Dashboard
        │                         ──► Customer Dashboard
        └── RegistPage (customers only)
```

### Admin Dashboard Tabs

| Tab | Features |
|---|---|
| Manage Rooms | Add, edit, delete rooms. Visual room picker dialog. |
| View Bookings | Table with edit, cancel, delete, print invoice (PDF). |
| Statistics | 6 live cards: total rooms, available, booked, bookings, revenue, customers. |

### Customer Dashboard Filters

Three simultaneous live filters using Java Streams:
- Room Type: All Types / Single / Double / Triple / Suite
- Price: All Prices / Under $100 / $100-$300 / Above $300
- Floor: All Floors / Floor 1 through Floor 5

### RoomPickerDialog

Visual floor map with color-coded room buttons. Green = available, Red = booked.

---

## 9. Threading Model

All database calls triggered from the UI are executed in a `SwingWorker` background thread. UI updates happen in the `done()` method on the EDT.

Methods using SwingWorker:
- `showBtnActionPerformed()` — loads all bookings into viewTable
- `loadRooms()` — populates tblRooms
- `loadStatistics()` — updates 6 statistics label cards

---

## 10. PDF Generation

Class: `hotel.utils.PDFGenerator`  
Library: Apache PDFBox 2.0.30

```java
String path = PDFGenerator.generateInvoice(
    bookingId, customerName, roomNo, type,
    checkIn, checkOut, nights, amount, status
);
```

Output: `invoices/Invoice_Booking_<ID>.pdf`  
After saving, the file is automatically opened with the system default PDF viewer.

---

## 11. Build and Deployment

### Prerequisites

- Eclipse Adoptium JDK 25
- MySQL running on localhost
- All JARs in `libs/` directory

### Compile

```powershell
powershell -ExecutionPolicy Bypass -File "F:\Java Project\compile3.ps1"
```

### Run

```powershell
powershell -ExecutionPolicy Bypass -File "F:\Java Project\run2.ps1"
```

### Standalone Desktop Export

```powershell
powershell -ExecutionPolicy Bypass -File "F:\Java Project\export.ps1"
```

Output: `F:\Java Project\HotelRoyal.jar`  
Launch: `HotelRoyal.bat` (double-click)

---

## 12. User Accounts

### Admin

| Email | Password |
|---|---|
| admin@hotel.com | admin123 |
| sarah.admin@hotelroyal.com | Admin@2026 |

### Receptionist

| Email | Password |
|---|---|
| reception@hotel.com | recep123 |
| omar.recep@hotelroyal.com | Recep@2026 |
| nada.recep@hotelroyal.com | Recep@2026 |

### Customer

| Email | Password |
|---|---|
| yousefmahmoudsaber@gmail.com | Youssef@#321 |
| ahmed.ali@gmail.com | Customer@2026 |
| fatima.nasser@gmail.com | Customer@2026 |
| mohamed.samir@gmail.com | Customer@2026 |

---

## 13. Known Constraints

| Item | Detail |
|---|---|
| Passwords | Stored as plain text. Apply BCrypt for production. |
| Connection pooling | Each method opens a fresh connection. Add HikariCP for scale. |
| Date format | Stored as TEXT in MySQL. Multiple formats accepted via tryParse(). |
| Floor detection | Based on first character of room number string. |
| PDF output path | Relative to JVM working directory. |
