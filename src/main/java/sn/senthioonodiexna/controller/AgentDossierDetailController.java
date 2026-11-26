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
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.DateUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.util.List;
import java.util.Optional;

public class AgentDossierDetailController {

    @FXML private Label statutBadgeLabel;
    @FXML private Label numeroSuiviLabel;
    @FXML private Label titreDemarcheLabel;
    @FXML private Label citoyenNomLabel;
    @FXML private Label citoyenEmailLabel;
    @FXML private Label citoyenTelLabel;
    @FXML private Label dateCreationLabel;
    @FXML private Label dateDepotLabel;
    @FXML private Label agentAssigneeLabel;

    @FXML private TableView<DocumentFourni> documentsTable;
    @FXML private TableColumn<DocumentFourni, String> colPieceNom;
    @FXML private TableColumn<DocumentFourni, String> colPieceObligatoire;
    @FXML private TableColumn<DocumentFourni, StatutVerification> colPieceVerification;
    @FXML private TableColumn<DocumentFourni, String> colPieceRemarque;
    @FXML private TableColumn<DocumentFourni, Void> colPieceAction;

    @FXML private VBox historyContainer;

    private Dossier dossier;
    private final DossierService dossierService = new DossierService();

    public void setDossier(Dossier d) {
        this.dossier = dossierService.getDossierById(d.getId()).orElse(d);
        setupDocumentsTable();
        renderDossier();
    }

    private void setupDocumentsTable() {
        colPieceNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomPiece()));
        colPieceObligatoire.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isObligatoire() ? "Obligatoire" : "Facultatif"));

        // ComboBox pour statut de vérification
        colPieceVerification.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<StatutVerification> combo = new ComboBox<>(
                    FXCollections.observableArrayList(StatutVerification.values())
            );
            {
                combo.setStyle("-fx-font-size: 11px;");
                combo.setOnAction(e -> {
                    DocumentFourni doc = getTableRow().getItem();
                    if (doc != null && combo.getValue() != null) {
                        doc.setStatutVerification(combo.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(StatutVerification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DocumentFourni doc = getTableRow().getItem();
                    combo.setValue(doc.getStatutVerification());
                    setGraphic(combo);
                }
            }
        });

        // Champ d'édition pour le commentaire
        colPieceRemarque.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setStyle("-fx-font-size: 11px;");
                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                    DocumentFourni doc = getTableRow().getItem();
                    if (doc != null) {
                        doc.setCommentaire(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DocumentFourni doc = getTableRow().getItem();
                    tf.setText(doc.getCommentaire() != null ? doc.getCommentaire() : "");
                    setGraphic(tf);
                }
            }
        });

        // Bouton de validation par pièce
        colPieceAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnSave = new Button("Enregistrer");
            {
                btnSave.getStyleClass().add("btn-primary");
                btnSave.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnSave.setOnAction(e -> {
                    DocumentFourni doc = getTableRow().getItem();
                    Utilisateur agent = AppSession.getInstance().getCurrentUser();
                    if (doc != null && agent != null) {
                        try {
                            dossierService.verifyDocument(doc.getId(), doc.getStatutVerification(), doc.getCommentaire(), agent, dossier.getId());
                            AlertUtil.showInfo("Vérification enregistrée", "Pièce \"" + doc.getNomPiece() + "\" mise à jour : " + doc.getStatutVerification().getLibelle());
                        } catch (Exception ex) {
                            AlertUtil.showError("Erreur", ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSave);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void renderDossier() {
        if (dossier == null) return;

        numeroSuiviLabel.setText("DOSSIER N° " + dossier.getNumeroSuivi());
        titreDemarcheLabel.setText("Démarche : " + dossier.getTitreDemarche());

        statutBadgeLabel.setText(dossier.getStatut().getLibelle());
        statutBadgeLabel.getStyleClass().setAll("status-badge", dossier.getStatut().getCssClass());

        citoyenNomLabel.setText(dossier.getNomCompletCitoyen());
        citoyenEmailLabel.setText(dossier.getEmailCitoyen());
        citoyenTelLabel.setText(dossier.getTelephoneCitoyen() != null ? dossier.getTelephoneCitoyen() : "-");

        dateCreationLabel.setText(DateUtil.formatDateTime(dossier.getDateCreation()));
        dateDepotLabel.setText(DateUtil.formatDateTime(dossier.getDateDepot()));
        agentAssigneeLabel.setText(dossier.getNomAgent() != null ? dossier.getNomAgent() : "Non assigné");

        documentsTable.setItems(FXCollections.observableArrayList(dossier.getDocuments()));

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
    public void handleStartVerification() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        try {
            dossierService.startVerification(dossier.getId(), agent);
            AlertUtil.showInfo("Statut mis à jour", "Le dossier est passé en cours de vérification.");
            refresh();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    public void handleSetEnTraitement() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        try {
            dossierService.setEnTraitement(dossier.getId(), agent, "Dossier conforme, instruction technique en cours.");
            AlertUtil.showInfo("Statut mis à jour", "Le dossier est passé en cours de traitement.");
            refresh();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    public void handleRequestComplement() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        Optional<String> result = AlertUtil.showTextInputDialog(
                "Demande de complément",
                "Préciser les éléments attendus (RG09)",
                "Indiquez précisément les pièces ou informations manquantes que l'usager doit fournir :",
                ""
        );

        if (result.isPresent()) {
            try {
                dossierService.requestComplement(dossier.getId(), agent, result.get());
                AlertUtil.showInfo("Complément demandé", "Une notification a été transmise à l'usager avec vos instructions.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    public void handleAcceptDossier() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        boolean confirm = AlertUtil.showConfirmation(
                "Décision favorable",
                "Valider et accepter le dossier ?",
                "Cette action validera définitivement la demande de l'usager (Statut ACCEPTE)."
        );

        if (confirm) {
            try {
                dossierService.acceptDossier(dossier.getId(), agent, "Dossier validé et accepté par le service.");
                AlertUtil.showInfo("Dossier accepté", "Le dossier a été accepté avec succès. L'usager a été notifié.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    public void handleRejectDossier() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        Optional<String> result = AlertUtil.showTextInputDialog(
                "Rejet du dossier (RG08)",
                "Motif obligatoire du rejet",
                "Veuillez obligatoirement saisir le motif légal ou technique du rejet de cette demande :",
                ""
        );

        if (result.isPresent()) {
            try {
                dossierService.rejectDossier(dossier.getId(), agent, result.get());
                AlertUtil.showWarning("Dossier rejeté", "Le dossier a été rejeté. Le motif a été notifié à l'usager.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    public void handleCloseDossier() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        boolean confirm = AlertUtil.showConfirmation(
                "Clôture du dossier",
                "Clôturer définitivement le suivi ?",
                "Le dossier passera au statut CLOTURE."
        );

        if (confirm) {
            try {
                dossierService.closeDossier(dossier.getId(), agent, "Suivi du dossier terminé.");
                AlertUtil.showInfo("Dossier clôturé", "Le dossier est désormais clôturé.");
                refresh();
            } catch (Exception e) {
                AlertUtil.showError("Erreur", e.getMessage());
            }
        }
    }

    private void refresh() {
        this.dossier = dossierService.getDossierById(dossier.getId()).orElse(dossier);
        renderDossier();
    }

    @FXML
    public void handleBack() {
        ViewNavigator.loadContent("/fxml/agent_dossiers.fxml");
    }
}
