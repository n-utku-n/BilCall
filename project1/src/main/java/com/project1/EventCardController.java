package com.project1;

import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import java.util.concurrent.CompletableFuture;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.project1.UserModel;
import com.project1.EventDetailController;

/**
 * Controller for the event card UI component.
 * Displays brief information about an event and allows users
 * to navigate to its detail page.
 *
 * @author Utku, Serra
 */
public class EventCardController {
private javafx.beans.value.ChangeListener<Number> widthListener;

    @FXML
    private StackPane photoWrapper;

    @FXML
    private Rectangle photoClip;

    @FXML 
    private StackPane progressContainer;

    @FXML
    private VBox eventCardRoot;

    @FXML 
    private ProgressBar participantBar;

    @FXML 
    private Rectangle minParticipantLine;
    
    @FXML
    private StackPane participantContainer; 

    @FXML 
    private Label currentParticipantsText;

    @FXML
    private Label maxParticipantsText;

    @FXML
    private Label eventFullLabel;

    @FXML 
    private Label eventName;

    @FXML 
    private Label eventDateText;
    @FXML
    private HBox ratingBox;
    @FXML
    private Label ratingStarsLabel;
    @FXML
    private Label ratingValueLabel;


    // REMOVED: eventDateText is no longer in the design
    // @FXML
    // private Label eventDateText;

    @FXML 
    private Label clubName;

    @FXML 
    private ImageView eventImage;

    @FXML 
    private ImageView clubLogo;

    private boolean isEditMode = false;
    private UserModel currentUser;
    public void setCurrentUser(UserModel user) { this.currentUser = user; }




    /**
     * Event card loading whether based on clicked or not
     * @author Serra
     */
   @FXML
private void onCardClicked(MouseEvent event) {
   
    System.out.println("Event card clicked!");
    
    if (eventId != null && eventData != null && currentUser != null) {
        navigateToEventDetail();
    } else {
        System.err.println("EventCardController: Missing data! eventId, eventData or currentUser is null.");
    }
}

    
    private String eventId;

   
    private String clubId;

    private Map<String, Object> eventData;
    

 
    public void setData(String eventId, Map<String, Object> data) {
        System.out.println("Setting data for event card");
        System.out.println("participantContainer null check: " + (participantContainer == null));
    
        
        initializeComponents();
        

        if (!validateComponents()) {
            System.out.println("Required components are missing, cannot populate card");
            return;
        }

        this.eventId = eventId;
        this.eventData = data;
       
        if (eventFullLabel != null) {
            eventFullLabel.setVisible(false);
            eventFullLabel.setManaged(false);
        }

       
        populateEventDetails(data);
        populateParticipantInfo(data);
        loadImages(data);
        showAverageRating(data);
        
    }

    private void showAverageRating(Map<String, Object> data) {
    double avg = 0.0;
    if (data.get("averageRating") instanceof Number) {
        avg = ((Number) data.get("averageRating")).doubleValue();
    }
   
    StringBuilder stars = new StringBuilder();
    int full = (int) avg;
    for (int i = 0; i < full; i++) stars.append("★");
    if (avg - full >= 0.5) stars.append("☆"); 
    while (stars.length() < 5) stars.append("☆");

    ratingStarsLabel.setText(stars.toString());
    ratingValueLabel.setText(String.format("%.1f", avg));
    
   
    if (avg > 0) {
        ratingBox.setVisible(true);
    } else {
        ratingBox.setVisible(false);
    }
}
    
