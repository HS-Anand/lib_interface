package cms;

import cms.DataKeys;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class Transact {
    private String tranID;
    private String bookID;
    private String memID;
    private String issue;
    private String due;
    private String return_date;

    public Transact(String tranID, String bookID, String memID, String issue, String due, String return_date) {
        this.tranID = tranID;
        this.bookID = bookID;
        this.memID = memID;
        this.issue = issue;
        this.due = due;
        this.return_date = return_date;
    }

    // Getters and Setters
    public String getTranID() {
        return tranID;
    }

    public String getBookID() {
        return bookID;
    }

    public String getMemID() {
        return memID;
    }

    public String getIssue() {
        return issue;
    }

    public String getDue() {
        return due;
    }

    public String getReturnDate() {
        // Check if return date is null or empty, return "Not Returned" if so.
        return (return_date == null || return_date.isEmpty()) ? "Not Returned" : return_date;
    }

    @Override
    public String toString() {
        return "Transaction ID: " + tranID + ", Book ID: " + bookID + ", Member ID: " + memID +
                ", Issue Date: " + issue + ", Due Date: " + due + ", Return Date: " + getReturnDate();
    }
}

class dbsC {
    private static Map<String, Transact> tranData = new HashMap<>();

    public static void DB_Info() {
        String url = DataKeys.url;
        String user = DataKeys.user;  // Corrected from DataKeys.url to DataKeys.user
        String password = DataKeys.password;

        try {
            // Connect to the database
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully.");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT transaction_id, book_id, member_id, issue_date, due_date, return_date FROM transactions");

            // Fetch data from result set
            while (rs.next()) {
                String tranID = rs.getString("transaction_id");
                String bookID = rs.getString("book_id");
                String memID = rs.getString("member_id");
                String issue = rs.getString("issue_date");
                String due = rs.getString("due_date");
                String returnDate = rs.getString("return_date");

                Transact tran = new Transact(tranID, bookID, memID, issue, due, returnDate);
                tranData.put(tranID, tran);
            }

            System.out.println("Total rows fetched: " + tranData.size());
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
    }

    public static Map<String, Transact> getTranData() {
        return tranData;
    }
}

public class historyView {
    public Scene historyViewScene(Stage stage) {
        stage.setTitle("📜 Admin - Transaction History");

        VBox transactionListVBox = new VBox(10);
        transactionListVBox.setPadding(new Insets(20));

        // Header Row with column labels
        HBox header = createHeaderRow();
        transactionListVBox.getChildren().add(header);

        // Fetch data from the database and populate transaction list
        dbsC.DB_Info();
        Map<String, Transact> tranMap = dbsC.getTranData();
        tranMap.values().stream()
                .map(this::createTransactionRow)
                .forEach(transactionListVBox.getChildren()::add);

        // Scroll Pane
        ScrollPane scrollPane = new ScrollPane(transactionListVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");
        scrollPane.setPadding(new Insets(10));

        // Back Button
        Button backBtn = createStyledButton("🔙 Back", "#f9a826", "#344955");
        backBtn.setOnAction(e -> {
            adminWork admin = new adminWork();
            Scene adminScene = admin.getAdminScene(stage);
            stage.setScene(adminScene);
        });

        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(20));

        // Main Layout
        VBox layout = new VBox(topBar, scrollPane);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(layout, 900, 600);
    }

    private HBox createHeaderRow() {
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #003049; -fx-padding: 10;");
        header.getChildren().addAll(
                createLabel("Tran ID", 80, true),
                createLabel("Book ID", 100, true),
                createLabel("Member ID", 100, true),
                createLabel("Issue Date", 100, true),
                createLabel("Due Date", 100, true),
                createLabel("Return Date", 120, true)
        );
        return header;
    }

    private HBox createTransactionRow(Transact tran) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: #edf2f4; -fx-padding: 8; -fx-background-radius: 8;");
        row.setOnMouseClicked((MouseEvent me) -> {
            System.out.println("Clicked Transaction: " + tran.getTranID());
        });
        row.getChildren().addAll(
                createLabel(tran.getTranID(), 80, false),
                createLabel(tran.getBookID(), 100, false),
                createLabel(tran.getMemID(), 100, false),
                createLabel(tran.getIssue(), 100, false),
                createLabel(tran.getDue(), 100, false),
                createLabel(tran.getReturnDate(), 120, false)
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

    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        return btn;
    }
}
