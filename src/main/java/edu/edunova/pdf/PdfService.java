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
        if (bulletin == null) throw new IOException("Bulletin null");
        if (destination == null) throw new IOException("Destination null");

        // S'assurer que le dossier parent existe
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        String xhtml = PdfTemplate.bulletinXhtml(bulletin, appreciationIA);

        try (OutputStream os = new FileOutputStream(destination)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(os);
            builder.run();
        }
        return destination;
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
