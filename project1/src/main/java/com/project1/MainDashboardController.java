package com.project1;

import com.project1.UserModel;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.cloud.Timestamp;
import com.project1.SceneChanger;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;

/**
 * Controller class for the main dashboard accessible by student and club_manager roles.
 * Displays user profile, event list, and allows searching.
 * @author Serra
 */
public class MainDashboardController {

    @FXML
    private VBox eventCardContainer;

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private GridPane eventGrid;

    @FXML
    private ImageView bilkentLogo; 

    @FXML
    private ImageView appLogo; 
    
     @FXML
    private FlowPane mainEventContainer;

        @FXML
    private ComboBox<String> sortComboBox;
    private UserModel loggedInUser;



@FXML
public void initialize() {
    System.out.println("initialize() starts");
    loadAppLogo();
    sortComboBox.getItems().addAll(
        "Highest Rated",
        "Lowest Rated",
        "Joined Clubs",
        "Most Popular",
        "Most Participants",
        "Least Participants",
        "Upcoming Events"
    );

    searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch(null));

    Platform.runLater(() -> {
        Stage stage = (Stage) mainEventContainer.getScene().getWindow();
        stage.setMaximized(true);
    });
}

@FXML
private void handleSortSelection(ActionEvent event) {
    String selected = sortComboBox.getValue();
    System.out.println("Sorting selected: " + selected);

    switch (selected) {
        case "Highest Rated":
            loadEventsSortedByRating(true);
            break;
        case "Lowest Rated":
            loadEventsSortedByRating(false);
            break;
        case "Joined Clubs":
            loadEventsFromJoinedClubs();
            break;
        case "Most Popular":
        case "Most Participants":
            loadEventsSortedByParticipantCount(true);
            break;
        case "Least Participants":
            loadEventsSortedByParticipantCount(false);
            break;
        case "Upcoming Events":
            loadUpcomingEvents();
            break;
        default:
            loadEvents(); 
    }
}
    

   @FXML
