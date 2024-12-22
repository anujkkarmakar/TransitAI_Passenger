package com.example.navixpassanger.pdf;

import static com.example.navixpassanger.pdf.AddTermsAndConditions.addTermsAndConditions;

import android.content.Context;
import android.graphics.Bitmap;

import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

public class PDFGenerator {

    private static void addTableRow(Table table, String key, String value) {
        table.addCell(new Cell().add(new Paragraph(key)).setBold());
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    public static File generateTicketPDF(Context context, Map<String, Object> bookingData) {
        File pdfFile = new File(context.getCacheDir(), "ticket_" + bookingData.get("pnr") + ".pdf");

        try {
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // First page with ticket details
            addTicketDetails(document, bookingData);

            // Start new page for Terms and Conditions
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            addTermsAndConditions(document);

            document.close();
            return pdfFile;
        } catch (IOException | FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Separate method for ticket details
    private static void addTicketDetails(Document document, Map<String, Object> bookingData) {
        // Add header
        Paragraph header = new Paragraph("Navix Passenger Information")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        // Add journey details
        document.add(new Paragraph(String.format("%s to %s",
                bookingData.get("fromStop"),
                bookingData.get("toStop")))
                .setTextAlignment(TextAlignment.CENTER));

        // Add PNR
        document.add(new Paragraph("PNR: " + bookingData.get("pnr"))
                .setTextAlignment(TextAlignment.RIGHT));

        // Create two-column layout
        float[] columnWidths = {2, 1};
        Table layout = new Table(UnitValue.createPercentArray(columnWidths));
        layout.setWidth(UnitValue.createPercentValue(100));

        // Left column: Ticket details
        Cell leftCell = new Cell();
        Table detailsTable = new Table(2);
        detailsTable.setWidth(UnitValue.createPercentValue(100));

        addTableRow(detailsTable, "Passenger Name", (String) bookingData.get("userName"));
        addTableRow(detailsTable, "Journey Type", (String) bookingData.get("journeyType"));
        addTableRow(detailsTable, "Bus Type", (String) bookingData.get("busType"));
        addTableRow(detailsTable, "Mobile", (String) bookingData.get("mobileNumber"));
        addTableRow(detailsTable, "Fare", "₹" + String.format("%.2f", bookingData.get("fare")));
        addTableRow(detailsTable, "Booking Date", (String) bookingData.get("timestamp"));

        leftCell.add(detailsTable);
        layout.addCell(leftCell);

        // Right column: QR Code
        Cell rightCell = new Cell();
        // Add QR code
        Bitmap qrBitmap = QRGenerator.generateQRCode(bookingData);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] qrBytes = stream.toByteArray();
        ImageData qrImageData = ImageDataFactory.create(qrBytes);
        Image qrImage = new Image(qrImageData);
        qrImage.setWidth(150);
        qrImage.setHeight(150);
        rightCell.add(qrImage);
        layout.addCell(rightCell);

        document.add(layout);

        // Add footer for first page
        document.add(new Paragraph("Scan QR code to verify ticket details")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());
        document.add(new Paragraph("Thank you for choosing Navix!")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());
    }
}