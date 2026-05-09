package hotelbookingsystem;

import hotel.dao.BookingDAO;
import hotel.dao.RoomDAO;
import hotel.model.Booking;
import hotel.model.Room;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Customer-facing room browser – clean card-based redesign.
 */
public class Custmor_Dashboard extends JFrame {

    // ─── state ──────────────────────────────────────────────────────────────
    private List<Room> allRooms = new ArrayList<>();
    private JPanel     roomsPanel;
    private JComboBox<String> typeCombo;
    private JComboBox<String> priceCombo;
    private JComboBox<String> floorCombo;
    private JLabel     statusLabel;

    // ─── entry point ─────────────────────────────────────────────────────────
    public Custmor_Dashboard() {
        super("Hotel Royal — Browse Rooms");
        buildUI();
        loadRooms();
    }

    // ════════════════════════════════════════════════════════════════════════
    // UI CONSTRUCTION
    // ════════════════════════════════════════════════════════════════════════

    private void buildUI() {
        setSize(750, 570);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // full-screen on open
        setMinimumSize(new Dimension(600, 400));
        setResizable(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 238, 228));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── HEADER ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(new Color(80, 45, 15));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // Left: hotel title
        JLabel title = new JLabel("🏨  Hotel Royal  —  Available Rooms");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Center: full filters
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        filterBox.setBackground(new Color(80, 45, 15));

