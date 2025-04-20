package cms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class addBook {

    public Scene addBookScene(Stage stage) {
        stage.setTitle("➕ Admin - Add New Books");

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Title:");
        TextField titleField = createTextField("Enter book title");

        Label authorLabel = new Label("Author:");
        TextField authorField = createTextField("Enter author name");

        Label genreLabel = new Label("Genre:");
        TextField genreField = createTextField("Enter book genre");

        Label isbnLabel = new Label("ISBN:");
        TextField isbnField = createTextField("Enter 13-digit ISBN");

        Label yearLabel = new Label("Publication Year:");
        TextField yearField = createTextField("Enter publication year");

        Label newCopiesLabel = new Label("New Copies:");
        TextField newCopiesField = createTextField("Enter number of new copies");

        Button submitButton = createStyledButton("Submit", "#52796f", "#2f3e46");

        Button backBtn = createStyledButton("🔙 Back", "#f9a826", "#344955");
        backBtn.setOnAction(e -> {
            adminWork admin = new adminWork();
            Scene adminScene = admin.getAdminScene(stage);
            stage.setScene(adminScene);
        });

        submitButton.setOnAction(e -> {
            // Input Validation
            if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                    genreField.getText().isEmpty() || isbnField.getText().isEmpty() ||
                    yearField.getText().isEmpty() || newCopiesField.getText().isEmpty()) {
                showError("All fields must be filled out.");
                return;
            }

            if (!authorField.getText().matches("[a-zA-Z\\s]+")) {
                showError("Author name must only contain alphabets.");
                return;
            }

            if (!yearField.getText().matches("\\d{4}")) {
                showError("Publication year must be a 4-digit number.");
                return;
            }

            if (!isbnField.getText().matches("\\d{13}")) {
                showError("ISBN must be a valid 13-digit number.");
                return;
            }

            int copies;
            try {
                copies = Integer.parseInt(newCopiesField.getText());
            } catch (NumberFormatException ex) {
                showError("New copies must be a valid number.");
                return;
            }

            try (Connection con = DriverManager.getConnection(DataKeys.url, DataKeys.user, DataKeys.password)) {

                // Get latest book_id and increment
                String getLastIdQuery = "SELECT book_id FROM Books ORDER BY book_id DESC LIMIT 1";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(getLastIdQuery);

                String newBookId = "B051"; // default starting point
                if (rs.next()) {
                    String lastId = rs.getString("book_id");
                    Matcher matcher = Pattern.compile("B(\\d{3})").matcher(lastId);
                    if (matcher.matches()) {
                        int number = Integer.parseInt(matcher.group(1)) + 1;
                        newBookId = String.format("B%03d", number);
                    }
                }
                rs.close();
                stmt.close();

                // Call the procedure
                CallableStatement cstmt = con.prepareCall("{ call bookinserts(?, ?, ?, ?, ?, ?, ?, ?) }");
                cstmt.setString(1, newBookId);
                cstmt.setString(2, titleField.getText());
                cstmt.setString(3, authorField.getText());
                cstmt.setString(4, genreField.getText());
                cstmt.setString(5, isbnField.getText());
                cstmt.setInt(6, Integer.parseInt(yearField.getText()));
                cstmt.setInt(7, copies);
                cstmt.setInt(8, copies);

                cstmt.execute();
                cstmt.close();

                // Clear fields after successful insert/update
                titleField.clear();
                authorField.clear();
                genreField.clear();
                isbnField.clear();
                yearField.clear();
                newCopiesField.clear();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Book Added");
                alert.setHeaderText("Book added successfully!");
                alert.setContentText("Book ID: " + newBookId);
                alert.showAndWait();

            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }

        });

        VBox formLayout = new VBox(15);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.getChildren().addAll(
                titleLabel, titleField,
                authorLabel, authorField,
                genreLabel, genreField,
                isbnLabel, isbnField,
                yearLabel, yearField,
                newCopiesLabel, newCopiesField
        );

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPadding(new Insets(20));

        HBox leftBox = new HBox(backBtn);
        leftBox.setAlignment(Pos.TOP_LEFT);
        HBox rightBox = new HBox(submitButton);
        rightBox.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        topBar.getChildren().addAll(leftBox, rightBox);

        VBox layout = new VBox(30, topBar, formLayout);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(layout, 650, 650);
    }

    private TextField createTextField(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setStyle("-fx-border-radius: 10; -fx-background-color: #f4f4f4; -fx-padding: 8;");
        return field;
    }

    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        return btn;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