private void handleSearch(ActionEvent event) {
    String keyword = searchField.getText().trim().toLowerCase();
    System.out.println("Search: " + keyword);
    mainEventContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            String name = doc.getString("name");
            String desc = doc.getString("description");
            String clubName = doc.getString("clubName");  

            com.google.cloud.Timestamp ts = doc.getTimestamp("eventDate");
            if (ts != null && ts.toDate().before(new Date())) {
                continue;  
            }

            if ((name != null && name.toLowerCase().contains(keyword)) ||
                (desc != null && desc.toLowerCase().contains(keyword)) || 
                (clubName != null && clubName.toLowerCase().contains(keyword))) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                VBox eventCard = loader.load();
                EventCardController controller = loader.getController();
                controller.setCurrentUser(loggedInUser);

                Map<String, Object> data = doc.getData();
                String clubId = (String) data.get("clubId");
                if (clubId != null && !clubId.isEmpty()) {
                    try {
                        com.google.cloud.firestore.DocumentSnapshot clubDoc = com.google.firebase.cloud.FirestoreClient.getFirestore()
                                .collection("clubs")
                                .document(clubId)
                                .get()
                                .get();
                        if (clubDoc.exists()) {
                            String cname = clubDoc.getString("name");
                            data.put("clubName", cname != null ? cname : "Unknown Club");
                            String logoUrl = clubDoc.getString("logoUrl");
                            if (logoUrl != null && !logoUrl.isEmpty()) {
                                data.put("logoUrl", logoUrl);
                            }
                        } else {
                            data.put("clubName", "Unknown Club");
                        }
                    } catch (Exception ex) {
                        data.put("clubName", "Unknown Club");
                    }
                } else {
                    data.put("clubName", "Unknown Club");
                }

                controller.setData(doc.getId(), data);
                mainEventContainer.getChildren().add(eventCard);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    public void setLoggedInUser(UserModel user) {
        this.loggedInUser = user;
        System.out.println("studentId = " + user.getStudentId());
        System.out.println("setLoggedInUser: clubId=" + user.getClubId() + ", clubName=" + user.getClubName());
        loadEvents();
    }
    private void loadAppLogo() {
        
        try {
            bilkentLogo.setImage(new Image(
                getClass().getResourceAsStream("/images/bilcall_logo.png")
            ));
            appLogo.setImage(new Image(
                getClass().getResourceAsStream("/images/bilkent_logo.png")
            ));
        } catch (Exception e) {
            System.err.println("Logo error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

  private void loadEvents() {
    System.out.println("loadEvents starts");
    mainEventContainer.getChildren().clear();
    System.out.println("cards are cleaned");

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        System.out.println("From fb " + documents.size() + "is taken");

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            String eventId = doc.getId();
            Map<String, Object> data = doc.getData();
            System.out.println("eventId=" + eventId);

            com.google.cloud.Timestamp ts = doc.getTimestamp("eventDate");
            if (ts != null && ts.toDate().before(new Date())) {
                System.out.println("event date passed skipping: eventId " + eventId);
                continue;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            VBox eventCard = loader.load();
            EventCardController controller = loader.getController();
            controller.setCurrentUser(loggedInUser);
            System.out.println("fxml loaded and controll is connected");

            String clubId = (String) data.get("clubId");
            if (clubId != null && !clubId.isEmpty()) {
                try {
                    com.google.cloud.firestore.DocumentSnapshot clubDoc = com.google.firebase.cloud.FirestoreClient.getFirestore()
                            .collection("clubs")
                            .document(clubId)
                            .get()
                            .get();
                    if (clubDoc.exists()) {
                        String cname = clubDoc.getString("name");
                        data.put("clubName", cname != null ? cname : "Unknown Club");
                        String logoUrl = clubDoc.getString("logoUrl");
                        if (logoUrl != null && !logoUrl.isEmpty()) {
                            data.put("logoUrl", logoUrl);
                        }
                        System.out.println("clubName ve logoUrl are set.");
                    } else {
                        data.put("clubName", "Unknown Club");
                        System.out.println("clubDoc cannot be found");
                    }
                } catch (Exception ex) {
                    data.put("clubName", "Unknown Club");
                    System.out.println("club info cannot be taken");
                    ex.printStackTrace();
                }
            } else {
                data.put("clubName", "Unknown Club");
            }

            controller.setData(eventId, data);
            mainEventContainer.getChildren().add(eventCard);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


 
   @FXML
private void onProfileButtonClicked(ActionEvent event) {
    SceneChanger.switchScene(event, "profile.fxml", ctrl -> {
        ((ProfileController)ctrl).setUser(loggedInUser);
    });
}

public void setUser(UserModel user) {

    this.loggedInUser = user;
    loadEvents();
   
}
   
    private void loadAllEvents() {
    eventCardContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.DocumentSnapshot doc : documents) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            VBox card = loader.load();

            EventCardController controller = loader.getController();
            controller.setData(doc.getId(), doc.getData());

            eventCardContainer.getChildren().add(card);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}



// private void uploadDummyEventsToFirestore() {
//     try {
//         com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
//         Timestamp now = Timestamp.now();

//         List<Map<String, Object>> events = Arrays.asList(
//             new HashMap<String, Object>() {{
//                 put("name", "Calculus Workshop");
//                 put("description", "Multiple Integrals & Applications");
//                 put("eventType", "workshop");
//                 put("clubId", "math_club");
//                 put("clubName", "Mathematics Club");
//                 put("timestamp", now);
//                 put("eventDate", new Date(2025 - 1900, Calendar.JULY, 20, 15, 0, 0));
//                 put("location", "Bilkent MA-02");
//                 put("minParticipants", 10);
//                 put("maxParticipants", 50);
//                 put("currentParticipants", 12);
//                 put("posterUrl", "https://via.placeholder.com/400x180.png");
//             }},
//             new HashMap<String, Object>() {{
//                 put("name", "Summer Hackathon");
//                 put("description", "Build your first full-stack app in 24h");
//                 put("eventType", "hackathon");
//                 put("clubId", "hack_club");
//                 put("clubName", "Hackathon Club");
//                 put("timestamp", now);
//                 put("eventDate", new Date(2025 - 1900, Calendar.AUGUST, 5, 9, 30, 0));
//                 put("location", "Bilkent CC-10");
//                 put("minParticipants", 5);
//                 put("maxParticipants", 80);
//                 put("currentParticipants", 40);
//                 put("posterUrl", "https://via.placeholder.com/400x180.png");
//             }}
//         );

    
//         for (Map<String, Object> event : events) {
//             com.google.cloud.firestore.DocumentReference docRef = db
//                 .collection("events")
//                 .add(event)
//                 .get();  
//             System.out.println("Dummy event yüklendi: " + docRef.getId());
//         }

//     } catch (Exception e) {
//         e.printStackTrace();
//     }
// }



private void loadEventsSortedByRating(boolean descending) {
    mainEventContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .orderBy("averageRating", descending ? com.google.cloud.firestore.Query.Direction.DESCENDING : com.google.cloud.firestore.Query.Direction.ASCENDING)
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            addEventCardIfUpcoming(doc);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void loadEventsSortedByParticipantCount(boolean descending) {
    mainEventContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .orderBy("currentParticipants", descending ? com.google.cloud.firestore.Query.Direction.DESCENDING : com.google.cloud.firestore.Query.Direction.ASCENDING)
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            addEventCardIfUpcoming(doc);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void loadEventsFromJoinedClubs() {
    System.out.println("loadEventsFromJoinedClubs");
    mainEventContainer.getChildren().clear();
    List<String> joinedClubIds = new ArrayList<>();
    String studentId = loggedInUser.getStudentId();
    String currentUserId = null;

    try {
        com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
        com.google.cloud.firestore.QuerySnapshot userSnapshots = db.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .get();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> userDocs = userSnapshots.getDocuments();
        if (!userDocs.isEmpty()) {
            currentUserId = userDocs.get(0).getId();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    try {
        com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> clubDocs = db
                .collection("clubs")
                .get()
                .get()
                .getDocuments();


        for (com.google.cloud.firestore.QueryDocumentSnapshot clubDoc : clubDocs) {
            String clubId = clubDoc.getId();
            List<String> participants = (List<String>) clubDoc.get("participants");

            System.out.println("CLUB: " + clubId);
            System.out.println("Current User ID: " + currentUserId);
            System.out.println("Participants: " + participants);

            if (participants != null && currentUserId != null && participants.contains(currentUserId)) {
                System.out.println( "user joined club " + clubId);
                joinedClubIds.add(clubId);
            } else {
                System.out.println("no match " + clubId);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }
    if (joinedClubIds.isEmpty()) return;

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            String clubId = doc.getString("clubId");
            if (clubId != null && joinedClubIds.contains(clubId)) {
                addEventCardIfUpcoming(doc);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void addEventCardIfUpcoming(com.google.cloud.firestore.QueryDocumentSnapshot doc) {
    try {
        com.google.cloud.Timestamp ts = doc.getTimestamp("eventDate");
        if (ts != null && ts.toDate().before(new java.util.Date())) return;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
        VBox eventCard = loader.load();
        EventCardController controller = loader.getController();
        controller.setCurrentUser(loggedInUser);

        Map<String, Object> data = doc.getData();
        String clubId = doc.getString("clubId");

        if (clubId != null && !clubId.isEmpty()) {
            try {
                com.google.cloud.firestore.DocumentSnapshot clubDoc = com.google.firebase.cloud.FirestoreClient.getFirestore()
                        .collection("clubs")
                        .document(clubId)
                        .get()
                        .get();
                if (clubDoc.exists()) {
                    data.put("clubName", clubDoc.getString("name"));
                    data.put("logoUrl", clubDoc.getString("logoUrl"));
                }
            } catch (Exception ignored) {
                data.put("clubName", "Unknown Club");
            }
        } else {
            data.put("clubName", "Unknown Club");
        }

        controller.setData(doc.getId(), data);
        mainEventContainer.getChildren().add(eventCard);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void loadUpcomingEvents() {
    mainEventContainer.getChildren().clear();

    try {
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = com.google.firebase.cloud.FirestoreClient
                .getFirestore()
                .collection("events")
                .orderBy("eventDate", com.google.cloud.firestore.Query.Direction.ASCENDING)
                .get()
                .get()
                .getDocuments();

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
            addEventCardIfUpcoming(doc);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}