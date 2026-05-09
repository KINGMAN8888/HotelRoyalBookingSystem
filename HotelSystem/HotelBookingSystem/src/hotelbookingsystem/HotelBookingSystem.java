package hotelbookingsystem;

public class HotelBookingSystem {

    public static void main(String[] args) {
        // Inject FlatLaf for modern UI Magic
        try {
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            // Optional: You can configure global UI properties here if needed.
        } catch (Exception ex) {
            System.err.println("Failed to initialize modern LaF");
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IndexPage().setVisible(true);
            }
        });
    }
}
