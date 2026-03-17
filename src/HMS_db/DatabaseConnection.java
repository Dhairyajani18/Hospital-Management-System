package HMS_db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    static final String URL = "jdbc:mysql://localhost:3306/Hospital";
    static final String USER = "root";
    static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database!");
            return conn;
        } catch (SQLException e)
        {
            System.out.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }
    }
}