package server;

import java.sql.*;

public class JDBC {

    private Connection connection;

    public void runDB() {

        try {
//            connection = DriverManager.getConnection("jdbc:mysql://localhost/chatter", "your username here", "your password here");
            // Create the db if it doesn't exist
            String createDB = "CREATE DATABASE IF NOT EXISTS chatter";
            Statement statement = connection.createStatement();
            statement.executeQuery(createDB);
            System.out.println("Database chatter created successfully");

            // Create a table of users
            System.out.println("Creating a users table...");
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users(" +
                    "name varchar(45) NOT NULL," +
                    "PRIMARY KEY(name))";
            statement = connection.createStatement();
            statement.executeQuery(createUsersTable);
            System.out.println("Table users created successfully");

            // Fill the table with users
            String insertUsers = "INSERT INTO users VALUES ('Adam'), ('Bob'), ('Joe'), ('Mary'), ('George')";
            statement.executeQuery(insertUsers);
            System.out.println("Users added to table");

            // View users table
            String viewUsers = "SELECT * FROM users";
            ResultSet users = statement.executeQuery(viewUsers);
            while (users.next()) {
                String name = users.getString("name");
                System.out.println("Name: " + name);
            }

        } catch (SQLException e) {
            System.err.println("Error connecting to database");
            e.printStackTrace();
        }
    }

    void closeDB() throws SQLException {
        connection.close();
    }

    boolean isUser(String name) {
        String query = "SELECT 1 FROM users WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            return false;
        }
    }
}
