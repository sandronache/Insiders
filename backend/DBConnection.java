import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://insiders.cvuoiq842szj.eu-central-1.rds.amazonaws.com:3306/insiders?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "admin";
    private static final String PASS = "Insiders2025";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed:");
            e.printStackTrace();
        }
    }
}