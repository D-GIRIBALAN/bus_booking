import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

public class Bus 
{
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bususer";
    private static final String USERNAME = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");
    // add new bus details
    public static void addBus(Scanner scanner) throws SQLException {
    
        System.out.print("Enter bus id: ");
        int busId = scanner.nextInt(); 
        scanner.nextLine(); 
    
        System.out.print("Enter bus number: ");
        String busNumber = scanner.nextLine();
    
        
        Connection con = DriverManager.getConnection(DB_URL,USERNAME, PASSWORD);
        String checkQuery = "SELECT COUNT(*) FROM bus WHERE bus_id = ? OR bus_number = ?";
        PreparedStatement checkPst = con.prepareStatement(checkQuery);
        checkPst.setInt(1, busId);
        checkPst.setString(2, busNumber);
    
        ResultSet rs = checkPst.executeQuery();
        rs.next();
        if (rs.getInt(1) > 0) {
            System.out.println("A bus with this ID or number already exists. Entry not saved.");
            con.close();
            return; 
        }
        System.out.print("Is this an AC bus? (true/false): ");
        String acInput = scanner.nextLine().trim().toLowerCase();
        boolean ac = Boolean.parseBoolean(acInput);
    
        System.out.print("Is this a sleeper bus? (true/false): ");
        String sleeperInput = scanner.nextLine().trim().toLowerCase();
        boolean sleeper = Boolean.parseBoolean(sleeperInput);
    
        System.out.print("Enter bus capacity: ");
        int capacity = scanner.nextInt();
    
        System.out.print("Enter the price per seat: ");
        int price = scanner.nextInt();
        scanner.nextLine();
    
        System.out.print("Enter the source: ");
        String source = scanner.nextLine();
    
        System.out.print("Enter the destination: ");
        String destination = scanner.nextLine();
    
        System.out.print("Enter the duration: ");
        String duration = scanner.nextLine();
    
        String query = "INSERT INTO bus (bus_id, bus_number, capacity, sleeper, ac, price, source, destination, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setInt(1, busId);
        pst.setString(2, busNumber);
        pst.setInt(3, capacity);
        pst.setBoolean(4, sleeper);
        pst.setBoolean(5, ac);
        pst.setInt(6, price);
        pst.setString(7, source);
        pst.setString(8, destination);
        pst.setString(9, duration);
    
        int rows = pst.executeUpdate();
        if (rows > 0) {
            System.out.println("Bus added successfully!");
        } else {
            System.out.println("Failed to add bus.");
        }
    
        con.close();
    }
    
// update bus details
public static void updateBus(int busId,Scanner scanner) throws SQLException {
    String url = "jdbc:mysql://localhost:3306/bususer";
    String username = System.getenv("DB_USERNAME"); 
    String password = System.getenv("DB_PASSWORD"); 
    System.out.print("Enter new capacity: ");
    int newCapacity = scanner.nextInt();
    System.out.print("Is the bus AC now? (true/false): ");
    boolean ac = scanner.nextBoolean();
    System.out.print("Is the bus a sleeper bus now? (true/false): ");
    boolean sleeper = scanner.nextBoolean();
    System.out.print("Enter the price per seat: ");
    int price = scanner.nextInt();
    scanner.nextLine();
    System.out.print("Enter the source: ");
    String source = scanner.nextLine();
    System.out.print("Enter the destination: ");
    String destination = scanner.nextLine();
    System.out.print("Enter the duration: ");
    String duration = scanner.nextLine();
    Connection con = DriverManager.getConnection(url, username, password);
    String query = "UPDATE bus SET ac = ?, sleeper = ?, capacity = ?, price = ?,source = ?,destination = ?,duration = ? WHERE bus_id = ?";
    PreparedStatement pst = con.prepareStatement(query);
    pst.setBoolean(1, ac);
    pst.setBoolean(2, sleeper);
    pst.setInt(3, newCapacity);
    pst.setInt(4, price);
    pst.setString(5, source);
    pst.setString(6, destination);
    pst.setString(7, duration);
    pst.setInt(8, busId);
    
    int rows = pst.executeUpdate();
    if (rows > 0) {
        System.out.println("Bus updated successfully!");
    } else {
        System.out.println("Failed to update bus.");
    }
    con.close();
}
// view bus details
public static void viewBuses() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/bususer";
    String username = System.getenv("DB_USERNAME"); 
    String password = System.getenv("DB_PASSWORD"); 
    Connection con = DriverManager.getConnection(url, username, password);
    String query = "SELECT * FROM Bus"; 
    PreparedStatement pst = con.prepareStatement(query); 
    ResultSet rs = pst.executeQuery(); 

    System.out.println("Bus Details:");
    System.out.println("------------------------------------------");

    while (rs.next()) {
        int busId = rs.getInt("bus_id");
        String busNumber = rs.getString("bus_number");
        boolean ac = rs.getBoolean("ac");
        boolean sleeper = rs.getBoolean("sleeper");
        int capacity = rs.getInt("capacity");
        int price = rs.getInt("price"); 
        String source = rs.getString("source");
        String destination = rs.getString("destination");
        String duration = rs.getString("duration");
        System.out.println("Bus ID: " + busId);
        System.out.println("Bus Number: " + busNumber);
        System.out.println("AC: " + (ac ? "Yes" : "No"));
        System.out.println("Sleeper: " + (sleeper ? "Yes" : "No"));
        System.out.println("Capacity: " + capacity);
        System.out.println("Price per seat: " + price);
        System.out.println("Source: " + source);
        System.out.println("Destination: " + destination);
        System.out.println("duration: " + duration);
        System.out.println("------------------------------------------");
    }

    con.close(); 
}
public static void viewBookingDetails() {
    String query = "SELECT booking_id, passenger_name, bus_id, seat_number, number_of_seats, total_price, payment_status, journey_date FROM Booking";

    try (Connection con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
         PreparedStatement pst = con.prepareStatement(query);
         ResultSet rs = pst.executeQuery()) {

        System.out.println("Booking Details:");
        System.out.println("---------------------------------------------------------");
        while (rs.next()) {
            int bookingId = rs.getInt("booking_id");
            String username = rs.getString("passenger_name");
            int busId = rs.getInt("bus_id");
            int seatNumber = rs.getInt("seat_number");
            int numberOfSeats = rs.getInt("number_of_seats");
            double totalPrice = rs.getDouble("total_price");
            String paymentStatus = rs.getString("payment_status");
            Date journeyDate = rs.getDate("journey_date");

            System.out.println("Booking ID: " + bookingId);
            System.out.println("Passenger Username: " + username);
            System.out.println("Bus ID: " + busId);
            System.out.println("Seat Number: " + seatNumber);
            System.out.println("Number of Seats: " + numberOfSeats);
            System.out.println("Total Price: $" + totalPrice);
            System.out.println("Payment Status: " + paymentStatus);
            System.out.println("Journey Date: " + journeyDate);
            System.out.println("---------------------------------------------------------");
        }
    } catch (SQLException e) {
        System.out.println("Error retrieving booking details.");
        e.printStackTrace();
    }
}


}
