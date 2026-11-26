package sn.senthioonodiexna.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sn.senthioonodiexna.service.AuthService;
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.ViewNavigator;

public class RegisterController {

    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void handleRegister() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String prenom = prenomField.getText();
        String nom = nomField.getText();
        String email = emailField.getText();
        String telephone = telephoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password == null || !password.equals(confirmPassword)) {
            errorLabel.setText("Les deux mots de passe ne correspondent pas.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        try {
            authService.register(nom, prenom, email, telephone, password);
            AlertUtil.showInfo(
                    "Compte créé avec succès",
                    "Votre compte usager a été enregistré. Vous pouvez maintenant vous connecter."
            );
            handleGoToLogin();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    @FXML
    public void handleGoToLogin() {
        ViewNavigator.switchScene("/fxml/login.fxml", "Sen Thioono Diexna — Connexion", 900, 680);
    }
}
