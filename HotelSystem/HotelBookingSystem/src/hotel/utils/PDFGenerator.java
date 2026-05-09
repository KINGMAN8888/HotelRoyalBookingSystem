package hotel.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    public static String generateInvoice(int bookingId, String customerName, String roomNo, 
                                       String type, String checkIn, String checkOut, 
                                       String nights, String amount, String status) {
        // Ensure invoices directory exists
        File outDir = new File("invoices");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        String fileName = "invoices/Invoice_Booking_" + bookingId + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(200, 750);
                contentStream.showText("HOTEL ROYAL INVOICE");
                contentStream.endText();

                // Date
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 710);
                String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                contentStream.showText("Generated On: " + currentDate);
                contentStream.endText();

                // Invoice Details
                int startY = 650;
                int lh = 25; // line height
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 14);
                contentStream.newLineAtOffset(50, startY);
                
                contentStream.showText("Booking ID: " + bookingId);
                contentStream.newLineAtOffset(0, -lh);
                contentStream.showText("Customer Name: " + customerName);
                contentStream.newLineAtOffset(0, -lh);
                contentStream.showText("Room Number: " + roomNo + " (" + type + ")");
                contentStream.newLineAtOffset(0, -lh);
                contentStream.showText("Check-In Date: " + checkIn);
                contentStream.newLineAtOffset(0, -lh);
                contentStream.showText("Check-Out Date: " + checkOut);
                contentStream.newLineAtOffset(0, -lh);
                contentStream.showText("Total Nights: " + nights);
                contentStream.newLineAtOffset(0, -lh);
                contentStream.showText("Status: " + status);
                contentStream.newLineAtOffset(0, -lh * 2);
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.showText("Total Amount: $" + amount);
                
                contentStream.endText();
                
                // Footer
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                contentStream.newLineAtOffset(200, 100);
                contentStream.showText("Thank you for choosing Hotel Royal!");
                contentStream.endText();
            }

            document.save(fileName);
            return fileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
