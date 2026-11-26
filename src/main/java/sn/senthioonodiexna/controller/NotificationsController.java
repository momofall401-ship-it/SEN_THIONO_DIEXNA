package sn.senthioonodiexna.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.Notification;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.DossierService;
import sn.senthioonodiexna.service.NotificationService;
import sn.senthioonodiexna.util.DateUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML private VBox notificationsListContainer;

    private final NotificationService notificationService = new NotificationService();
    private final DossierService dossierService = new DossierService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadNotifications();
    }

    private void loadNotifications() {
        notificationsListContainer.getChildren().clear();
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        List<Notification> list = notificationService.getNotificationsForUser(user.getId());

        if (list.isEmpty()) {
            Label empty = new Label("Vous n'avez aucune notification.");
            empty.getStyleClass().add("text-muted");
            notificationsListContainer.getChildren().add(empty);
            return;
        }

        for (Notification n : list) {
            notificationsListContainer.getChildren().add(createNotificationCard(n));
        }

        if (ViewNavigator.getMainLayoutController() != null) {
            ViewNavigator.getMainLayoutController().refreshBadge();
        }
    }

    private VBox createNotificationCard(Notification n) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        if (!n.isEstLue()) {
            card.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #86efac; -fx-border-width: 1.5px;");
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(n.isEstLue() ? "📨" : "🔔");
        Label title = new Label(n.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label date = new Label(DateUtil.formatDateTime(n.getDateCreation()));
        date.getStyleClass().add("text-muted");

        top.getChildren().addAll(icon, title, date);

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155; -fx-padding: 0 0 0 24px;");

        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setStyle("-fx-padding: 4px 0 0 0;");

        if (n.getIdDossier() != null && n.getIdDossier() > 0) {
            Button btnViewDossier = new Button("Ouvrir le dossier concerné →");
            btnViewDossier.getStyleClass().add("btn-outline");
            btnViewDossier.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
            btnViewDossier.setOnAction(e -> {
                notificationService.markAsRead(n.getId());
                Optional<Dossier> opt = dossierService.getDossierById(n.getIdDossier());
                if (opt.isPresent()) {
                    ViewNavigator.loadContent("/fxml/dossier_detail.fxml", (DossierDetailController ctrl) -> ctrl.setDossier(opt.get()));
                }
            });
            bottom.getChildren().add(btnViewDossier);
        }

        if (!n.isEstLue()) {
            Button btnRead = new Button("Marquer comme lu");
            btnRead.getStyleClass().add("btn-secondary");
            btnRead.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
            btnRead.setOnAction(e -> {
                notificationService.markAsRead(n.getId());
                loadNotifications();
            });
            bottom.getChildren().add(btnRead);
        }

        Button btnDel = new Button("🗑️");
        btnDel.getStyleClass().add("btn-secondary");
        btnDel.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
        btnDel.setOnAction(e -> {
            notificationService.deleteNotification(n.getId());
            loadNotifications();
        });
        bottom.getChildren().add(btnDel);

        card.getChildren().addAll(top, msg, bottom);
        return card;
    }

    @FXML
    public void handleMarkAllAsRead() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
            loadNotifications();
        }
    }
}
