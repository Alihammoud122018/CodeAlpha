package Task1;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.LineBorder;

public class WordCounter extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextArea textArea;
    private JLabel lblCharacters;
    private JLabel lblWords;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    WordCounter frame = new WordCounter();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public WordCounter() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 500, 400);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Real-Time Character and Word Counter");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setHorizontalAlignment(JLabel.CENTER);
        contentPane.add(title, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setBorder(new LineBorder(Color.GRAY, 1));
        JScrollPane scrollPane = new JScrollPane(textArea);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.SOUTH);
        panel.setLayout(new BorderLayout(10, 10));

        lblCharacters = new JLabel("Characters: 0");
        lblCharacters.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblCharacters, BorderLayout.WEST);
        
        lblWords = new JLabel("Words: 0");
        lblWords.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lblWords, BorderLayout.EAST);

        // Listener for the text/input added by user.
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCounts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCounts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCounts();
            }
        });
        
        updateCounts();
    }
    
    // Function to update the character and word counts.
    private void updateCounts() {
        String text = textArea.getText();
        
        // Function to count the characters
        int characterCount = text.length();
        lblCharacters.setText("Characters: " + characterCount);
        
        // Function to count the words
        if (text.trim().isEmpty()) {
            lblWords.setText("Words: 0");
        } else {
            String[] words = text.trim().split("\\s+");
            int wordCount = words.length;
            lblWords.setText("Words: " + wordCount);
        }
    }
}
