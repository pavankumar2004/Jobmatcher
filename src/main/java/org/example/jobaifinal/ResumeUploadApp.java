package org.example.jobaifinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ResumeUploadApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file for the login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/jobaifinal/LoginView.fxml"));

        // Ensure the FXML file is loaded
        Parent root = loader.load();

        // Set the scene and stage
        primaryStage.setTitle("Job AI Application");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Store hostServices in the scene for later use
        scene.getProperties().put("hostServices", getHostServices());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}