package cms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class Member {
    private String memID;
    private String name;
    private String email;
    private String phone;
    private String memType;
    private String regDate;

    public Member(String memID, String name, String email, String phone, String memType, String regDate) {
        this.memID = memID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.memType = memType;
        this.regDate = regDate;
    }

    public Member(int memberId, String name) {
    }

    public String getmemId() { return memID; }
    public String getNme() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getType() { return memType; }
    public String getDate() { return regDate; }

    // Setters for the member's name
    public void setName(String name) {
        this.name = name;
    }
}

class dbsConnecting {
    private static Map<String, Member> membersData = new HashMap<>();

    public static void DB_Info() {
        String url = DataKeys.url;
        String user = DataKeys.user;
        String password = DataKeys.password;

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully.");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT member_id, name, email, phone_number, membership_type, registration_date FROM members");

            while (rs.next()) {
                String memID = rs.getString("member_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone_number");
                String memType = rs.getString("membership_type");
                String regDate = rs.getString("registration_date");

                Member member = new Member(memID, name, email, phone, memType, regDate);
                membersData.put(memID, member);
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

    public static Map<String, Member> getMembersData() {
        return membersData;
    }
}

class dbDelete {
    public static void DB_delete(String memID) {
        String url = DataKeys.url;
        String user = DataKeys.user;
        String password = DataKeys.password;

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database successfully.");

            PreparedStatement pstmt = con.prepareStatement("DELETE FROM members WHERE member_id = ?");
            pstmt.setString(1, memID);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Deleted rows: " + rowsAffected);

            pstmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Database delete error.");
            e.printStackTrace();
        }
    }
}

public class userManage {

    public Scene userManageScene(Stage stage) {
        stage.setTitle("👥 Admin - Manage Users");

        dbsConnecting.DB_Info();
        List<Member> members = getSortedMembers();

        VBox userListVBox = new VBox(10);
        userListVBox.setPadding(new Insets(20));

        HBox header = createHeaderRow();
        userListVBox.getChildren().add(header);

        members.forEach(member -> userListVBox.getChildren().add(createUserRow(member, userListVBox, header)));

        ScrollPane scrollPane = new ScrollPane(userListVBox);
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
            userListVBox.getChildren().setAll(header);
            getSortedMembers().stream()
                    .filter(m -> matchesQuery(m, query))
                    .map(m -> createUserRow(m, userListVBox, header))
                    .forEach(userListVBox.getChildren()::add);
        });

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.setPadding(new Insets(20));

        HBox leftBox = new HBox(backBtn);
        leftBox.setAlignment(Pos.TOP_LEFT);
        HBox rightBox = new HBox(20, searchField, searchBtn);
        rightBox.setAlignment(Pos.TOP_RIGHT);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        topBar.getChildren().addAll(leftBox, rightBox);

        VBox layout = new VBox(topBar, scrollPane);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(layout, 900, 600);
    }

    private List<Member> getSortedMembers() {
        List<Member> sorted = new ArrayList<>(dbsConnecting.getMembersData().values());
        sorted.sort((b1, b2) -> {
            try {
                return Integer.compare(Integer.parseInt(b1.getmemId()), Integer.parseInt(b2.getmemId()));
            } catch (NumberFormatException e) {
                return b1.getmemId().compareToIgnoreCase(b2.getmemId());
            }
        });
        return sorted;
    }

    private HBox createHeaderRow() {
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #003049; -fx-padding: 10;");
        header.getChildren().addAll(
                createLabel("ID", 50, true),
                createLabel("Name", 150, true),
                createLabel("Email", 150, true),
                createLabel("Phone", 100, true),
                createLabel("Membership", 100, true),
                createLabel("Reg. Date", 100, true),
                createLabel("Ban", 80, true) // New label for updating name
        );
        return header;
    }

    private HBox createUserRow(Member member, VBox userListVBox, HBox header) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color: #edf2f4; -fx-padding: 8; -fx-background-radius: 8;");
        row.setOnMouseClicked((MouseEvent me) -> {
            System.out.println("Clicked User: " + member.getNme());
        });

        Button banBtn = createStyledButton("✖️", "#e63946", "#b5171f");
        banBtn.setOnAction(e -> {
            System.out.println("User " + member.getNme() + " has been banned.");
            dbDelete.DB_delete(member.getmemId());
            dbsConnecting.getMembersData().remove(member.getmemId());
            userListVBox.getChildren().setAll(header);
            getSortedMembers().forEach(m -> userListVBox.getChildren().add(createUserRow(m, userListVBox, header)));
        });









        row.getChildren().addAll(
                createLabel(member.getmemId(), 50, false),
                createLabel(member.getNme(), 150, false),
                createLabel(member.getEmail(), 150, false),
                createLabel(member.getPhone(), 100, false),
                createLabel(member.getType(), 100, false),
                createLabel(member.getDate(), 100, false),
                banBtn// Add button to update name
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

    private boolean matchesQuery(Member m, String query) {
        return m.getmemId().toLowerCase().contains(query) ||
                m.getNme().toLowerCase().contains(query) ||
                m.getEmail().toLowerCase().contains(query) ||
                m.getPhone().toLowerCase().contains(query) ||
                m.getType().toLowerCase().contains(query) ||
                m.getDate().toLowerCase().contains(query);
    }

    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        return btn;
    }
}
