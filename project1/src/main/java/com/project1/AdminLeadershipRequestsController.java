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

public class AdminLeadershipRequestsController implements Initializable {

    @FXML private VBox requestListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRequests();
    }

    private void loadRequests() {
        requestListContainer.getChildren().clear(); // her seferinde temizle
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
                        Button deleteBtn = new Button("🗑️");

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

    @FXML
private void handleBack(ActionEvent event) {
    FXMLLoader loader = SceneChanger.switchScene(event, "admin_dashboard.fxml");
    if (loader != null) {
        AdminDashboardController ctrl = loader.getController();
        if (ctrl != null) {
            ctrl.setLoggedInUser(UserModel.getCurrentUser()); // 👈 önemli olan bu
        }
    }
}

}
