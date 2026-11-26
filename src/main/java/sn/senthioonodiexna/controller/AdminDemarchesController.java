package sn.senthioonodiexna.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import sn.senthioonodiexna.dao.ServiceDao;
import sn.senthioonodiexna.dao.impl.ServiceDaoImpl;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.ServiceAdministratif;
import sn.senthioonodiexna.model.StatutPublication;
import sn.senthioonodiexna.service.DemarcheService;
import sn.senthioonodiexna.util.AlertUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDemarchesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<StatutPublication> statusFilterCombo;
    @FXML private TableView<Demarche> demarchesTable;

    @FXML private TableColumn<Demarche, Number> colId;
    @FXML private TableColumn<Demarche, String> colTitre;
    @FXML private TableColumn<Demarche, String> colService;
    @FXML private TableColumn<Demarche, String> colFrais;
    @FXML private TableColumn<Demarche, String> colDelai;
    @FXML private TableColumn<Demarche, String> colStatutPublication;
    @FXML private TableColumn<Demarche, Void> colAction;

    private final DemarcheService demarcheService = new DemarcheService();
    private final ServiceDao serviceDao = new ServiceDaoImpl();
    private List<Demarche> allDemarches = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        statusFilterCombo.setItems(FXCollections.observableArrayList(StatutPublication.values()));
        statusFilterCombo.setOnAction(e -> handleFilter());
        searchField.setOnAction(e -> handleFilter());
        loadDemarches();
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colService.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomService()));
        colFrais.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFraisIndicatifs() != null ? c.getValue().getFraisIndicatifs() : "Gratuit"
        ));
        colDelai.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDelaiEstime() != null ? c.getValue().getDelaiEstime() : "-"
        ));

        colStatutPublication.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatutPublication().getLibelle()));
        colStatutPublication.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    if ("Publiée".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #047857;");
                    } else if ("Brouillon".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309;");
                    } else {
                        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnToggle = new Button("Publier/Retirer");
            private final Button btnDelete = new Button("🗑️");
            private final HBox box = new HBox(6, btnEdit, btnToggle, btnDelete);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-outline");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnEdit.setOnAction(e -> {
                    Demarche d = getTableRow().getItem();
                    if (d != null) showDemarcheDialog(d);
                });

                btnToggle.getStyleClass().add("btn-secondary");
                btnToggle.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnToggle.setOnAction(e -> {
                    Demarche d = getTableRow().getItem();
                    if (d != null) handleTogglePublication(d);
                });

                btnDelete.getStyleClass().add("btn-danger");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnDelete.setOnAction(e -> {
                    Demarche d = getTableRow().getItem();
                    if (d != null) handleDeleteDemarche(d);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    private void loadDemarches() {
        allDemarches = demarcheService.getAllDemarchesForAdmin();
        applyFilter();
    }

    @FXML
    public void handleFilter() {
        applyFilter();
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        statusFilterCombo.setValue(null);
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        StatutPublication selectedStatus = statusFilterCombo.getValue();

        List<Demarche> filtered = allDemarches.stream().filter(d -> {
            boolean matchQuery = query.isEmpty() ||
                    d.getTitre().toLowerCase().contains(query) ||
                    (d.getNomService() != null && d.getNomService().toLowerCase().contains(query));

            boolean matchStatus = (selectedStatus == null) || (d.getStatutPublication() == selectedStatus);

            return matchQuery && matchStatus;
        }).toList();

        demarchesTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleNewDemarche() {
        showDemarcheDialog(null);
    }

    private void handleTogglePublication(Demarche d) {
        StatutPublication newStatus = (d.getStatutPublication() == StatutPublication.PUBLIEE)
                ? StatutPublication.BROUILLON : StatutPublication.PUBLIEE;

        demarcheService.setPublicationStatus(d.getId(), newStatus);
        AlertUtil.showInfo("Statut mis à jour (RG05)", "La démarche est désormais : " + newStatus.getLibelle());
        loadDemarches();
    }

    private void handleDeleteDemarche(Demarche d) {
        boolean confirm = AlertUtil.showConfirmation(
                "Suppression de démarche",
                "Confirmer la suppression ?",
                "Êtes-vous sûr de vouloir supprimer définitivement la démarche \"" + d.getTitre() + "\" ?"
        );
        if (confirm) {
            demarcheService.deleteDemarche(d.getId());
            loadDemarches();
        }
    }

    private void showDemarcheDialog(Demarche existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Créer une nouvelle démarche" : "Modifier la démarche");
        dialog.setHeaderText("Renseignez les informations officielles de la démarche");

        ButtonType saveBtnType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        TextField titreTf = new TextField(existing != null ? existing.getTitre() : "");
        titreTf.setPromptText("Titre officiel de la démarche");

        TextArea descTa = new TextArea(existing != null ? existing.getDescription() : "");
        descTa.setPromptText("Description détaillée...");
        descTa.setPrefRowCount(3);

        TextArea condTa = new TextArea(existing != null && existing.getConditions() != null ? existing.getConditions() : "");
        condTa.setPromptText("Conditions et prérequis d'éligibilité...");
        condTa.setPrefRowCount(2);

        TextField fraisTf = new TextField(existing != null && existing.getFraisIndicatifs() != null ? existing.getFraisIndicatifs() : "");
        fraisTf.setPromptText("Ex: 500 FCFA");

        TextField delaiTf = new TextField(existing != null && existing.getDelaiEstime() != null ? existing.getDelaiEstime() : "");
        delaiTf.setPromptText("Ex: 24 à 48 heures");

        List<ServiceAdministratif> services = serviceDao.findAll();
        ComboBox<ServiceAdministratif> serviceCombo = new ComboBox<>(FXCollections.observableArrayList(services));
        if (existing != null) {
            for (ServiceAdministratif s : services) {
                if (s.getId() == existing.getIdService()) {
                    serviceCombo.setValue(s);
                    break;
                }
            }
        } else if (!services.isEmpty()) {
            serviceCombo.setValue(services.get(0));
        }

        ComboBox<StatutPublication> pubCombo = new ComboBox<>(FXCollections.observableArrayList(StatutPublication.values()));
        pubCombo.setValue(existing != null ? existing.getStatutPublication() : StatutPublication.PUBLIEE);

        grid.add(new Label("Titre * :"), 0, 0);
        grid.add(titreTf, 1, 0);

        grid.add(new Label("Service * :"), 0, 1);
        grid.add(serviceCombo, 1, 1);

        grid.add(new Label("Statut publication :"), 0, 2);
        grid.add(pubCombo, 1, 2);

        grid.add(new Label("Frais :"), 0, 3);
        grid.add(fraisTf, 1, 3);

        grid.add(new Label("Délai :"), 0, 4);
        grid.add(delaiTf, 1, 4);

        grid.add(new Label("Description * :"), 0, 5);
        grid.add(descTa, 1, 5);

        grid.add(new Label("Conditions :"), 0, 6);
        grid.add(condTa, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveBtnType) {
            if (titreTf.getText().isBlank() || serviceCombo.getValue() == null) {
                AlertUtil.showError("Champs requis", "Le titre et le service sont obligatoires.");
                return;
            }

            try {
                if (existing == null) {
                    Demarche d = new Demarche(
                            titreTf.getText().trim(),
                            descTa.getText().trim(),
                            condTa.getText().trim(),
                            delaiTf.getText().trim(),
                            fraisTf.getText().trim(),
                            serviceCombo.getValue().getId(),
                            pubCombo.getValue()
                    );
                    demarcheService.createDemarche(d, null, null);
                    AlertUtil.showInfo("Démarche créée", "La démarche a été ajoutée avec succès.");
                } else {
                    existing.setTitre(titreTf.getText().trim());
                    existing.setDescription(descTa.getText().trim());
                    existing.setConditions(condTa.getText().trim());
                    existing.setDelaiEstime(delaiTf.getText().trim());
                    existing.setFraisIndicatifs(fraisTf.getText().trim());
                    existing.setIdService(serviceCombo.getValue().getId());
                    existing.setStatutPublication(pubCombo.getValue());
                    demarcheService.updateDemarche(existing, null, null);
                    AlertUtil.showInfo("Démarche modifiée", "Les modifications ont été enregistrées.");
                }
                loadDemarches();
            } catch (Exception ex) {
                AlertUtil.showError("Erreur", ex.getMessage());
            }
        }
    }
}