        JLabel typeLbl = new JLabel("Room type:");
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeLbl.setForeground(new Color(210, 185, 150));
        typeCombo = new JComboBox<>(new String[]{"All Types", "Single", "Double", "Triple", "Suite"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeCombo.setPreferredSize(new Dimension(100, 30));
        typeCombo.addActionListener(e -> filterRooms());

        JLabel priceLbl = new JLabel("Price:");
        priceLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priceLbl.setForeground(new Color(210, 185, 150));
        priceCombo = new JComboBox<>(new String[]{"All Prices", "Under $100", "$100 - $300", "Above $300"});
        priceCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priceCombo.setPreferredSize(new Dimension(110, 30));
        priceCombo.addActionListener(e -> filterRooms());

        JLabel floorLbl = new JLabel("Floor:");
        floorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        floorLbl.setForeground(new Color(210, 185, 150));
        floorCombo = new JComboBox<>(new String[]{"All Floors", "Floor 1", "Floor 2", "Floor 3", "Floor 4", "Floor 5"});
        floorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        floorCombo.setPreferredSize(new Dimension(90, 30));
        floorCombo.addActionListener(e -> filterRooms());

        filterBox.add(typeLbl);
        filterBox.add(typeCombo);
        filterBox.add(priceLbl);
        filterBox.add(priceCombo);
        filterBox.add(floorLbl);
        filterBox.add(floorCombo);
        header.add(filterBox, BorderLayout.CENTER);

        // Right: name + role badge
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setBackground(new Color(80, 45, 15));

        String name = (Session.getCurrentUser() != null)
            ? Session.getCurrentUser().getName() : "Guest";

        JLabel nameLabel = new JLabel("👤  " + name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("  CUSTOMER  ");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setOpaque(true);
        roleLabel.setBackground(new Color(46, 125, 50));
        roleLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        userPanel.add(nameLabel);
        userPanel.add(roleLabel);
        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    // ── CENTER: scrollable room cards ────────────────────────────────────────
    private JScrollPane buildCenter() {
        roomsPanel = new JPanel();
        roomsPanel.setLayout(new BoxLayout(roomsPanel, BoxLayout.Y_AXIS));
        roomsPanel.setBackground(new Color(245, 238, 228));
        roomsPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JScrollPane scroll = new JScrollPane(roomsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 238, 228));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        return scroll;
    }

    // ── FOOTER ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(90, 52, 20));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backBtn = makeBtn("← Back", new Color(120, 75, 25));
        backBtn.addActionListener(e -> {
            dispose();
            new RegistPage().setVisible(true);
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(200, 175, 140));

        footer.add(backBtn,     BorderLayout.WEST);
        footer.add(statusLabel, BorderLayout.CENTER);
        return footer;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATA
    // ════════════════════════════════════════════════════════════════════════

    private void loadRooms() {
        statusLabel.setText("Loading rooms…");
        try {
            allRooms = new RoomDAO().getAvailableRooms();
            filterRooms();
            statusLabel.setText(allRooms.size() + " room(s) available");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not load rooms:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterRooms() {
        if (typeCombo == null || priceCombo == null || floorCombo == null) return;
        
        String selType = (String) typeCombo.getSelectedItem();
        String selPrice = (String) priceCombo.getSelectedItem();
        String selFloor = (String) floorCombo.getSelectedItem();

        List<Room> shown = allRooms.stream()
            .filter(r -> "All Types".equals(selType) || r.getType().equalsIgnoreCase(selType))
            .filter(r -> {
                if ("All Prices".equals(selPrice)) return true;
                double price = r.getPrice();
                if ("Under $100".equals(selPrice)) return price < 100;
                if ("$100 - $300".equals(selPrice)) return price >= 100 && price <= 300;
                if ("Above $300".equals(selPrice)) return price > 300;
                return true;
            })
            .filter(r -> {
                if ("All Floors".equals(selFloor)) return true;
                String roomNum = r.getRoomNumber();
                if (roomNum == null || roomNum.isEmpty()) return false;
                char floorChar = roomNum.charAt(0);
                String selectedFloorChar = selFloor.replace("Floor ", "");
                return String.valueOf(floorChar).equals(selectedFloorChar);
            })
            .collect(Collectors.toList());

        renderCards(shown);
        if (statusLabel != null)
            statusLabel.setText(shown.size() + " room(s) shown");
    }

    // ════════════════════════════════════════════════════════════════════════
    // RENDER CARDS
    // ════════════════════════════════════════════════════════════════════════

    private void renderCards(List<Room> rooms) {
        roomsPanel.removeAll();

        if (rooms.isEmpty()) {
            JLabel empty = new JLabel("No rooms available for this type.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            empty.setForeground(new Color(140, 110, 60));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            roomsPanel.add(Box.createVerticalStrut(40));
            roomsPanel.add(empty);
        } else {
            for (Room r : rooms) {
                roomsPanel.add(buildCard(r));
                roomsPanel.add(Box.createVerticalStrut(10));
            }
        }

        roomsPanel.revalidate();
        roomsPanel.repaint();
    }

    /**
     * Builds one horizontal room card:
     *  [ Floor badge ]  Room 201 — Double     $120 / night    [ Book Now ]
     */
    private JPanel buildCard(Room room) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        card.setBackground(new Color(255, 252, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 165, 100), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // ── Left: floor badge ──
        char floorChar = room.getRoomNumber().charAt(0);
        JLabel floorBadge = new JLabel("Floor " + floorChar, SwingConstants.CENTER);
        floorBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        floorBadge.setForeground(Color.WHITE);
        floorBadge.setOpaque(true);
        floorBadge.setBackground(floorColor(floorChar));
        floorBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        floorBadge.setPreferredSize(new Dimension(62, 40));

        // ── Center: room info ──
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setBackground(new Color(255, 252, 245));

        JLabel roomNum = new JLabel("Room  " + room.getRoomNumber());
        roomNum.setFont(new Font("Segoe UI", Font.BOLD, 16));
        roomNum.setForeground(new Color(60, 35, 10));

        JLabel roomType = new JLabel(room.getType() + " Room");
        roomType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roomType.setForeground(new Color(130, 100, 55));

        info.add(roomNum);
        info.add(roomType);

        // ── Right: price + button ──
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setBackground(new Color(255, 252, 245));

        JLabel price = new JLabel(String.format("$%.0f / night", room.getPrice()));
        price.setFont(new Font("Segoe UI", Font.BOLD, 15));
        price.setForeground(new Color(46, 125, 50));

        JButton bookBtn = makeBtn("Book Now", new Color(46, 125, 50));
        bookBtn.setPreferredSize(new Dimension(110, 36));
        bookBtn.addActionListener(e -> openBookingDialog(room));

        // Hover
        bookBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { bookBtn.setBackground(new Color(30, 100, 35)); }
            @Override public void mouseExited (MouseEvent e) { bookBtn.setBackground(new Color(46, 125, 50)); }
        });

        right.add(price);
        right.add(bookBtn);

        card.add(floorBadge, BorderLayout.WEST);
        card.add(info,       BorderLayout.CENTER);
        card.add(right,      BorderLayout.EAST);

        // Card hover highlight
        MouseAdapter hover = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(new Color(255, 248, 235)); info.setBackground(new Color(255, 248, 235)); right.setBackground(new Color(255, 248, 235)); }
            @Override public void mouseExited (MouseEvent e) { card.setBackground(new Color(255, 252, 245)); info.setBackground(new Color(255, 252, 245)); right.setBackground(new Color(255, 252, 245)); }
        };
        card.addMouseListener(hover);
        info.addMouseListener(hover);

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BOOKING DIALOG
    // ════════════════════════════════════════════════════════════════════════

    private void openBookingDialog(Room r) {
        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                "Your session has expired. Please log in again.",
                "Session Expired", JOptionPane.WARNING_MESSAGE);
            dispose();
            new IndexPage().setVisible(true);
            return;
        }

        // Check-in
        String checkIn = JOptionPane.showInputDialog(this,
            "Enter Check-In Date  (format: YYYY-MM-DD):",
            "Check-In", JOptionPane.QUESTION_MESSAGE);
        if (checkIn == null || checkIn.trim().isEmpty()) return;

        // Check-out
        String checkOut = JOptionPane.showInputDialog(this,
            "Enter Check-Out Date  (format: YYYY-MM-DD):",
            "Check-Out", JOptionPane.QUESTION_MESSAGE);
        if (checkOut == null || checkOut.trim().isEmpty()) return;

        try {
            java.time.LocalDate ci = java.time.LocalDate.parse(checkIn.trim());
            java.time.LocalDate co = java.time.LocalDate.parse(checkOut.trim());
            long nights = java.time.temporal.ChronoUnit.DAYS.between(ci, co);

            if (nights <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Check-out date must be after check-in date.",
                    "Invalid Dates", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double total = nights * r.getPrice();

            // Confirmation summary
            String summary =
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "  Room:        " + r.getRoomNumber() + " (" + r.getType() + ")\n" +
                "  Check-In:    " + checkIn.trim() + "\n" +
                "  Check-Out:   " + checkOut.trim() + "\n" +
                "  Nights:      " + nights + "\n" +
                "  Total Cost:  $" + (int) total + "\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "  Confirm this booking?";

            int confirm = JOptionPane.showConfirmDialog(this,
                summary, "Confirm Booking",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            Booking booking = new Booking(
                0,
                Session.getCurrentUser().getUserId(),
                r.getRoomId(),
                checkIn.trim(), checkOut.trim(),
                total, "Confirmed"
            );

            if (new BookingDAO().createBooking(booking)) {
                JOptionPane.showMessageDialog(this,
                    "✅  Booking Confirmed!\n" +
                    "Room " + r.getRoomNumber() + " is reserved for you.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRooms(); // refresh list (booked room disappears)
            } else {
                JOptionPane.showMessageDialog(this,
                    "Booking failed. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-05-15).",
                "Format Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "An error occurred:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /** Color-code floors for quick visual orientation. */
    private Color floorColor(char floor) {
        switch (floor) {
            case '1': return new Color(25, 118, 210);   // blue  — Single
            case '2': return new Color(46, 125, 50);    // green — Double
            case '3': return new Color(156, 39, 176);   // purple — Triple
            case '4': return new Color(198, 40, 40);    // red   — Suite
            default:  return new Color(80, 45, 15);
        }
    }

    /** Styled button factory. */
    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }
}
