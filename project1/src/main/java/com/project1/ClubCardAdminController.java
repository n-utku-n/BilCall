package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for the admin club card UI.
 * Allows admins to view club details and delete clubs.
 * Automatically refreshes the dashboard after deletion.
 * 
 * @author Utku
 */
public class ClubCardAdminController {

    @FXML
    private Label clubNameLabel;

    @FXML
    private Label eventCountLabel;

    @FXML
    private Label managerCountLabel;

    @FXML
    private Label participantCountLabel;

    @FXML
    private ImageView clubLogo;

    @FXML
    private Button viewButton;

    @FXML
    private Button deleteButton;

    private String clubId;
    private String clubName;

    private AdminDashboardController dashboardController;

    /**
     * Sets the parent dashboard controller to allow refreshing after actions.
     * 
     * @param controller the AdminDashboardController instance
     */
    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    /**
 * Populates the card UI with club data from Firestore.
 *
 * @param id   the club document ID
 * @param data the club document data as a map
 */
public void setData(String id, Map<String, Object> data) {
    this.clubId = id;
    this.clubName = (String) data.get("name");

    clubNameLabel.setText(clubName);

    updateCounts(data);

    String logoUrl = (String) data.get("logoUrl");
    if (logoUrl != null && !logoUrl.isEmpty()) {
        try {
            Image image = new Image(logoUrl, true);
            clubLogo.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
private void updateCounts(Map<String, Object> data) {
    // 1) Participants (followers)
    @SuppressWarnings("unchecked")
    List<String> parts = (List<String>) data.get("participants");
    int pCount = parts != null ? parts.size() : 0;
    participantCountLabel.setText("Followers: " + pCount);

    // 2) Managers
    @SuppressWarnings("unchecked")
    List<String> mgrs = (List<String>) data.get("managers");
    int mCount = mgrs != null ? mgrs.size() : 0;
    managerCountLabel.setText("Managers: " + mCount);

    // 3) Active events
    Object aec = data.get("activeEventCount");
    if (aec instanceof Number) {
        eventCountLabel.setText("Events: " + ((Number) aec).intValue());
    } else {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> fut = db.collection("events")
            .whereEqualTo("clubId", clubId)
            .get();
        fut.addListener(() -> {
            try {
                int eCount = fut.get().getDocuments().size();
                Platform.runLater(() ->
                    eventCountLabel.setText("Events: " + eCount)
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, Runnable::run);
    }
}





    /**
     * Handles the "View" button click to navigate to the club profile screen.
     * 
     * @param event the button click event
     */
@FXML
private void handleView(ActionEvent event) throws IOException {
    String selectedClubId = this.clubId; 

    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/views/club_profile.fxml")
    );
    Parent root = loader.load();

    ClubProfileController c = loader.getController();
    c.setClubContext(selectedClubId,null); 
    c.setCurrentUser(parentController.getLoggedInUser());

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setScene(new Scene(root));
    stage.show();
}


    /**
     * Handles the "Delete" button click with a confirmation dialog.
     * Deletes the club from Firestore and refreshes the club list.
     */
   @FXML
private void handleDelete() {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Delete Club");
    confirm.setHeaderText("Are you sure you want to delete this club?");
    confirm.setContentText("This action cannot be undone.");

    confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> future = db.collection("clubs").document(clubId).delete();

            try {
                future.get(); 
                System.out.println("✅ Club deleted: " + clubId);

                if (parentController != null) {
                    parentController.refreshClubList();
                }

            } catch (Exception e) {
                System.err.println("Failed to delete club: " + e.getMessage());
                e.printStackTrace();
            }
        }
    });
}



        private AdminDashboardController parentController;

        public void setParentController(AdminDashboardController controller) {
            this.parentController = controller;
        }
}
