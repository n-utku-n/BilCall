package com.project1;

import com.project1.ClubProfileController;
import javafx.event.ActionEvent;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.Timestamp;
import com.google.firebase.cloud.FirestoreClient;
import java.util.Collections;

import javafx.scene.layout.VBox;
import com.project1.ClubCardController;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ProfileController implements Initializable {

    @FXML private Button backButton;
    @FXML private Button logOutButton;
    @FXML private Button createEventButton;
    @FXML private Button editClubButton;
    @FXML private Button applicationButton;

    @FXML private Label nameLabel;
    @FXML private Label surnameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;

    @FXML private ScrollPane joinedEventsScrollPane;
    @FXML private GridPane joinedEventsContainer;
    @FXML private VBox clubCardContainer;

    private UserModel loggedInUser;
    private UserModel viewerUser;
    private UserModel profileUser;  

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setOnAction(this::handleBackButton);
        if (logOutButton != null) logOutButton.setOnAction(this::handleLogoutButton);
        if (createEventButton != null) createEventButton.setOnAction(this::handleCreateEventButton);
        if (editClubButton != null) editClubButton.setOnAction(this::handleEditClubButton);
    }

    private void handleLogoutButton(ActionEvent event) {
        SceneChanger.switchScene(event, "welcome.fxml");
    }

@FXML
private void handleBackButton(ActionEvent event) {
    // 1) Geri dönerken kim dönecek? viewerUser varsa o, yoksa profileUser
    UserModel who = (viewerUser != null) ? viewerUser : profileUser;
    String role = who.getRole().toLowerCase();

    // 2) Hedef fxml'i belirle
    String targetFxml = role.equals("admin")
         ? "admin_dashboard.fxml"
         : "main_dashboard.fxml";

    // 3) Sahne değiştir
    FXMLLoader loader = SceneChanger.switchScene(event, targetFxml);
    if (loader == null) return;

    // 4) Geri dönerken controller’a kim döndüyse onu aktar
    if (role.equals("admin")) {
        AdminDashboardController adm = loader.getController();
        adm.setLoggedInUser(who);
    } else {
        MainDashboardController main = loader.getController();
        main.setLoggedInUser(who);
    }
}



/**
 * Called externally or via loadUser to set the current profile user
 * and update the UI accordingly.
 */
public void setUser(UserModel user) {
    // 1. Set profileUser and guard null
    this.profileUser = user;
    if (user == null) {
        System.err.println("ProfileController.setUser called with null user, skipping.");
        return;
    }

    // 2. Populate labels
    nameLabel.setText(user.getName() != null ? user.getName() : "N/A");
    surnameLabel.setText(user.getSurname() != null ? user.getSurname() : "N/A");
    emailLabel.setText(user.getEmail() != null ? user.getEmail() : "N/A");
    roleLabel.setText(user.getRole() != null ? user.getRole() : "N/A");

    // 3. Show club-manager buttons only if this profile belongs to a club manager
    boolean isClubManagerProfile = "club_manager".equalsIgnoreCase(user.getRole());
    createEventButton.setVisible(isClubManagerProfile);
    createEventButton.setManaged(isClubManagerProfile);
    editClubButton.setVisible(isClubManagerProfile);
    editClubButton.setManaged(isClubManagerProfile);

    // 4. If viewerUser is an admin viewing someone else's profile,
    //    disable those buttons entirely
    if (viewerUser != null
        && "admin".equalsIgnoreCase(viewerUser.getRole())
        && !viewerUser.getStudentId().equals(profileUser.getStudentId())) {

        createEventButton.setVisible(false);
        createEventButton.setManaged(false);
        editClubButton.setVisible(false);
        editClubButton.setManaged(false);
    }

    // 5. Load and display the club card if this is the owner's profile
    if (isClubManagerProfile) {
        clubCardContainer.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/club_card.fxml"));
            Node clubCard = loader.load();
            ClubCardController ccc = loader.getController();
            ccc.setClubInfo(user.getClubId(), user.getClubName());
            ccc.setCurrentUser(user);
            clubCardContainer.getChildren().add(clubCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 6. Load events this user has joined
    loadJoinedEvents(user.getStudentId());

        // —— YENİ EKLENECEK: ADMIN İÇİN İSTENMEYEN BUTONLARI KALDIR —— 
    if (viewerUser != null && "admin".equalsIgnoreCase(viewerUser.getRole())) {
        logOutButton.setVisible(false);
        logOutButton.setManaged(false);

        applicationButton.setVisible(false);
        applicationButton.setManaged(false);


    }
}

    public void setViewerUser(UserModel viewer) {
    this.viewerUser = viewer;
    }

    private void loadJoinedEvents(String studentId) {
        if (studentId == null || studentId.isEmpty()) return;
        Firestore db = FirestoreClient.getFirestore();
        joinedEventsContainer.getChildren().clear();

        CompletableFuture.runAsync(() -> {
            try {
                QuerySnapshot snapshot = db.collection("events").get().get();
                List<QueryDocumentSnapshot> docs = snapshot.getDocuments();
                long now = System.currentTimeMillis();
                // show only events the user joined
                int col = 0, row = 0;
                for (QueryDocumentSnapshot doc : docs) {
                    @SuppressWarnings("unchecked")
                    List<String> participants = (List<String>) doc.get("participants");
                    if (participants == null) {
                        participants = Collections.emptyList();
                    }
                    Timestamp ts = doc.getTimestamp("eventDate");
                    if (ts != null && ts.toDate().getTime() >= now && participants.contains(studentId)) {
                        Map<String, Object> data = doc.getData();
                        final int c = col, r = row;
                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/event_card.fxml"));
                                Parent card = loader.load();
                                EventCardController ec = loader.getController();
                                ec.setCurrentUser(loggedInUser);
                                ec.setData(doc.getId(), data);
                                joinedEventsContainer.add(card, c, r);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        col = (col + 1) % 2;
                        if (col == 0) row++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Load user profile including club fields and invoke setUser
     */
    public void loadUser(String uid) {
        Firestore db = FirestoreClient.getFirestore();
        CompletableFuture.runAsync(() -> {
            try {
                DocumentSnapshot doc = db.collection("users").document(uid).get().get();
                if (doc.exists()) {
                    UserModel user = doc.toObject(UserModel.class);
                    user.setClubId(doc.getString("clubId"));
                    user.setClubName(doc.getString("clubName"));
                    Platform.runLater(() -> setUser(user));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void handleCreateEventButton(ActionEvent event) {
         FXMLLoader loader = SceneChanger.switchScene(event, "create_event.fxml");
        CreateEventController cec = loader.getController();
        cec.setUser(loggedInUser);
        cec.setClubInfo(loggedInUser.getClubId(), loggedInUser.getClubName());
    }

    @FXML
    private void handleEditClubButton(ActionEvent event) {
        FXMLLoader loader = SceneChanger.switchScene(event, "club_profile.fxml");
        Object ctrl = loader.getController();
        if (ctrl instanceof ClubProfileController cpc) {
            cpc.setCurrentUser(loggedInUser);
            cpc.setClubContext(loggedInUser.getClubId(), null);
        }
    }

    // @FXML
    // private void handleEditClubButton(ActionEvent event) {
    //     FXMLLoader loader = SceneChanger.switchScene(event, "edit_club.fxml");
    //     Object ctrl = loader.getController();
    //     if (ctrl instanceof EditClubController ecc) {
    //         ecc.setClubInfo(loggedInUser.getClubId(), loggedInUser.getClubName());
    //     }
    // }
}