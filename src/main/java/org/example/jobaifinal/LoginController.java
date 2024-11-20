package org.example.jobaifinal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label messageLabel;

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            if (DatabaseOperations.validateUser(username, password)) {
                int userId = DatabaseOperations.getUserId(username);
                loadMainView(userId);
            } else {
                messageLabel.setText("Invalid username or password");
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void register() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            DatabaseOperations.addUser(username, password);
            messageLabel.setText("User registered successfully!");
        } catch (SQLException e) {
            messageLabel.setText("Registration error: " + e.getMessage());
        }
    }

    private void loadMainView(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/jobaifinal/MainView.fxml"));
            Parent root = loader.load();

            ResumeUploadController controller = loader.getController();
            controller.initData(userId);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error loading main view: " + e.getMessage());
        }
    }
}