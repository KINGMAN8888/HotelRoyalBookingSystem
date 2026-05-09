CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL,
    phone_number TEXT
);

CREATE TABLE rooms (
    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_number TEXT NOT NULL,
    type TEXT NOT NULL,
    price REAL NOT NULL,
    is_available INTEGER NOT NULL
);

CREATE TABLE bookings (
    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER,
    room_id INTEGER,
    check_in_date TEXT,
    check_out_date TEXT,
    amount REAL,
    status TEXT,
    FOREIGN KEY(customer_id) REFERENCES users(user_id),
    FOREIGN KEY(room_id) REFERENCES rooms(room_id)
);