//package main.java.util;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DBConnection {
//    private static final String URL = "jdbc:postgresql://ec2-63-176-52-116.eu-central-1.compute.amazonaws.com:5432/reddit";
//    private static final String USER = "user";
//    private static final String PASSWORD = "password";
//
//    static {
//        try {
//            Class.forName("org.postgresql.Driver");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
//        }
//    }
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL, USER, PASSWORD);
//    }
//}