package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Controller class for the admin dashboard view.
 * This class handles UI interactions for admin-related tasks such as
 * viewing users, events, and club information from Firebase.
 * @author Utku
 */
public class AdminDashboardController {

    @FXML
    private Button profileButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox userListBox;

    @FXML
    private VBox eventListBox;

    @FXML
    private VBox clubsListBox;
    
    @FXML
    private Button addClubButton;

    @FXML
    private ImageView adminImageView;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label adminMailLabel;

   @FXML
    private void initialize() {
    System.out.println(" Admin dashboard initialized");

    // ComboBox 
    filterComboBox.getItems().addAll("All", "student", "club_manager", "pending");
    filterComboBox.setValue("All");

    refreshClubList();

    loadUsers("", "All");
    loadEvents();

    // Refresh 
    refreshButton.setOnAction(e -> {
        refreshClubList(); 
        loadUsers(searchField.getText().trim(), filterComboBox.getValue());
        loadEvents();
    });


    searchField.textProperty().addListener((observable, oldVal, newVal) -> {
        loadUsers(newVal.trim(), filterComboBox.getValue());
    });


    filterComboBox.valueProperty().addListener((observable, oldVal, newVal) -> {
        loadUsers(searchField.getText().trim(), newVal);
    });

    System.out.println("adminImageView = " + adminImageView);
    }

    @FXML
    private void handleAddClubButton(ActionEvent event) {
        SceneChanger.switchScene(event, "add_club.fxml");
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        SceneChanger.switchScene(event, "welcome.fxml");
    }

    @FXML
private void onOpenLeadershipRequests(ActionEvent event) {
    try {
        SceneChanger.switchScene(event, "admin_leadership_requests.fxml");
    } catch (Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Cannot open Leadership Requests.");
        alert.showAndWait();
    }
}

