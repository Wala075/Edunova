package edu.edunova.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import edu.edunova.entities.Bulletin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Génère un fichier PDF à partir des entités du domaine.
 * Utilise OpenHTMLtoPDF (XHTML+CSS -> PDF).
 *
 * Usage :
 *   File f = new PdfService().genererBulletinPdf(bulletin, appreciation, dest);
 */
public class PdfService {

    /**
     * Génère le PDF d'un bulletin.
     *
     * @param bulletin       le bulletin source
     * @param appreciationIA appréciation Gemini (null/blank si non disponible)
     * @param destination    fichier de sortie (sera créé/écrasé)
     * @return le fichier PDF créé
     * @throws IOException si erreur d'écriture / rendu
     */
    public File genererBulletinPdf(Bulletin bulletin, String appreciationIA, File destination)
            throws IOException {
        return genererBulletinPdf(bulletin, appreciationIA, null, destination);
    }

    /**
     * Génère le PDF d'un bulletin avec appréciation FR + version arabe optionnelle.
     *
     * @param bulletin       le bulletin source
     * @param appreciationIA appréciation française (null/blank si non disponible)
     * @param appreciationAr appréciation arabe (null/blank si non traduite)
     * @param destination    fichier de sortie
     */
    public File genererBulletinPdf(Bulletin bulletin, String appreciationIA,
                                   String appreciationAr, File destination)
            throws IOException {
        if (bulletin == null) throw new IOException("Bulletin null");
        if (destination == null) throw new IOException("Destination null");

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        String xhtml = PdfTemplate.bulletinXhtml(bulletin, appreciationIA, appreciationAr);

        try (OutputStream os = new FileOutputStream(destination)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Support Bidi (arabe RTL)
            try {
                builder.useUnicodeBidiSplitter(
                        new com.openhtmltopdf.bidi.support.ICUBidiSplitter.ICUBidiSplitterFactory());
                builder.useUnicodeBidiReorderer(
                        new com.openhtmltopdf.bidi.support.ICUBidiReorderer());
                builder.defaultTextDirection(
                        com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection.LTR);
            } catch (Throwable t) {
                System.err.println("[PDF] Bidi non disponible : " + t.getMessage());
            }

            // Enregistrer une police qui supporte l'arabe (essai sur les chemins Windows usuels)
            registerArabicFont(builder);

            builder.withHtmlContent(xhtml, null);
            builder.toStream(os);
            builder.run();
        }
        return destination;
    }

    /**
     * Tente d'enregistrer une police qui supporte l'arabe.
     * Cherche dans : .env (PDF_FONT_PATH), Windows Fonts, Linux fonts.
     */
    private void registerArabicFont(PdfRendererBuilder builder) {
        String[] candidates = {
                edu.edunova.utils.ConfigLoader.get("PDF_FONT_PATH"),
                "C:/Windows/Fonts/tahoma.ttf",       // Windows : Tahoma supporte l'arabe
                "C:/Windows/Fonts/arial.ttf",        // Arial (Windows)
                "C:/Windows/Fonts/seguiemj.ttf",     // Segoe UI
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/Library/Fonts/Arial.ttf"           // macOS
        };
        for (String path : candidates) {
            if (path == null || path.isBlank()) continue;
            File f = new File(path);
            if (f.exists() && f.canRead()) {
                try {
                    builder.useFont(f, "ArabicFont");
                    System.out.println("[PDF] Font arabe : " + path);
                    return;
                } catch (Exception e) {
                    System.err.println("[PDF] Erreur font " + path + " : " + e.getMessage());
                }
            }
        }
        System.err.println("[PDF] Aucune police arabe trouvée. Le texte arabe peut s'afficher en carrés.");
    }

    /**
     * Construit un nom de fichier par défaut : Bulletin_Nom_Prenom_T1_2024-2025.pdf
     */
    public static String filenameFor(Bulletin b) {
        String nom = "eleve";
        String prenom = "";
        if (b.getStudent() != null) {
            nom = safe(b.getStudent().getNom_s());
            prenom = safe(b.getStudent().getPrenom_s());
        }
        return "Bulletin_" + nom + "_" + prenom + "_T" + b.getTrimestre()
                + "_" + safe(b.getAnnee()) + ".pdf";
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
