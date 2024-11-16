import java.sql.*;
import java.util.Scanner;
public class BusMain
{
    public static void main(String[] args) throws Exception 
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to KEC bus reservation system");
        System.out.println("Do you want to (1) Sign up or (2) Login?");
        int choice = scanner.nextInt();
        
        if (choice == 1) {
            signup();
        } else if (choice == 2) {
            Login.login();
        } else {
            System.out.println("Invalid choice!");
        }
        scanner.close();
    }
    public static void signup() throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter new username: ");
        String name = scanner.nextLine();
        System.out.print("Enter new password: ");
        String pass = scanner.nextLine();
        String url = "jdbc:mysql://localhost:3306/bususer";
        String username = System.getenv("DB_USERNAME"); 
        String password = System.getenv("DB_PASSWORD"); 
        
        String checkQuery = "SELECT * FROM user WHERE username = ?";
        Connection con = DriverManager.getConnection(url, username, password);
        PreparedStatement checkPst = con.prepareStatement(checkQuery);
        checkPst.setString(1, name);
        ResultSet rs = checkPst.executeQuery();

        if (rs.next()) {
            System.out.println("Username already exists. Please try a different one.");
        } else {
            String query = "INSERT INTO user (username, password) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, pass);
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Sign-up successful! You can now log in.");
                Login.login();
            } else {
                System.out.println("Sign-up failed. Please try again.");
            }
        }
        scanner.close();
        con.close();
    }
}