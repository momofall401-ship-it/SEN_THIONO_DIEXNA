package sn.senthioonodiexna.service;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.util.AlertUtil;
import sn.senthioonodiexna.util.PdfUtil;

import java.awt.Desktop;
import java.io.File;

public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    public boolean exportDemarcheSummary(Demarche demarche, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la fiche récapitulative PDF");
        String safeName = demarche.getTitre().replaceAll("[^a-zA-Z0-9.-]", "_");
        fileChooser.setInitialFileName("Fiche_" + safeName + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF (*.pdf)", "*.pdf"));

        File target = fileChooser.showSaveDialog(window);
        if (target != null) {
            try {
                PdfUtil.generateDemarcheSummary(demarche, target);
                logger.info("Fiche PDF générée avec succès : {}", target.getAbsolutePath());
                boolean open = AlertUtil.showConfirmation(
                        "Génération réussie",
                        "Fiche récapitulative PDF générée avec succès.",
                        "Souhaitez-vous ouvrir le document dès maintenant ?"
                );
                if (open && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(target);
                }
                return true;
            } catch (Exception e) {
                logger.error("Erreur lors de la génération PDF : ", e);
                AlertUtil.showError("Erreur d'exportation", "Impossible de générer le fichier PDF : " + e.getMessage());
            }
        }
        return false;
    }

    public boolean exportDossierReceipt(Dossier dossier, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le récépissé de dépôt PDF");
        fileChooser.setInitialFileName("Recepisse_" + dossier.getNumeroSuivi() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF (*.pdf)", "*.pdf"));

        File target = fileChooser.showSaveDialog(window);
        if (target != null) {
            try {
                PdfUtil.generateDossierReceipt(dossier, target);
                logger.info("Récépissé PDF généré avec succès : {}", target.getAbsolutePath());
                boolean open = AlertUtil.showConfirmation(
                        "Génération réussie",
                        "Récépissé officiel PDF généré avec succès.",
                        "Souhaitez-vous ouvrir le document dès maintenant ?"
                );
                if (open && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(target);
                }
                return true;
            } catch (Exception e) {
                logger.error("Erreur lors de la génération du récépissé PDF : ", e);
                AlertUtil.showError("Erreur d'exportation", "Impossible de générer le récépissé PDF : " + e.getMessage());
            }
        }
        return false;
    }
}