    private void initializeComponents() {

        if (progressContainer == null) {
            progressContainer = (StackPane) eventCardRoot.lookup("#progressContainer");
            System.out.println("lookup progressContainer: " + (progressContainer != null));
        }
        if (eventCardRoot == null) {
            System.out.println("eventCardRoot is null - FXML injection failed");
            return;
        }

        if (clubName == null) {
            clubName = (Label) eventCardRoot.lookup("#clubName");
            System.out.println("lookup clubName: " + (clubName != null));
        }

        if (eventName == null) {
            eventName = (Label) eventCardRoot.lookup("#eventName");
            System.out.println("lookup eventName: " + (eventName != null));
        }

        if (eventDateText == null) {
            eventDateText = (Label) eventCardRoot.lookup("#eventDateText");
            System.out.println("lookup eventDateText: " + (eventDateText != null));
        }

        if (participantBar == null) {
            participantBar = (ProgressBar) eventCardRoot.lookup("#participantBar");
            System.out.println("lookup participantBar: " + (participantBar != null));
        }

        if (minParticipantLine == null) {
            minParticipantLine = (Rectangle) eventCardRoot.lookup("#minParticipantLine");
            System.out.println("lookup minParticipantLine: " + (minParticipantLine != null));
        }

        if (participantContainer == null) {
            participantContainer = (StackPane) eventCardRoot.lookup("#participantContainer");
            System.out.println("lookup participantContainer: " + (participantContainer != null));
        }

        if (currentParticipantsText == null) {
            currentParticipantsText = (Label) eventCardRoot.lookup("#currentParticipantsText");
            System.out.println("lookup currentParticipantsText: " + (currentParticipantsText != null));
        }

        if (maxParticipantsText == null) {
            maxParticipantsText = (Label) eventCardRoot.lookup("#maxParticipantsText");
            System.out.println("lookup maxParticipantsText: " + (maxParticipantsText != null));
        }

        if (eventImage == null) {
            eventImage = (ImageView) eventCardRoot.lookup("#eventImage");
            System.out.println("lookup eventImage: " + (eventImage != null));
        }

        if (clubLogo == null) {
            clubLogo = (ImageView) eventCardRoot.lookup("#clubLogo");
            System.out.println("lookup clubLogo: " + (clubLogo != null));
        }

        if (eventFullLabel == null) {
            eventFullLabel = (Label) eventCardRoot.lookup("#eventFullLabel");
            System.out.println("lookup eventFullLabel: " + (eventFullLabel != null));
        }

    }

   
    private boolean validateComponents() {
        boolean allValid = true;

        if (eventName == null) {
            System.out.println("eventName is null");
            allValid = false;
        }
        if (eventDateText == null) {
            System.out.println("eventDateText is null");
            allValid = false;
        }
        if (clubName == null) {
            System.out.println("clubName is null");
            allValid = false;
        }
        if (participantBar == null) {
            System.out.println("participantBar is null");
            allValid = false;
        }
        if (currentParticipantsText == null) {
            System.out.println("currentParticipantsText is null");
            allValid = false;
        }
        if (maxParticipantsText == null) {
            System.out.println("maxParticipantsText is null");
            allValid = false;
        }

        return allValid;
    }

   
    private void populateEventDetails(Map<String, Object> data) {
        String eventNameStr = (String) data.get("name");
        if (eventNameStr != null) {
            eventName.setText(eventNameStr);
        } else {
            eventName.setText("Unnamed Event");
        }
        String clubNameStr = (String) data.get("clubName");
        if (clubNameStr != null && !clubNameStr.isEmpty()) {
            clubName.setText(clubNameStr);
        } else {
            clubName.setText("Unknown Club");
        }

        Timestamp eventDate = (Timestamp) data.get("eventDate");
        if (eventDate != null && eventDateText != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault());
            eventDateText.setText(formatter.format(eventDate.toDate().toInstant()));
        } else {
            eventDateText.setText("Date unavailable");
        }
    }

    private void populateParticipantInfo(Map<String, Object> data) {
        int current = 0, min = 0, max = 100;
        try {
            Object o;
            if (data.get("participants") instanceof java.util.List) {
                current = ((java.util.List<?>) data.get("participants")).size();
            } else if ((o = data.get("currentParticipants")) != null) {
                current = ((Number)o).intValue();
            }
            if ((o = data.get("minParticipants")) != null) min = ((Number)o).intValue();
            if ((o = data.get("maxParticipants")) != null) max = ((Number)o).intValue();
        } catch (Exception e) {
            System.out.println("Error parsing participant numbers: " + e.getMessage());
        }

        participantBar.setProgress(max > 0 ? (double) current / max : 0);

        final int finalMin = min;
        final int finalMax = max;
        Platform.runLater(() -> {
            double barWidth = participantBar.getWidth();
            double lineWidth = minParticipantLine.getWidth();
            double lineCenterOffset = lineWidth / 2;

            if (barWidth < 2 || lineWidth < 1) {
                Platform.runLater(() -> {
                    double retryWidth = participantBar.getWidth();
                    double retryLineWidth = minParticipantLine.getWidth();
                    double retryCenterOffset = retryLineWidth / 2;
                    double ratio = finalMax > 0 ? (double) finalMin / finalMax : 0;

                    if (finalMin <= 0 || finalMin >= finalMax) {
                        minParticipantLine.setOpacity(0);
                    } else {
                        double translateX = (retryWidth * ratio) - retryCenterOffset;
                        minParticipantLine.setTranslateX(translateX);
                        minParticipantLine.setOpacity(1);
                    }
                });
            } else {
                double minRatio = finalMax > 0 ? (double) finalMin / finalMax : 0;
                if (finalMin <= 0 || finalMin >= finalMax) {
                    minParticipantLine.setOpacity(0);
                } else {
                    double translateX = (barWidth * minRatio) - lineCenterOffset;
                    minParticipantLine.setTranslateX(translateX);
                    minParticipantLine.setOpacity(1);
                }
            }
        });

        currentParticipantsText.setText(String.valueOf(current));
        maxParticipantsText.setText(String.valueOf(max));

       
        boolean isFull = current >= max;
        if (eventFullLabel != null) {
            eventFullLabel.setVisible(isFull);
            eventFullLabel.setManaged(isFull);
        }
    }
    
    private void positionMinParticipantLine(int min, int max) {
        if (max > 0 && min <= max && minParticipantLine != null && participantBar != null) {
            Platform.runLater(() -> {
                try {
                    participantBar.applyCss();
                    participantBar.layout();

                    double barWidth = participantBar.getWidth();
                    if (barWidth > 0) {
                        double ratio = (double) min / max;
                        double lineCenterOffset = minParticipantLine.getWidth() / 2;
                        double translateX = (barWidth * ratio) - lineCenterOffset;
                        minParticipantLine.setTranslateX(translateX);
                        minParticipantLine.setOpacity(1);
                    }
                } catch (Exception e) {
                    System.out.println("Error positioning min participant line: " + e.getMessage());
                }
            });
        } else {
            System.out.println("Cannot position min line - invalid parameters or null components");
        }
    }
 

   
  private void loadImages(Map<String, Object> data) {
    
    String posterUrl = (String) data.get("posterUrl");
    if (posterUrl != null && !posterUrl.isEmpty()) {
        try {
            System.out.println("🔍 Loading event image: " + posterUrl);
            eventImage.setImage(new Image(posterUrl, true));
        } catch (Exception e) {
            System.out.println("Error loading event image: " + e.getMessage());
        }
    }

 
    String logoUrl = (String) data.get("logoUrl");
    System.out.println("🔍 logoUrl from data: " + logoUrl);

    
    if ((logoUrl == null || logoUrl.isEmpty()) && data.containsKey("clubId")) {
        String clubId = (String) data.get("clubId");
        System.out.println("🔍 Fallback fetching logo for clubId: " + clubId);
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                .collection("clubs")
                .document(clubId)
                .get()
                .get();
            if (clubDoc.exists()) {
                logoUrl = clubDoc.getString("logoUrl");
                System.out.println("🔍 logoUrl from Firestore: " + logoUrl);
            }
        } catch (Exception e) {
            System.out.println("Error fetching logoUrl from Firestore: " + e.getMessage());
        }
    }

    
    if (logoUrl != null && !logoUrl.isEmpty()) {
        try {
            System.out.println("🔍 Setting clubLogo image: " + logoUrl);
            clubLogo.setImage(new Image(logoUrl, true));
        } catch (Exception e) {
            System.out.println("Error loading club logo image: " + e.getMessage());
        }
    } else {
        System.out.println("No valid logoUrl found, clubLogo remains empty.");
    }
} 

    
    private void loadClubLogo(String clubId) {
        try {
            DocumentSnapshot clubDoc = FirestoreClient.getFirestore()
                .collection("clubs")
                .document(clubId)
                .get()
                .get();

            if (clubDoc.exists()) {
                String clubLogoUrl = (String) clubDoc.get("logoUrl");
                if (clubLogoUrl != null && !clubLogoUrl.isEmpty()) {
                    clubLogo.setImage(new Image(clubLogoUrl, true));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading club logo: " + e.getMessage());
        }
    }

   
 private void navigateToEventDetail() {
    if (currentUser == null) {
        System.err.println("[EventCardController] User is null, cannot proceed to event details!");
        return;
    }

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_detail.fxml"));
        Parent root = loader.load();

       
        EventDetailController controller = loader.getController();
        controller.setLoggedInUser(currentUser);        
        controller.setEventData(eventId, eventData); 
        if (currentUser != null) {
            controller.setLoggedInUser(currentUser);
        } else {
            System.err.println("currentUser null, could not be sent to EventDetailController!");
            return; 
        }
        
        controller.setEventData(eventId, eventData);

        Stage stage = (Stage) eventCardRoot.getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

   
    @FXML
    private void handleDetails(ActionEvent event) {
        navigateToEventDetail();
    }
    
}