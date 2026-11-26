package sn.senthioonodiexna.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.AuthService;
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField roleField;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Label passwordErrorLabel;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserProfile();
    }

    private void loadUserProfile() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user != null) {
            prenomField.setText(user.getPrenom());
            nomField.setText(user.getNom());
            emailField.setText(user.getEmail());
            telephoneField.setText(user.getTelephone() != null ? user.getTelephone() : "");
            roleField.setText(user.getRole().getLibelle() +
                    (user.getNomService() != null ? " (" + user.getNomService() + ")" : ""));
        }
    }

    @FXML
    public void handleSaveProfile() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        String prenom = prenomField.getText();
        String nom = nomField.getText();
        String tel = telephoneField.getText();

        if (prenom == null || prenom.isBlank() || nom == null || nom.isBlank()) {
            AlertUtil.showError("Erreur", "Le prénom et le nom sont obligatoires.");
            return;
        }

        try {
            boolean ok = authService.updateProfile(user.getId(), nom, prenom, tel);
            if (ok) {
                AlertUtil.showInfo("Profil mis à jour", "Vos informations ont été enregistrées avec succès.");
                if (ViewNavigator.getMainLayoutController() != null) {
                    ViewNavigator.getMainLayoutController().refreshUserInfo();
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    public void handleChangePassword() {
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);

        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confPass = confirmNewPasswordField.getText();

        if (newPass == null || !newPass.equals(confPass)) {
            passwordErrorLabel.setText("Les nouveaux mots de passe ne correspondent pas.");
            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setManaged(true);
            return;
        }

        try {
            boolean ok = authService.changePassword(user.getId(), oldPass, newPass);
            if (ok) {
                AlertUtil.showInfo("Mot de passe modifié", "Votre mot de passe a été mis à jour avec succès.");
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmNewPasswordField.clear();
            }
        } catch (Exception e) {
            passwordErrorLabel.setText(e.getMessage());
            passwordErrorLabel.setVisible(true);
            passwordErrorLabel.setManaged(true);
        }
    }
}
