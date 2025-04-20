package cms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Books {
    private String bookId;
    private String title;
    private int totalCopies;
    private int availableCopies;

    public Books(String bookId, String title, int totalCopies, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
}

class Member1 {
    int memberId;
    String name;
    List<String> issuedBookIds = new ArrayList<>();

    Member1(int memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }
}

class dbsCONNECT {
    private static final String url = "your_database_url";
    private static final String user = "your_database_user";
    private static final String password = "your_database_password";

    public static Books getBookInfo(String bookId) {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT book_id, title, total_copies, available_copies FROM Books WHERE book_id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Books(
                        bookId,
                        rs.getString("title"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                );
            }
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
        return null;
    }

    public static Member1 getMemberInfo(int memberId) {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT member_id, name FROM Members WHERE member_id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Member1(memberId, rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateAvailableCopies(String bookId, int newAvailableCopies) {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String query = "UPDATE Books SET available_copies = ? WHERE book_id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, newAvailableCopies);
            stmt.setString(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database update error.");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean insertIssuedBook(int memberId, String bookId, LocalDate issueDate, LocalDate dueDate) {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            String query = "INSERT INTO IssuedBooks (member_id, book_id, issue_date, due_date) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, memberId);
            stmt.setString(2, bookId);
            stmt.setDate(3, Date.valueOf(issueDate));
            stmt.setDate(4, Date.valueOf(dueDate));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Insert issued book error.");
            e.printStackTrace();
        }
        return false;
    }
}

public class issueBook {
    public Scene issueBookScene(Stage stage) {
        stage.setTitle("📚 Admin - Issue Book");

        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(40));
        centerBox.setAlignment(Pos.TOP_CENTER);

        TextField bookIdField = new TextField();
        bookIdField.setPromptText("Enter Book ID");
        bookIdField.setMaxWidth(200);

        TextField memberIdField = new TextField();
        memberIdField.setPromptText("Enter Member ID");
        memberIdField.setMaxWidth(200);

        Label notification = new Label();
        notification.setWrapText(true);
        notification.setStyle("-fx-text-fill: green; -fx-font-size: 13px;");

        Button issueBtn = new Button("✅ Issue Book");
        issueBtn.setStyle("-fx-background-color: #52796f; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 10;");

        issueBtn.setOnAction(e -> {
            notification.setText("");
            String bookId = bookIdField.getText().trim();
            String memberIdText = memberIdField.getText().trim();

            if (bookId.isEmpty() || memberIdText.isEmpty()) {
                notification.setText("❌ Book ID and Member ID must not be empty.");
                notification.setStyle("-fx-text-fill: red;");
                return;
            }

            try {
                int memberId = Integer.parseInt(memberIdText);

                Books book = dbsCONNECT.getBookInfo(bookId);
                Member1 member = dbsCONNECT.getMemberInfo(memberId);

                if (book == null) {
                    notification.setText("❌ Book not found.");
                    notification.setStyle("-fx-text-fill: red;");
                    return;
                }

                if (member == null) {
                    notification.setText("❌ Member not found.");
                    notification.setStyle("-fx-text-fill: red;");
                    return;
                }

                if (book.getAvailableCopies() <= 0) {
                    notification.setText("❌ Book is currently not available.");
                    notification.setStyle("-fx-text-fill: red;");
                    return;
                }

                if (member.issuedBookIds.size() >= 3) {
                    notification.setText("❌ Member already has 3 books issued.");
                    notification.setStyle("-fx-text-fill: red;");
                    return;
                }

                // Issue process
                member.issuedBookIds.add(book.getBookId());
                int newAvailableCopies = book.getAvailableCopies() - 1;

                boolean updated = dbsCONNECT.updateAvailableCopies(book.getBookId(), newAvailableCopies);
                if (!updated) {
                    notification.setText("❌ Failed to update book availability.");
                    notification.setStyle("-fx-text-fill: red;");
                    return;
                }

                LocalDate issueDate = LocalDate.now();
                LocalDate dueDate = issueDate.plusDays(14);

                boolean inserted = dbsCONNECT.insertIssuedBook(memberId, bookId, issueDate, dueDate);
                if (!inserted) {
                    notification.setText("❌ Failed to record issued book.");
                    notification.setStyle("-fx-text-fill: red;");
                    return;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                notification.setText("✅ Book Issued!\n📚 Title: " + book.getTitle() +
                        "\n👤 Member: " + member.name +
                        "\n📅 Due Date: " + dueDate.format(formatter));
                notification.setStyle("-fx-text-fill: green;");
                bookIdField.clear();
                memberIdField.clear();

            } catch (NumberFormatException ex) {
                notification.setText("❌ Please enter a valid numeric Member ID.");
                notification.setStyle("-fx-text-fill: red;");
            } catch (Exception ex) {
                notification.setText("❌ Unexpected error occurred.");
                notification.setStyle("-fx-text-fill: red;");
                ex.printStackTrace();
            }
        });

        centerBox.getChildren().addAll(bookIdField, memberIdField, issueBtn, notification);

        Button backBtn = new Button("🔙 Back");
        backBtn.setStyle("-fx-background-color: #f9a826; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6 14; -fx-background-radius: 10;");
        HBox topRightBox = new HBox(backBtn);
        topRightBox.setAlignment(Pos.TOP_RIGHT);
        topRightBox.setPadding(new Insets(10, 20, 0, 0));

        backBtn.setOnAction(e -> {
            adminWork admin = new adminWork();
            stage.setScene(admin.getAdminScene(stage));
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(centerBox);
        borderPane.setTop(topRightBox);
        borderPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(borderPane, 600, 600);
    }
}
