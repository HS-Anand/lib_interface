package cms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class Book {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private String isbn;
    private String publicationYear;
    private String totalCopies;
    private String availableCopies;

    public Book(String bookId, String title, String author, String genre, String isbn,
                String publicationYear, String totalCopies, String availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // Getters (add setters if you need to modify them)
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getIsbn() { return isbn; }
    public String getPublicationYear() { return publicationYear; }
    public String getTotalCopies() { return totalCopies; }
    public String getAvailableCopies() { return availableCopies; }
}


class dbsCon {
    private static Map<String, Book> booksData = new HashMap<>();

    public static void DB_Info() {
        String url = DataKeys.url;
        String user = DataKeys.user;
        String password = DataKeys.password;

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully.");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT book_id, title, author, genre, isbn, publication_year, total_copies, available_copies FROM books");

            while (rs.next()) {
                String bookId = rs.getString("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                String isbn = rs.getString("isbn");
                String year = rs.getString("publication_year");
                String total = rs.getString("total_copies");
                String avail = rs.getString("available_copies");

                Book book = new Book(bookId, title, author, genre, isbn, year, total, avail);
                booksData.put(bookId, book);
            }

            System.out.println("Total rows fetched: " + booksData.size());

            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
    }

    public static Map<String, Book> getBooksData() {
        return booksData;
    }
}



public class searchBook {

    public Scene searchBookScene(Stage stage) {
        stage.setTitle("🔍 Admin - Search Books");

        VBox bookListVBox = new VBox(10);
        bookListVBox.setPadding(new Insets(20));

        HBox header = createHeaderRow();
        bookListVBox.getChildren().add(header);

        // 🔽 Load data from the database
        dbsCon.DB_Info();
        Map<String, Book> booksMap = dbsCon.getBooksData();
        List<Book> books = new ArrayList<>(booksMap.values());
        books.sort((b1, b2) -> b1.getBookId().compareToIgnoreCase(b2.getBookId()));


        books.stream()
                .map(this::createBookRow)
                .forEach(bookListVBox.getChildren()::add);

        ScrollPane scrollPane = new ScrollPane(bookListVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");
        scrollPane.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by any attribute...");
        searchField.setPrefWidth(250);

        Button searchBtn = createStyledButton("🔍 Search", "#52796f", "#2f3e46");
        Button backBtn = createStyledButton("🔙 Back", "#f9a826", "#344955");

        backBtn.setOnAction(e -> {
            adminWork admin = new adminWork();
            Scene adminScene = admin.getAdminScene(stage);
            stage.setScene(adminScene);
        });

        searchBtn.setOnAction(e -> {
            String query = searchField.getText().toLowerCase().trim();
            bookListVBox.getChildren().setAll(header); // reset with header
            books.stream()
                    .filter(book -> matchesQuery(book, query))
                    .map(this::createBookRow)
                    .forEach(bookListVBox.getChildren()::add);
        });

        HBox leftBox = new HBox(backBtn);
        leftBox.setAlignment(Pos.TOP_LEFT);
        HBox rightBox = new HBox(20, searchField, searchBtn);
        rightBox.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        HBox topBar = new HBox(10, leftBox, rightBox);
        topBar.setPadding(new Insets(20));

        VBox layout = new VBox(topBar, scrollPane);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(layout, 900, 600);
    }

    private HBox createHeaderRow() {
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #003049; -fx-padding: 10;");
        header.getChildren().addAll(
                createLabel("ID", 50, true),
                createLabel("Title", 150, true),
                createLabel("Author", 100, true),
                createLabel("Genre", 80, true),
                createLabel("ISBN", 100, true),
                createLabel("Year", 60, true),
                createLabel("Total", 60, true),
                createLabel("Available", 80, true)
        );
        return header;
    }

    private HBox createBookRow(Book book) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: #edf2f4; -fx-padding: 8; -fx-background-radius: 8;");
        row.setOnMouseClicked((MouseEvent me) -> {
            System.out.println("Clicked Book: " + book.getTitle());
        });
        row.getChildren().addAll(
                createLabel(book.getBookId(), 50, false),
                createLabel(book.getTitle(), 150, false),
                createLabel(book.getAuthor(), 100, false),
                createLabel(book.getGenre(), 80, false),
                createLabel(book.getIsbn(), 100, false),
                createLabel(book.getPublicationYear(), 60, false),
                createLabel(book.getTotalCopies(), 60, false),
                createLabel(book.getAvailableCopies(), 80, false)
        );
        return row;
    }

    private Label createLabel(String text, int width, boolean isHeader) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle(isHeader
                ? "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;"
                : "-fx-text-fill: #2b2d42; -fx-font-size: 13px;");
        return label;
    }

    private boolean matchesQuery(Book book, String query) {
        return book.getBookId().toLowerCase().contains(query) ||
                book.getTitle().toLowerCase().contains(query) ||
                book.getAuthor().toLowerCase().contains(query) ||
                book.getGenre().toLowerCase().contains(query) ||
                book.getIsbn().toLowerCase().contains(query) ||
                book.getPublicationYear().toLowerCase().contains(query) ||
                book.getTotalCopies().toLowerCase().contains(query) ||
                book.getAvailableCopies().toLowerCase().contains(query);
    }

    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        return btn;
    }
}
