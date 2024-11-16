import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {

    public static void login() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String inputName = scanner.nextLine();
        System.out.print("Enter password: ");
        String inputPass = scanner.nextLine();

        String url = "jdbc:mysql://localhost:3306/bususer";
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";

        try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, inputName);
            pst.setString(2, inputPass);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                boolean isAdmin = rs.getBoolean("is_admin");
                System.out.println("Login successful! Welcome, " + inputName);

                if (isAdmin) {
                    System.out.println("Access granted: Admin Mode");
                    adminOperations(scanner); 
                } else {
                    userOperations(inputName, scanner); 
                }
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
// admin operation
    private static void adminOperations(Scanner scanner) throws SQLException {
        int option = -1;
        while (option != 4) {
            System.out.println("Do you want to:");
            System.out.println("(1) Add Bus");
            System.out.println("(2) Update Bus Details");
            System.out.println("(3) View Booking Details");
            System.out.println("(4) Exit");
            System.out.print("Enter your choice: ");

            if (scanner.hasNextInt()) {
                option = scanner.nextInt();
                scanner.nextLine(); 

                switch (option) {
                    case 1:
                        Bus.addBus(scanner);
                        break;
                    case 2:
                        System.out.print("Enter bus_id: ");
                        if (scanner.hasNextInt()) {
                            int busId = scanner.nextInt();
                            scanner.nextLine();
                            Bus.updateBus(busId, scanner);
                        } else {
                            System.out.println("Invalid bus_id. Please enter a valid integer.");
                            scanner.nextLine();
                        }
                        break;
                    case 3:
                        Bus.viewBookingDetails();
                        break;
                    case 4:
                        System.out.println("Exiting the program.");
                        break;
                    default:
                        System.out.println("Invalid option. Please select between 1 and 4.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number between 1 and 4.");
                scanner.nextLine(); 
            }

            System.out.println();
        }
    }
// normal user operation
    private static void userOperations(String username, Scanner scanner) throws Exception {
        Booking.showBookingStatus(username);
        if (Payment.hasPendingPayments(username)) {
            System.out.println("You have a pending payment. Would you like to update it? (1) Yes (2) No");
            int choice = scanner.nextInt();
            if (choice == 1) {
                Payment.updatePendingPayment(username, scanner);
            }
        }
        System.out.println("Do you want to (1) Book Tickets (2) Cancel Booking (3) Exit ?");
        
        int option = scanner.nextInt();
        switch (option) {
            case 1:
                Bus.viewBuses();
                Booking.bookTickets(username);
                break;
            case 2:
                scanner.nextLine(); 
                System.out.print("Enter your Passenger name: ");
                String passengerName = scanner.nextLine().toUpperCase();

                System.out.print("Enter your journey date (dd-MM-yyyy): ");
                String journeyDate = scanner.nextLine();
                System.out.print("Enter bus ID: ");
                int busId = scanner.nextInt();

                Booking.cancelBooking(passengerName, busId, journeyDate);
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid option.");
        }
    }
}
