import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StudentManagementGUI extends JFrame {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String USER = "root";
    private static final String PASSWORD = "Tripti@12";

    public StudentManagementGUI() {
        setTitle("Student Management System – Mini Project");
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 🎓 CUIMS-style red and white theme
        Color red = new Color(204, 0, 0);
        Color white = Color.WHITE;

        // ---------- HEADER ----------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(white);

        // CU Logo (top-left, small)
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("cu_logo.jpg"));
            Image scaledLogo = logoIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
            logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            headerPanel.add(logoLabel, BorderLayout.WEST);
        } catch (Exception e) {
            System.out.println("⚠️ Logo not found or couldn't be loaded.");
        }

        // Title
        JLabel title = new JLabel("Student Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        headerPanel.add(title, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // ---------- MAIN PANEL ----------
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 40, 40));
        mainPanel.setBackground(red);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JButton addBtn = createButton("Add Student", white);
        JButton viewBtn = createButton("View Students", white);
        JButton searchBtn = createButton("Search Student", white);
        JButton updateBtn = createButton("Update Student", white);
        JButton deleteBtn = createButton("Delete Student", white);

        mainPanel.add(addBtn);
        mainPanel.add(viewBtn);
        mainPanel.add(searchBtn);
        mainPanel.add(updateBtn);
        mainPanel.add(deleteBtn);

        add(mainPanel, BorderLayout.CENTER);

        // ---------- EXIT BUTTON ----------
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setBackground(white);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBackground(Color.BLACK);
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitBtn.setPreferredSize(new Dimension(90, 35));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> System.exit(0));

        bottomPanel.add(exitBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // ---------- BUTTON ACTIONS ----------
        addBtn.addActionListener(e -> addStudentPopup());
        viewBtn.addActionListener(e -> viewStudentsPopup());
        searchBtn.addActionListener(e -> searchStudentPopup());
        updateBtn.addActionListener(e -> updateStudentPopup());
        deleteBtn.addActionListener(e -> deleteStudentPopup());
    }

    // 🩷 Create styled button
    private JButton createButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true));
        return button;
    }

    // 🌸 Add Student Popup
    private void addStudentPopup() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField dobField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] fields = {
            "Student ID:", idField,
            "Name:", nameField,
            "Birthday (YYYY-MM-DD):", dobField,
            "Email:", emailField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO students VALUES (?, ?, ?, ?)");
                stmt.setInt(1, Integer.parseInt(idField.getText()));
                stmt.setString(2, nameField.getText());
                stmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(dobField.getText())));
                stmt.setString(4, emailField.getText());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Student Added Successfully!");
            } catch (SQLException | DateTimeParseException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        }
    }

    // 🌸 View Students Popup
    private void viewStudentsPopup() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            StringBuilder sb = new StringBuilder("<< Student List >>\n\n");
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("student_id")).append("\n");
                sb.append("Name: ").append(rs.getString("name")).append("\n");
                sb.append("Birthday: ").append(rs.getDate("birthday")).append("\n");
                sb.append("Email: ").append(rs.getString("email")).append("\n\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // 🌸 Search Student Popup
    private void searchStudentPopup() {
        String name = JOptionPane.showInputDialog("Enter student name:");
        if (name == null || name.isEmpty()) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM students WHERE UPPER(name) = UPPER(?)")) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String info = "ID: " + rs.getInt("student_id") +
                              "\nName: " + rs.getString("name") +
                              "\nBirthday: " + rs.getDate("birthday") +
                              "\nEmail: " + rs.getString("email");
                JOptionPane.showMessageDialog(null, info);
            } else {
                JOptionPane.showMessageDialog(null, "No student found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // 🌸 Update Student Popup
    private void updateStudentPopup() {
        String idStr = JOptionPane.showInputDialog("Enter Student ID to update:");
        if (idStr == null || idStr.isEmpty()) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int id = Integer.parseInt(idStr);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM students WHERE student_id=?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JTextField nameField = new JTextField(rs.getString("name"));
                JTextField dobField = new JTextField(rs.getDate("birthday").toString());
                JTextField emailField = new JTextField(rs.getString("email"));

                Object[] fields = {
                    "Name:", nameField,
                    "Birthday (YYYY-MM-DD):", dobField,
                    "Email:", emailField
                };

                int option = JOptionPane.showConfirmDialog(null, fields, "Update Student", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE students SET name=?, birthday=?, email=? WHERE student_id=?");
                    updateStmt.setString(1, nameField.getText());
                    updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(dobField.getText())));
                    updateStmt.setString(3, emailField.getText());
                    updateStmt.setInt(4, id);
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Student Updated Successfully!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "No student found with that ID!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // 🌸 Delete Student Popup
    private void deleteStudentPopup() {
        String idStr = JOptionPane.showInputDialog("Enter Student ID to delete:");
        if (idStr == null || idStr.isEmpty()) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int id = Integer.parseInt(idStr);
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM students WHERE student_id=?");
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Student Deleted Successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "No student found with that ID!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // 🌸 Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentManagementGUI().setVisible(true);
        });
    }
}
