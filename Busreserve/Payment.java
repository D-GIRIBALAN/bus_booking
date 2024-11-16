import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Payment 
{
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bususer";
    private static final String USERNAME = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static double getPricePerSeat(int busId) throws Exception {
        String query = "SELECT price FROM bus WHERE bus_id = ?";
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, busId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        }
        return 0;
    }
    public static double processPayment(double totalPrice, String username, int busId, java.sql.Date journeyDate, Scanner scanner) {
    double amountPaid = 0.0;
    boolean paymentCompleted = false;

    while (amountPaid < totalPrice) {
        System.out.println("Total Price: " + totalPrice);
        System.out.println("Amount Paid So Far: " + amountPaid);

        double remainingAmount = totalPrice - amountPaid;
        System.out.println("Remaining Amount: " + remainingAmount);
        System.out.print("Enter amount to pay: ");

        try {
            double payment = scanner.nextDouble();
            if (payment > remainingAmount) {
                amountPaid += remainingAmount;
                double extraAmount = payment - remainingAmount;
                System.out.println("Payment complete. Total paid: " + amountPaid);
                System.out.println("Extra amount received: " + extraAmount + ". This amount will be refunded.");
                paymentCompleted = true;
                break;
            } else {
                amountPaid += payment;
                if (amountPaid >= totalPrice) {
                    System.out.println("Payment complete. Total paid: " + amountPaid);
                    paymentCompleted = true;
                    break;
                } else {
                    System.out.println("Partial payment received. Remaining amount: " + (totalPrice - amountPaid));
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid amount.");
            scanner.next();
        }
    }

    if (paymentCompleted) {
        updatePaymentStatus(username, busId, journeyDate, "Paid");
    }
    
    return amountPaid;
}

    public static void updatePaymentStatus(String username, int busId, java.sql.Date journeyDate, String status) {
        String query = "UPDATE booking SET payment_status = ? WHERE username = ? AND bus_id = ? AND journey_date = ?";
        
        try {
            Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query);
            
            pst.setString(1, status);
            pst.setString(2, username);
            pst.setInt(3, busId);
            pst.setDate(4, journeyDate);
            int rowsAffected = pst.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Payment status updated to: " + status);
            } else {
                System.out.println("Failed to update payment status. Please check booking details.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean hasPendingPayments(String username) throws Exception {
        String query = "SELECT COUNT(*) FROM booking WHERE username = ? AND payment_status = 'Pending'";
        
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; 
            }
        }
        return false;
    }
    
    public static void updatePendingPayment(String username, Scanner scanner) throws Exception {
        String query = "SELECT booking_id,bus_id, journey_date, total_price FROM booking WHERE username = ? AND payment_status = 'Pending'";
        
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                int busId = rs.getInt("bus_id");
                java.sql.Date journeyDate = rs.getDate("journey_date");
                double totalPrice = rs.getDouble("total_price");
                System.out.println("Pending payment found for Bus ID: " + busId + ", Journey Date: " + journeyDate + ", Total Price: " + totalPrice);

            double amountPaid = processPayment(totalPrice, username, busId, journeyDate,scanner);
            if (amountPaid >= totalPrice) {
                System.out.println("Full payment received. Generating ticket...");

                Booking.generateTicket(bookingId);
            } else {
                System.out.println("Partial payment received. Booking status remains pending.");
            }
                
            }
        }
    }

    
}
