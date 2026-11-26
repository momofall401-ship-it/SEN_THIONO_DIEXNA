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
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.dao.ServiceDao;
import sn.senthioonodiexna.dao.impl.ServiceDaoImpl;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.ServiceAdministratif;
import sn.senthioonodiexna.model.StatutCompte;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.AuthService;
import sn.senthioonodiexna.util.AlertUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminUsersController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<Role> roleFilterCombo;
    @FXML private TableView<Utilisateur> usersTable;

    @FXML private TableColumn<Utilisateur, Number> colId;
    @FXML private TableColumn<Utilisateur, String> colNomComplet;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colTelephone;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colService;
    @FXML private TableColumn<Utilisateur, String> colStatut;
    @FXML private TableColumn<Utilisateur, Void> colAction;

    private final AuthService authService = new AuthService();
    private final ServiceDao serviceDao = new ServiceDaoImpl();
    private List<Utilisateur> allUsers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        roleFilterCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleFilterCombo.setOnAction(e -> handleFilter());
        searchField.setOnAction(e -> handleFilter());
        loadUsers();
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colNomComplet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomComplet()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colTelephone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelephone() != null ? c.getValue().getTelephone() : "-"
        ));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().getLibelle()));
        colService.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNomService() != null ? c.getValue().getNomService() : "-"
        ));

        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatutCompte().getLibelle()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    if ("Actif".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #047857;");
                    } else {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnRole = new Button("Rôle & Service");
            private final Button btnToggle = new Button("Statut");
            private final HBox box = new HBox(6, btnRole, btnToggle);

            {
                box.setAlignment(Pos.CENTER);
                btnRole.getStyleClass().add("btn-outline");
                btnRole.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnRole.setOnAction(e -> {
                    Utilisateur u = getTableRow().getItem();
                    if (u != null) {
                        showRoleServiceDialog(u);
                    }
                });

                btnToggle.getStyleClass().add("btn-secondary");
                btnToggle.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnToggle.setOnAction(e -> {
                    Utilisateur u = getTableRow().getItem();
                    if (u != null) {
                        handleToggleStatus(u);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Utilisateur u = getTableRow().getItem();
                    btnToggle.setText(u.getStatutCompte() == StatutCompte.ACTIF ? "Désactiver" : "Activer");
                    setGraphic(box);
                }
            }
        });
    }

    private void loadUsers() {
        allUsers = authService.getAllUsers();
        applyFilter();
    }

    @FXML
    public void handleFilter() {
        applyFilter();
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        roleFilterCombo.setValue(null);
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        Role selectedRole = roleFilterCombo.getValue();

        List<Utilisateur> filtered = allUsers.stream().filter(u -> {
            boolean matchQuery = query.isEmpty() ||
                    u.getNom().toLowerCase().contains(query) ||
                    u.getPrenom().toLowerCase().contains(query) ||
                    u.getEmail().toLowerCase().contains(query);

            boolean matchRole = (selectedRole == null) || (u.getRole() == selectedRole);

            return matchQuery && matchRole;
        }).toList();

        usersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void handleToggleStatus(Utilisateur u) {
        // Empêcher de désactiver son propre compte
        Utilisateur current = AppSession.getInstance().getCurrentUser();
        if (current != null && current.getId() == u.getId()) {
            AlertUtil.showWarning("Action impossible", "Vous ne pouvez pas désactiver votre propre compte administrateur.");
            return;
        }

        String action = (u.getStatutCompte() == StatutCompte.ACTIF) ? "désactiver" : "activer";
        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation de statut (RG11)",
                action.substring(0, 1).toUpperCase() + action.substring(1) + " le compte ?",
                "Êtes-vous sûr de vouloir " + action + " le compte de " + u.getNomComplet() + " ?"
        );

        if (confirm) {
            authService.toggleAccountStatus(u.getId());
            loadUsers();
        }
    }

    private void showRoleServiceDialog(Utilisateur u) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modification Rôle & Service — " + u.getNomComplet());
        dialog.setHeaderText("Modifier les attributions d'accès de l'utilisateur (RBAC)");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        ComboBox<Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleCombo.setValue(u.getRole());

        List<ServiceAdministratif> services = serviceDao.findAll();
        ServiceAdministratif noneService = new ServiceAdministratif();
        noneService.setId(0);
        noneService.setNom("Aucun service assigné");
        services.add(0, noneService);

        ComboBox<ServiceAdministratif> serviceCombo = new ComboBox<>(FXCollections.observableArrayList(services));
        serviceCombo.setValue(noneService);
        if (u.getIdService() != null && u.getIdService() > 0) {
            for (ServiceAdministratif s : services) {
                if (s.getId() == u.getIdService()) {
                    serviceCombo.setValue(s);
                    break;
                }
            }
        }

        grid.add(new Label("Nouveau rôle :"), 0, 0);
        grid.add(roleCombo, 1, 0);
        grid.add(new Label("Service administratif :"), 0, 1);
        grid.add(serviceCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            Role newRole = roleCombo.getValue();
            ServiceAdministratif selectedSvc = serviceCombo.getValue();
            Integer svcId = (selectedSvc != null && selectedSvc.getId() > 0) ? selectedSvc.getId() : null;

            authService.updateUserRoleAndService(u.getId(), newRole, svcId);
            AlertUtil.showInfo("Rôle mis à jour", "Les attributions de " + u.getNomComplet() + " ont été mises à jour.");
            loadUsers();
        }
    }
}
