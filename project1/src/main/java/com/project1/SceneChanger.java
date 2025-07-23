package com.project1;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.scene.Node;

/**
 * Utility class for switching between scenes in a JavaFX application.
 * <p>
 * This class provides a static method to change the current scene based on an ActionEvent,
 * typically triggered by a button press.
 * </p>
 * 
 * <p>
 * It loads the specified FXML file from the <code>/views/</code> directory and displays it
 * in the same window.
 * </p>
 * 
 * @author Utku
 */
public class SceneChanger {

    /**
     * Switches the current JavaFX scene to the given FXML file.
     *
     * @param event        the ActionEvent triggered by the UI element (e.g. button)
     * @param fxmlFileName the name of the FXML file to load (must be in /views/)
     */
    public static FXMLLoader switchScene(ActionEvent event, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneChanger.class.getResource("/views/" + fxmlFileName.trim()));

            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            return loader;

        } catch (Exception e) {
            System.out.println("❌ Sahne geçişinde hata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

        public static FXMLLoader switchScene(ActionEvent event, String fxmlFileName, Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneChanger.class.getResource("/views/" + fxmlFileName.trim()));

            Parent root = loader.load();

            if (controllerConsumer != null) {
                Object controller = loader.getController();
                controllerConsumer.accept(controller);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            return loader;

        } catch (Exception e) {
            System.out.println("❌ Sahne geçişinde hata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

     /**
     * Geri butonları için ortak dashboard switcher.
     * @param event ActionEvent (buton click)
     * @param user  O anki giriş yapmış kullanıcı
     * @return FXMLLoader ya da null
     */
    public static FXMLLoader goBackToDashboard(ActionEvent event, UserModel user) {
        String role = user.getRole().toLowerCase();
        String target = role.equals("admin")
            ? "admin_dashboard.fxml"
            : "main_dashboard.fxml";

        FXMLLoader loader = switchScene(event, target);
        if (loader == null) return null;

        // Controller tipine göre kullanıcıyı set et
        Object ctrl = loader.getController();
        if (ctrl instanceof AdminDashboardController adm) {
            adm.setLoggedInUser(user);
        } else if (ctrl instanceof MainDashboardController main) {
            main.setLoggedInUser(user);
        }
        return loader;
    }
}
