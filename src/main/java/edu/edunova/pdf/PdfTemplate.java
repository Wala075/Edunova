package edu.edunova.pdf;

import edu.edunova.entities.Bulletin;
import edu.edunova.entities.Bulletin.BulletinLigne;
import edu.edunova.entities.Student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Templates XHTML pour les PDF (utilisé par PdfService + OpenHTMLtoPDF).
 *
 * IMPORTANT : OpenHTMLtoPDF requiert du XHTML strict (pas du HTML5) :
 *   - tags fermés : <br/>, <hr/>, <meta .../>
 *   - pas de flexbox (utiliser tables ou width/float)
 *   - CSS 2.1 + petite partie de CSS3
 */
public class PdfTemplate {

    private static final String COLOR_PRIMARY = "#7c3aed";
    private static final String COLOR_TEXT    = "#1f2937";
    private static final String COLOR_MUTED   = "#6b7280";

    private PdfTemplate() {}

    /** Compatibilité ascendante (sans version arabe). */
    public static String bulletinXhtml(Bulletin b, String appreciationIA) {
        return bulletinXhtml(b, appreciationIA, null);
    }

    /**
     * Génère le bulletin XHTML pour OpenHTMLtoPDF.
     * Si appreciationAr est non-null, affiche les deux versions (FR + AR) côte à côte.
     */
    public static String bulletinXhtml(Bulletin b, String appreciationIA, String appreciationAr) {
        Student s = b.getStudent();
        String studentName = s == null ? "—" : esc(s.getNomComplet().trim());
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        StringBuilder rows = new StringBuilder();
        if (b.getLignes() != null) {
            for (BulletinLigne l : b.getLignes()) {
                String moyColor = couleurMoyenne(l.getMoyenne());
                rows.append("<tr>")
                    .append("<td class='cell-mat'>").append(esc(l.getMatiere())).append("</td>")
                    .append("<td class='cell-num'>").append(l.getNbNotes()).append("</td>")
                    .append("<td class='cell-moy' style='color:").append(moyColor).append(";'>")
                        .append(String.format("%.2f", l.getMoyenne())).append(" / 20</td>")
                    .append("<td class='cell-app' style='color:").append(moyColor).append(";'>")
                        .append(esc(l.getAppreciation())).append("</td>")
                    .append("</tr>");
            }
        }

        boolean hasFr = appreciationIA != null && !appreciationIA.isBlank();
        boolean hasAr = appreciationAr != null && !appreciationAr.isBlank();

        StringBuilder ap = new StringBuilder();
        if (hasFr && hasAr) {
            // Mode bilingue : 2 colonnes côte à côte
            ap.append("<table class='bilingue-table'><tr>")
              // Colonne FR
              .append("<td class='bilingue-fr'>")
                  .append("<div class='appreciation'>")
                      .append("<div class='appreciation-title'>")
                          .append("APPRÉCIATION DU PROFESSEUR PRINCIPAL")
                      .append("</div>")
                      .append("<p>").append(esc(appreciationIA)).append("</p>")
                  .append("</div>")
              .append("</td>")
              // Colonne AR
              .append("<td class='bilingue-ar'>")
                  .append("<div class='appreciation appreciation-ar' dir='rtl'>")
                      .append("<div class='appreciation-title appreciation-title-ar'>")
                          .append("ملاحظة المعلم الرئيسي")
                      .append("</div>")
                      .append("<p dir='rtl'>").append(esc(appreciationAr)).append("</p>")
                  .append("</div>")
              .append("</td>")
              .append("</tr></table>");
        } else if (hasFr) {
            // Mode FR seul (comportement existant)
            ap.append("<div class='appreciation'>")
              .append("<div class='appreciation-title'>")
                  .append("APPRÉCIATION DU PROFESSEUR PRINCIPAL")
              .append("</div>")
              .append("<p>").append(esc(appreciationIA)).append("</p>")
              .append("</div>");
        } else if (hasAr) {
            // Mode AR seul
            ap.append("<div class='appreciation appreciation-ar' dir='rtl'>")
              .append("<div class='appreciation-title appreciation-title-ar'>")
                  .append("ملاحظة المعلم الرئيسي")
              .append("</div>")
              .append("<p dir='rtl'>").append(esc(appreciationAr)).append("</p>")
              .append("</div>");
        }

        // Note : <!DOCTYPE html PUBLIC...> avec XHTML 1.0 Strict pour OpenHTMLtoPDF
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
             + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
             + "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"fr\">"
             + "<head>"
             + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>"
             + "<title>Bulletin scolaire - " + studentName + "</title>"
             + "<style>" + css() + "</style>"
             + "</head>"
             + "<body>"

             + "<div class='header'>"
                 + "<table class='header-table'><tr>"
                     + "<td class='header-left'>"
                         + "<div class='logo'>EduNova</div>"
                         + "<div class='subtitle'>Établissement scolaire</div>"
                     + "</td>"
                     + "<td class='header-right'>"
                         + "<div class='doc-title'>BULLETIN SCOLAIRE</div>"
                         + "<div class='doc-info'>Trimestre " + b.getTrimestre()
                             + " &middot; Année " + esc(b.getAnnee()) + "</div>"
                         + "<div class='doc-date'>Édité le " + today + "</div>"
                     + "</td>"
                 + "</tr></table>"
             + "</div>"

             + "<div class='student-card'>"
                 + "<table class='student-table'><tr>"
                     + "<td class='student-info'>"
                         + "<div class='student-label'>ÉLÈVE</div>"
                         + "<div class='student-name'>" + studentName + "</div>"
                     + "</td>"
                     + "<td class='moy-info'>"
                         + "<div class='student-label'>MOYENNE GÉNÉRALE</div>"
                         + "<div class='moy-value' style='color:"
                             + couleurMoyenne(b.getMoyenneGenerale()) + ";'>"
                             + String.format("%.2f", b.getMoyenneGenerale()) + " / 20"
                         + "</div>"
                         + "<div class='mention'>" + esc(b.getMention()) + "</div>"
                     + "</td>"
                 + "</tr></table>"
             + "</div>"

             + "<table class='notes-table'>"
                 + "<thead><tr>"
                     + "<th class='th-mat'>Matière</th>"
                     + "<th class='th-num'>Nb notes</th>"
                     + "<th class='th-moy'>Moyenne</th>"
                     + "<th class='th-app'>Appréciation</th>"
                 + "</tr></thead>"
                 + "<tbody>" + rows.toString() + "</tbody>"
             + "</table>"

             + ap.toString()

             + "<div class='decision'>"
                 + "<strong>Décision :</strong> " + esc(b.getDecision())
             + "</div>"

             + "<div class='footer'>"
                 + "Bulletin généré automatiquement par EduNova &middot; " + today
             + "</div>"

             + "</body></html>";
    }

