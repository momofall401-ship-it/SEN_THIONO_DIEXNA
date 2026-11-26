package sn.senthioonodiexna;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.util.ViewNavigator;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Démarrage de l'application SEN THIOONO DIEXNA...");
        // Initialisation de la base de données et du pool HikariCP
        try {
            DatabaseConfig.initialize();
        } catch (Exception e) {
            logger.error("Avertissement lors de l'initialisation de la base de données : ", e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        ViewNavigator.setPrimaryStage(primaryStage);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        try {
            var iconStream = MainApp.class.getResourceAsStream("/images/logo.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception e) {
            logger.warn("Impossible de charger l'icône de l'application : {}", e.getMessage());
        }

        ViewNavigator.switchScene(
                "/fxml/login.fxml",
                "Sen Thioono Diexna — Accompagnement des démarches administratives",
                950, 700
        );
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        logger.info("Arrêt de l'application. Fermeture des ressources...");
        DatabaseConfig.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
