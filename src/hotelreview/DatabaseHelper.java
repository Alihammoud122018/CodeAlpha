package hotelreview;

import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:hotel_reviews.db";
    private Connection conn;

    public DatabaseHelper() {
        try {
            conn = DriverManager.getConnection(URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT, password TEXT)";
        String createHotelTable = "CREATE TABLE IF NOT EXISTS hotels (id INTEGER PRIMARY KEY, name TEXT, location TEXT, rating REAL)";
        String createReviewTable = "CREATE TABLE IF NOT EXISTS reviews (id INTEGER PRIMARY KEY, user_id INTEGER, hotel_id INTEGER, rating INTEGER, comment TEXT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createHotelTable);
            stmt.execute(createReviewTable);
        }
    }

    public boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addHotel(String name, String location, double rating) {
        if (rating < 1 || rating > 5) {
            System.out.println("Rating must be between 1 and 5.");
            return false;
        }

        String insertHotelQuery = "INSERT INTO hotels (name, location) VALUES (?, ?)";
        String insertReviewQuery = "INSERT INTO reviews (user_id, hotel_id, rating, comment) VALUES (?, ?, ?, ?)";
        String updateHotelRatingQuery = "UPDATE hotels SET rating = ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

           
            int hotelId;
            try (PreparedStatement insertHotelStmt = conn.prepareStatement(insertHotelQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertHotelStmt.setString(1, name);
                insertHotelStmt.setString(2, location);
                insertHotelStmt.executeUpdate();

                try (ResultSet generatedKeys = insertHotelStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        hotelId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

          
            int userId = 1; 
            try (PreparedStatement insertReviewStmt = conn.prepareStatement(insertReviewQuery)) {
                insertReviewStmt.setInt(1, userId);
                insertReviewStmt.setInt(2, hotelId);
                insertReviewStmt.setDouble(3, rating);
                insertReviewStmt.setString(4, "Initial rating");
                insertReviewStmt.executeUpdate();
            }

           
            try (PreparedStatement updateHotelRatingStmt = conn.prepareStatement(updateHotelRatingQuery)) {
                updateHotelRatingStmt.setDouble(1, rating);
                updateHotelRatingStmt.setInt(2, hotelId);
                updateHotelRatingStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }





    public ResultSet getHotels() {
        String query = "SELECT hotels.id, hotels.name, hotels.location, " +
                       "IFNULL(AVG(reviews.rating), hotels.rating) AS average_rating " + 
                       "FROM hotels " +
                       "LEFT JOIN reviews ON hotels.id = reviews.hotel_id " +
                       "GROUP BY hotels.id " +
                       "ORDER BY average_rating DESC";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



    
    public boolean deleteHotel(int hotelId) {
        String query = "DELETE FROM hotels WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hotelId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addReview(int userId, int hotelId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            System.out.println("Rating must be between 1 and 5.");
            return false;
        }

        if (comment == null || comment.trim().isEmpty()) {
            System.out.println("Comment cannot be empty.");
            return false;
        }

        String query = "INSERT INTO reviews (user_id, hotel_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, hotelId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getHotelIdByName(String name) {
        String query = "SELECT id FROM hotels WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }



}
