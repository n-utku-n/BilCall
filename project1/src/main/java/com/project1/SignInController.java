package com.project1;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import org.json.JSONObject;

/**
 * @author Utku and Hanne
 */

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private PasswordField passwordField;

    private static final String API_KEY = "AIzaSyDYluEpPgovtKRDW5bjIMMg4BNLgjy52YM";

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        SceneChanger.switchScene(event, "forgot_password.fxml");
    }

    @FXML
private void handleSignIn(ActionEvent event) {
    String email = emailField.getText().trim();
    String password = passwordField.getText().trim();

    if (email.isEmpty() || password.isEmpty()) {
        errorLabel.setText("Email and password cannot be empty.");
        return;
    }

    try {
        URL url = new URL(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" 
            + API_KEY
        );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        JSONObject requestData = new JSONObject();
        requestData.put("email", email);
        requestData.put("password", password);
        requestData.put("returnSecureToken", true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String uid = new JSONObject(response).getString("localId");

            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("users").document(uid);
            DocumentSnapshot document = docRef.get().get();
            if (!document.exists()) {
                errorLabel.setText("User data not found.");
                return;
            }

            // ① Firestore’dan okunan alanlarla UserModel oluşturun
            UserModel user = new UserModel(
                document.getString("name"),
                document.getString("surname"),
                document.getString("studentId"),
                document.getString("email"),
                document.getString("role")
            );
            user.setUid(uid);
            // Eğer clubId/clubName de varsa
            if (document.contains("clubId"))   user.setClubId(document.getString("clubId"));
            if (document.contains("clubName")) user.setClubName(document.getString("clubName"));

            errorLabel.setText("");

            // ② role’a göre sahne değiştirirken UserModel’i de geçiriyoruz
            switch (user.getRole()) {
                case "student", "club_manager" -> 
                    SceneChanger.switchScene(event, "main_dashboard.fxml", ctrl -> {
                        ((MainDashboardController)ctrl).setLoggedInUser(user);
                    });
                case "admin" -> 
                    SceneChanger.switchScene(event, "admin_dashboard.fxml", ctrl -> {
                        ((AdminDashboardController)ctrl).setLoggedInUser(user);
                    });
                default -> 
                    errorLabel.setText("Unknown role. Please contact support.");
            }
        } else {
            String err = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            String msg = new JSONObject(err).getJSONObject("error").getString("message");
            switch (msg) {
                case "EMAIL_NOT_FOUND", "INVALID_PASSWORD" -> 
                    errorLabel.setText("Incorrect email or password.");
                case "USER_DISABLED" -> 
                    errorLabel.setText("This account has been disabled.");
                default -> 
                    errorLabel.setText("Login failed. Please try again.");
            }
        }

    } catch (Exception e) {
        errorLabel.setText("An unexpected error occurred. Please try again.");
    }
}

    @FXML
    public void initialize() {
        emailField.clear();
        passwordField.clear();
    }

}