    private static String css() {
        return ""
            + "@page { size: A4; margin: 22mm 18mm; }"
            + "* { box-sizing: border-box; }"
            + "body { font-family: 'Helvetica', sans-serif; font-size: 11pt; color: " + COLOR_TEXT + "; margin: 0; }"

            + ".header-table { width: 100%; border-bottom: 3px solid " + COLOR_PRIMARY + "; padding-bottom: 10px; margin-bottom: 18px; }"
            + ".header-left { width: 50%; vertical-align: top; }"
            + ".header-right { width: 50%; text-align: right; vertical-align: top; }"
            + ".logo { font-size: 22pt; font-weight: bold; color: " + COLOR_PRIMARY + "; }"
            + ".subtitle { font-size: 9pt; color: " + COLOR_MUTED + "; }"
            + ".doc-title { font-size: 14pt; font-weight: bold; }"
            + ".doc-info { font-size: 10pt; color: " + COLOR_MUTED + "; }"
            + ".doc-date { font-size: 9pt; color: " + COLOR_MUTED + "; margin-top: 4px; }"

            + ".student-card { background: #f9fafb; border-radius: 6px; padding: 14px 16px; margin-bottom: 18px; border: 1px solid #e5e7eb; }"
            + ".student-table { width: 100%; }"
            + ".student-info { width: 60%; vertical-align: middle; }"
            + ".moy-info { width: 40%; text-align: right; vertical-align: middle; }"
            + ".student-label { font-size: 8pt; color: " + COLOR_MUTED + "; font-weight: bold; letter-spacing: 0.5px; }"
            + ".student-name { font-size: 16pt; font-weight: bold; margin-top: 4px; }"
            + ".moy-value { font-size: 22pt; font-weight: bold; }"
            + ".mention { font-size: 11pt; color: " + COLOR_PRIMARY + "; font-weight: 600; }"

            + ".notes-table { width: 100%; border-collapse: collapse; margin-bottom: 18px; }"
            + ".notes-table th { background: " + COLOR_PRIMARY + "; color: white; padding: 9px 10px; font-weight: bold; font-size: 10pt; }"
            + ".notes-table .th-mat { text-align: left; }"
            + ".notes-table .th-num, .notes-table .th-moy { text-align: center; }"
            + ".notes-table .th-app { text-align: left; }"
            + ".notes-table td { border-bottom: 1px solid #e5e7eb; padding: 9px 10px; font-size: 10pt; }"
            + ".cell-mat { font-weight: 600; }"
            + ".cell-num { text-align: center; }"
            + ".cell-moy { text-align: center; font-weight: bold; }"
            + ".cell-app { font-weight: 600; }"

            + ".appreciation { background: #faf5ff; border-left: 3px solid " + COLOR_PRIMARY + "; padding: 12px 16px; margin-bottom: 18px; }"
            + ".appreciation-title { font-size: 9pt; color: " + COLOR_PRIMARY + "; font-weight: bold; margin-bottom: 6px; }"
            + ".appreciation p { margin: 0; line-height: 1.5; font-size: 10pt; }"

            // Layout bilingue (FR + AR côte à côte)
            + ".bilingue-table { width: 100%; border-collapse: separate; border-spacing: 8px 0; margin-bottom: 18px; }"
            + ".bilingue-fr, .bilingue-ar { width: 50%; vertical-align: top; }"
            + ".bilingue-fr .appreciation, .bilingue-ar .appreciation { margin-bottom: 0; }"
            // Style spécifique arabe
            + ".appreciation-ar { border-left: none; border-right: 3px solid " + COLOR_PRIMARY + "; text-align: right; }"
            + ".appreciation-ar p { direction: rtl; text-align: right; font-size: 11pt; line-height: 1.7; "
            + "font-family: 'ArabicFont', 'Tahoma', sans-serif; }"
            + ".appreciation-title-ar { text-align: right; font-family: 'ArabicFont', 'Tahoma', sans-serif; }"

            + ".decision { font-size: 11pt; padding: 8px 0; }"

            + ".footer { position: running(footer); border-top: 1px solid #e5e7eb; padding-top: 8px; margin-top: 24px; font-size: 8pt; color: " + COLOR_MUTED + "; text-align: center; }"
            ;
    }

    private static String couleurMoyenne(double m) {
        if (m >= 16) return "#16a34a";
        if (m >= 12) return "#2563eb";
        if (m >= 10) return "#7c3aed";
        if (m >= 8)  return "#f59e0b";
        return "#dc2626";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
