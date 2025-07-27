package com.project1;

import com.project1.SceneChanger;
import com.project1.UserModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.Node;
import com.project1.MainDashboardController;

public class EventDetailController {

    @FXML private Label eventNameLabel;
    @FXML private Label participantInfoLabel;
    @FXML private ImageView eventImage;
    // Description is now a TextFlow and Text node for dynamic height
    @FXML private TextFlow descriptionLabelContainer;
    @FXML private Text descriptionText;
    @FXML private VBox clubCardPlaceholder;

    @FXML private ProgressBar participantBar;
    @FXML private Rectangle minParticipantLine;
    private ChangeListener<Number> widthListener;

    @FXML private Button joinButton;
    @FXML private Button commentsButton;
    @FXML private Button quitButton;
    @FXML private Button editDetailButton;
    @FXML private Button deleteDetailButton;

    // keep event data for edit/delete
    private Map<String, Object> eventData;

    private String eventId;
    private String clubId;

    
    private UserModel loggedInUser;
    private String currentUserName = "Anon";
    private String currentUserId = "";

    
    public void setLoggedInUser(UserModel user) {
        if (user == null) {
            System.err.println("setLoggedInUser: user is null!");
            return;
        }
        this.loggedInUser = user;
        this.currentUserName = user.getName()
            + (user.getSurname().isEmpty() ? "" : " " + user.getSurname());
        this.currentUserId = user.getStudentId();
    }


public void setEventData(String eventId, Map<String, Object> data) {
    this.eventId = eventId;
    this.eventData = data;

    
    eventNameLabel.setText(getString(data, "name", "Unnamed Event"));
    descriptionText.setText(getString(data, "description", ""));

    
    clubCardPlaceholder.getChildren().clear();
    String clubId = getString(data, "clubId", null);
    if (clubId != null && !clubId.isEmpty()) {
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                .collection("clubs")
                .document(clubId)
                .get().get();
            if (clubDoc.exists()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card.fxml"));
                Parent clubCard = loader.load();
                ClubCardController ctl = loader.getController();
                ctl.setData(clubDoc.getId(), clubDoc.getData());
                ctl.setPreviousEventId(this.eventId);
                ctl.setCurrentUser(loggedInUser);
                clubCardPlaceholder.getChildren().add(clubCard);
            } else {
                showClubError("Club not found: " + clubId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showClubError("Error loading club.");
        }
    } else {
        showClubError("No clubId provided.");
    }

   
    List<String> participantsList = (List<String>) data.get("participants");
    if (participantsList == null) {
        participantsList = new ArrayList<>();
    } else {
        participantsList.removeIf(Objects::isNull);
    }
    int current = participantsList.size();
    int min = getNumber(data, "minParticipants", 0);
    int max = getNumber(data, "maxParticipants", 0);
    participantInfoLabel.setText(String.format("👥 Participants: %d (Min: %d, Max: %d)", current, min, max));
    double progress = max > 0 ? (double) current / max : 0;
    participantBar.setProgress(progress);

    if (widthListener != null) {
        participantBar.widthProperty().removeListener(widthListener);
    }
    final double ratio = max > 0 ? (double) min / max : 0;
    widthListener = (obs, o, n) -> minParticipantLine.setTranslateX(n.doubleValue() * ratio);
    participantBar.applyCss();
    participantBar.layout();
    minParticipantLine.setTranslateX(participantBar.getWidth() * ratio);
    participantBar.widthProperty().addListener(widthListener);

   
    boolean hasJoined = participantsList.stream()
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .anyMatch(id -> id.trim().equals(currentUserId.trim()));
    quitButton.setVisible(hasJoined);
    quitButton.setManaged(hasJoined);

    if (current >= max) {
        if (hasJoined) {
            joinButton.setText("Show Comments");
            joinButton.setDisable(false);
            joinButton.setOnAction(this::onCommentsClicked);
        } else {
            joinButton.setText("Event Full");
            joinButton.setDisable(true);
            joinButton.setOnAction(null);
        }
        commentsButton.setVisible(false);
        commentsButton.setManaged(false);
    } else {
        if (hasJoined) {
            joinButton.setText("Show Comments");
            joinButton.setOnAction(this::onCommentsClicked);
        } else {
            joinButton.setText("Join");
            joinButton.setOnAction(this::onJoinClicked);
        }
        joinButton.setDisable(false);
        commentsButton.setVisible(false);
        commentsButton.setManaged(false);
    }

    
    boolean isAdmin = loggedInUser != null
        && "admin".equalsIgnoreCase(loggedInUser.getRole());
    boolean isManager = loggedInUser != null
        && "club_manager".equalsIgnoreCase(loggedInUser.getRole())
        && Objects.equals(data.get("clubId"), loggedInUser.getClubId());
    java.util.Date now = new java.util.Date();
    Object rawDate = data.get("eventDate");
    java.util.Date eventDateObj = (rawDate instanceof com.google.cloud.Timestamp ts)
        ? ts.toDate()
        : (rawDate instanceof java.util.Date ud ? ud : now);
    boolean isFuture = eventDateObj.after(now);
    boolean allowEditDelete = isAdmin || (isManager && isFuture);

    editDetailButton.setVisible(allowEditDelete);
    editDetailButton.setManaged(allowEditDelete);
    deleteDetailButton.setVisible(allowEditDelete);
    deleteDetailButton.setManaged(allowEditDelete);

    
    String posterUrl = getString(data, "posterUrl", null);
    if (posterUrl != null && !posterUrl.isEmpty()) {
        try {
            eventImage.setImage(new Image(posterUrl, true));
        } catch (Exception e) {
            System.err.println(" Not upload poster " + posterUrl);
        }
    }
}

    private void showClubError(String msg) {
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        clubCardPlaceholder.getChildren().setAll(lbl);
    }

    private String getString(Map<String,Object> data, String key, String fallback) {
        Object o = data.get(key);
        return (o instanceof String) ? (String)o : fallback;
    }

    private int getNumber(Map<String,Object> data, String key, int fallback) {
        Object o = data.get(key);
        return (o instanceof Number) ? ((Number)o).intValue() : fallback;
    }

@FXML
private void handleBack(ActionEvent event) {
    
    String role = loggedInUser.getRole();
    String targetFxml = "main_dashboard.fxml";
    if ("admin".equals(role)) {
        targetFxml = "admin_dashboard.fxml";
    }

    
    FXMLLoader loader = SceneChanger.switchScene(event, targetFxml);
    if (loader != null) {
        if ("admin".equals(role)) {
            
            AdminDashboardController adminCtrl = loader.getController();
            adminCtrl.setLoggedInUser(loggedInUser);
        } else {
            MainDashboardController mainCtrl = loader.getController();
            mainCtrl.setLoggedInUser(loggedInUser);
        }
    }
}

    
    @FXML
    private void onJoinClicked(ActionEvent event) {
        if (eventId == null || eventId.trim().isEmpty() || loggedInUser == null) return;
        try {
            DocumentSnapshot snap = FirestoreClient.getFirestore()
                    .collection("events").document(eventId).get().get();
            List<String> participantsList = (List<String>) snap.get("participants");
            if (participantsList != null && participantsList.contains(currentUserId)) {
                
                onCommentsClicked(event);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("events")
                    .document(eventId)
                    .update(
                            "participants", FieldValue.arrayUnion(currentUserId),
                            "currentParticipants", FieldValue.increment(1)
                    )
                    .get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

       
        FXMLLoader loader = SceneChanger.switchScene(event, "comments.fxml");
        if (loader != null) {
            CommentsController cc = loader.getController();
            cc.setCurrentUser(loggedInUser);
            cc.setEventContext(eventId, eventNameLabel.getText());
        }
    }

    
    @FXML
    private void onCommentsClicked(ActionEvent event) {
        FXMLLoader loader = SceneChanger.switchScene(event, "comments.fxml");
        if (loader != null) {
            CommentsController cc = loader.getController();
            cc.setCurrentUser(loggedInUser);
            cc.setEventContext(eventId, eventNameLabel.getText());
        }
    }

  
@FXML
private void onEditDetail(ActionEvent evt) {
    try {
       
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/create_event.fxml"));
        Parent root = loader.load();

   
        CreateEventController ctl = loader.getController();
        ctl.setUser(loggedInUser);
        ctl.setClubInfo(
            (String) eventData.get("clubId"),
            (String) eventData.get("clubName")
        );
        ctl.populateForEdit(eventId, eventData);

       
        Stage stage = (Stage) ((Node) evt.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    @FXML
    private void onDeleteDetail(ActionEvent evt) {
        CompletableFuture.runAsync(() -> {
            try {
                Firestore db = FirestoreClient.getFirestore();
               
                db.collection("events").document(eventId).delete().get();
               
                String clubId = (String)eventData.get("clubId");
                db.collection("clubs").document(clubId)
                    .update("activeEventCount", FieldValue.increment(-1)).get();
             
                Platform.runLater(() -> {
                    FXMLLoader loader = SceneChanger.switchScene(evt, "main_dashboard.fxml");
                    MainDashboardController ctrl = (MainDashboardController)loader.getController();
                    ctrl.setLoggedInUser(loggedInUser);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

   
    public void setEventId(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            System.err.println("Warning: setEventId received empty eventId, skipping reload.");
            return;
        }
        this.eventId = eventId;
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot snap = db.collection("events").document(eventId).get().get();
            if (snap.exists()) {
                setEventData(eventId, snap.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   
    @FXML
    private void onQuitClicked(ActionEvent event) {
        if (eventId == null || eventId.trim().isEmpty() || currentUserId == null || currentUserId.trim().isEmpty()) {
            System.err.println("eventId or userId is missing for the quit operation.");
            return;
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference eventRef = db.collection("events").document(eventId);

        CompletableFuture.runAsync(() -> {
            try {
                db.runTransaction(transaction -> {
                    DocumentSnapshot snap = transaction.get(eventRef).get();
                    if (!snap.exists()) {
                        throw new RuntimeException("Not found event :  " + eventId);
                    }

                    List<String> participants = (List<String>) snap.get("participants");
                    Long currentCount = snap.contains("currentParticipants") ? snap.getLong("currentParticipants") : 0L;

                    if (participants == null || !participants.contains(currentUserId)) {
                        System.out.println("The user is not active, the logout process was skipped.");
                        return null;
                    }

                   
                    var commentsCol = eventRef.collection("comments");
                    var commentsQuery = commentsCol.whereEqualTo("studentNo", currentUserId).get().get();

                    
                    double ratingSum = snap.contains("ratingSum") ? snap.getDouble("ratingSum") : 0.0;
                    long ratingCount = snap.contains("ratingCount") ? snap.getLong("ratingCount") : 0;

                   
                    for (var doc : commentsQuery.getDocuments()) {
                        double rating = doc.contains("rating") ? doc.getDouble("rating") : 0.0;
                        ratingSum -= rating;
                        ratingCount = Math.max(0, ratingCount - 1);
                        transaction.delete(doc.getReference());
                    }

                    participants.remove(currentUserId);

                    double newAvg = ratingCount > 0 ? ratingSum / ratingCount : 0.0;

                    transaction.update(eventRef,
                            "participants", participants,
                            "currentParticipants", Math.max(0, currentCount - 1),
                            "ratingSum", ratingSum,
                            "ratingCount", ratingCount,
                            "averageRating", newAvg
                    );

                    return null;
                }).get();

                Platform.runLater(() -> {
                    quitButton.setVisible(false);  
                    setEventId(eventId);           
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("Error leaving event: " + ex.getMessage());
                });
            }
        });
    }
}