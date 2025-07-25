package com.project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

/**
 * @author Utku and Hanne
 */

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label infoLabel;

    private static final String API_KEY = "AIzaSyDYluEpPgovtKRDW5bjIMMg4BNLgjy52YM";

    @FXML
    private void handleSendResetLink(ActionEvent event) {
        String email = emailField.getText().trim();

        if (!email.endsWith("@ug.bilkent.edu.tr")) {
            infoLabel.setText("Please use your Bilkent email address.");
            infoLabel.getStyleClass().add("error-label");
            return;
        }

        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JSONObject request = new JSONObject();
            request.put("requestType", "PASSWORD_RESET");
            request.put("email", email);

            OutputStream os = conn.getOutputStream();
            os.write(request.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                infoLabel.setText("✅ Check your email for the reset link.");
                infoLabel.getStyleClass().remove("error-label");
                infoLabel.getStyleClass().add("feedback-label");
            } else {
                infoLabel.setText("❌ Error sending reset link.");
                infoLabel.getStyleClass().add("error-label");
            }

        } catch (Exception e) {
            e.printStackTrace();
            infoLabel.setText("❌ Unexpected error occurred.");
            infoLabel.getStyleClass().add("error-label");
        }
    }

    @FXML
    private void handleBackToSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SignIn.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            infoLabel.setText("❌ Failed to return to Sign In screen.");
            infoLabel.getStyleClass().add("error-label");
        }
    }
}
