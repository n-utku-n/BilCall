package com.project1;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;


import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserCardAdminController {

    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button changeRoleButton;
    @FXML private Button detailsButton;
    @FXML private Button deleteButton;

    private String userId;
    private String currentClub; 
    private Firestore db;

    public void initialize() {
        db = FirebaseInitializer.getFirestore(); 
    }

    public void setUserData(String userId, String fullName, String role, String currentClubName) {
        this.userId = userId;
        this.currentClub = currentClubName;

        nameLabel.setText(fullName);
        if ("club_manager".equals(role) && currentClubName != null && !currentClubName.isEmpty()) {
            roleLabel.setText("Role: club_manager (" + currentClubName + ")");
        } else {
            roleLabel.setText("Role: student");
        }
    }

    private java.util.Map<String, String> clubNameToIdMap = new java.util.HashMap<>();

    public void setClubList(List<DocumentSnapshot> clubDocs) {
        roleComboBox.getItems().clear();          
        roleComboBox.getItems().add("Student");   // add always student option

        clubNameToIdMap.clear();                  // Eski eşleşmeleri sıfırla

        for (DocumentSnapshot doc : clubDocs) {
            String id = doc.getId();
            String name = doc.getString("name"); 
            if (name != null) {
                roleComboBox.getItems().add(name);           
                clubNameToIdMap.put(name, id);              
            }
        }
}

@FXML
private void handleChangeRole() {
    String selected = roleComboBox.getValue();

    if (selected == null) {
        showAlert("Please select a role or club.");
        return;
    }

    DocumentReference userRef = db.collection("users").document(userId);

    try {
        if (selected.equals("Student")) {
            // club_manager to student
            ApiFuture<WriteResult> future = userRef.update(
                    "role", "student",
                    "club", FieldValue.delete(),
                    "clubId", FieldValue.delete(),
                    "clubName", FieldValue.delete()
            );
            future.get();

            roleLabel.setText("Role: student");
            removeUserFromPreviousClub(); 
            currentClub = null;

        } else {
            // student to club_manager
            String clubId = clubNameToIdMap.get(selected); 
            if (clubId == null) {
                showAlert("Selected club not found.");
                return;
            }

            ApiFuture<WriteResult> future = userRef.update(
                    "role", "club_manager",
                    "club", clubId,
                    "clubId", clubId,
                    "clubName", selected
            );
            future.get();

            roleLabel.setText("Role: club_manager (" + selected + ")");
            addUserToSelectedClub(clubId);
            removeUserFromPreviousClub(); 
            currentClub = clubId;
        }
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        showAlert("An error occurred while updating role.");
    }
}



private void removeUserFromPreviousClub() {
    if (currentClub == null || currentClub.isEmpty()) return;
    DocumentReference clubRef = db.collection("clubs").document(currentClub);
    try {
        clubRef.update("managers", FieldValue.arrayRemove(userId)).get();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void addUserToSelectedClub(String clubId) {
    DocumentReference clubRef = db.collection("clubs").document(clubId);
    try {
        clubRef.update("managers", FieldValue.arrayUnion(userId)).get();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }

    public String getUserId() {
        return userId;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Role Change");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
