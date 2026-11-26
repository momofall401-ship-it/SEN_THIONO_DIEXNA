package sn.senthioonodiexna;

/**
 * Lanceur principal standard (Bootstrap Launcher).
 * Ne dérivant pas directement de javafx.application.Application, cette classe permet
 * d'exécuter et déboguer l'application JavaFX directement depuis VS Code, Eclipse ou IntelliJ
 * en un seul clic sur "Run" / "Start Debugging", sans lever l'erreur :
 * "JavaFX runtime components are missing, and are required to run this application".
 */
public class AppLauncher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
