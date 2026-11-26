package sn.senthioonodiexna.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.DemarchePiece;
import sn.senthioonodiexna.model.DocumentFourni;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.Etape;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PdfUtil {

    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_OBLIQUE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    private static final Color COLOR_PRIMARY = new Color(15, 81, 50);      // Vert sénégalais
    private static final Color COLOR_DARK = new Color(30, 41, 59);          // Slate
    private static final Color COLOR_GOLD = new Color(212, 175, 55);        // Doré
    private static final Color COLOR_MUTED = new Color(100, 116, 139);      // Muted gray
    private static final Color COLOR_BORDER = new Color(203, 213, 225);     // Border light

    public static void generateDemarcheSummary(Demarche demarche, File targetFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float margin = 50;
                float y = 800;

                // En-tête officiel
                drawHeader(cs, "REPUBLIQUE DU SENEGAL", "Un Peuple - Un But - Une Foi", margin, y);
                y -= 50;

                // Titre de l'application
                cs.setNonStrokingColor(COLOR_PRIMARY);
                cs.beginText();
                cs.setFont(FONT_BOLD, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("SEN THIOONO DIEXNA - FICHE RECAPITULATIVE");
                cs.endText();
                y -= 25;

                // Titre de la démarche
                cs.setNonStrokingColor(COLOR_DARK);
                cs.beginText();
                cs.setFont(FONT_BOLD, 14);
                cs.newLineAtOffset(margin, y);
                cs.showText("Demarche : " + sanitize(demarche.getTitre()));
                cs.endText();
                y -= 20;

                // Administration & Service
                cs.setFont(FONT_REGULAR, 10);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("Administration : " + sanitize(demarche.getNomAdministration()) + " | Service : " + sanitize(demarche.getNomService()));
                cs.endText();
                y -= 15;

                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("Frais indicatifs : " + sanitize(demarche.getFraisIndicatifs()) + " | Delai estime : " + sanitize(demarche.getDelaiEstime()));
                cs.endText();
                y -= 15;

                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText("Contact : " + sanitize(demarche.getTelephoneService()) + " - " + sanitize(demarche.getEmailService()));
                cs.endText();
                y -= 25;

                // Ligne séparatrice
                cs.setStrokingColor(COLOR_BORDER);
                cs.setLineWidth(1f);
                cs.moveTo(margin, y);
                cs.lineTo(PDRectangle.A4.getWidth() - margin, y);
                cs.stroke();
                y -= 20;

                // Description & Conditions
                y = drawSection(cs, "DESCRIPTION & CONDITIONS D'ELIGIBILITE", margin, y);
                List<String> descLines = wrapText(demarche.getDescription() + "\n" + (demarche.getConditions() != null ? demarche.getConditions() : ""), 85);
                for (String line : descLines) {
                    if (y < 60) break;
                    cs.beginText();
                    cs.setFont(FONT_REGULAR, 9);
                    cs.newLineAtOffset(margin, y);
                    cs.showText(sanitize(line));
                    cs.endText();
                    y -= 12;
                }
                y -= 10;

                // Pièces à fournir
                y = drawSection(cs, "PIECES EXIGEES", margin, y);
                if (demarche.getPieces() != null && !demarche.getPieces().isEmpty()) {
                    for (DemarchePiece dp : demarche.getPieces()) {
                        if (y < 60) break;
                        String flag = dp.isObligatoire() ? "[OBLIGATOIRE]" : "[FACULTATIF]";
                        cs.beginText();
                        cs.setFont(FONT_BOLD, 9);
                        cs.newLineAtOffset(margin, y);
                        cs.showText("- " + flag + " " + sanitize(dp.getNomPiece()));
                        cs.endText();
                        y -= 12;

                        if (dp.getInstructions() != null && !dp.getInstructions().isBlank()) {
                            cs.beginText();
                            cs.setFont(FONT_OBLIQUE, 8);
                            cs.newLineAtOffset(margin + 15, y);
                            cs.showText(sanitize(dp.getInstructions()));
                            cs.endText();
                            y -= 11;
                        }
                    }
                } else {
                    cs.beginText();
                    cs.setFont(FONT_REGULAR, 9);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Aucune piece specifiee.");
                    cs.endText();
                    y -= 14;
                }
                y -= 10;

                // Étapes de la démarche
                y = drawSection(cs, "ETAPES DU PARCOURS", margin, y);
                if (demarche.getEtapes() != null && !demarche.getEtapes().isEmpty()) {
                    for (Etape e : demarche.getEtapes()) {
                        if (y < 60) break;
                        cs.beginText();
                        cs.setFont(FONT_BOLD, 9);
                        cs.newLineAtOffset(margin, y);
                        cs.showText(e.getOrdre() + ". " + sanitize(e.getTitre()));
                        cs.endText();
                        y -= 12;

                        if (e.getDescription() != null && !e.getDescription().isBlank()) {
                            cs.beginText();
                            cs.setFont(FONT_REGULAR, 8);
                            cs.newLineAtOffset(margin + 15, y);
                            cs.showText(sanitize(e.getDescription()));
                            cs.endText();
                            y -= 11;
                        }
                    }
                }
                y -= 20;

                // Pied de page
                drawFooter(cs, margin, 40);
            }

            document.save(targetFile);
        }
    }

    public static void generateDossierReceipt(Dossier dossier, File targetFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float margin = 50;
                float y = 800;

                drawHeader(cs, "REPUBLIQUE DU SENEGAL", "Un Peuple - Un But - Une Foi", margin, y);
                y -= 50;

                cs.setNonStrokingColor(COLOR_PRIMARY);
                cs.beginText();
                cs.setFont(FONT_BOLD, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("RECEPISSE DE DEPOT DE DOSSIER");
                cs.endText();
                y -= 25;

                // Cadre Numéro de suivi
                cs.setStrokingColor(COLOR_PRIMARY);
                cs.setLineWidth(1.5f);
                cs.addRect(margin, y - 25, PDRectangle.A4.getWidth() - 2 * margin, 35);
                cs.stroke();

                cs.beginText();
                cs.setFont(FONT_BOLD, 14);
                cs.newLineAtOffset(margin + 15, y - 10);
                cs.showText("NUMERO DE SUIVI : " + dossier.getNumeroSuivi());
                cs.endText();
                y -= 45;

                // Informations Citoyen & Démarche
                y = drawSection(cs, "INFORMATIONS GENERALES", margin, y);

                drawLabelValue(cs, "Usager :", dossier.getNomCompletCitoyen(), margin, y);
                y -= 14;
                drawLabelValue(cs, "E-mail :", dossier.getEmailCitoyen(), margin, y);
                y -= 14;
                drawLabelValue(cs, "Telephone :", dossier.getTelephoneCitoyen() != null ? dossier.getTelephoneCitoyen() : "-", margin, y);
                y -= 14;
                drawLabelValue(cs, "Demarche :", dossier.getTitreDemarche(), margin, y);
                y -= 14;
                drawLabelValue(cs, "Service concerne :", dossier.getNomService() + " (" + dossier.getNomAdministration() + ")", margin, y);
                y -= 14;
                drawLabelValue(cs, "Date de creation :", DateUtil.formatDateTime(dossier.getDateCreation()), margin, y);
                y -= 14;
                drawLabelValue(cs, "Date de depot :", DateUtil.formatDateTime(dossier.getDateDepot()), margin, y);
                y -= 14;
                drawLabelValue(cs, "Statut actuel :", dossier.getStatut() != null ? dossier.getStatut().getLibelle() : "-", margin, y);
                y -= 25;

                // Liste des pièces fournies ou attendues
                y = drawSection(cs, "PIECES ASSOCIEES AU DOSSIER", margin, y);
                if (dossier.getDocuments() != null && !dossier.getDocuments().isEmpty()) {
                    for (DocumentFourni doc : dossier.getDocuments()) {
                        if (y < 70) break;
                        String verif = doc.getStatutVerification() != null ? doc.getStatutVerification().getLibelle() : "En attente";
                        cs.beginText();
                        cs.setFont(FONT_BOLD, 9);
                        cs.newLineAtOffset(margin, y);
                        cs.showText("- " + sanitize(doc.getNomPiece()) + " - Etat: " + verif);
                        cs.endText();
                        y -= 13;
                        if (doc.getCommentaire() != null && !doc.getCommentaire().isBlank()) {
                            cs.beginText();
                            cs.setFont(FONT_OBLIQUE, 8);
                            cs.newLineAtOffset(margin + 15, y);
                            cs.showText("Observation : " + sanitize(doc.getCommentaire()));
                            cs.endText();
                            y -= 11;
                        }
                    }
                } else {
                    cs.beginText();
                    cs.setFont(FONT_REGULAR, 9);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Aucun document enregistre.");
                    cs.endText();
                    y -= 15;
                }
                y -= 20;

                // Note d'authenticité et avertissement légal
                cs.setStrokingColor(COLOR_GOLD);
                cs.setLineWidth(1f);
                cs.addRect(margin, y - 40, PDRectangle.A4.getWidth() - 2 * margin, 50);
                cs.stroke();

                cs.setNonStrokingColor(COLOR_MUTED);
                cs.beginText();
                cs.setFont(FONT_OBLIQUE, 8);
                cs.newLineAtOffset(margin + 10, y - 10);
                cs.showText("Ce document atteste de la preparation et de l'enregistrement de votre dossier sur l'application Sen Thioono Diexna.");
                cs.endText();
                cs.beginText();
                cs.setFont(FONT_OBLIQUE, 8);
                cs.newLineAtOffset(margin + 10, y - 22);
                cs.showText("Conservez precieusement votre numero de suivi pour consulter l'avancement de votre demande aupres des services.");
                cs.endText();
                cs.beginText();
                cs.setFont(FONT_OBLIQUE, 8);
                cs.newLineAtOffset(margin + 10, y - 34);
                cs.showText("Edite le : " + DateUtil.formatDateTime(LocalDateTime.now()) + " par la plateforme Sen Thioono Diexna.");
                cs.endText();

                drawFooter(cs, margin, 40);
            }

            document.save(targetFile);
        }
    }

    private static void drawHeader(PDPageContentStream cs, String topText, String subText, float margin, float y) throws IOException {
        cs.setNonStrokingColor(COLOR_DARK);
        cs.beginText();
        cs.setFont(FONT_BOLD, 12);
        cs.newLineAtOffset(margin, y);
        cs.showText(topText);
        cs.endText();

        cs.beginText();
        cs.setFont(FONT_OBLIQUE, 9);
        cs.newLineAtOffset(margin, y - 14);
        cs.showText(subText);
        cs.endText();
    }

    private static float drawSection(PDPageContentStream cs, String title, float margin, float y) throws IOException {
        cs.setNonStrokingColor(COLOR_PRIMARY);
        cs.beginText();
        cs.setFont(FONT_BOLD, 11);
        cs.newLineAtOffset(margin, y);
        cs.showText(title);
        cs.endText();
        cs.setStrokingColor(COLOR_PRIMARY);
        cs.setLineWidth(1f);
        cs.moveTo(margin, y - 4);
        cs.lineTo(margin + 200, y - 4);
        cs.stroke();
        cs.setNonStrokingColor(COLOR_DARK);
        return y - 18;
    }

    private static void drawLabelValue(PDPageContentStream cs, String label, String value, float margin, float y) throws IOException {
        cs.setNonStrokingColor(COLOR_MUTED);
        cs.beginText();
        cs.setFont(FONT_BOLD, 9);
        cs.newLineAtOffset(margin, y);
        cs.showText(label);
        cs.endText();

        cs.setNonStrokingColor(COLOR_DARK);
        cs.beginText();
        cs.setFont(FONT_REGULAR, 9);
        cs.newLineAtOffset(margin + 130, y);
        cs.showText(sanitize(value));
        cs.endText();
    }

    private static void drawFooter(PDPageContentStream cs, float margin, float y) throws IOException {
        cs.setStrokingColor(COLOR_BORDER);
        cs.setLineWidth(0.5f);
        cs.moveTo(margin, y + 15);
        cs.lineTo(PDRectangle.A4.getWidth() - margin, y + 15);
        cs.stroke();

        cs.setNonStrokingColor(COLOR_MUTED);
        cs.beginText();
        cs.setFont(FONT_REGULAR, 8);
        cs.newLineAtOffset(margin, y);
        cs.showText("Sen Thioono Diexna - Accompagnement numerique des demarches administratives");
        cs.endText();
    }

    private static String sanitize(String text) {
        if (text == null) return "-";
        return text.replace("\r", "")
                .replace("’", "'")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("î", "i")
                .replace("ï", "i")
                .replace("ô", "o")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ç", "c")
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace("À", "A")
                .replace("Ç", "C");
    }

    private static List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> result = new ArrayList<>();
        if (text == null) return result;
        String[] paragraphs = text.split("\n");
        for (String p : paragraphs) {
            String[] words = p.split(" ");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (sb.length() + w.length() + 1 > maxCharsPerLine) {
                    result.add(sb.toString());
                    sb = new StringBuilder();
                }
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(w);
            }
            if (!sb.isEmpty()) {
                result.add(sb.toString());
            }
        }
        return result;
    }
}
