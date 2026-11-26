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
import sn.senthioonodiexna.dao.AdministrationDao;
import sn.senthioonodiexna.dao.ServiceDao;
import sn.senthioonodiexna.dao.impl.AdministrationDaoImpl;
import sn.senthioonodiexna.dao.impl.ServiceDaoImpl;
import sn.senthioonodiexna.model.Administration;
import sn.senthioonodiexna.model.ServiceAdministratif;
import sn.senthioonodiexna.util.AlertUtil;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminServicesController implements Initializable {

    @FXML private TableView<ServiceAdministratif> servicesTable;
    @FXML private TableColumn<ServiceAdministratif, Number> colServiceId;
    @FXML private TableColumn<ServiceAdministratif, String> colServiceNom;
    @FXML private TableColumn<ServiceAdministratif, String> colServiceAdmin;
    @FXML private TableColumn<ServiceAdministratif, String> colServiceTel;
    @FXML private TableColumn<ServiceAdministratif, String> colServiceEmail;
    @FXML private TableColumn<ServiceAdministratif, Void> colServiceAction;

    @FXML private TableView<Administration> administrationsTable;
    @FXML private TableColumn<Administration, Number> colAdminId;
    @FXML private TableColumn<Administration, String> colAdminNom;
    @FXML private TableColumn<Administration, String> colAdminAdresse;
    @FXML private TableColumn<Administration, String> colAdminTel;
    @FXML private TableColumn<Administration, String> colAdminEmail;
    @FXML private TableColumn<Administration, Void> colAdminAction;

    private final AdministrationDao adminDao = new AdministrationDaoImpl();
    private final ServiceDao serviceDao = new ServiceDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupServicesTable();
        setupAdministrationsTable();
        loadAll();
    }

    private void setupServicesTable() {
        colServiceId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colServiceNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colServiceAdmin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomAdministration()));
        colServiceTel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelephone() != null ? c.getValue().getTelephone() : "-"));
        colServiceEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : "-"));

        colServiceAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("🗑️");
            private final HBox box = new HBox(6, btnEdit, btnDel);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-outline");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnEdit.setOnAction(e -> {
                    ServiceAdministratif s = getTableRow().getItem();
                    if (s != null) showServiceDialog(s);
                });

                btnDel.getStyleClass().add("btn-danger");
                btnDel.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnDel.setOnAction(e -> {
                    ServiceAdministratif s = getTableRow().getItem();
                    if (s != null) handleDeleteService(s);
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

    private void setupAdministrationsTable() {
        colAdminId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colAdminNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colAdminAdresse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAdresse() != null ? c.getValue().getAdresse() : "-"));
        colAdminTel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelephone() != null ? c.getValue().getTelephone() : "-"));
        colAdminEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : "-"));

        colAdminAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("🗑️");
            private final HBox box = new HBox(6, btnEdit, btnDel);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-outline");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnEdit.setOnAction(e -> {
                    Administration a = getTableRow().getItem();
                    if (a != null) showAdminDialog(a);
                });

                btnDel.getStyleClass().add("btn-danger");
                btnDel.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px;");
                btnDel.setOnAction(e -> {
                    Administration a = getTableRow().getItem();
                    if (a != null) handleDeleteAdmin(a);
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

    private void loadAll() {
        servicesTable.setItems(FXCollections.observableArrayList(serviceDao.findAll()));
        administrationsTable.setItems(FXCollections.observableArrayList(adminDao.findAll()));
    }

    @FXML
    public void handleNewAdministration() {
        showAdminDialog(null);
    }

    @FXML
    public void handleNewService() {
        showServiceDialog(null);
    }

    private void handleDeleteService(ServiceAdministratif s) {
        boolean confirm = AlertUtil.showConfirmation(
                "Suppression de service",
                "Supprimer le service " + s.getNom() + " ?",
                "Attention : cette action peut affecter les démarches associées."
        );
        if (confirm) {
            serviceDao.delete(s.getId());
            loadAll();
        }
    }

    private void handleDeleteAdmin(Administration a) {
        boolean confirm = AlertUtil.showConfirmation(
                "Suppression d'administration",
                "Supprimer " + a.getNom() + " ?",
                "Attention : les services rattachés seront également supprimés en cascade."
        );
        if (confirm) {
            adminDao.delete(a.getId());
            loadAll();
        }
    }

    private void showAdminDialog(Administration existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle Administration" : "Modifier l'administration");

        ButtonType okBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        TextField nomTf = new TextField(existing != null ? existing.getNom() : "");
        TextField adrTf = new TextField(existing != null && existing.getAdresse() != null ? existing.getAdresse() : "");
        TextField telTf = new TextField(existing != null && existing.getTelephone() != null ? existing.getTelephone() : "");
        TextField emailTf = new TextField(existing != null && existing.getEmail() != null ? existing.getEmail() : "");
        TextField webTf = new TextField(existing != null && existing.getSiteWeb() != null ? existing.getSiteWeb() : "");

        grid.add(new Label("Nom * :"), 0, 0);
        grid.add(nomTf, 1, 0);
        grid.add(new Label("Adresse :"), 0, 1);
        grid.add(adrTf, 1, 1);
        grid.add(new Label("Téléphone :"), 0, 2);
        grid.add(telTf, 1, 2);
        grid.add(new Label("E-mail :"), 0, 3);
        grid.add(emailTf, 1, 3);
        grid.add(new Label("Site web :"), 0, 4);
        grid.add(webTf, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBtn) {
            if (nomTf.getText().isBlank()) {
                AlertUtil.showError("Erreur", "Le nom est obligatoire.");
                return;
            }
            if (existing == null) {
                Administration a = new Administration(nomTf.getText().trim(), "", adrTf.getText().trim(),
                        telTf.getText().trim(), emailTf.getText().trim(), webTf.getText().trim());
                adminDao.create(a);
            } else {
                existing.setNom(nomTf.getText().trim());
                existing.setAdresse(adrTf.getText().trim());
                existing.setTelephone(telTf.getText().trim());
                existing.setEmail(emailTf.getText().trim());
                existing.setSiteWeb(webTf.getText().trim());
                adminDao.update(existing);
            }
            loadAll();
        }
    }

    private void showServiceDialog(ServiceAdministratif existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau Service Administratif" : "Modifier le service");

        ButtonType okBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        TextField nomTf = new TextField(existing != null ? existing.getNom() : "");
        TextField telTf = new TextField(existing != null && existing.getTelephone() != null ? existing.getTelephone() : "");
        TextField emailTf = new TextField(existing != null && existing.getEmail() != null ? existing.getEmail() : "");

        List<Administration> admins = adminDao.findAll();
        ComboBox<Administration> adminCombo = new ComboBox<>(FXCollections.observableArrayList(admins));
        if (existing != null) {
            for (Administration a : admins) {
                if (a.getId() == existing.getIdAdministration()) {
                    adminCombo.setValue(a);
                    break;
                }
            }
        } else if (!admins.isEmpty()) {
            adminCombo.setValue(admins.get(0));
        }

        grid.add(new Label("Nom du service * :"), 0, 0);
        grid.add(nomTf, 1, 0);
        grid.add(new Label("Administration * :"), 0, 1);
        grid.add(adminCombo, 1, 1);
        grid.add(new Label("Téléphone :"), 0, 2);
        grid.add(telTf, 1, 2);
        grid.add(new Label("E-mail :"), 0, 3);
        grid.add(emailTf, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBtn) {
            if (nomTf.getText().isBlank() || adminCombo.getValue() == null) {
                AlertUtil.showError("Erreur", "Le nom et l'administration sont obligatoires.");
                return;
            }
            if (existing == null) {
                ServiceAdministratif s = new ServiceAdministratif(nomTf.getText().trim(), "",
                        telTf.getText().trim(), emailTf.getText().trim(), adminCombo.getValue().getId());
                serviceDao.create(s);
            } else {
                existing.setNom(nomTf.getText().trim());
                existing.setTelephone(telTf.getText().trim());
                existing.setEmail(emailTf.getText().trim());
                existing.setIdAdministration(adminCombo.getValue().getId());
                serviceDao.update(existing);
            }
            loadAll();
        }
    }
}
