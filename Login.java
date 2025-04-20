package cms;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class dbConn {
    private static Map<String, String> membersData = new HashMap<>();  // To store member_id and password

    // Method to retrieve member data from the database
    public static void DB_Info(){
        String url = DataKeys.url;
        String user = DataKeys.user;  // Corrected from DataKeys.url to DataKeys.user
        String password = DataKeys.password;  // Corrected from DataKeys.url to DataKeys.password

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully.");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT member_id, password FROM members");

            if (!rs.isBeforeFirst()) {
                System.out.println("No data found.");
            }

            // Process the result set and store data in membersData
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




public class Login extends Application {

    private final String admin_name = "ad";
    private final String admin_pass = "ad";

    private static MemberManager memberManager;

    public static void setMemberManager(MemberManager manager) {
        memberManager = manager;
    }

    @Override
    public void start(Stage myStage) {
        myStage.setTitle("📚 Library Management System - Login");

        // Title Label
        Label titleLabel = new Label("Library Portal");
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#1a1a2e"));

        Label subtitle = new Label("Welcome back! Please log in to continue.");
        subtitle.setFont(Font.font("Georgia", 14));
        subtitle.setTextFill(Color.web("#3a3a50"));

        // Error Label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: crimson; -fx-font-weight: bold;");

        // Username input
        Label l1 = new Label("Username:");
        TextField t1 = new TextField();
        t1.setPromptText("Enter your username");
        t1.setMaxWidth(280);
        t1.setStyle("-fx-background-radius: 10; -fx-padding: 8;");

        // Password input
        Label l2 = new Label("Password:");
        PasswordField t2 = new PasswordField();
        t2.setPromptText("Enter your password");
        t2.setMaxWidth(280);
        t2.setStyle("-fx-background-radius: 10; -fx-padding: 8;");

        // Role selection
        RadioButton adminRadio = new RadioButton("Admin");
        RadioButton memberRadio = new RadioButton("Member");

        ToggleGroup roleGroup = new ToggleGroup();
        adminRadio.setToggleGroup(roleGroup);
        memberRadio.setToggleGroup(roleGroup);

        // Buttons
        Button log = new Button("Login");
        Button reg = new Button("Register");

        log.setStyle("-fx-background-color: #344955; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        reg.setStyle("-fx-background-color: #f9a826; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");

        HBox radioLayout = new HBox(30, adminRadio, memberRadio);
        radioLayout.setAlignment(Pos.CENTER);

        VBox inputFields = new VBox(10, l1, t1, l2, t2, radioLayout);
        inputFields.setAlignment(Pos.CENTER);

        HBox buttonLayout = new HBox(30, log, reg);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox upLay = new VBox(10, titleLabel, subtitle);
        upLay.setAlignment(Pos.CENTER);

        VBox downLay = new VBox(30, inputFields, buttonLayout);
        downLay.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(20, upLay, downLay, errorLabel);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed); -fx-border-radius: 15; -fx-background-radius: 15;");

        Scene loginScene = new Scene(mainLayout, 500, 380);

        log.setOnAction(e -> {
            try {
                dbConn dbs = new dbConn();
                dbs.DB_Info();
                String username = t1.getText();
                String password = t2.getText();

                if (username.isEmpty() || password.isEmpty()) {
                    throw new MissingCredentialsException("⚠️ Username and password cannot be empty.");
                }

                Toggle selectedToggle = roleGroup.getSelectedToggle();
                if (selectedToggle == null) {
                    throw new MissingCredentialsException("⚠️ Please select a role (Admin or Member).");
                }

                String selectedRole = ((RadioButton) selectedToggle).getText();

                if (selectedRole.equals("Admin")) {
                    if (username.equals(admin_name) && password.equals(admin_pass)) {
                        adminWork adminDash = new adminWork();
                        myStage.setScene(adminDash.getAdminScene(myStage));
                    } else {
                        throw new InvalidLoginException("❌ Invalid admin credentials.");
                    }
                } else if (selectedRole.equals("Member")) {
                    // Check if member exists in the stored database information
                    Map<String, String> membersData = dbConn.getMembersData();
                    if (membersData.containsKey(username) && membersData.get(username).equals(password)) {
                        // Successful member login
                        Main main = new Main();
                        myStage.setScene(main.getMainScene(myStage));
                    } else {
                        throw new InvalidLoginException("❌ Incorrect login credentials for member.");
                    }
                }

            } catch (MissingCredentialsException | InvalidLoginException ex) {
                errorLabel.setText(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠️ An unexpected error occurred. Please try again.");
            }
        });


        reg.setOnAction(e -> {
            Outer_Register registration = new Outer_Register();
            myStage.setScene(registration.getRegisterScene(myStage));
        });

        myStage.setScene(loginScene);
        myStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// --- Custom Exceptions ---
class MissingCredentialsException extends Exception {
    public MissingCredentialsException(String message) {
        super(message);
    }
}

class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }
}
