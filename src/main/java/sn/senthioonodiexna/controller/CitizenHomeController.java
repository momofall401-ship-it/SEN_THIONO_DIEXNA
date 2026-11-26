package sn.senthioonodiexna.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.StatutDossier;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.DemarcheService;
import sn.senthioonodiexna.service.DossierService;
import sn.senthioonodiexna.util.DateUtil;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CitizenHomeController implements Initializable {

    @FXML private TextField quickSearchField;
    @FXML private Label dossiersEnCoursLabel;
    @FXML private Label complementsLabel;
    @FXML private Label dossiersAcceptesLabel;
    @FXML private FlowPane popularDemarchesBox;

    @FXML private TableView<Dossier> recentDossiersTable;
    @FXML private TableColumn<Dossier, String> colNumeroSuivi;
    @FXML private TableColumn<Dossier, String> colDemarche;
    @FXML private TableColumn<Dossier, String> colDate;
    @FXML private TableColumn<Dossier, String> colStatut;
    @FXML private TableColumn<Dossier, Void> colAction;

    private final DemarcheService demarcheService = new DemarcheService();
    private final DossierService dossierService = new DossierService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        colNumeroSuivi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroSuivi()));
        colDemarche.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitreDemarche()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDateTime(c.getValue().getDateCreation())));

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
            private final Button btn = new Button("Détails");
            {
                btn.getStyleClass().add("btn-outline");
                btn.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btn.setOnAction(e -> {
                    Dossier d = getTableRow().getItem();
                    if (d != null) {
                        openDossierDetail(d);
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

    private void loadData() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        // Dossiers du citoyen (RG03)
        List<Dossier> userDossiers = dossierService.getDossiersForCitizen(user.getId());

        int enCours = 0;
        int complements = 0;
        int acceptes = 0;

        for (Dossier d : userDossiers) {
            if (d.getStatut() == StatutDossier.COMPLEMENT_DEMANDE) {
                complements++;
            } else if (d.getStatut() == StatutDossier.ACCEPTE) {
                acceptes++;
            } else if (d.getStatut() != StatutDossier.REJETE && d.getStatut() != StatutDossier.CLOTURE) {
                enCours++;
            }
        }

        dossiersEnCoursLabel.setText(String.valueOf(enCours));
        complementsLabel.setText(String.valueOf(complements));
        dossiersAcceptesLabel.setText(String.valueOf(acceptes));

        // Récents dossiers (max 5)
        List<Dossier> recents = userDossiers.stream().limit(5).toList();
        recentDossiersTable.setItems(FXCollections.observableArrayList(recents));

        // Démarches populaires (RG05)
        popularDemarchesBox.getChildren().clear();
        List<Demarche> demarches = demarcheService.getPublishedDemarches().stream().limit(4).toList();
        for (Demarche d : demarches) {
            popularDemarchesBox.getChildren().add(createDemarcheCard(d));
        }
    }

    private VBox createDemarcheCard(Demarche d) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "card-hover");
        card.setPrefWidth(260);

        Label title = new Label(d.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        title.setWrapText(true);

        Label service = new Label(d.getNomService());
        service.getStyleClass().add("text-muted");
        service.setWrapText(true);

        Label frais = new Label("Frais : " + (d.getFraisIndicatifs() != null ? d.getFraisIndicatifs() : "Gratuit"));
        frais.setStyle("-fx-font-size: 11px; -fx-text-fill: #0f5132; -fx-font-weight: bold;");

        Label delai = new Label("Délai : " + (d.getDelaiEstime() != null ? d.getDelaiEstime() : "-"));
        delai.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Button btn = new Button("Consulter & Préparer");
        btn.getStyleClass().add("btn-primary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> openDemarcheDetail(d));

        card.getChildren().addAll(title, service, new Separator(), frais, delai, btn);
        return card;
    }

    @FXML
    public void handleQuickSearch() {
        String query = quickSearchField.getText();
        ViewNavigator.loadContent("/fxml/catalogue.fxml", (CatalogueController controller) -> {
            if (query != null && !query.isBlank()) {
                controller.setSearchQuery(query);
            }
        });
    }

    @FXML
    public void handleViewAllCatalogue() {
        ViewNavigator.loadContent("/fxml/catalogue.fxml");
    }

    @FXML
    public void handleViewAllDossiers() {
        ViewNavigator.loadContent("/fxml/mes_dossiers.fxml");
    }

    private void openDemarcheDetail(Demarche d) {
        ViewNavigator.loadContent("/fxml/demarche_detail.fxml", (DemarcheDetailController ctrl) -> ctrl.setDemarche(d));
    }

    private void openDossierDetail(Dossier d) {
        ViewNavigator.loadContent("/fxml/dossier_detail.fxml", (DossierDetailController ctrl) -> ctrl.setDossier(d));
    }
}
