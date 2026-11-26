package sn.senthioonodiexna.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sn.senthioonodiexna.dao.ServiceDao;
import sn.senthioonodiexna.dao.impl.ServiceDaoImpl;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.ServiceAdministratif;
import sn.senthioonodiexna.service.DemarcheService;
import sn.senthioonodiexna.service.PdfExportService;
import sn.senthioonodiexna.util.ViewNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CatalogueController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<ServiceAdministratif> serviceFilterCombo;
    @FXML private Label resultCountLabel;
    @FXML private FlowPane demarchesContainer;

    private final DemarcheService demarcheService = new DemarcheService();
    private final ServiceDao serviceDao = new ServiceDaoImpl();
    private final PdfExportService pdfExportService = new PdfExportService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupServiceFilter();
        loadDemarches(null, null);

        searchField.setOnAction(e -> handleSearch());
    }

    public void setSearchQuery(String query) {
        searchField.setText(query);
        handleSearch();
    }

    private void setupServiceFilter() {
        List<ServiceAdministratif> services = serviceDao.findAll();
        serviceFilterCombo.setItems(FXCollections.observableArrayList(services));
        serviceFilterCombo.setOnAction(e -> handleSearch());
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText();
        ServiceAdministratif selectedService = serviceFilterCombo.getValue();
        Integer serviceId = (selectedService != null) ? selectedService.getId() : null;

        loadDemarches(keyword, serviceId);
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        serviceFilterCombo.setValue(null);
        loadDemarches(null, null);
    }

    private void loadDemarches(String keyword, Integer serviceId) {
        demarchesContainer.getChildren().clear();
        List<Demarche> list = demarcheService.searchDemarches(keyword, serviceId);

        resultCountLabel.setText(list.size() + " démarche(s) disponible(s)");

        if (list.isEmpty()) {
            Label empty = new Label("Aucune démarche ne correspond à vos critères.");
            empty.getStyleClass().add("text-muted");
            demarchesContainer.getChildren().add(empty);
            return;
        }

        for (Demarche d : list) {
            demarchesContainer.getChildren().add(createCard(d));
        }
    }

    private VBox createCard(Demarche d) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "card-hover");
        card.setPrefWidth(350);

        Label title = new Label(d.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
        title.setWrapText(true);

        Label service = new Label(d.getNomService() + " (" + d.getNomAdministration() + ")");
        service.getStyleClass().add("text-muted");
        service.setWrapText(true);

        String descShort = d.getDescription();
        if (descShort != null && descShort.length() > 140) {
            descShort = descShort.substring(0, 137) + "...";
        }
        Label desc = new Label(descShort);
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");

        HBox meta = new HBox(12);
        Label frais = new Label("💰 " + (d.getFraisIndicatifs() != null ? d.getFraisIndicatifs() : "Gratuit"));
        frais.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0f5132;");

        Label delai = new Label("⏱️ " + (d.getDelaiEstime() != null ? d.getDelaiEstime() : "-"));
        delai.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        meta.getChildren().addAll(frais, delai);

        HBox actions = new HBox(8);
        Button btnView = new Button("Consulter & Préparer");
        btnView.getStyleClass().add("btn-primary");
        btnView.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnView, javafx.scene.layout.Priority.ALWAYS);
        btnView.setOnAction(e -> openDemarcheDetail(d));

        Button btnPdf = new Button("📄 PDF");
        btnPdf.getStyleClass().add("btn-outline");
        btnPdf.setOnAction(e -> pdfExportService.exportDemarcheSummary(d, ViewNavigator.getPrimaryStage()));

        actions.getChildren().addAll(btnView, btnPdf);

        card.getChildren().addAll(title, service, desc, meta, new Separator(), actions);
        return card;
    }

    private void openDemarcheDetail(Demarche d) {
        ViewNavigator.loadContent("/fxml/demarche_detail.fxml", (DemarcheDetailController ctrl) -> ctrl.setDemarche(d));
    }
}
