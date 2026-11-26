package sn.senthioonodiexna.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.StatutDossier;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.DossierService;
import sn.senthioonodiexna.service.PdfExportService;
import sn.senthioonodiexna.util.DateUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MesDossiersController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<StatutDossier> statutFilterCombo;
    @FXML private TableView<Dossier> dossiersTable;

    @FXML private TableColumn<Dossier, String> colNumeroSuivi;
    @FXML private TableColumn<Dossier, String> colDemarche;
    @FXML private TableColumn<Dossier, String> colService;
    @FXML private TableColumn<Dossier, String> colDateCreation;
    @FXML private TableColumn<Dossier, String> colStatut;
    @FXML private TableColumn<Dossier, Void> colAction;

    private final DossierService dossierService = new DossierService();
    private final PdfExportService pdfExportService = new PdfExportService();
    private List<Dossier> allUserDossiers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        loadDossiers();

        searchField.setOnAction(e -> handleFilter());
    }

    private void setupTable() {
        colNumeroSuivi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroSuivi()));
        colDemarche.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitreDemarche()));
        colService.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomService()));
        colDateCreation.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDateTime(c.getValue().getDateCreation())));

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
            private final Button btnDetails = new Button("Détails");
            private final Button btnPdf = new Button("📄");
            private final HBox box = new HBox(6, btnDetails, btnPdf);

            {
                box.setAlignment(Pos.CENTER);
                btnDetails.getStyleClass().add("btn-outline");
                btnDetails.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnDetails.setOnAction(e -> {
                    Dossier d = getTableRow().getItem();
                    if (d != null) {
                        openDossierDetail(d);
                    }
                });

                btnPdf.getStyleClass().add("btn-secondary");
                btnPdf.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnPdf.setTooltip(new Tooltip("Télécharger le récépissé PDF"));
                btnPdf.setOnAction(e -> {
                    Dossier d = getTableRow().getItem();
                    if (d != null) {
                        pdfExportService.exportDossierReceipt(d, ViewNavigator.getPrimaryStage());
                    }
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

    private void setupFilters() {
        statutFilterCombo.setItems(FXCollections.observableArrayList(StatutDossier.values()));
        statutFilterCombo.setOnAction(e -> handleFilter());
    }

    private void loadDossiers() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        // RG03: Un citoyen ne consulte que ses propres dossiers
        allUserDossiers = dossierService.getDossiersForCitizen(user.getId());
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

        List<Dossier> filtered = allUserDossiers.stream().filter(d -> {
            boolean matchesQuery = query.isEmpty() ||
                    d.getNumeroSuivi().toLowerCase().contains(query) ||
                    (d.getTitreDemarche() != null && d.getTitreDemarche().toLowerCase().contains(query)) ||
                    (d.getNomService() != null && d.getNomService().toLowerCase().contains(query));

            boolean matchesStatut = (selectedStatut == null) || (d.getStatut() == selectedStatut);

            return matchesQuery && matchesStatut;
        }).toList();

        dossiersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleNewDemarche() {
        ViewNavigator.loadContent("/fxml/catalogue.fxml");
    }

    private void openDossierDetail(Dossier d) {
        ViewNavigator.loadContent("/fxml/dossier_detail.fxml", (DossierDetailController ctrl) -> ctrl.setDossier(d));
    }
}
