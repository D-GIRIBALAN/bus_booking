import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Booking {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bususer";
    private static final String USERNAME = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");
    
    public static void bookTickets(String user) throws Exception {
        int bookingId = 0; 
        Scanner scanner = new Scanner(System.in);
        String name;
    
        while (true) {
            System.out.print("Enter your name: ");
              name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please enter a valid name.");
                continue;
            }
            if (!name.matches("[a-zA-Z\\s]+")) {
                System.out.println("Invalid name. Only letters and spaces are allowed.");
                continue;
            }
            break;
        }
        String username = name.toUpperCase();
        System.out.print("Enter bus ID: ");
        int busId = scanner.nextInt();
    
        System.out.print("Enter number of seats: ");
        int numberOfSeats = scanner.nextInt();
    
        System.out.print("Enter journey date (dd-MM-yyyy): ");
        String journeyDateStr = scanner.next();
    
        // Parse the journey date
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = formatter.parse(journeyDateStr);
        java.sql.Date journeyDate = new java.sql.Date(date.getTime());

        // Check if the journey date is valid (not in the past)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        if (date.before(today)) {
            System.out.println("Invalid booking: Journey date is in the past.");
            scanner.close();
            return; 
        }
    
        int busCapacity = getBusCapacity(busId);
        int bookedSeatsForDate = getBookedSeatsForDate(busId, journeyDate);
    
        if (bookedSeatsForDate + numberOfSeats > busCapacity) {
            System.out.println("Not enough available seats for the selected date. Try another bus or date.");
            scanner.close();
            return;
        }
    
        double pricePerSeat = getPricePerSeat(busId);
        double totalPrice = pricePerSeat * numberOfSeats; 
    
        int seatCount = 0;
        for (int seat = 1; seat <= busCapacity && seatCount < numberOfSeats; seat++) {
            if (!isSeatBooked(busId, seat, journeyDate)) {
                String query = "INSERT INTO booking (passenger_name, bus_id, seat_number, number_of_seats, total_price, payment_status, journey_date, username) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
                try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                     PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setString(1, username);
                    pst.setInt(2, busId);
                    pst.setInt(3, seat);
                    pst.setInt(4, numberOfSeats);
                    pst.setDouble(5, totalPrice);
                    pst.setString(6, "Pending");
                    pst.setDate(7, journeyDate);
                    pst.setString(8, user);
                    pst.executeUpdate();

                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        bookingId = generatedKeys.getInt(1); // Retrieve the generated bookingId
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                seatCount++;
            }
        }

        System.out.println("Do you want to pay (1) Now (2) Later?");
        int choice = scanner.nextInt();
        if (choice == 1) {
            double amountPaid = Payment.processPayment(totalPrice, user, busId, journeyDate, scanner);
            if (amountPaid >= totalPrice) {
                System.out.println("Full payment received. Generating ticket...");
                generateTicket(bookingId); 
            } else {
                System.out.println("Full payment not received. Booking will remain pending.");
            }
        }

        if (seatCount == numberOfSeats) {
            System.out.println("Booking successful! " + numberOfSeats + " seats have been booked.");
        } else {
            System.out.println("Booking failed due to unavailable seats.");
        }

    }

    public static int getBookedSeatsForDate(int busId, java.sql.Date journeyDate) throws Exception {
        String query = "SELECT COUNT(*) FROM booking WHERE bus_id = ? AND journey_date = ?";
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, busId);
            pst.setDate(2, journeyDate);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); 
            }
        }
        return 0;
    }

    public static boolean isSeatBooked(int busId, int seatNumber, java.sql.Date journeyDate) throws Exception {
        String query = "SELECT * FROM booking WHERE bus_id = ? AND seat_number = ? AND journey_date = ?";
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, busId);
            pst.setInt(2, seatNumber);
            pst.setDate(3, journeyDate);
            ResultSet rs = pst.executeQuery();
            return rs.next(); 
        }
    }

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

    public static int getBusCapacity(int busId) throws Exception {
        String query = "SELECT capacity FROM bus WHERE bus_id = ?";
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, busId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("capacity");
            }
        }
        return 0;
    }
    
    public static void showBookingStatus(String username) throws Exception {
        String query = "SELECT * FROM booking WHERE username = ?";
        
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(query)) {
            
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            boolean hasBookings = false;
            
            System.out.println("Booking status for user: " + username);
            
            while (rs.next()) {
                hasBookings = true;
                int id = rs.getInt("booking_id");
                int busId = rs.getInt("bus_id");
                String name = rs.getString("passenger_name");
                int seatNumber = rs.getInt("seat_number");
                int numberOfSeats = rs.getInt("number_of_seats");
                double totalPrice = rs.getDouble("total_price");
                String paymentStatus = rs.getString("payment_status");
                Date journeyDate = rs.getDate("journey_date");

                System.out.println("-----------------------------------");
                System.out.println("Booking ID: " + id);
                System.out.println("Bus ID: " + busId);
                System.out.println("Passenger Name: " + name);
                System.out.println("Seat Number: " + seatNumber);
                System.out.println("Number of Seats: " + numberOfSeats);
                System.out.println("Total Price: " + totalPrice);
                System.out.println("Payment Status: " + paymentStatus);
                System.out.println("Journey Date: " + journeyDate);
                System.out.println("-----------------------------------");
            }
            
            if (!hasBookings) {
                System.out.println("No bookings found for user: " + username);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void cancelBooking(String username, int busId, String journeyDateStr) throws Exception {
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = formatter.parse(journeyDateStr);
        java.sql.Date journeyDate = new java.sql.Date(date.getTime());
    
       
        String checkQuery = "SELECT total_price FROM booking WHERE passenger_name = ? AND bus_id = ? AND journey_date = ?";
        String deleteQuery = "DELETE FROM booking WHERE passenger_name = ? AND bus_id = ? AND journey_date = ?";
    
        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement checkPst = con.prepareStatement(checkQuery);
             PreparedStatement deletePst = con.prepareStatement(deleteQuery)) {
            
            
            checkPst.setString(1, username);
            checkPst.setInt(2, busId);
            checkPst.setDate(3, journeyDate);
            
            ResultSet rs = checkPst.executeQuery();
    
            if (rs.next()) {

                double totalPrice = rs.getDouble("total_price");
    
                
                deletePst.setString(1, username);
                deletePst.setInt(2, busId);
                deletePst.setDate(3, journeyDate);
                
                int rowsAffected = deletePst.executeUpdate();
    
                if (rowsAffected > 0) {
                    System.out.println("Booking canceled successfully.");
                    System.out.println("Amount refunded: " + totalPrice);
                } else {
                    System.out.println("Failed to cancel the booking. Please try again.");
                }
            } else {
                System.out.println("No booking found for the provided details.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } 
    public static void generateTicket(int bookingId) throws Exception {
        String ticketQuery = "SELECT passenger_name, bus_id, seat_number, journey_date, total_price "
                           + "FROM booking WHERE booking_id = ?";

        try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pst = con.prepareStatement(ticketQuery)) {

            pst.setInt(1, bookingId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String passengerName = rs.getString("passenger_name");
                int busId = rs.getInt("bus_id");
                int seatNumber = rs.getInt("seat_number");
                Date journeyDate = rs.getDate("journey_date");
                double totalPrice = rs.getDouble("total_price");

                System.out.println("------------ Ticket ------------");
                System.out.println("Booking ID: " + bookingId);
                System.out.println("Passenger Name: " + passengerName);
                System.out.println("Bus ID: " + busId);
                System.out.println("Seat Number: " + seatNumber);
                System.out.println("Journey Date: " + journeyDate);
                System.out.println("Total Price: " + totalPrice);
                System.out.println("Status: Confirmed");
                System.out.println("Thank you for booking with us! Safe travels.");
                System.out.println("--------------------------------");
            } else {
                System.out.println("No booking found for the provided Booking ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
