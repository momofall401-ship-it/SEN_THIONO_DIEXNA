package sn.senthioonodiexna.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.controller.MainLayoutController;

import java.io.IOException;
import java.util.function.Consumer;

public class ViewNavigator {

    private static final Logger logger = LoggerFactory.getLogger(ViewNavigator.class);

    private static Stage primaryStage;
    private static MainLayoutController mainLayoutController;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setMainLayoutController(MainLayoutController controller) {
        mainLayoutController = controller;
    }

    public static MainLayoutController getMainLayoutController() {
        return mainLayoutController;
    }

    // Basculer l'ensemble de la scène (ex: Login, Register, MainLayout)
    public static void switchScene(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);

            // Charger CSS
            String css = ViewNavigator.class.getResource("/css/style.css") != null
                    ? ViewNavigator.class.getResource("/css/style.css").toExternalForm() : null;
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

            Object controller = loader.getController();
            if (controller instanceof MainLayoutController mlc) {
                mainLayoutController = mlc;
            }
        } catch (IOException e) {
            logger.error("Erreur lors du changement de scène vers {} : ", fxmlPath, e);
            AlertUtil.showError("Erreur d'affichage", "Impossible de charger l'écran : " + e.getMessage());
        }
    }

    // Charger une sous-vue dans la zone centrale du layout principal
    public static <T> T loadContent(String fxmlPath, Consumer<T> controllerConsumer) {
        if (mainLayoutController == null) {
            logger.warn("MainLayoutController non disponible. Chargement du layout principal d'abord...");
            switchScene("/fxml/main_layout.fxml", "Sen Thioono Diexna — Gestion des démarches administratives", 1200, 750);
        }

        try {
            FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
            Parent content = loader.load();
            T controller = loader.getController();

            if (controllerConsumer != null && controller != null) {
                controllerConsumer.accept(controller);
            }

            if (mainLayoutController != null) {
                mainLayoutController.setContent(content);
                mainLayoutController.refreshBadge();
            }

            return controller;
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de la sous-vue {} : ", fxmlPath, e);
            AlertUtil.showError("Erreur", "Impossible de charger la vue : " + e.getMessage());
            return null;
        }
    }

    public static void loadContent(String fxmlPath) {
        loadContent(fxmlPath, null);
    }
}