    /**
     * Loads and filters user data from Firestore, then displays it.
     *
     * @param keyword     Keyword to search for in user names or emails.
     * @param roleFilter  Selected role to filter users by (All, student, club_manager).
     */
    public void loadUsers(String keyword, String roleFilter) {
    userListBox.getChildren().clear();
    Firestore db = FirestoreClient.getFirestore();

    CollectionReference usersRef = db.collection("users");
    ApiFuture<QuerySnapshot> future = usersRef.get();

    try {
        List<QueryDocumentSnapshot> userDocs = future.get().getDocuments();
        List<DocumentSnapshot> clubDocs = loadClubDocuments(); 

        for (QueryDocumentSnapshot doc : userDocs) {
            String name = doc.getString("name");
            String surname = doc.getString("surname");
            String email = doc.getString("email");
            String role = doc.getString("role");
            String club = doc.contains("club") ? doc.getString("club") : null;
            String userId = doc.getId();

            // Filter
            if (!keyword.isEmpty() &&
                !(name.toLowerCase().contains(keyword.toLowerCase()) ||
                  email.toLowerCase().contains(keyword.toLowerCase()))) {
                continue;
            }

            if (!roleFilter.equals("All") && !role.equals(roleFilter)) {
                continue;
            }

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_card_admin.fxml"));
            HBox userCard = loader.load();

            // Controller
            UserCardAdminController controller = loader.getController();


            String currentClubName = getClubNameById(clubDocs, club); 
            String fullName = name + " " + surname;

            controller.setUserData(userId, fullName, role, currentClubName);
            controller.setClubList(clubDocs); 

            // Detail and delete button
            controller.getDeleteButton().setOnAction(e -> deleteUser(userId));
            

            // Add to List
            userListBox.getChildren().add(userCard);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private String getClubNameById(List<DocumentSnapshot> clubDocs, String clubId) {
    if (clubId == null) return null;

    for (DocumentSnapshot doc : clubDocs) {
        if (doc.getId().equals(clubId)) {
            return doc.getString("name");
        }
    }
    return null;
}

private List<DocumentSnapshot> loadClubDocuments() throws ExecutionException, InterruptedException {
    CollectionReference clubsRef = FirestoreClient.getFirestore().collection("clubs");
    List<QueryDocumentSnapshot> docs = clubsRef.get().get().getDocuments();
    return new ArrayList<>(docs);
}

private UserModel loggedInUser;
public void setLoggedInUser(UserModel user) {
    this.loggedInUser = user;
    refreshClubList();
    loadUsers("", filterComboBox.getValue());
    loadEvents();

}

/**
 * Loads and displays all events from Firestore using the shared EventCard design.
 * EventDetail.fxml yolu proje kaynaklarınıza (/views/event_detail.fxml) göre ayarlandı.
 */
private void loadEvents() {
    // Clear cards
    eventListBox.getChildren().clear();

    // Firestore sample
    Firestore db = FirestoreClient.getFirestore();

    // pull docs from "events" collection
    ApiFuture<QuerySnapshot> future = db.collection("events").get();
    try {
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot doc : documents) {
            // load event_card.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
            Parent cardRoot = loader.load();

            // Send data 
            EventCardController cardCtrl = loader.getController();
            cardCtrl.setCurrentUser(loggedInUser);
            cardCtrl.setData(doc.getId(), doc.getData());

            cardRoot.setOnMouseClicked(evt -> {
    ActionEvent ae = new ActionEvent(cardRoot, null);
    SceneChanger.switchScene(
        ae,
        "event_detail.fxml",          
        controller -> {
            EventDetailController detailCtrl = (EventDetailController) controller;
            detailCtrl.setLoggedInUser(loggedInUser);
            detailCtrl.setEventId(doc.getId());
        }
    );
});

            eventListBox.getChildren().add(cardRoot);
        }
    } catch (InterruptedException | ExecutionException | IOException ex) {
        ex.printStackTrace();
    }
}




/**
 * Updates a user's role in Firestore and refreshes the view.
 *
 * @param uid      User document ID
 * @param newRole  The new role to assign (student or club_manager)
 * @deprecated
 */
private void updateRole(String uid, String newRole) {
    Firestore db = FirestoreClient.getFirestore();
    db.collection("users").document(uid).update("role", newRole);
    refreshButton.fire();
}

/**
 * Deletes a user from Firestore and refreshes the dashboard.
 *
 * @param uid User document ID to delete
 * @deprecated
 */
private void deleteUser(String uid) {
    Firestore db = FirestoreClient.getFirestore();
    db.collection("users").document(uid).delete();
    refreshButton.fire();
}

/**
 * Deletes an event from Firestore and refreshes the dashboard.
 *
 * @param eventId Event document ID to delete
 * @deprecated
 */
private void deleteEvent(String eventId) {
    Firestore db = FirestoreClient.getFirestore();
    db.collection("events").document(eventId).delete();
    refreshButton.fire();
}

/**
 * Refreshes the list of clubs displayed in the UI.
 * Fetches all documents from the 'clubs' collection in Firestore,
 * loads a UI card for each club using the 'club_card.fxml' template,
 * sets the data on the card controller, and adds the card to the list box.
 * In case of any errors (e.g., Firestore connection issues), the exception is printed.
 * @deprecated
 */
protected void refreshClubList() {
    clubsListBox.getChildren().clear(); // Önce eski kartları sil

    Firestore db = FirestoreClient.getFirestore();
    ApiFuture<QuerySnapshot> future = db.collection("clubs").get();

    future.addListener(() -> {
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Node> newClubCards = new ArrayList<>();

            for (DocumentSnapshot doc : documents) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card_admin.fxml"));
                    AnchorPane clubCard = loader.load();

                    ClubCardAdminController controller = loader.getController();
                    controller.setParentController(this); 
                    controller.setData(doc.getId(), doc.getData());

                    newClubCards.add(clubCard);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Platform.runLater(() -> {
                clubsListBox.getChildren().addAll(newClubCards);

                if (documents.isEmpty()) {
                    System.out.println("No Club found.");
                } else {
                    System.out.println("Clubs succesfuly downloaded");
                }
            });

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Error occured while downloading datas from firestore");
        }
    }, Runnable::run);
}
public UserModel getLoggedInUser() {
    return this.loggedInUser;
}



}
