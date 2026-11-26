package sn.senthioonodiexna.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.StatutDossier;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.DossierService;
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.DateUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AgentDossiersController implements Initializable {

    @FXML private Label serviceTitleLabel;
    @FXML private Label serviceSubtitleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<StatutDossier> statutFilterCombo;
    @FXML private TableView<Dossier> dossiersTable;

    @FXML private TableColumn<Dossier, String> colNumeroSuivi;
    @FXML private TableColumn<Dossier, String> colCitoyen;
    @FXML private TableColumn<Dossier, String> colDemarche;
    @FXML private TableColumn<Dossier, String> colDateDepot;
    @FXML private TableColumn<Dossier, String> colStatut;
    @FXML private TableColumn<Dossier, Void> colAction;

    private final DossierService dossierService = new DossierService();
    private List<Dossier> serviceDossiers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        loadServiceDossiers();

        searchField.setOnAction(e -> handleFilter());
    }

    private void setupTable() {
        colNumeroSuivi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroSuivi()));
        colCitoyen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomCompletCitoyen() + " (" + c.getValue().getEmailCitoyen() + ")"));
        colDemarche.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitreDemarche()));
        colDateDepot.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateDepot() != null ? DateUtil.formatDateTime(c.getValue().getDateDepot()) : "Non déposé"
        ));

        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut().getLibelle()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Dossier d = getTableRow().getItem();
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("status-badge", d.getStatut().getCssClass());
                    setGraphic(badge);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Instruire →");
            {
                btn.getStyleClass().add("btn-primary");
                btn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
                btn.setOnAction(e -> {
                    Dossier d = getTableRow().getItem();
                    if (d != null) {
                        openAgentDossierDetail(d);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupFilters() {
        statutFilterCombo.setItems(FXCollections.observableArrayList(StatutDossier.values()));
        statutFilterCombo.setOnAction(e -> handleFilter());
    }

    private void loadServiceDossiers() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        // RG04: Un agent traite uniquement les dossiers de son service (sauf ADMIN)
        if (user.getRole() == Role.ADMIN) {
            serviceDossiers = dossierService.searchDossiers(null, null, null);
            serviceTitleLabel.setText("Supervision Globale — Tous les services");
            serviceSubtitleLabel.setText("Vue administrative consolidée de l'ensemble des dossiers");
        } else {
            Integer idService = user.getIdService();
            if (idService != null && idService > 0) {
                serviceDossiers = dossierService.getDossiersForService(idService);
                serviceTitleLabel.setText("Espace Agent — " + (user.getNomService() != null ? user.getNomService() : "Service administratif"));
                serviceSubtitleLabel.setText("Instruction des dossiers rattachés à votre service (RG04)");
            } else {
                serviceDossiers = new ArrayList<>();
                serviceTitleLabel.setText("Espace Agent — Aucun service assigné");
                serviceSubtitleLabel.setText("Veuillez demander à un administrateur de vous affecter à un service.");
            }
        }

        applyFilter();
    }

    @FXML
    public void handleFilter() {
        applyFilter();
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        statutFilterCombo.setValue(null);
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        StatutDossier selectedStatut = statutFilterCombo.getValue();

        List<Dossier> filtered = serviceDossiers.stream().filter(d -> {
            boolean matchesQuery = query.isEmpty() ||
                    d.getNumeroSuivi().toLowerCase().contains(query) ||
                    (d.getNomCitoyen() != null && d.getNomCitoyen().toLowerCase().contains(query)) ||
                    (d.getPrenomCitoyen() != null && d.getPrenomCitoyen().toLowerCase().contains(query)) ||
                    (d.getTitreDemarche() != null && d.getTitreDemarche().toLowerCase().contains(query));

            boolean matchesStatut = (selectedStatut == null) || (d.getStatut() == selectedStatut);

            return matchesQuery && matchesStatut;
        }).toList();

        dossiersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleRegisterPhysicalDeposit() {
        Utilisateur agent = AppSession.getInstance().getCurrentUser();
        if (agent == null) return;

        Optional<String> result = AlertUtil.showTextInputDialog(
                "Enregistrement de dépôt physique",
                "Réception au guichet",
                "Saisissez le numéro de suivi du dossier présenté par l'usager (ex: STD-2026-00003) :",
                ""
        );

        if (result.isPresent() && !result.get().isBlank()) {
            String num = result.get().trim();
            Optional<Dossier> opt = dossierService.getDossierByNumeroSuivi(num);
            if (opt.isEmpty()) {
                AlertUtil.showError("Dossier introuvable", "Aucun dossier trouvé avec le numéro : " + num);
                return;
            }

            Dossier d = opt.get();
            try {
                dossierService.checkAgentServiceAuthorization(d, agent);
                boolean ok = dossierService.registerPhysicalDeposit(d.getId(), agent, "Dépôt physique validé au guichet.");
                if (ok) {
                    AlertUtil.showInfo("Dépôt enregistré", "Le dépôt physique du dossier n° " + d.getNumeroSuivi() + " a été validé avec succès.");
                    loadServiceDossiers();
                    openAgentDossierDetail(d);
                }
            } catch (Exception e) {
                AlertUtil.showError("Erreur d'enregistrement", e.getMessage());
            }
        }
    }

    private void openAgentDossierDetail(Dossier d) {
        ViewNavigator.loadContent("/fxml/agent_dossier_detail.fxml", (AgentDossierDetailController ctrl) -> ctrl.setDossier(d));
    }
}
