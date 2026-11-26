package sn.senthioonodiexna.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.model.Faq;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.service.FaqService;
import sn.senthioonodiexna.util.AlertUtil;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FaqController implements Initializable {

    @FXML private Button btnAddFaq;
    @FXML private TextField searchField;
    @FXML private VBox faqItemsContainer;

    private final FaqService faqService = new FaqService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkPrivileges();
        loadFaq(null);

        searchField.setOnAction(e -> handleSearch());
    }

    private void checkPrivileges() {
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        boolean canManage = user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.RESPONSABLE);
        btnAddFaq.setVisible(canManage);
        btnAddFaq.setManaged(canManage);
    }

    private void loadFaq(String keyword) {
        faqItemsContainer.getChildren().clear();
        Utilisateur user = AppSession.getInstance().getCurrentUser();
        boolean isAdmin = user != null && user.getRole() == Role.ADMIN;

        List<Faq> list = (keyword != null && !keyword.isBlank())
                ? faqService.searchFaq(keyword, null)
                : (isAdmin ? faqService.getAllFaqForAdmin() : faqService.getPublishedFaq());

        if (list.isEmpty()) {
            Label empty = new Label("Aucune question trouvée dans la FAQ.");
            empty.getStyleClass().add("text-muted");
            faqItemsContainer.getChildren().add(empty);
            return;
        }

        for (Faq f : list) {
            faqItemsContainer.getChildren().add(createFaqCard(f, isAdmin));
        }
    }

    private VBox createFaqCard(Faq f, boolean canManage) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label qLabel = new Label("❓  " + f.getQuestion());
        qLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        qLabel.setWrapText(true);
        HBox.setHgrow(qLabel, Priority.ALWAYS);

        header.getChildren().add(qLabel);

        if (f.getNomService() != null) {
            Label svcBadge = new Label(f.getNomService());
            svcBadge.getStyleClass().add("status-badge");
            svcBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
            header.getChildren().add(svcBadge);
        }

        if (canManage) {
            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("btn-outline");
            btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 2px 6px;");
            btnEdit.setOnAction(e -> showFaqDialog(f));

            Button btnDel = new Button("🗑️");
            btnDel.getStyleClass().add("btn-danger");
            btnDel.setStyle("-fx-font-size: 11px; -fx-padding: 2px 6px;");
            btnDel.setOnAction(e -> handleDeleteFaq(f));

            header.getChildren().addAll(btnEdit, btnDel);
        }

        Label aLabel = new Label(f.getReponse());
        aLabel.setWrapText(true);
        aLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155; -fx-line-spacing: 3px; -fx-padding: 4px 0 0 24px;");

        card.getChildren().addAll(header, new Separator(), aLabel);
        return card;
    }

    @FXML
    public void handleSearch() {
        loadFaq(searchField.getText());
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        loadFaq(null);
    }

    @FXML
    public void handleNewFaq() {
        showFaqDialog(null);
    }

    private void handleDeleteFaq(Faq f) {
        boolean confirm = AlertUtil.showConfirmation(
                "Suppression FAQ",
                "Supprimer cette question ?",
                "Êtes-vous sûr de vouloir supprimer cette question de la FAQ ?"
        );
        if (confirm) {
            faqService.deleteFaq(f.getId());
            loadFaq(searchField.getText());
        }
    }

    private void showFaqDialog(Faq existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter une question FAQ" : "Modifier la question FAQ");

        ButtonType okBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        TextField qTf = new TextField(existing != null ? existing.getQuestion() : "");
        qTf.setPromptText("Question fréquente...");

        TextArea aTa = new TextArea(existing != null ? existing.getReponse() : "");
        aTa.setPromptText("Réponse officielle détaillée...");
        aTa.setPrefRowCount(4);

        CheckBox pubCb = new CheckBox("Publiée pour les citoyens");
        pubCb.setSelected(existing == null || existing.isPubliee());

        grid.add(new Label("Question * :"), 0, 0);
        grid.add(qTf, 1, 0);
        grid.add(new Label("Réponse * :"), 0, 1);
        grid.add(aTa, 1, 1);
        grid.add(pubCb, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBtn) {
            if (qTf.getText().isBlank() || aTa.getText().isBlank()) {
                AlertUtil.showError("Champs requis", "La question et la réponse sont obligatoires.");
                return;
            }
            if (existing == null) {
                faqService.createFaq(qTf.getText().trim(), aTa.getText().trim(), pubCb.isSelected(), null, 1);
            } else {
                existing.setQuestion(qTf.getText().trim());
                existing.setReponse(aTa.getText().trim());
                existing.setPubliee(pubCb.isSelected());
                faqService.updateFaq(existing);
            }
            loadFaq(searchField.getText());
        }
    }
}
