package sn.senthioonodiexna.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.AuthService;
import sn.senthioonodiexna.util.ViewNavigator;

public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void handleLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            Utilisateur user = authService.login(email, password);
            logger.info("Connexion réussie : {} ({})", user.getEmail(), user.getRole());

            ViewNavigator.switchScene(
                    "/fxml/main_layout.fxml",
                    "Sen Thioono Diexna — Espace " + user.getRole().getLibelle(),
                    1200, 750
            );
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    @FXML
    public void handleGoToRegister() {
        ViewNavigator.switchScene("/fxml/register.fxml", "Sen Thioono Diexna — Inscription", 950, 700);
    }

    @FXML
    public void fillCitoyen() {
        emailField.setText("citoyen@gmail.com");
        passwordField.setText("Citoyen@2026");
    }

    @FXML
    public void fillAgent() {
        emailField.setText("agent.dpaf@senthioono.sn");
        passwordField.setText("Agent@2026");
    }

    @FXML
    public void fillResponsable() {
        emailField.setText("responsable.dpaf@senthioono.sn");
        passwordField.setText("Pass@2026");
    }

    @FXML
    public void fillAdmin() {
        emailField.setText("admin@senthioono.sn");
        passwordField.setText("Admin@2026");
    }
}
