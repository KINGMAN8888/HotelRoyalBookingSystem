package hotelbookingsystem;

import hotel.dao.BookingDAO;
import hotel.model.Receptionist;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class Receptionist_Dashboard extends JFrame {

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JLabel statusBar;

    public Receptionist_Dashboard() {
        super("Receptionist Dashboard");
        buildUI();
        loadBookings();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // full-screen on open
        setMinimumSize(new Dimension(700, 450));    // safe minimum
        setResizable(true);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(245, 238, 228));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildSouth(),   BorderLayout.SOUTH);
    }

    // ── HEADER ───────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(80, 45, 15));
        p.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("  🛎  Receptionist Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        // Right side: name + role badge
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setBackground(new Color(80, 45, 15));

        String name = (Session.getCurrentUser() != null)
            ? Session.getCurrentUser().getName() : "Receptionist";

        JLabel nameLabel = new JLabel("👤  " + name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("  RECEPTIONIST  ");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setOpaque(true);
        roleLabel.setBackground(new Color(25, 118, 210));
        roleLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        userPanel.add(nameLabel);
        userPanel.add(roleLabel);

        p.add(title,     BorderLayout.WEST);
        p.add(userPanel, BorderLayout.EAST);
        return p;
    }

    // ── CENTER TABLE ─────────────────────────────────────────────
    private JPanel buildCenter() {
        String[] cols = {"ID", "Customer Name", "Room No.", "Type",
                         "Check-In", "Check-Out", "Amount ($)", "Status", "RoomID", "Price"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(tableModel);
        styleTable(bookingTable);

        // Hide RoomID (8) and Price (9) helper columns
        for (int ci : new int[]{8, 9}) {
            bookingTable.getColumnModel().getColumn(ci).setMinWidth(0);
            bookingTable.getColumnModel().getColumn(ci).setMaxWidth(0);
            bookingTable.getColumnModel().getColumn(ci).setWidth(0);
        }
        bookingTable.getColumnModel().getColumn(1).setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(bookingTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(245, 238, 228));
        center.add(scroll, BorderLayout.CENTER);
        return center;
    }

    // ── SOUTH BUTTONS ────────────────────────────────────────────
    private JPanel buildSouth() {
        JPanel outer = new JPanel(new BorderLayout());

        // Status bar
        statusBar = new JLabel("  Select a booking and use the action buttons below.");
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.setForeground(new Color(100, 60, 20));
        statusBar.setBackground(new Color(220, 200, 170));
        statusBar.setOpaque(true);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        outer.add(statusBar, BorderLayout.NORTH);

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnRow.setBackground(new Color(90, 52, 20));

        JButton checkInBtn  = makeBtn("Check In",       new Color(46, 125, 50));
        JButton checkOutBtn = makeBtn("Check Out",       new Color(25, 118, 210));
        JButton invoiceBtn  = makeBtn("Generate Invoice",new Color(230, 130, 20));
        JButton refreshBtn  = makeBtn("Refresh",         new Color(100, 60, 20));
        JButton backBtn     = makeBtn("Logout",          new Color(198, 40, 40));

        checkInBtn .addActionListener(e -> doCheckIn());
        checkOutBtn.addActionListener(e -> doCheckOut());
        invoiceBtn .addActionListener(e -> doInvoice());
        refreshBtn .addActionListener(e -> loadBookings());
        backBtn    .addActionListener(e -> { Session.logout(); setVisible(false); new IndexPage().setVisible(true); });

        btnRow.add(checkInBtn);
        btnRow.add(checkOutBtn);
        btnRow.add(invoiceBtn);
        btnRow.add(refreshBtn);
        btnRow.add(backBtn);
        outer.add(btnRow, BorderLayout.SOUTH);
        return outer;
    }

    // ── DATA LOADING ─────────────────────────────────────────────
    private void loadBookings() {
        tableModel.setRowCount(0);
        try {
            List<Object[]> bookings = new BookingDAO().getAllBookingsDetailed();
            for (Object[] row : bookings) tableModel.addRow(row);
            statusBar.setText("  " + bookings.size() + " booking(s) loaded.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── ACTIONS ──────────────────────────────────────────────────
    private void doCheckIn() {
        Object[] row = selectedRow();
        if (row == null) return;
        int id     = (Integer) row[0];
        String st  = (String)  row[7];
        if (!"Confirmed".equals(st)) {
            warn("Only 'Confirmed' bookings can be checked in. Current status: " + st); return;
        }
        if (confirm("Check in booking #" + id + "?")) {
            try {
                if (new BookingDAO().checkInBooking(id)) {
                    JOptionPane.showMessageDialog(this, "Guest checked in for booking #" + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBookings();
                } else warn("Check-in failed — booking may have changed.");
            } catch (Exception e) { err(e); }
        }
    }

    private void doCheckOut() {
        Object[] row = selectedRow();
        if (row == null) return;
        int id     = (Integer) row[0];
        int roomId = (Integer) row[8];
        String st  = (String)  row[7];
        if (!"CheckedIn".equals(st)) {
            warn("Only 'CheckedIn' bookings can be checked out. Current status: " + st); return;
        }
        if (confirm("Check out booking #" + id + "? The room will become available.")) {
            try {
                if (new BookingDAO().checkOutBooking(id, roomId)) {
                    JOptionPane.showMessageDialog(this, "Guest checked out for booking #" + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBookings();
                } else warn("Check-out failed — booking may have changed.");
            } catch (Exception e) { err(e); }
        }
    }

    private void doInvoice() {
        Object[] row = selectedRow();
        if (row == null) return;
        new InvoiceDialog(this, row).setVisible(true);
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private Object[] selectedRow() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) { warn("Please select a booking row first."); return null; }
        int cols = tableModel.getColumnCount();
        Object[] data = new Object[cols];
        for (int i = 0; i < cols; i++) data[i] = tableModel.getValueAt(row, i);
        return data;
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void err(Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(145, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(new Color(40, 40, 40));
        t.setBackground(Color.WHITE);
        t.setGridColor(new Color(220, 200, 175));
        t.setSelectionBackground(new Color(160, 110, 50));
        t.setSelectionForeground(Color.WHITE);
        t.setShowGrid(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(100, 60, 20));
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(0, 32));
    }
}
