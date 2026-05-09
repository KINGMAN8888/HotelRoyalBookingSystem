package hotelbookingsystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class InvoiceDialog extends JDialog {

    // row: [0=bookingId, 1=customerName, 2=roomNumber, 3=roomType,
    //       4=checkIn,   5=checkOut,     6=amount,     7=status,
    //       8=roomId,    9=pricePerNight]
    private Object[] dataRow;

    public InvoiceDialog(Window parent, Object[] row) {
        super(parent, "Invoice", ModalityType.APPLICATION_MODAL);
        this.dataRow = row;
        buildUI(row);
    }

    // Also accept Frame (backward compat)
    public InvoiceDialog(Frame parent, Object[] row) {
        super(parent, "Invoice", true);
        this.dataRow = row;
        buildUI(row);
    }

    private void buildUI(Object[] row) {
        int    bookingId     = (Integer) row[0];
        String customerName  = (String)  row[1];
        String roomNumber    = (String)  row[2];
        String roomType      = (String)  row[3];
        String checkIn       = (String)  row[4];
        String checkOut      = (String)  row[5];
        String status        = (String)  row[7];
        double pricePerNight = (row.length > 9 && row[9] != null)
            ? ((Number) row[9]).doubleValue() : 0.0;

        long   nights      = calcNights(checkIn, checkOut);
        double totalAmount = (nights > 0 && pricePerNight > 0)
            ? nights * pricePerNight
            : Double.parseDouble(((String) row[6]).replace(",", ""));

        setSize(480, 560);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(80, 45, 15));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel hotelLbl = new JLabel("HOTEL ROYAL");
        hotelLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        hotelLbl.setForeground(Color.WHITE);
        hotelLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subLbl = new JLabel("Official Invoice");
        subLbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subLbl.setForeground(new Color(220, 200, 160));
        subLbl.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(hotelLbl, BorderLayout.CENTER);
        header.add(subLbl,   BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // ── Body ─────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(250, 245, 235));
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        body.add(infoRow("Booking #",      String.valueOf(bookingId)));
        body.add(infoRow("Date Issued",    LocalDate.now().toString()));
        body.add(sep());
        body.add(infoRow("Customer",       customerName));
        body.add(sep());
        body.add(infoRow("Room Number",    roomNumber));
        body.add(infoRow("Room Type",      roomType));
        body.add(infoRow("Check-In",       checkIn));
        body.add(infoRow("Check-Out",      checkOut));
        body.add(infoRow("Nights",         nights > 0 ? String.valueOf(nights) : "—"));
        body.add(sep());
        body.add(infoRow("Price / Night",  pricePerNight > 0
            ? String.format("$ %.2f", pricePerNight) : "—"));
        body.add(totalRow("Total Amount",  String.format("$ %.2f", totalAmount)));
        body.add(infoRow("Status",         status));
        body.add(sep());

        JLabel thanks = new JLabel("Thank you for choosing Hotel Royal!");
        thanks.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        thanks.setForeground(new Color(120, 80, 30));
        thanks.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(Box.createVerticalStrut(8));
        body.add(thanks);

        add(new JScrollPane(body), BorderLayout.CENTER);

        // ── Buttons ──────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        south.setBackground(new Color(90, 52, 20));

        JButton printBtn = makeBtn("Print Invoice", new Color(46, 125, 50));
        JButton closeBtn = makeBtn("Close",         new Color(198, 40, 40));

        // Build final values for lambda
        final int   fBookingId     = bookingId;
        final String fCustomer     = customerName;
        final String fRoom         = roomNumber;
        final String fType         = roomType;
        final String fCheckIn      = checkIn;
        final String fCheckOut     = checkOut;
        final long   fNights       = nights;
        final double fPrice        = pricePerNight;
        final double fTotal        = totalAmount;
        final String fStatus       = status;

        printBtn.addActionListener(e -> doPrint(
            fBookingId, fCustomer, fRoom, fType,
            fCheckIn, fCheckOut, fNights, fPrice, fTotal, fStatus));
        closeBtn.addActionListener(e -> dispose());

        south.add(printBtn);
        south.add(closeBtn);
        add(south, BorderLayout.SOUTH);
    }

    // ── PRINT ─────────────────────────────────────────────────────
    private void doPrint(int id, String customer, String room, String type,
                         String checkIn, String checkOut, long nights,
                         double price, double total, String status) {
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("Hotel Royal Invoice #" + id);

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics;
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            int ix = (int) pageFormat.getImageableX();
            int iy = (int) pageFormat.getImageableY();
            int iw = (int) pageFormat.getImageableWidth();

            // Header bar
            g2.setColor(new java.awt.Color(80, 45, 15));
            g2.fillRect(ix, iy, iw, 58);

            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.setColor(java.awt.Color.WHITE);
            String titleStr = "HOTEL ROYAL";
            g2.drawString(titleStr, ix + (iw - fm.stringWidth(titleStr)) / 2, iy + 33);

            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 12));
            fm = g2.getFontMetrics();
            g2.setColor(new java.awt.Color(220, 200, 160));
            String subStr = "Official Invoice";
            g2.drawString(subStr, ix + (iw - fm.stringWidth(subStr)) / 2, iy + 52);

            // Body
            int lx = ix + 15;
            int vx = ix + 170;
            int cy = iy + 80;
            int lh = 22;

            String[][] lines = {
                {"Booking #:",      String.valueOf(id)},
                {"Date Issued:",    java.time.LocalDate.now().toString()},
                {"Customer:",       customer},
                {"Room Number:",    room},
                {"Room Type:",      type},
                {"Check-In:",       checkIn},
                {"Check-Out:",      checkOut},
                {"Nights:",         nights > 0 ? String.valueOf(nights) : "\u2014"},
                {"Price / Night:",  price > 0 ? String.format("$%.2f", price) : "\u2014"},
            };

            for (String[] pair : lines) {
                g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                g2.setColor(new java.awt.Color(100, 60, 20));
                g2.drawString(pair[0], lx, cy);
                g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
                g2.setColor(new java.awt.Color(40, 40, 40));
                g2.drawString(pair[1], vx, cy);
                cy += lh;
            }

            // Divider
            g2.setColor(new java.awt.Color(200, 175, 140));
            g2.drawLine(lx, cy, ix + iw - 15, cy);
            cy += 8;

            // Total row
            g2.setColor(new java.awt.Color(80, 45, 15));
            g2.fillRect(lx - 5, cy - 4, iw - 20, 28);
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString("Total Amount:", lx, cy + 17);
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
            g2.drawString(String.format("$%.2f", total), vx, cy + 17);
            cy += 35;

            // Status
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
            g2.setColor(new java.awt.Color(100, 60, 20));
            g2.drawString("Status:", lx, cy);
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
            g2.setColor(new java.awt.Color(40, 40, 40));
            g2.drawString(status, vx, cy);
            cy += 30;

            // Footer
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.ITALIC, 12));
            g2.setColor(new java.awt.Color(120, 80, 30));
            String thanks = "Thank you for choosing Hotel Royal!";
            fm = g2.getFontMetrics();
            g2.drawString(thanks, ix + (iw - fm.stringWidth(thanks)) / 2, cy);

            return java.awt.print.Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (java.awt.print.PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                    "Print error: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── UI HELPERS ───────────────────────────────────────────────
    private JPanel infoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(250, 245, 235));
        p.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(100, 60, 20));
        lbl.setPreferredSize(new Dimension(155, 22));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(new Color(40, 40, 40));
        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return p;
    }

    private JPanel totalRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(80, 45, 15));
        p.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(220, 200, 160));
        lbl.setPreferredSize(new Dimension(155, 26));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        val.setForeground(Color.WHITE);
        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return p;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(200, 175, 140));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        return s;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(155, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── DATE PARSING ─────────────────────────────────────────────
    private long calcNights(String checkIn, String checkOut) {
        try {
            LocalDate ci = parseDate(checkIn);
            LocalDate co = parseDate(checkOut);
            if (ci == null || co == null) return 0;
            long n = ChronoUnit.DAYS.between(ci, co);
            return n > 0 ? n : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalDate parseDate(String d) {
        if (d == null || d.isBlank()) return null;
        d = d.trim();
        // Try ISO YYYY-MM-DD
        try { return LocalDate.parse(d); } catch (Exception ignored) {}
        // Try DD/MM/YYYY
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch (Exception ignored) {}
        // Try MM/DD/YYYY
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("MM/dd/yyyy")); } catch (Exception ignored) {}
        // Try DD-MM-YYYY
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd-MM-yyyy")); } catch (Exception ignored) {}
        return null;
    }
}
