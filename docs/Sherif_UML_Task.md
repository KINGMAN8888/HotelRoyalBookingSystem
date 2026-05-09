# 📐 Sherif's Task — UML Class Diagram Fix & Update
**Project:** Hotel Booking System  
**Tool:** draw.io (or any UML editor)  
**Assigned To:** Sherif  
**Role:** UML Diagram Designer  

---

## 🎯 Your Objective

The current UML Class Diagram for our Hotel Booking System has **several missing classes, incomplete methods, and missing relationships** compared to what is actually implemented in the code.

Your job is to **open the existing diagram, fix all issues listed below, and submit the corrected version.**

> [!IMPORTANT]
> The existing diagram file is: `F:\Java Project\Copy of java project.drawio.pdf`  
> Re-draw or update it based **exactly** on the fixes described below.

---

## ❌ Issue #1 — Missing `Receptionist` Class (CRITICAL)

**Problem:** The diagram currently shows `Admin` and `Customer` inheriting from `User`, but the `Receptionist` class is completely missing — even though it exists in the actual code.

### ✅ Fix: Add `Receptionist` Class

Add a new class box for `Receptionist` that **inherits from `User`** (use a generalization/inheritance arrow pointing up to `User`).

The class must contain exactly:

```
┌─────────────────────────────────────┐
│           Receptionist              │
├─────────────────────────────────────┤
│ (no additional attributes)          │
├─────────────────────────────────────┤
│ + manageBooking(b: Booking): void   │
│ + checkInGuest(bookingID: int): void│
│ + checkOutGuest(bookingID:int): void│
│ + getDetails(): String              │
└─────────────────────────────────────┘
```

**Relationship:**
```
User ◁────────── Receptionist   (Inheritance / Generalization)
```

**Placement:** Place `Receptionist` next to `Admin` and `Customer` under the `User` class.

---

## ❌ Issue #2 — `Booking` Class is Missing Relationships

**Problem:** The `Booking` class does not show a link to `Customer` (who made the booking) or to `Room` (which was booked).

### ✅ Fix: Update `Booking` Class Attributes

Add the missing foreign-key attributes inside the `Booking` class box:

```
┌──────────────────────────────────┐
│              Booking             │
├──────────────────────────────────┤
│ - bookingID: int                 │
│ - customerID: int      ← ADD     │
│ - roomID: int          ← ADD     │
│ - checkInDate: Date              │
│ - checkOutDate: Date             │
│ - totalAmount: double            │
│ - status: String                 │
├──────────────────────────────────┤
│ + calculateTotal(): double       │
│ + generateInvoice(): void        │
└──────────────────────────────────┘
```

### ✅ Fix: Draw Two Association Arrows from `Booking`

| Arrow | From | To | Label | Multiplicity |
|---|---|---|---|---|
| Association | `Customer` | `Booking` | "makes" | `1` → `0..*` |
| Association | `Booking` | `Room` | "reserves" | `0..*` → `1` |

---

## ❌ Issue #3 — `IReservable` Interface is Incomplete

**Problem:** The `IReservable` interface has two blank method entries and does not show which class implements it.

### ✅ Fix: Complete the Interface Box

```
┌──────────────────────────────────┐
│         «interface»              │
│          IReservable             │
├──────────────────────────────────┤
│ + reserve(): boolean             │
│ + cancelReservation(): boolean   │
└──────────────────────────────────┘
```
Remove the two empty lines. The interface should only have those 2 methods.

### ✅ Fix: Show Which Class Implements It

Draw a **dashed arrow with an open triangle** (`- - - ▷`) from `Booking` to `IReservable`:

```
Booking - - - ▷ IReservable   (Realization / Implementation)
```

---

## ❌ Issue #4 — `BookingDAO` is Missing Methods

**Problem:** The current `BookingDAO` only shows 2 methods, but the real implementation has more.

### ✅ Fix: Update `BookingDAO` Class Box

```
┌──────────────────────────────────────────────────────┐
│                    Booking (DAO)                     │
├──────────────────────────────────────────────────────┤
│ + createBooking(b: Booking): boolean                 │
│ + getBookingsForCustomer(customerID: int): List      │
│ + getAllBookingsDetailed(): List          ← ADD       │
│ + cancelBooking(bookingID: int, roomID: int): boolean← ADD │
│ + deleteBooking(bookingID: int, roomID: int): boolean← ADD │
│ + calculateRevenue(): double             ← ADD       │
└──────────────────────────────────────────────────────┘
```

