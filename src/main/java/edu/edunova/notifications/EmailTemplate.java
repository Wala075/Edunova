package edu.edunova.notifications;

import edu.edunova.entities.Alerte;
import edu.edunova.entities.Bulletin;
import edu.edunova.entities.Bulletin.BulletinLigne;

/**
 * Templates HTML pour les emails Brevo.
 *
 * Tous les templates sont auto-suffisants (CSS inline) car la majorité
 * des clients mail (Gmail, Outlook, etc.) ne supportent pas les <style> externes.
 */
public class EmailTemplate {

    private static final String COLOR_PRIMARY  = "#7c3aed"; // violet
    private static final String COLOR_DANGER   = "#dc2626"; // rouge
    private static final String COLOR_SUCCESS  = "#16a34a"; // vert
    private static final String COLOR_TEXT     = "#1f2937";
    private static final String COLOR_MUTED    = "#6b7280";
    private static final String COLOR_BG       = "#f9fafb";

    private EmailTemplate() {}

    // ============================================================
    // 1. BULLETIN TRIMESTRIEL
    // ============================================================
    public static String bulletinHtml(Bulletin b, String appreciationIA) {
        StringBuilder rows = new StringBuilder();
        if (b.getLignes() != null) {
            for (BulletinLigne l : b.getLignes()) {
                rows.append("<tr>")
                        .append("<td style='padding:10px 14px;border-bottom:1px solid #eee;'>")
                            .append(esc(l.getMatiere())).append("</td>")
                        .append("<td style='padding:10px 14px;border-bottom:1px solid #eee;text-align:center;'>")
                            .append(l.getNbNotes()).append("</td>")
                        .append("<td style='padding:10px 14px;border-bottom:1px solid #eee;text-align:right;font-weight:600;'>")
                            .append(String.format("%.2f / 20", l.getMoyenne())).append("</td>")
                        .append("<td style='padding:10px 14px;border-bottom:1px solid #eee;color:")
                            .append(couleurMoyenne(l.getMoyenne())).append(";font-weight:600;'>")
                            .append(esc(l.getAppreciation())).append("</td>")
                        .append("</tr>");
            }
        }

        String appBlock = "";
        if (appreciationIA != null && !appreciationIA.isBlank()) {
            appBlock =
                    "<div style='background:#faf5ff;border-left:4px solid " + COLOR_PRIMARY + ";" +
                    "padding:16px 20px;margin:24px 0;border-radius:6px;'>" +
                        "<div style='font-size:13px;color:" + COLOR_PRIMARY + ";font-weight:600;margin-bottom:8px;'>" +
                            "✨ APPRÉCIATION DU PROFESSEUR PRINCIPAL" +
                        "</div>" +
                        "<p style='margin:0;color:" + COLOR_TEXT + ";line-height:1.6;'>" + esc(appreciationIA) + "</p>" +
                    "</div>";
        }

        String studentName = b.getStudent() == null ? "—"
                : esc(b.getStudent().getNomComplet().trim());

        return wrap(
                "Bulletin trimestriel — Trimestre " + b.getTrimestre(),
                COLOR_PRIMARY,
                "📊",
                "Bulletin scolaire",
                "Trimestre " + b.getTrimestre() + " · Année " + esc(b.getAnnee()),

                "<h2 style='margin:0 0 8px 0;color:" + COLOR_TEXT + ";'>" + studentName + "</h2>" +
                "<p style='margin:0 0 24px 0;color:" + COLOR_MUTED + ";'>Voici le bulletin scolaire de votre enfant.</p>" +

                "<div style='display:flex;gap:14px;margin-bottom:20px;'>" +
                    bigStat("MOYENNE GÉNÉRALE", String.format("%.2f / 20", b.getMoyenneGenerale()),
                            couleurMoyenne(b.getMoyenneGenerale())) +
                    bigStat("MENTION", esc(b.getMention()), COLOR_PRIMARY) +
                "</div>" +

                "<table cellspacing='0' cellpadding='0' style='width:100%;border-collapse:collapse;" +
                "background:white;border-radius:8px;overflow:hidden;border:1px solid #e5e7eb;margin-bottom:8px;'>" +
                    "<thead><tr style='background:" + COLOR_PRIMARY + ";color:white;'>" +
                        "<th style='padding:12px 14px;text-align:left;'>Matière</th>" +
                        "<th style='padding:12px 14px;text-align:center;'>Notes</th>" +
                        "<th style='padding:12px 14px;text-align:right;'>Moyenne</th>" +
                        "<th style='padding:12px 14px;text-align:left;'>Appréciation</th>" +
                    "</tr></thead>" +
                    "<tbody>" + rows.toString() + "</tbody>" +
                "</table>" +

                appBlock +

                "<p style='color:" + COLOR_MUTED + ";font-size:13px;margin-top:24px;'>" +
                    "<strong>Décision :</strong> " + esc(b.getDecision()) +
                "</p>"
        );
    }

