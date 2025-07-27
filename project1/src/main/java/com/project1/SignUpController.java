package com.project1;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.cloud.firestore.Firestore;
import com.google.api.services.storage.Storage.Projects.HmacKeys.Create;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

/**
 * Controller class for user registration (Sign Up).
 * <p>
 * Handles form validation, user creation via Firebase Authentication,
 * and stores user metadata in Firestore. Redirects to the main dashboard on success.
 * </p>
 *
 * @author Utku
 */
public class SignUpController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField idField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;


    /**
     * Handles the sign-up button click event.
     * Validates inputs, registers the user in Firebase Authentication,
     * and saves user data in Firestore.
     *
     * @param event the ActionEvent triggered by the Sign Up button.
     */
    @FXML
    private void handleSignUp(ActionEvent event) {
        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String studentId = idField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = "student";

        if (!email.endsWith("@ug.bilkent.edu.tr")) {
            System.out.println(" Please use a Bilkent University email address.");
            errorLabel.setText("Please use a Bilkent University email address.");
            return;
        }

        if (password.length() < 6) {
            System.out.println(" Password must be at least 6 characters long.");
            errorLabel.setText("Password must be at least 6 characters long.");
            return;
        }

         try {
            //Create a user with Firebase Auth
            CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(name + " " + surname);
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            String uid = userRecord.getUid();
            System.out.println("User created: " + uid);

            //Create UserModel for Firestore and set all fields
            UserModel user = new UserModel(name, surname, studentId, email, role);
            user.setUid(uid);
            //if the role is "club_manager" in the future:
            // user.setClubId(selectedClubId);
            // user.setClubName(selectedClubName);

            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.set(user);
            System.out.println("User registered to Firestore.");

           //Switch to Dashboard: MainDashboardController.setLoggedInUser(user) will be called
            SceneChanger.switchScene(event, "main_dashboard.fxml", controller -> {
                if (controller instanceof MainDashboardController mainCtrl) {
                    mainCtrl.setLoggedInUser(user);
                }
            });

        } catch (Exception e) {
            System.out.println("Registration failed. " + e.getMessage());
            errorLabel.setText("Registration failed." +e.getMessage()+ " Please try again.");
        }
    }
}
