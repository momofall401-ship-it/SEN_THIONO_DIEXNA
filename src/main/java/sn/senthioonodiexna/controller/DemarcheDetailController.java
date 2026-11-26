package sn.senthioonodiexna.controller;

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
import sn.senthioonodiexna.util.ViewNavigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemarcheDetailController {

    @FXML private Label titreLabel;
    @FXML private Label serviceLabel;
    @FXML private Label fraisLabel;
    @FXML private Label delaiLabel;
    @FXML private Label contactLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label conditionsLabel;

    @FXML private Label checklistProgressLabel;
    @FXML private ProgressBar checklistProgressBar;
    @FXML private VBox checklistItemsContainer;
    @FXML private Label checklistMessageLabel;

    @FXML private VBox etapesContainer;

    private Demarche demarche;
    private final DossierService dossierService = new DossierService();
    private final PdfExportService pdfExportService = new PdfExportService();

    private final Map<DemarchePiece, CheckBox> pieceCheckBoxMap = new HashMap<>();

    public void setDemarche(Demarche demarche) {
        this.demarche = demarche;
        renderDemarche();
    }

    private void renderDemarche() {
        if (demarche == null) return;

        titreLabel.setText(demarche.getTitre());
        serviceLabel.setText(demarche.getNomService() + " — " + demarche.getNomAdministration());
        fraisLabel.setText(demarche.getFraisIndicatifs() != null ? demarche.getFraisIndicatifs() : "Gratuit");
        delaiLabel.setText(demarche.getDelaiEstime() != null ? demarche.getDelaiEstime() : "Non précisé");
        contactLabel.setText(
                (demarche.getTelephoneService() != null ? demarche.getTelephoneService() : "") +
                (demarche.getEmailService() != null ? " / " + demarche.getEmailService() : "")
        );
        descriptionLabel.setText(demarche.getDescription());
        conditionsLabel.setText(demarche.getConditions() != null ? demarche.getConditions() : "Aucune condition spécifique renseignée.");

        renderChecklist();
        renderEtapes();
    }

    private void renderChecklist() {
        checklistItemsContainer.getChildren().clear();
        pieceCheckBoxMap.clear();

        List<DemarchePiece> pieces = demarche.getPieces();
        if (pieces == null || pieces.isEmpty()) {
            checklistItemsContainer.getChildren().add(new Label("Aucune pièce requise pour cette démarche."));
            updateProgress();
            return;
        }

        for (DemarchePiece dp : pieces) {
            VBox itemBox = new VBox(4);
            itemBox.getStyleClass().add("checklist-item");

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            CheckBox cb = new CheckBox(dp.getNomPiece());
            cb.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> updateProgress());
            pieceCheckBoxMap.put(dp, cb);

            Label badge = new Label(dp.isObligatoire() ? "Obligatoire" : "Facultatif");
            badge.getStyleClass().add("status-badge");
            badge.setStyle(dp.isObligatoire()
                    ? "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;"
                    : "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;");

            row.getChildren().addAll(cb, badge);
            itemBox.getChildren().add(row);

            if (dp.getInstructions() != null && !dp.getInstructions().isBlank()) {
                Label instructions = new Label("ℹ️ " + dp.getInstructions());
                instructions.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-padding: 0 0 0 24px;");
                itemBox.getChildren().add(instructions);
            }

            checklistItemsContainer.getChildren().add(itemBox);
        }

        updateProgress();
    }

    private void updateProgress() {
        int total = pieceCheckBoxMap.size();
        if (total == 0) {
            checklistProgressBar.setProgress(1.0);
            checklistProgressLabel.setText("Dossier complet (100%)");
            checklistMessageLabel.setText("Aucune pièce n'est exigée.");
            return;
        }

        int checked = 0;
        int mandatoryMissing = 0;

        for (Map.Entry<DemarchePiece, CheckBox> entry : pieceCheckBoxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                checked++;
            } else if (entry.getKey().isObligatoire()) {
                mandatoryMissing++;
            }
        }

        double progress = (double) checked / total;
        int percent = (int) (progress * 100);

        checklistProgressBar.setProgress(progress);
        checklistProgressLabel.setText(checked + " / " + total + " pièce(s) prête(s) (" + percent + "%)");

        if (mandatoryMissing == 0 && checked == total) {
            checklistMessageLabel.setText("🎉 Bravo ! Toutes vos pièces sont prêtes. Vous pouvez déposer votre dossier.");
            checklistMessageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
        } else if (mandatoryMissing == 0) {
            checklistMessageLabel.setText("✓ Toutes les pièces obligatoires sont cochées. Votre dossier peut être déposé.");
            checklistMessageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
        } else {
            checklistMessageLabel.setText("⚠️ Attention : il vous reste encore " + mandatoryMissing + " pièce(s) obligatoire(s) à préparer.");
            checklistMessageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d97706;");
        }
    }

    private void renderEtapes() {
        etapesContainer.getChildren().clear();
        List<Etape> etapes = demarche.getEtapes();

        if (etapes == null || etapes.isEmpty()) {
            etapesContainer.getChildren().add(new Label("Aucune étape détaillée."));
            return;
        }

        for (Etape e : etapes) {
            HBox stepRow = new HBox(12);
            stepRow.setAlignment(Pos.TOP_LEFT);

            Label circle = new Label(String.valueOf(e.getOrdre()));
            circle.setStyle("-fx-background-color: #0f5132; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-background-radius: 50%; -fx-min-width: 26px; -fx-min-height: 26px; -fx-alignment: center;");

            VBox textCol = new VBox(3);
            Label stepTitle = new Label(e.getTitre());
            stepTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

            Label stepDesc = new Label(e.getDescription() != null ? e.getDescription() : "");
            stepDesc.setWrapText(true);
            stepDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

            textCol.getChildren().addAll(stepTitle, stepDesc);
            stepRow.getChildren().addAll(circle, textCol);
            etapesContainer.getChildren().add(stepRow);
        }
    }

    private List<Integer> getCheckedPieceIds() {
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<DemarchePiece, CheckBox> entry : pieceCheckBoxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                list.add(entry.getKey().getIdPiece());
            }
        }
        return list;
    }

    @FXML
    public void handleDownloadPdf() {
        pdfExportService.exportDemarcheSummary(demarche, ViewNavigator.getPrimaryStage());
    }

    @FXML
    public void handleSaveDraft() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) {
            AlertUtil.showWarning("Authentification requise", "Veuillez vous connecter pour enregistrer un dossier.");
            return;
        }

        try {
            Dossier d = dossierService.createDossier(user.getId(), demarche.getId(), getCheckedPieceIds(), false);
            AlertUtil.showInfo("Brouillon enregistré", "Votre dossier n° " + d.getNumeroSuivi() + " a été enregistré en brouillon.");
            ViewNavigator.loadContent("/fxml/dossier_detail.fxml", (DossierDetailController ctrl) -> ctrl.setDossier(d));
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'enregistrer le dossier : " + e.getMessage());
        }
    }

    @FXML
    public void handleSubmitDossier() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) {
            AlertUtil.showWarning("Authentification requise", "Veuillez vous connecter pour déposer un dossier.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de dépôt",
                "Déposer officiellement ce dossier ?",
                "Un numéro de suivi officiel sera généré. Vous pourrez ensuite vous présenter au guichet du service ou suivre l'instruction en ligne."
        );

        if (confirm) {
            try {
                Dossier d = dossierService.createDossier(user.getId(), demarche.getId(), getCheckedPieceIds(), true);
                AlertUtil.showInfo(
                        "Dossier déposé avec succès",
                        "Votre dossier a été enregistré sous le numéro de suivi : " + d.getNumeroSuivi() + "\nConservez-le précieusement."
                );
                ViewNavigator.loadContent("/fxml/dossier_detail.fxml", (DossierDetailController ctrl) -> ctrl.setDossier(d));
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de déposer le dossier : " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleBack() {
        ViewNavigator.loadContent("/fxml/catalogue.fxml");
    }
}