    // ============================================================
    // 2. ALERTE CRITIQUE
    // ============================================================
    public static String alerteCritiqueHtml(Alerte a, String studentNom) {
        return wrap(
                "🚨 Alerte importante - " + (a.getTitre() == null ? "" : a.getTitre()),
                COLOR_DANGER,
                "🚨",
                "Alerte importante",
                "Concernant " + esc(studentNom == null ? "votre enfant" : studentNom),

                "<div style='background:#fef2f2;border-left:4px solid " + COLOR_DANGER + ";" +
                "padding:18px 22px;border-radius:6px;margin-bottom:24px;'>" +
                    "<h2 style='margin:0 0 10px 0;color:" + COLOR_DANGER + ";font-size:18px;'>" +
                        esc(a.getTitre()) + "</h2>" +
                    "<p style='margin:0;color:" + COLOR_TEXT + ";line-height:1.6;'>" +
                        esc(a.getMessage()) + "</p>" +
                "</div>" +

                "<p style='color:" + COLOR_TEXT + ";line-height:1.6;'>" +
                    "Madame, Monsieur,<br><br>" +
                    "L'équipe pédagogique d'EduNova vous informe d'une situation à surveiller " +
                    "concernant la scolarité de votre enfant. " +
                    "Nous vous invitons à prendre rendez-vous avec le professeur principal " +
                    "pour échanger sur les actions à mettre en place." +
                "</p>" +

                "<div style='background:#f3f4f6;padding:14px 18px;border-radius:6px;margin-top:24px;'>" +
                    "<div style='font-size:12px;color:" + COLOR_MUTED + ";font-weight:600;margin-bottom:4px;'>" +
                        "DÉTECTION AUTOMATIQUE" +
                    "</div>" +
                    "<div style='font-size:13px;color:" + COLOR_TEXT + ";'>" +
                        "Type : " + esc(a.getTypeAlerte() == null ? "" : a.getTypeAlerte().name()) + "<br>" +
                        "Sévérité : <strong style='color:" + COLOR_DANGER + ";'>CRITIQUE</strong>" +
                    "</div>" +
                "</div>"
        );
    }

    // ============================================================
    // 3. EXCELLENCE / FÉLICITATIONS
    // ============================================================
    public static String excellenceHtml(Alerte a, String studentNom) {
        return wrap(
                "🎉 Félicitations !",
                COLOR_SUCCESS,
                "🎉",
                "Félicitations !",
                "Bravo à " + esc(studentNom == null ? "votre enfant" : studentNom),

                "<div style='background:#f0fdf4;border-left:4px solid " + COLOR_SUCCESS + ";" +
                "padding:18px 22px;border-radius:6px;margin-bottom:24px;'>" +
                    "<h2 style='margin:0 0 10px 0;color:" + COLOR_SUCCESS + ";font-size:18px;'>" +
                        "🌟 " + esc(a.getTitre()) + "</h2>" +
                    "<p style='margin:0;color:" + COLOR_TEXT + ";line-height:1.6;'>" +
                        esc(a.getMessage()) + "</p>" +
                "</div>" +

                "<p style='color:" + COLOR_TEXT + ";line-height:1.6;'>" +
                    "Madame, Monsieur,<br><br>" +
                    "Nous sommes ravis de partager avec vous cette excellente nouvelle. " +
                    "Le travail et l'engagement de votre enfant méritent d'être soulignés. " +
                    "Toute l'équipe pédagogique tient à le/la féliciter chaleureusement et " +
                    "vous encourage à continuer de soutenir son investissement." +
                "</p>"
        );
    }

    // ============================================================
    // Wrapper commun (header + footer + structure responsive)
    // ============================================================
    private static String wrap(String docTitle, String color, String icon,
                               String headerTitle, String headerSubtitle,
                               String contentHtml) {
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='utf-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>" + esc(docTitle) + "</title>" +
                "</head>" +
                "<body style='margin:0;padding:0;background:" + COLOR_BG + ";" +
                "font-family:Segoe UI,Helvetica,Arial,sans-serif;color:" + COLOR_TEXT + ";'>" +

                "<table cellpadding='0' cellspacing='0' role='presentation' " +
                "style='width:100%;background:" + COLOR_BG + ";padding:30px 0;'><tr><td align='center'>" +

                "<table cellpadding='0' cellspacing='0' role='presentation' " +
                "style='width:600px;max-width:96%;background:white;border-radius:14px;" +
                "overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,0.06);'>" +

                "<tr><td style='background:" + color + ";color:white;padding:32px 32px 24px 32px;'>" +
                    "<div style='font-size:34px;line-height:1;'>" + icon + "</div>" +
                    "<h1 style='margin:8px 0 4px 0;font-size:24px;font-weight:700;'>" + esc(headerTitle) + "</h1>" +
                    "<div style='opacity:0.85;font-size:14px;'>" + esc(headerSubtitle) + "</div>" +
                "</td></tr>" +

                "<tr><td style='padding:30px 32px 8px 32px;'>" + contentHtml + "</td></tr>" +

                "<tr><td style='padding:24px 32px;border-top:1px solid #f0f0f0;background:#fafafa;" +
                "color:" + COLOR_MUTED + ";font-size:12px;'>" +
                    "Cet email est envoyé automatiquement par <strong>EduNova</strong>. " +
                    "Pour toute question, contactez l'établissement." +
                "</td></tr>" +

                "</table>" +
                "</td></tr></table>" +
                "</body></html>";
    }

    // ---- Helpers ----

    private static String bigStat(String label, String value, String color) {
        return "<div style='flex:1;background:white;border:1px solid #e5e7eb;border-radius:10px;" +
                "padding:14px 18px;'>" +
                "<div style='font-size:11px;color:" + COLOR_MUTED + ";font-weight:700;letter-spacing:0.6px;'>" +
                    esc(label) +
                "</div>" +
                "<div style='font-size:22px;font-weight:700;color:" + color + ";margin-top:4px;'>" +
                    esc(value) +
                "</div>" +
                "</div>";
    }

    private static String couleurMoyenne(double m) {
        if (m >= 16) return "#16a34a"; // vert
        if (m >= 12) return "#2563eb"; // bleu
        if (m >= 10) return "#7c3aed"; // violet
        if (m >= 8)  return "#f59e0b"; // orange
        return "#dc2626";              // rouge
    }

    /** Échappe les caractères HTML pour éviter l'injection. */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
