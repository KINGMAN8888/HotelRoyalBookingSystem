# Hotel Booking System

A desktop application for managing hotel room reservations, built with Java Swing and MySQL.

---

## Description

Hotel Booking System is a multi-role desktop application that handles the full lifecycle of a hotel room reservation — from browsing available rooms to confirming bookings, managing check-ins, and generating invoices.

The application supports three user roles, each with a dedicated dashboard and permissions: Administrator, Receptionist, and Customer.

---

## Features

### Administrator
- Add, edit, and delete hotel rooms
- View and manage all bookings across the system
- Cancel or delete any booking
- Generate PDF invoices for any booking
- View live statistics: total rooms, available rooms, occupancy count, total revenue, and registered customers
- Visual room picker with floor-by-floor layout

### Receptionist
- View all current bookings
- Perform guest check-in and check-out
- Update booking status in real time

### Customer
- Browse all available rooms
- Filter rooms by type, price range, and floor
- Select a room through an interactive visual floor map
- Submit bookings with check-in and check-out dates
- View personal booking history and invoice details

---

## Prerequisites

Before running the application, ensure the following are installed and configured:

- **Java Development Kit (JDK):** Version 8 or higher
- **MySQL Server:** Running on localhost, default port 3306
- **MySQL database:** Created with the schema described below

---

## Installation

### 1. Clone or extract the project

Place the project folder at a known path, for example:

```
F:\Java Project\HotelSystem\HotelBookingSystem\
```

### 2. Configure the database connection

Open `src/hotel/database/DatabaseConnection.java` and verify the connection parameters match your MySQL setup:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/hotel_db";
private static final String USER     = "root";
private static final String PASSWORD = "your_password";
```

### 3. Create the database schema

Run the following SQL statements in your MySQL client:

```sql
CREATE DATABASE hotel_db;
USE hotel_db;

CREATE TABLE users (
    user_id      INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    password     VARCHAR(100) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    phone_number VARCHAR(20)
);

CREATE TABLE rooms (
    room_id      INT PRIMARY KEY AUTO_INCREMENT,
    room_number  VARCHAR(10)  NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    price        DOUBLE       NOT NULL,
    is_available TINYINT      NOT NULL DEFAULT 1
);

CREATE TABLE bookings (
    booking_id     INT PRIMARY KEY AUTO_INCREMENT,
    customer_id    INT,
    room_id        INT,
    check_in_date  VARCHAR(20),
    check_out_date VARCHAR(20),
    amount         DOUBLE,
    status         VARCHAR(30),
    FOREIGN KEY (customer_id) REFERENCES users(user_id),
    FOREIGN KEY (room_id)     REFERENCES rooms(room_id)
);
```

### 4. Insert the default admin account

```sql
INSERT INTO users (name, email, password, role)
VALUES ('Admin', 'admin@hotel.com', 'admin123', 'admin');
```

---

## Building and Running

### Compile

```powershell
powershell -ExecutionPolicy Bypass -File "F:\Java Project\compile3.ps1"
```

A successful compile produces no error output and exits with code 0.

### Run

```powershell
powershell -ExecutionPolicy Bypass -File "F:\Java Project\run2.ps1"
```

### Standalone Desktop Application

To export the project as a self-contained JAR with launcher:

```powershell
powershell -ExecutionPolicy Bypass -File "F:\Java Project\export.ps1"
```

This produces `HotelRoyal.jar` and `HotelRoyal.bat`. Double-click `HotelRoyal.bat` to launch.

---

## Project Structure

```
src/
  hotel/
    dao/           BookingDAO, RoomDAO, UserDAO
    database/      DatabaseConnection
    exception/     DatabaseException
    model/         User, Admin, Receptionist, Customer, Room, Booking, IReservable
    utils/         PDFGenerator
  hotelbookingsystem/
    HotelBookingSystem.java    Entry point
    Session.java               Authenticated user state
    IndexPage.java
    LoginPage.java
    RegistPage.java
    admin_dashboard.java
    Receptionist_Dashboard.java
    Custmor_Dashboard.java
    RoomPickerDialog.java
    InvoiceDialog.java

libs/
  AbsoluteLayout.jar
  flatlaf-3.4.jar
  mysql-connector-j-9.7.0.jar
  pdfbox-app-2.0.30.jar
```

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| FlatLaf | 3.4 | Modern Swing look and feel |
| MySQL Connector/J | 9.7.0 | JDBC database driver |
| Apache PDFBox | 2.0.30 | PDF invoice generation |
| AbsoluteLayout | NetBeans | Legacy form layout support |

---

## Default Login Credentials

| Role | Email | Password |
|---|---|---|
| Admin | admin@hotel.com | admin123 |
| Receptionist | reception@hotel.com | recep123 |

---

## Notes

- Passwords are stored as plain text. This is appropriate for academic purposes only. In a production environment, apply a hashing algorithm such as BCrypt.
- Generated PDF invoices are saved to an `invoices/` folder in the application's working directory.
- Room floor is determined by the first digit of the room number. Room "201" is on Floor 2.

---

## License

This project was developed as a university coursework assignment.  
All rights reserved by the project team.
