package cms;

import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.*;

public class Main {

    public Scene getMainScene(Stage stage) {
        stage.setTitle("Book Search");

        // Create VBox for the layout
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed); -fx-border-radius: 15; -fx-background-radius: 15;-fx-padding: 20;");

        // Create a HBox for the header to include the logout button
        HBox header = new HBox(10);
        header.setAlignment(Pos.TOP_RIGHT);
        Button logoutButton = createStyledButton("Logout", "#f9a826", "#344955");
        header.getChildren().add(logoutButton);

        // Create text fields for title, author, and genre input
        TextField titleField = createTextField("Enter book title");
        TextField authorField = createTextField("Enter author name");
        TextField genreField = createTextField("Enter genre");

        // Create search button
        Button searchButton = createStyledButton("Search", "#52796f", "#2f3e46");

        // Create a ListView to display the results (only one list box)
        ListView<String> bookListView = new ListView<>();
        bookListView.setMaxHeight(300);  // Set a max height for scrollability
        ScrollPane scrollPane = new ScrollPane(bookListView);
        scrollPane.setMaxHeight(300);  // Set a max height for the scroll pane

        // Add search button action
        searchButton.setOnAction(e -> {
            // Input Validation
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre = genreField.getText().trim();

            if (title.isEmpty() && author.isEmpty() && genre.isEmpty()) {
                showError("Please enter at least one search criterion (Title, Author, or Genre).");
                return;
            }

            List<String> bookResults = searchBooks(title, author, genre);
            if (bookResults.isEmpty()) {
                showError("No books found matching your criteria.");
            } else {
                bookListView.getItems().clear();
                bookListView.getItems().addAll(bookResults);
            }
        });

        // Add logout button action
        logoutButton.setOnAction(e -> {
            // Switch to the login scene (you need to create the login scene)
            Login login = new Login();
            login.start(stage);
        });

        // Add components to the layout
        layout.getChildren().addAll(
                header,  // Add the header with the logout button
                new Label("Search for a Book:"),
                titleField,
                authorField,
                genreField,
                searchButton,
                scrollPane
        );

        return new Scene(layout, 600, 400);
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

    private List<String> searchBooks(String title, String author, String genre) {
        List<String> results = new ArrayList<>();
        String query = "SELECT title, author, genre, availability FROM CustomerBook WHERE 1=1";
        if (!title.isEmpty()) query += " AND title LIKE ?";
        if (!author.isEmpty()) query += " AND author LIKE ?";
        if (!genre.isEmpty()) query += " AND genre LIKE ?";

        try (Connection con = DriverManager.getConnection(DataKeys.url, DataKeys.user, DataKeys.password);
             PreparedStatement stmt = con.prepareStatement(query)) {

            int index = 1;
            if (!title.isEmpty()) stmt.setString(index++, "%" + title + "%");
            if (!author.isEmpty()) stmt.setString(index++, "%" + author + "%");
            if (!genre.isEmpty()) stmt.setString(index++, "%" + genre + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bookDetails = rs.getString("title") + " by " + rs.getString("author") +
                        " (Genre: " + rs.getString("genre") + ") - " +
                        (rs.getBoolean("availability") ? "Available" : "Not Available");
                results.add(bookDetails);
            }
        } catch (SQLException e) {
            System.out.println("Database error.");
            e.printStackTrace();
        }

        return results;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Scene getLoginScene() {
        // You need to implement the login scene and return it
        // Here, we are just returning a placeholder scene for now.
        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.getChildren().add(new Label("Login Page"));
        return new Scene(loginLayout, 400, 300);
    }
}
