package sn.senthioonodiexna.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.*;
import sn.senthioonodiexna.service.DossierService;
import sn.senthioonodiexna.service.PdfExportService;
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.DateUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.util.List;

public class DossierDetailController {

    @FXML private Button submitDraftBtn;
    @FXML private Label numeroSuiviLabel;
    @FXML private Label demarcheLabel;
    @FXML private Label statutBadgeLabel;

    @FXML private VBox alertBox;
    @FXML private Label alertTitleLabel;
    @FXML private Label alertMessageLabel;

    @FXML private Label serviceLabel;
    @FXML private Label administrationLabel;
    @FXML private Label dateCreationLabel;
    @FXML private Label dateDepotLabel;
    @FXML private Label agentLabel;

    @FXML private TableView<DocumentFourni> documentsTable;
    @FXML private TableColumn<DocumentFourni, String> colPieceNom;
    @FXML private TableColumn<DocumentFourni, String> colPieceObligatoire;
    @FXML private TableColumn<DocumentFourni, String> colPieceStatut;
    @FXML private TableColumn<DocumentFourni, String> colPieceRemarque;

    @FXML private VBox historyContainer;

    private Dossier dossier;
    private final DossierService dossierService = new DossierService();
    private final PdfExportService pdfExportService = new PdfExportService();

    public void setDossier(Dossier d) {
        // Recharger le dossier pour avoir les dernières données fraîches
        this.dossier = dossierService.getDossierById(d.getId()).orElse(d);
        setupDocumentsTable();
        renderDossier();
    }

    private void setupDocumentsTable() {
        colPieceNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomPiece()));
        colPieceObligatoire.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isObligatoire() ? "Obligatoire" : "Facultatif"));

        colPieceStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatutVerification().getLibelle()));
        colPieceStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    if ("Conforme".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #047857;");
                    } else if ("Non conforme".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
                    } else if ("À compléter".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309;");
                    } else {
                        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        colPieceRemarque.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCommentaire() != null ? c.getValue().getCommentaire() : "-"
        ));
    }

    private void renderDossier() {
        if (dossier == null) return;

        numeroSuiviLabel.setText("DOSSIER N° " + dossier.getNumeroSuivi());
        demarcheLabel.setText("Démarche : " + dossier.getTitreDemarche());

        // Badge Statut
        statutBadgeLabel.setText(dossier.getStatut().getLibelle());
        statutBadgeLabel.getStyleClass().setAll("status-badge", dossier.getStatut().getCssClass());

        // Bouton de dépôt de brouillon
        boolean isDraft = dossier.getStatut() == StatutDossier.BROUILLON;
        submitDraftBtn.setVisible(isDraft);
        submitDraftBtn.setManaged(isDraft);

        // Alertes (Rejet ou Complément)
        if (dossier.getStatut() == StatutDossier.REJETE) {
            alertBox.setVisible(true);
            alertBox.setManaged(true);
            alertBox.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #ef4444; -fx-border-radius: 8px; -fx-padding: 14px;");
            alertTitleLabel.setText("❌ Demande Rejetée (Motif obligatoire)");
            alertTitleLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
            alertMessageLabel.setText(dossier.getMotifRejet() != null ? dossier.getMotifRejet() : "Aucun motif spécifié.");
            alertMessageLabel.setStyle("-fx-text-fill: #7f1d1d;");
        } else if (dossier.getStatut() == StatutDossier.COMPLEMENT_DEMANDE) {
            alertBox.setVisible(true);
            alertBox.setManaged(true);
            alertBox.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-border-radius: 8px; -fx-padding: 14px;");
            alertTitleLabel.setText("⚠️ Complément d'informations ou de pièces requis");
            alertTitleLabel.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");

            // Récupérer le dernier commentaire dans l'historique
            String msg = "Veuillez vérifier les pièces marquées 'Non conforme' ou 'À compléter' ci-dessous.";
            if (dossier.getHistoriques() != null && !dossier.getHistoriques().isEmpty()) {
                Historique last = dossier.getHistoriques().get(dossier.getHistoriques().size() - 1);
                if (last.getCommentaire() != null) {
                    msg = last.getCommentaire();
                }
            }
            alertMessageLabel.setText(msg);
            alertMessageLabel.setStyle("-fx-text-fill: #78350f;");
        } else {
            alertBox.setVisible(false);
            alertBox.setManaged(false);
        }

        serviceLabel.setText(dossier.getNomService() != null ? dossier.getNomService() : "-");
        administrationLabel.setText(dossier.getNomAdministration() != null ? dossier.getNomAdministration() : "-");
        dateCreationLabel.setText(DateUtil.formatDateTime(dossier.getDateCreation()));
        dateDepotLabel.setText(DateUtil.formatDateTime(dossier.getDateDepot()));
        agentLabel.setText(dossier.getNomAgent() != null ? dossier.getNomAgent() : "Non encore assigné");

        // Documents
        documentsTable.setItems(FXCollections.observableArrayList(dossier.getDocuments()));

        // Historique (RG07)
        renderHistory();
    }

    private void renderHistory() {
        historyContainer.getChildren().clear();
        List<Historique> list = dossier.getHistoriques();

        if (list == null || list.isEmpty()) {
            historyContainer.getChildren().add(new Label("Aucun événement consigné pour le moment."));
            return;
        }

        for (Historique h : list) {
            VBox item = new VBox(4);
            item.getStyleClass().add("timeline-item");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label dot = new Label();
            dot.getStyleClass().add("timeline-dot");

            Label date = new Label(DateUtil.formatDateTime(h.getDateAction()));
            date.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 12px;");

            String transition = (h.getAncienStatut() != null ? h.getAncienStatut() + " ➔ " : "") + h.getNouveauStatut();
            Label statusChange = new Label(transition);
            statusChange.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 4px; -fx-padding: 2px 8px; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label author = new Label("par " + (h.getNomUtilisateur() != null ? h.getNomUtilisateur() : "Système"));
            author.getStyleClass().add("text-muted");

            header.getChildren().addAll(dot, date, statusChange, author);

            Label comment = new Label(h.getCommentaire() != null ? h.getCommentaire() : "");
            comment.setWrapText(true);
            comment.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155; -fx-padding: 0 0 0 20px;");

            item.getChildren().addAll(header, comment);
            historyContainer.getChildren().add(item);
        }
    }

    @FXML
    public void handleSubmitDraft() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        boolean confirm = AlertUtil.showConfirmation(
                "Dépôt officiel",
                "Déposer officiellement ce dossier ?",
                "Le dossier passera au statut DÉPOSÉ et sera transmis au service compétent."
        );

        if (confirm) {
            try {
                dossierService.submitDraft(dossier.getId(), user.getId());
                AlertUtil.showInfo("Dépôt enregistré", "Votre dossier a été officiellement déposé.");
                setDossier(dossier); // Refresh
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de déposer le dossier : " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleDownloadReceiptPdf() {
        pdfExportService.exportDossierReceipt(dossier, ViewNavigator.getPrimaryStage());
    }

    @FXML
    public void handleBack() {
        ViewNavigator.loadContent("/fxml/mes_dossiers.fxml");
    }
}
