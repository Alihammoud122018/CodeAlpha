package hotelreview;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import java.sql.*;
import java.text.DecimalFormat;

public class HotelReviewApp {
    private DatabaseHelper dbHelper;
    private JFrame frame;
    @SuppressWarnings("unused")
	private JTextField usernameField, hotelNameField, hotelLocationField, ratingField, commentField;
    private JPasswordField passwordField;

    public HotelReviewApp() {
        dbHelper = new DatabaseHelper();
        frame = new JFrame("Hotel Review System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600); 
        showLoginScreen();
    }

    private void showLoginScreen() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username and password cannot be empty");
            } else if (dbHelper.loginUser(username, password)) {
                showHotelListScreen();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials");
            }
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username and password cannot be empty");
            } else if (dbHelper.registerUser(username, password)) {
                JOptionPane.showMessageDialog(frame, "User registered successfully");
            } else {
                JOptionPane.showMessageDialog(frame, "Registration failed");
            }
        });

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showHotelListScreen() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        
        JPanel addHotelPanel = createAddHotelPanel();
        frame.add(addHotelPanel, BorderLayout.NORTH);

        
        JPanel hotelListPanel = new JPanel();
        hotelListPanel.setLayout(new BoxLayout(hotelListPanel, BoxLayout.Y_AXIS));

        ResultSet rs = dbHelper.getHotels();
        DecimalFormat df = new DecimalFormat("#.#");
        try {
            while (rs.next()) {
                int hotelId = rs.getInt("id");
                String hotelName = rs.getString("name");
                String location = rs.getString("location");
                double rating = rs.getDouble("average_rating");

                
                String formattedRating = df.format(rating);

                JPanel hotelPanel = new JPanel(new BorderLayout());
                hotelPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

                JLabel hotelLabel = new JLabel("<html><b>" + hotelName + "</b><br>Location: " + location + "<br>Rating: " + formattedRating + "</html>");
                hotelPanel.add(hotelLabel, BorderLayout.CENTER);

                hotelLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            JPopupMenu contextMenu = createHotelContextMenu(hotelId, hotelName);
                            contextMenu.show(hotelLabel, e.getX(), e.getY());
                        }
                    }
                });

                hotelListPanel.add(hotelPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(hotelListPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel addReviewPanel = createAddReviewPanel(0, "");
        frame.add(addReviewPanel, BorderLayout.SOUTH);

        frame.revalidate();
        frame.repaint();
    }

    

    
    private JPopupMenu createHotelContextMenu(int hotelId, String hotelName) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem deleteMenuItem = new JMenuItem("Delete Hotel");
        deleteMenuItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this hotel?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dbHelper.deleteHotel(hotelId)) {
                    JOptionPane.showMessageDialog(frame, "Hotel deleted successfully");
                    showHotelListScreen();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to delete hotel");
                }
            }
        });

        JMenuItem addReviewMenuItem = new JMenuItem("Add Review");
        addReviewMenuItem.addActionListener(e -> {
            JPanel addReviewPanel = createAddReviewPanel(hotelId, hotelName);
            frame.getContentPane().removeAll();
            frame.getContentPane().add(addReviewPanel);
            frame.revalidate();
            frame.repaint();
        });

        contextMenu.add(deleteMenuItem);
        contextMenu.add(addReviewMenuItem);

        return contextMenu;
    }
    
    private JPanel createAddHotelPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField nameField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField ratingField = new JTextField();
        JButton addButton = new JButton("Add Hotel");

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            String ratingText = ratingField.getText().trim();

            if (name.isEmpty() || location.isEmpty() || ratingText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled");
                return;
            }

            try {
                int rating = Integer.parseInt(ratingText);
                if (rating < 1 || rating > 5) {
                    JOptionPane.showMessageDialog(frame, "Rating must be between 1 and 5");
                    return;
                }

                if (dbHelper.addHotel(name, location, rating)) {
                    JOptionPane.showMessageDialog(frame, "Hotel added successfully");
                    showHotelListScreen();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to add hotel");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid rating format");
            }
        });

        panel.add(new JLabel("Hotel Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Initial Rating:"));
        panel.add(ratingField);
        panel.add(addButton);

        return panel;
    }

    private JPanel createAddReviewPanel(int selectedHotelId, String hotelName) {
        JPanel panel = new JPanel(new GridLayout(5, 2));
        JComboBox<String> hotelComboBox = new JComboBox<>();
        JTextField ratingField = new JTextField();
        JTextField commentField = new JTextField();
        JButton submitButton = new JButton("Submit Review");

        
        try {
            ResultSet rs = dbHelper.getHotels();
            while (rs.next()) {
                String newhotelName = rs.getString("name");
                hotelComboBox.addItem(newhotelName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        submitButton.addActionListener(e -> {
            String selectedHotelName = (String) hotelComboBox.getSelectedItem();
            String ratingText = ratingField.getText().trim();
            String comment = commentField.getText().trim();

            if (selectedHotelName == null || ratingText.isEmpty() || comment.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled");
                return;
            }

            try {
                int rating = Integer.parseInt(ratingText);
                if (rating < 1 || rating > 5) {
                    JOptionPane.showMessageDialog(frame, "Rating must be between 1 and 5");
                    return;
                }

                int hotelId = dbHelper.getHotelIdByName(selectedHotelName);
                int userId = 1; 

                if (dbHelper.addReview(userId, hotelId, rating, comment)) {
                    JOptionPane.showMessageDialog(frame, "Review added successfully");
                    showHotelListScreen();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to add review");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid rating format");
            } 
        });

        panel.add(new JLabel("Hotel:"));
        panel.add(hotelComboBox);
        panel.add(new JLabel("Rating:"));
        panel.add(ratingField);
        panel.add(new JLabel("Comment:"));
        panel.add(commentField);
        panel.add(submitButton);

        return panel;
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReviewApp().frame.setVisible(true));
    }
}
