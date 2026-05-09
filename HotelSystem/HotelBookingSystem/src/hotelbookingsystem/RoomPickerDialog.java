package hotelbookingsystem;

import hotel.dao.RoomDAO;
import hotel.model.Room;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class RoomPickerDialog extends JDialog {

    private Room selectedRoom;
    private Room lastRoom;          // tracks which room lastClicked belongs to
    private JLabel detailLabel;
    private JButton confirmBtn;
    private final boolean availableOnly;
    private JButton lastClicked;

    // availableOnly=true → customer mode (only green rooms selectable)
    // availableOnly=false → admin mode (all rooms selectable)
    public RoomPickerDialog(Frame parent, boolean availableOnly) {
        super(parent, "Hotel Room Map", true);
        this.availableOnly = availableOnly;
        buildUI();
    }

    private void buildUI() {
        setSize(750, 530);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(245, 238, 228));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildSouth(),   BorderLayout.SOUTH);
    }

    // ── HEADER ──────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(80, 45, 15));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("  Hotel Royal  —  Room Selection");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        legend.setBackground(new Color(80, 45, 15));
        legend.add(legendChip("Available", new Color(46, 125, 50)));
        legend.add(legendChip("Booked",    new Color(198, 40, 40)));
        header.add(legend, BorderLayout.EAST);
        return header;
    }

    // ── FLOOR PANELS ────────────────────────────────────────────
    private JScrollPane buildCenter() {
        JPanel floors = new JPanel();
        floors.setLayout(new BoxLayout(floors, BoxLayout.Y_AXIS));
        floors.setBackground(new Color(245, 238, 228));
        floors.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

        RoomDAO dao = new RoomDAO();
        List<Room> allRooms;
        try {
            allRooms = dao.getAllRooms();
        } catch (Exception e) {
            allRooms = new ArrayList<>();
        }

        // Group by floor (first char of room number)
        Map<Character, List<Room>> byFloor = new TreeMap<>(Collections.reverseOrder());
        for (Room r : allRooms) {
            char f = r.getRoomNumber().charAt(0);
            byFloor.computeIfAbsent(f, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<Character, List<Room>> entry : byFloor.entrySet()) {
            floors.add(buildFloorPanel("Floor " + entry.getKey(), entry.getValue()));
            floors.add(Box.createVerticalStrut(7));
        }

        JScrollPane scroll = new JScrollPane(floors);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 238, 228));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    private JPanel buildFloorPanel(String name, List<Room> rooms) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(new Color(232, 215, 190));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 100, 50), 1),
            name, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(80, 45, 15)
        ));

        for (Room r : rooms) {
            panel.add(buildRoomButton(r));
        }
        return panel;
    }

    private JButton buildRoomButton(Room r) {
        boolean avail = r.isAvailable();
        Color bgColor  = avail ? new Color(46, 125, 50)  : new Color(198, 40, 40);
        Color bdrColor = avail ? new Color(28,  90, 32)  : new Color(140, 25, 25);

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout(0, 2));
        btn.setPreferredSize(new Dimension(88, 58));
        btn.setBackground(bgColor);
        btn.setBorder(BorderFactory.createLineBorder(bdrColor, 2));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(
            (availableOnly && !avail) ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR
        ));

        JLabel numLbl = new JLabel(r.getRoomNumber(), SwingConstants.CENTER);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        numLbl.setForeground(Color.WHITE);

        JLabel typeLbl = new JLabel(r.getType(), SwingConstants.CENTER);
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        typeLbl.setForeground(new Color(220, 220, 220));
        typeLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));

        btn.add(numLbl,  BorderLayout.CENTER);
        btn.add(typeLbl, BorderLayout.SOUTH);

        // In availableOnly mode, grey out booked rooms
        if (availableOnly && !avail) {
            btn.setBackground(new Color(160, 160, 160));
            btn.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 2));
            numLbl.setForeground(new Color(200, 200, 200));
        }

        btn.addActionListener(e -> onRoomClick(btn, r, avail));
        return btn;
    }

    private void onRoomClick(JButton btn, Room r, boolean avail) {
        if (availableOnly && !avail) return;

        // Reset previous selection border using the PREVIOUS room's availability
        if (lastClicked != null && lastRoom != null) {
            boolean prevAvail = lastRoom.isAvailable();
            if (availableOnly && !prevAvail) {
                // greyed-out booked room in customer mode
                lastClicked.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 2));
            } else {
                Color bdr = prevAvail ? new Color(28, 90, 32) : new Color(140, 25, 25);
                lastClicked.setBorder(BorderFactory.createLineBorder(bdr, 2));
            }
        }
        // Highlight selected
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3));
        lastClicked = btn;
        lastRoom = r;

        selectedRoom = r;

        String status = avail
            ? "<font color='#2E7D32'><b>✔ Available</b></font>"
            : "<font color='#C62828'><b>✘ Booked</b></font>";

        detailLabel.setText(String.format(
            "<html><center>"
            + "Room: <b>%s</b> &nbsp;|&nbsp; "
            + "Type: <b>%s</b> &nbsp;|&nbsp; "
            + "Price: <b>$%.0f / night</b> &nbsp;|&nbsp; "
            + "Status: %s"
            + "</center></html>",
            r.getRoomNumber(), r.getType(), r.getPrice(), status
        ));

        confirmBtn.setEnabled(avail || !availableOnly);
    }

    // ── SOUTH: detail + buttons ──────────────────────────────────
    private JPanel buildSouth() {
        JPanel south = new JPanel(new BorderLayout(0, 0));

        // Detail strip
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(new Color(220, 200, 170));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(160, 120, 60)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        detailLabel = new JLabel(
            "<html><center><i>Click on a room to see its details</i></center></html>");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailLabel.setForeground(new Color(80, 45, 15));
        detailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailPanel.add(detailLabel, BorderLayout.CENTER);
        south.add(detailPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        btnRow.setBackground(new Color(90, 52, 20));

        confirmBtn = new JButton("Select This Room");
        confirmBtn.setEnabled(false);
        styleDialogBtn(confirmBtn, new Color(46, 125, 50));
        confirmBtn.addActionListener(e -> dispose());

        JButton cancelBtn = new JButton("Cancel");
        styleDialogBtn(cancelBtn, new Color(198, 40, 40));
        cancelBtn.addActionListener(e -> { selectedRoom = null; dispose(); });

        btnRow.add(confirmBtn);
        btnRow.add(cancelBtn);
        south.add(btnRow, BorderLayout.SOUTH);
        return south;
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private JLabel legendChip(String text, Color color) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.WHITE);
        l.setOpaque(true);
        l.setBackground(color);
        l.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        return l;
    }

    private void styleDialogBtn(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(180, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public Room getSelectedRoom() { return selectedRoom; }
}
