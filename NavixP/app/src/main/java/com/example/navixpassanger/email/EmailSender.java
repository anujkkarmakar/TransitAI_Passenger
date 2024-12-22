package com.example.navixpassanger.email;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.navixpassanger.ticket.Ticket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String email = "anujgamerz2002@gmail.com"; // Sender's email
    private static final String password = "fpzw uztq bnkr rgmb";       // Sender's password

    public static void sendCancellationEmail(Context context, Ticket ticket, double refundAmount) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");  // Use SSL for port 465
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.socketFactory.port", "465");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        // Execute email sending in background
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    // Create message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(email));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(ticket.getUserEmail()));
                    message.setSubject("Ticket Cancellation Confirmation - PNR: " + ticket.getPnr());
                    message.setContent(createEmailContent(ticket, refundAmount), "text/html; charset=utf-8");

                    // Send message
                    Transport.send(message);
                    return true;
                } catch (MessagingException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(context, "Cancellation email sent successfully",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to send cancellation email",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private static String createEmailContent(Ticket ticket, double refundAmount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #1976D2;'>Ticket Cancellation Confirmation</h2>" +
                "<p>Dear Passenger,</p>" +
                "<p>Your ticket has been successfully cancelled. Here are the details:</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>PNR:</strong> " + ticket.getPnr() + "</p>" +
                "<p><strong>Route:</strong> " + ticket.getFromStop() + " → " + ticket.getToStop() + "</p>" +
                "<p>We understand plans can change and appreciate your understanding of our " +
                "cancellation policy. We hope to serve you again soon.</p>" +
                "<p>If you have any questions about your refund, please don't hesitate to contact " +
                "our customer support.</p>" +
                "<p>Thank you for choosing our service!</p>" +
                "<p style='margin-top: 20px;'>Best regards,<br>Bus Service Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}