package cms;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Alert.AlertType;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

class dbConnect {
    private static Map<String, String> membersData = new HashMap<>();

    public static void DB_Info(){
        String url = DataKeys.url;
        String user = DataKeys.user;
        String password = DataKeys.password;

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully.");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT member_id, password FROM members");

            while (rs.next()) {
                String memberId = rs.getString("member_id");
                String psword = rs.getString("password");
                membersData.put(memberId, psword);
            }

            System.out.println("Total rows fetched: " + membersData.size());

            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
    }

    public static Map<String, String> getMembersData() {
        return membersData;
    }
}

public class Outer_Register {
    private MemberManager memberManager;

    public Outer_Register() {
        this.memberManager = memberManager;
    }

    public Scene getRegisterScene(Stage stage) {
        stage.setTitle("📖 Library System - Register");

        Label title = new Label("Library Registration");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1a1a2e"));

        Label subtitle = new Label("Join our digital library!");
        subtitle.setFont(Font.font("Georgia", 14));
        subtitle.setTextFill(Color.web("#3a3a50"));

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. you@example.com");

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();
        phoneField.setPromptText("10-digit phone number");

        Label typeLabel = new Label("Membership Type:");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Student", "Faculty", "Guest");
        typeBox.setPromptText("Select membership type");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 8 chars, 1 Upper, 1 Lower, 1 Digit");

        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");

        Button registerBtn = new Button("Register");
        Button backBtn = new Button("Back");

        VBox formBox = new VBox(14,
                title, subtitle,
                new VBox(5, nameLabel, nameField),
                new VBox(5, emailLabel, emailField),
                new VBox(5, phoneLabel, phoneField),
                new VBox(5, typeLabel, typeBox),
                new VBox(5, passwordLabel, passwordField),
                new VBox(5, confirmPasswordLabel, confirmPasswordField),
                new HBox(40, backBtn, registerBtn)
        );

        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #fefefe, #dde6ed); -fx-border-radius: 15; -fx-background-radius: 15;");
        formBox.setMaxWidth(400);

        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL, 20, 20);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.getChildren().add(formBox);

        Scene scene = new Scene(flowPane, 500, 600);

        // Style buttons
        registerBtn.setStyle("-fx-background-color: #344955; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        backBtn.setStyle("-fx-background-color: #f9a826; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");

        // 🔒 Password Regex
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String membershipType = typeBox.getValue();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || membershipType == null || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(AlertType.ERROR, "Incomplete Form", "All fields must be filled out.");
                return;
            }

            if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
                showAlert(AlertType.ERROR, "Invalid Email", "Please enter a valid email format.");
                return;
            }

            if (!phone.matches("\\d{10}")) {
                showAlert(AlertType.ERROR, "Invalid Phone Number", "Phone number must be 10 digits.");
                return;
            }

            if (!password.matches(passwordRegex)) {
                showAlert(AlertType.ERROR, "Weak Password", "Password must be 8+ chars with upper, lower, and digit.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert(AlertType.ERROR, "Password Mismatch", "Passwords do not match.");
                return;
            }

            try {
                Connection con = DriverManager.getConnection(DataKeys.url, DataKeys.user, DataKeys.password);
                String insertSQL = "INSERT INTO Members (name, email, phone_number, membership_type, registration_date, password) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = con.prepareStatement(insertSQL);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, membershipType);
                pstmt.setDate(5, Date.valueOf(LocalDate.now()));
                pstmt.setString(6, password); // Plaintext — should hash in real applications

                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();

                // Now fetch the auto-generated member_id
                String fetchIdSQL = "SELECT member_id FROM Members WHERE email = ?";
                PreparedStatement fetchStmt = con.prepareStatement(fetchIdSQL);
                fetchStmt.setString(1, email);
                ResultSet rs = fetchStmt.executeQuery();

                String memberId = "N/A";
                if (rs.next()) {
                    memberId = rs.getString("member_id");
                }

                rs.close();
                fetchStmt.close();
                con.close();

                if (rowsAffected > 0) {
                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("Registration Successful");
                    successAlert.setHeaderText("Welcome to the Library!");
                    successAlert.setContentText(
                            "🎉 Your Username: " + memberId + "\n\n" +
                                    "Name: " + name + "\n" +
                                    "Email: " + email + "\n" +
                                    "Phone: " + phone + "\n" +
                                    "Membership Type: " + membershipType + "\n" +
                                    "Registration Date: " + LocalDate.now()
                    );
                    successAlert.showAndWait();

                    nameField.clear();
                    emailField.clear();
                    phoneField.clear();
                    typeBox.getSelectionModel().clearSelection();
                    passwordField.clear();
                    confirmPasswordField.clear();
                } else {
                    showAlert(AlertType.ERROR, "Registration Failed", "Could not register. Please try again.");
                }

            } catch (SQLException ex) {
                if (ex.getMessage().toLowerCase().contains("unique") && ex.getMessage().toLowerCase().contains("email")) {
                    showAlert(AlertType.ERROR, "Duplicate Email", "Email already registered.");
                } else {
                    showAlert(AlertType.ERROR, "Database Error", ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // 🔙 Back button
        backBtn.setOnAction(e -> {
            Login login = new Login();
            login.setMemberManager(memberManager);
            login.start(stage);
        });

        stage.setScene(scene);
        return scene;
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
