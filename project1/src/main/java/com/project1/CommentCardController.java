package com.project1;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.project1.AddCommentController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CommentCardController {

    @FXML private Label userNameLabel;
    @FXML private Label studentNoLabel;
    @FXML private Label roleLabel;
    @FXML private Label commentTextLabel;
    @FXML private Label timeLabel;
    @FXML private HBox starsBox;
    @FXML private HBox actionsBox;
    @FXML private HBox rootContainer;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private GridPane root;
    private String commentId;
    private String eventId;
    private String userName;
    private String studentNumber;
    private String role;
    private String text;
    private int rating;
    private String currentUserName;
    private Timestamp timestamp;
    private Runnable refreshCommentsCallback;
    private String currentUserId;

public void setRefreshCommentsCallback(Runnable callback) {
    this.refreshCommentsCallback = callback;
}


    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        updateActionsVisibility(); 
    }
    

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                             .withZone(ZoneId.systemDefault());

   public void setData(String commentId, String eventId,
                    String userName, String studentNumber, String role,
                    String text, int rating,
                    Timestamp ts, String currentUserName) {
    this.commentId = commentId;
    this.eventId    = eventId;
    this.userName   = userName;
    this.studentNumber = studentNumber;
    this.role       = role;
    this.text       = text;
    this.rating     = rating;
    this.timestamp  = ts;
    this.currentUserName = currentUserName;

    userNameLabel.setText(userName != null && !userName.isBlank() ? userName : "Anonim");
    roleLabel.setText(role != null ? role : "");
    studentNoLabel.setText(studentNumber != null ? studentNumber : "");
    commentTextLabel.setText(text != null ? text : "");
    timeLabel.setText(ts != null
        ? TS_FMT.format(Instant.ofEpochSecond(ts.getSeconds()))
        : ""
    );
    starsBox.getChildren().clear();
    for (int i = 0; i < 5; i++) {
        Label star = new Label(i < rating ? "★" : "☆");
        star.getStyleClass().add("comment-star");
        starsBox.getChildren().add(star);
    }

    updateActionsVisibility();
}

@FXML
private void handleEdit(ActionEvent e) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/addCommentPage.fxml"));
        Parent root = loader.load();
        AddCommentController ctrl = loader.getController();

        UserModel user = new UserModel(
            userName != null ? userName : "",
            "",  
            studentNumber != null ? studentNumber : "",
            "",  
            role != null ? role : ""
        );
        ctrl.setCurrentUser(user);
        ctrl.setEventContext(eventId); 
        ctrl.setEditMode(commentId, text, rating);

        
        Stage stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        root.setUserData(stage.getScene().getRoot().getUserData()); 
        stage.getScene().setRoot(root);
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}

@FXML
private void handleDelete(ActionEvent e) {
    try {
        Firestore db = FirestoreClient.getFirestore();
        db.collection("events")
          .document(eventId)
          .collection("comments")
          .document(commentId)
          .delete()
          .get();

        if (refreshCommentsCallback != null) {
            refreshCommentsCallback.run();
        } else if (root != null && root.getParent() instanceof Pane) {
            ((Pane) root.getParent()).getChildren().remove(root);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

 private void updateActionsVisibility() {
        boolean show = false;
        if (currentUserId != null && studentNumber != null) {
            show = currentUserId.trim().equals(studentNumber.trim());
        } else if (currentUserName != null && userName != null) {
            show = currentUserName.trim().equalsIgnoreCase(userName.trim());
        }
        actionsBox.setVisible(show);
        actionsBox.setManaged(show);
    }

    public String getCommentId() { return commentId; }
    public String getEventId() { return eventId; }
    public String getUserName() { return userName; }
    public String getText() { return text; }
    public int getRating() { return rating; }

    @FXML
    private void initialize() {
        if (editButton != null) {
            editButton.setTooltip(new Tooltip("edit comment"));
        }
        if (deleteButton != null) {
            deleteButton.setTooltip(new Tooltip("delete comment"));
        }
        updateActionsVisibility();
    }
}
