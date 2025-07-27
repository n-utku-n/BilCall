package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Controller for displaying and managing leadership requests submitted by users.
 * <p>
 * Loads pending requests from the Firestore collection "leadership_requests" and
 * presents each request with an option to delete it. Provides navigation back to the
 * admin dashboard.
 * </p>
 * @author Utku
 */
public class AdminLeadershipRequestsController implements Initializable {

    @FXML private VBox requestListContainer;

    /**
     * Called to initialize the controller after its root element has been completely processed.
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if none.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRequests();
    }

    /**
     * Loads all pending leadership requests from Firestore and adds them to the UI.
     * Each request is displayed with the applicant's email and the club name,
     * along with a button to delete the request.
     */
    private void loadRequests() {
        requestListContainer.getChildren().clear(); 
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("leadership_requests").get();

        new Thread(() -> {
            try {
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    String requestId = doc.getId();
                    String email     = doc.getString("email");
                    String clubName  = doc.getString("clubName");

                    if (email == null || clubName == null) continue;

                    Platform.runLater(() -> {
                        Label msg = new Label(email + " applied for " + clubName);
                        msg.getStyleClass().add("label");
                        Button deleteBtn = new Button("Delete");

                        deleteBtn.setOnAction(e -> handleDelete(requestId));

                        HBox row = new HBox(10, msg, deleteBtn);
                        requestListContainer.getChildren().add(row);
                    });
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).start();
    }
    /**
     * Deletes the specified leadership request document from Firestore.
     * Shows a confirmation alert once deletion is complete and reloads the request list.
     * @param requestId The Firestore document ID of the leadership request to delete.
     */
    private void handleDelete(String requestId) {
        Firestore db = FirestoreClient.getFirestore();

        new Thread(() -> {
            try {
                db.collection("leadership_requests").document(requestId).delete().get();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Deleted");
                    alert.setHeaderText(null);
                    alert.setContentText("Request deleted.");
                    alert.showAndWait();
                    loadRequests();
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles the action of the Back button, switching the scene to the admin dashboard.
     * Passes the current user model to the dashboard controller.
     * @param event The action event triggered by clicking the Back button.
     */
    @FXML
private void handleBack(ActionEvent event) {
    FXMLLoader loader = SceneChanger.switchScene(event, "admin_dashboard.fxml");
    if (loader != null) {
        AdminDashboardController ctrl = loader.getController();
        if (ctrl != null) {
            ctrl.setLoggedInUser(UserModel.getCurrentUser()); 
        }
    }
}

}
