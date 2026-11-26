package sn.senthioonodiexna.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.dao.ServiceDao;
import sn.senthioonodiexna.dao.impl.ServiceDaoImpl;
import sn.senthioonodiexna.model.*;
import sn.senthioonodiexna.service.DashboardService;
import sn.senthioonodiexna.service.DossierService;
import sn.senthioonodiexna.util.DateUtil;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label dashboardScopeLabel;
    @FXML private HBox adminFilterBox;
    @FXML private ComboBox<ServiceAdministratif> serviceFilterCombo;

    @FXML private Label kpiTotalLabel;
    @FXML private Label kpiEnCoursLabel;
    @FXML private Label kpiComplementsLabel;
    @FXML private Label kpiAcceptesLabel;
    @FXML private Label tauxAcceptationLabel;
    @FXML private Label kpiRejetesLabel;
    @FXML private Label tauxRejetLabel;

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> statusBarChart;

    @FXML private TableView<Dossier> recentTable;
    @FXML private TableColumn<Dossier, String> colNumeroSuivi;
    @FXML private TableColumn<Dossier, String> colCitoyen;
    @FXML private TableColumn<Dossier, String> colDemarche;
    @FXML private TableColumn<Dossier, String> colService;
    @FXML private TableColumn<Dossier, String> colDate;
    @FXML private TableColumn<Dossier, String> colStatut;

    private final DashboardService dashboardService = new DashboardService();
    private final DossierService dossierService = new DossierService();
    private final ServiceDao serviceDao = new ServiceDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRecentTable();
        setupScope();
    }

    private void setupRecentTable() {
        colNumeroSuivi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroSuivi()));
        colCitoyen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomCompletCitoyen()));
        colDemarche.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitreDemarche()));
        colService.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomService()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateDepot() != null ? DateUtil.formatDateTime(c.getValue().getDateDepot()) : DateUtil.formatDateTime(c.getValue().getDateCreation())
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
    }

    private void setupScope() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        if (user == null) return;

        if (user.getRole() == Role.ADMIN) {
            adminFilterBox.setVisible(true);
            adminFilterBox.setManaged(true);
            dashboardScopeLabel.setText("Tableau de bord — Supervision Nationale");

            List<ServiceAdministratif> services = serviceDao.findAll();
            // Option globale
            ServiceAdministratif all = new ServiceAdministratif();
            all.setId(0);
            all.setNom("Tous les services (Global)");
            services.add(0, all);

            serviceFilterCombo.setItems(FXCollections.observableArrayList(services));
            serviceFilterCombo.setValue(all);
            serviceFilterCombo.setOnAction(e -> {
                ServiceAdministratif selected = serviceFilterCombo.getValue();
                Integer serviceId = (selected != null && selected.getId() > 0) ? selected.getId() : null;
                refreshMetrics(serviceId);
            });

            refreshMetrics(null);
        } else if (user.getRole() == Role.RESPONSABLE || user.getRole() == Role.AGENT) {
            adminFilterBox.setVisible(false);
            adminFilterBox.setManaged(false);
            dashboardScopeLabel.setText("Tableau de bord — " + (user.getNomService() != null ? user.getNomService() : "Mon Service"));
            refreshMetrics(user.getIdService());
        }
    }

    private void refreshMetrics(Integer idService) {
        Map<StatutDossier, Integer> counts = dashboardService.getStatusCounts(idService);
        int total = dashboardService.getTotalDossiers(idService);
        int enCours = dashboardService.getEnCoursDossiers(idService);
        int compl = counts.getOrDefault(StatutDossier.COMPLEMENT_DEMANDE, 0);
        int acceptes = counts.getOrDefault(StatutDossier.ACCEPTE, 0);
        int rejetes = counts.getOrDefault(StatutDossier.REJETE, 0);

        kpiTotalLabel.setText(String.valueOf(total));
        kpiEnCoursLabel.setText(String.valueOf(enCours));
        kpiComplementsLabel.setText(String.valueOf(compl));
        kpiAcceptesLabel.setText(String.valueOf(acceptes));
        kpiRejetesLabel.setText(String.valueOf(rejetes));

        int tauxAcc = total > 0 ? (acceptes * 100 / total) : 0;
        int tauxRej = total > 0 ? (rejetes * 100 / total) : 0;
        tauxAcceptationLabel.setText("Taux : " + tauxAcc + "%");
        tauxRejetLabel.setText("Taux : " + tauxRej + "%");

        // Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<StatutDossier, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieChart.Data(entry.getKey().getLibelle() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        statusPieChart.setData(pieData);

        // Bar Chart
        statusBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Statuts");
        for (StatutDossier st : StatutDossier.values()) {
            series.getData().add(new XYChart.Data<>(st.getLibelle(), counts.getOrDefault(st, 0)));
        }
        statusBarChart.getData().add(series);

        // Recent Dossiers
        List<Dossier> recents = (idService != null && idService > 0)
                ? dossierService.getDossiersForService(idService).stream().limit(8).toList()
                : dossierService.searchDossiers(null, null, null).stream().limit(8).toList();

        recentTable.setItems(FXCollections.observableArrayList(recents));
    }
}
