package sn.senthioonodiexna.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.AuthService;
import sn.senthioonodiexna.service.NotificationService;
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private VBox navMenuBox;
    @FXML private Label pageTitleLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label notificationBadge;
    @FXML private StackPane contentArea;

    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();

    private final List<Button> navButtons = new ArrayList<>();
    private Button currentActiveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewNavigator.setMainLayoutController(this);
        refreshUserInfo();
        buildNavigation();
        loadInitialView();
        refreshBadge();
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    public void setPageTitle(String title) {
        pageTitleLabel.setText(title);
    }

    public void refreshUserInfo() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNomComplet());
            userRoleLabel.setText(user.getRole().getLibelle());
        }
    }

    public void refreshBadge() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user != null) {
            int unread = notificationService.getUnreadCount(user.getId());
            if (unread > 0) {
                notificationBadge.setText(String.valueOf(unread));
                notificationBadge.setVisible(true);
                notificationBadge.setManaged(true);
            } else {
                notificationBadge.setVisible(false);
                notificationBadge.setManaged(false);
            }
        }
    }

    private void buildNavigation() {
        navMenuBox.getChildren().clear();
        navButtons.clear();

        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        Role role = user.getRole();

        if (role == Role.CITOYEN) {
            addNavButton("🏠  Accueil", "/fxml/citizen_home.fxml", "Accueil");
            addNavButton("📋  Catalogue démarches", "/fxml/catalogue.fxml", "Catalogue des démarches");
            addNavButton("📁  Mes dossiers", "/fxml/mes_dossiers.fxml", "Mes dossiers en cours");
            addNavButton("❓  Foire aux questions", "/fxml/faq.fxml", "Foire aux questions (FAQ)");
        } else if (role == Role.AGENT) {
            addNavButton("📁  Dossiers du service", "/fxml/agent_dossiers.fxml", "Gestion des dossiers");
            addNavButton("📋  Catalogue démarches", "/fxml/catalogue.fxml", "Catalogue des démarches");
            addNavButton("❓  Foire aux questions", "/fxml/faq.fxml", "Foire aux questions (FAQ)");
        } else if (role == Role.RESPONSABLE) {
            addNavButton("📊  Tableau de bord", "/fxml/dashboard.fxml", "Tableau de bord de supervision");
            addNavButton("📁  Dossiers du service", "/fxml/agent_dossiers.fxml", "Supervision des dossiers");
            addNavButton("📋  Catalogue démarches", "/fxml/catalogue.fxml", "Catalogue des démarches");
            addNavButton("❓  Foire aux questions", "/fxml/faq.fxml", "Foire aux questions (FAQ)");
        } else if (role == Role.ADMIN) {
            addNavButton("📊  Tableau de bord", "/fxml/dashboard.fxml", "Tableau de bord général");
            addNavButton("📋  Gestion des démarches", "/fxml/admin_demarches.fxml", "Administration des démarches");
            addNavButton("🏛️  Services & Ministères", "/fxml/admin_services.fxml", "Gestion des services");
            addNavButton("👥  Gestion des comptes", "/fxml/admin_users.fxml", "Administration des utilisateurs");
            addNavButton("❓  Gestion de la FAQ", "/fxml/faq.fxml", "Administration de la FAQ");
        }
    }

    private void addNavButton(String title, String fxmlPath, String pageTitle) {
        Button btn = new Button(title);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");

        btn.setOnAction(e -> {
            setActiveButton(btn);
            setPageTitle(pageTitle);
            ViewNavigator.loadContent(fxmlPath);
        });

        navButtons.add(btn);
        navMenuBox.getChildren().add(btn);
    }

    private void setActiveButton(Button btn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        currentActiveButton = btn;
        if (currentActiveButton != null && !currentActiveButton.getStyleClass().contains("nav-button-active")) {
            currentActiveButton.getStyleClass().add("nav-button-active");
        }
    }

    private void loadInitialView() {
        if (!navButtons.isEmpty()) {
            Button first = navButtons.get(0);
            first.fire();
        }
    }

    @FXML
    public void handleNavNotifications() {
        setActiveButton(null);
        setPageTitle("Mes Notifications");
        ViewNavigator.loadContent("/fxml/notifications.fxml");
    }

    @FXML
    public void handleNavProfile() {
        setActiveButton(null);
        setPageTitle("Mon Profil Utilisateur");
        ViewNavigator.loadContent("/fxml/profile.fxml");
    }

    @FXML
    public void handleLogout() {
        boolean confirm = AlertUtil.showConfirmation(
                "Déconnexion",
                "Confirmation de déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter de votre espace ?"
        );
        if (confirm) {
            authService.logout();
            ViewNavigator.switchScene("/fxml/login.fxml", "Sen Thioono Diexna — Connexion", 900, 680);
        }
    }
}