---

## ❌ Issue #5 — `RoomDAO` is Missing Methods

**Problem:** `RoomDAO` only shows 2 methods, the real class has more.

### ✅ Fix: Update `RoomDAO` Class Box

```
┌────────────────────────────────────────────────────┐
│                    Room (DAO)                      │
├────────────────────────────────────────────────────┤
│ + getAllRooms(): List<Room>                         │
│ + getAvailableRooms(): List<Room>        ← ADD     │
│ + addRoom(r: Room): boolean              ← ADD     │
│ + deleteRoom(roomNumber: String): boolean← ADD     │
│ + updateRoomStatus(roomID: int, status: boolean): boolean │
└────────────────────────────────────────────────────┘
```

---

## ❌ Issue #6 — Missing `Session` Utility Class

**Problem:** The system uses a `Session` class to track the currently logged-in user throughout the application. It is missing from the diagram entirely.

### ✅ Fix: Add `Session` Class in the "Database & Utilities" Section

```
┌────────────────────────────────────────────┐
│                  Session                   │
├────────────────────────────────────────────┤
│ - currentUser: User          (static)      │
├────────────────────────────────────────────┤
│ + setCurrentUser(u: User): void  (static)  │
│ + getCurrentUser(): User         (static)  │
│ + clearSession(): void           (static)  │
└────────────────────────────────────────────┘
```

---

## ❌ Issue #7 — Multiplicity Labels Are Unclear

**Problem:** The numbers `1`, `0..*`, `1..*` appear floating in the diagram without being clearly attached to specific relationship arrows.

### ✅ Fix: Attach Correct Multiplicity to Each Arrow

| Relationship | From Side | To Side |
|---|---|---|
| User → Booking | `1` | `0..*` |
| Room → Booking | `1` | `0..*` |
| Admin → Room (manages) | `1` | `0..*` |

---

## 📐 Final Diagram Structure Overview

After all fixes, your diagram must have this structure:

```
┌─────────────────────────────────────────────────────────────────┐
│                        «User Layer»                             │
│   ┌────────┐    ┌──────────────┐    ┌───────────────────┐       │
│   │  User  │◁───│    Admin     │    │     Customer      │       │
│   └────────┘    └──────────────┘    └───────────────────┘       │
│        ◁──────────────────────────────────────────────          │
│                  │ Receptionist (NEW) │                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      «Core Entities»                            │
│   ┌────────┐  1     0..*  ┌───────────┐  0..*    1  ┌────────┐ │
│   │Customer│──────────────│  Booking  │─────────────│  Room  │ │
│   └────────┘  makes       └───────────┘  reserves   └────────┘ │
│                                 │                               │
│                           - - - ▷ IReservable                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                  «Database & Utilities»                         │
│  ┌────────────────────┐   ┌────────────┐   ┌───────────────┐   │
│  │  DatabaseConnection│   │IReservable │   │    Session    │   │
│  └────────────────────┘   └────────────┘   └───────────────┘   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│               «Data Access Layer (DAO)»                         │
│   ┌──────────┐    ┌────────────┐    ┌────────────┐             │
│   │ UserDAO  │    │  RoomDAO   │    │ BookingDAO │             │
│   └──────────┘    └────────────┘    └────────────┘             │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ Completion Checklist

Before submitting the updated diagram, check every item:

- [ ] `Receptionist` class added with correct inheritance from `User`
- [ ] `Booking` class has `customerID` and `roomID` attributes
- [ ] Arrow drawn: `Customer` → `Booking` (1 to 0..*)
- [ ] Arrow drawn: `Booking` → `Room` (0..* to 1)
- [ ] `IReservable` interface has exactly 2 methods (no empty lines)
- [ ] `Booking` → `IReservable` realization arrow drawn (dashed)
- [ ] `BookingDAO` updated with all 6 methods
- [ ] `RoomDAO` updated with all 5 methods
- [ ] `Session` class added in the Utilities section
- [ ] All multiplicity labels are clearly attached to their arrows
- [ ] Diagram is clean, aligned, and readable

---

> [!NOTE]
> You can use **draw.io** (free online at https://app.diagrams.net/) to edit the diagram.  
> Export the final version as both `.drawio` (editable) and `.pdf` (for submission).

**Good luck, Sherif! 🚀**
