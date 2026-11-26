package sn.senthioonodiexna.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sn.senthioonodiexna.model.*;
import sn.senthioonodiexna.util.PdfUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfUtilTest {

    @Test
    @DisplayName("Génération PDFBox d'une fiche récapitulative de démarche")
    void testGenerateDemarcheSummaryPdf(@TempDir Path tempDir) throws IOException {
        Demarche d = new Demarche(1, "Extrait de Casier Judiciaire", "Description détaillée...",
                "Être de nationalité sénégalaise", "24h", "500 FCFA", 1, StatutPublication.PUBLIEE);
        d.setNomAdministration("Ministère de la Justice");
        d.setNomService("Greffe du Tribunal d'Instance de Dakar");
        d.setTelephoneService("+221 33 821 12 45");
        d.setEmailService("tribunal@justice.sn");

        DemarchePiece dp = new DemarchePiece(1, 1, true, "Acte de naissance légalisé");
        dp.setNomPiece("Copie d'acte de naissance");
        d.setPieces(List.of(dp));

        Etape e = new Etape(1, "Dépôt au greffe", "Se présenter au guichet", 1, 1);
        d.setEtapes(List.of(e));

        File pdfFile = tempDir.resolve("test_demarche.pdf").toFile();
        PdfUtil.generateDemarcheSummary(d, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 500);
    }

    @Test
    @DisplayName("Génération PDFBox d'un récépissé officiel de dépôt de dossier")
    void testGenerateDossierReceiptPdf(@TempDir Path tempDir) throws IOException {
        Dossier dossier = new Dossier(1, "STD-2026-00001", LocalDateTime.now(), LocalDateTime.now(),
                StatutDossier.ACCEPTE, null, "Note de service", 5, 1, 4);
        dossier.setNomCitoyen("Ba");
        dossier.setPrenomCitoyen("Ousmane");
        dossier.setEmailCitoyen("citoyen@gmail.com");
        dossier.setTelephoneCitoyen("+221 77 555 12 34");
        dossier.setTitreDemarche("Extrait de Casier Judiciaire");
        dossier.setNomService("Tribunal d'Instance de Dakar");
        dossier.setNomAdministration("Ministère de la Justice");

        DocumentFourni doc = new DocumentFourni(1, "acte.pdf", LocalDateTime.now(), StatutVerification.CONFORME, "Validé", 1, 1);
        doc.setNomPiece("Acte de naissance");
        dossier.setDocuments(List.of(doc));

        File pdfFile = tempDir.resolve("test_recepisse.pdf").toFile();
        PdfUtil.generateDossierReceipt(dossier, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 500);
    }
}
