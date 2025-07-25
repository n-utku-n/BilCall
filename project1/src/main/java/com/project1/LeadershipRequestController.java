package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
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
import java.util.*;
import java.util.concurrent.ExecutionException;

public class LeadershipRequestController implements Initializable {

    @FXML private VBox clubListContainer;
    private UserModel currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadClubs();
    }

    private void loadClubs() {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("clubs").get();

        new Thread(() -> {
            try {
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    String clubId = doc.getId();
                    String clubName = doc.getString("name");

                    Platform.runLater(() -> {
                        HBox row = new HBox(10);
                        Label nameLbl = new Label(clubName);
                        Button applyBtn = new Button("Apply");

                        applyBtn.setOnAction(e -> {
                            applyBtn.setDisable(true);
                            submitRequest(clubId, clubName, applyBtn);
                        });

                        row.getChildren().addAll(nameLbl, applyBtn);
                        clubListContainer.getChildren().add(row);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void submitRequest(String clubId, String clubName, Button applyBtn) {
        Firestore db = FirestoreClient.getFirestore();
        UserModel user = UserModel.getCurrentUser();
        String email = user.getEmail();

        new Thread(() -> {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("email", email);
                data.put("clubId", clubId);
                data.put("clubName", clubName);
                data.put("timestamp", Timestamp.now());

                db.collection("leadership_requests").add(data).get();

                Platform.runLater(() -> {
                    applyBtn.setText("Sent");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Application Sent");
                    alert.setHeaderText(null);
                    alert.setContentText("Your request has been submitted.");
                    alert.showAndWait();
                });
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void setUser(UserModel user) {
        this.currentUser = user;
        UserModel.setCurrentUser(user);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        FXMLLoader loader = SceneChanger.switchScene(event, "profile.fxml");
        if (loader != null) {
            ProfileController ctrl = loader.getController();
            UserModel user = this.currentUser != null ? this.currentUser : UserModel.getCurrentUser();
            ctrl.setViewerUser(null);
            ctrl.setUser(user);
        }
    }
}
