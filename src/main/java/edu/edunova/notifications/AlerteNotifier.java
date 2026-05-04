package edu.edunova.notifications;

import edu.edunova.entities.Alerte;
import edu.edunova.entities.Alerte.Severite;
import edu.edunova.entities.Alerte.TypeAlerte;
import edu.edunova.entities.Student;
import edu.edunova.notifications.BrevoService.SendResult;
import edu.edunova.services.AlerteService;
import edu.edunova.services.StudentService;

import java.util.List;

/**
 * Pont entre les alertes et les notifications (email Brevo).
 * Décide quand un email doit partir et quel template utiliser.
 *
 * RÈGLES :
 *   - Sévérité CRITIQUE => email "Alerte importante" aux parents
 *   - Type EXCELLENCE   => email "Félicitations" aux parents
 *   - Si email_parent vide => aucun envoi (silencieux)
 *   - Si déjà envoyé (email_envoye=true) => skip (pas de spam)
 */
public class AlerteNotifier {

    private final BrevoService brevo = new BrevoService();
    private final AlerteService alerteService = new AlerteService();
    private final StudentService studentService = new StudentService();
    private final EmailLogService logService = new EmailLogService();

    /** Résultat d'une session de notification. */
    public static class NotifyReport {
        public int sentEmails    = 0;
        public int skippedNoMail = 0;
        public int skippedAlready = 0;
        public int failures      = 0;

        @Override
        public String toString() {
            return String.format("Notifications: %d emails envoyés, " +
                            "%d skip (pas d'email), %d skip (déjà envoyé), %d échecs",
                    sentEmails, skippedNoMail, skippedAlready, failures);
        }
    }

    /**
     * Parcourt les alertes et envoie les emails appropriés.
     * À appeler APRÈS un scan, sur la liste des alertes détectées.
     *
     * @param alertes liste d'alertes à traiter (peut être null)
     */
    public NotifyReport notifierBatch(List<Alerte> alertes) {
        NotifyReport r = new NotifyReport();
        if (alertes == null) return r;

        for (Alerte a : alertes) {
            if (!doitEnvoyerEmail(a)) continue;

            // Récupère l'alerte fraîche depuis la DB pour avoir l'id_a et les flags à jour
            Alerte stored = retrouverAlerteEnDb(a);
            if (stored == null) continue; // pas (encore) en DB
            if (stored.isEmailEnvoye()) {
                r.skippedAlready++;
                continue;
            }

            // Récupère l'email parent
            Student s = studentService.findById(stored.getStudentId());
            if (s == null || s.getEmail_parent() == null || s.getEmail_parent().isBlank()) {
                r.skippedNoMail++;
                continue;
            }

            String html;
            String subject;
            if (stored.getSeverite() == Severite.CRITIQUE) {
                subject = "🚨 Alerte importante - " + s.getNomComplet().trim();
                html = EmailTemplate.alerteCritiqueHtml(stored, s.getNomComplet().trim());
            } else if (stored.getTypeAlerte() == TypeAlerte.EXCELLENCE) {
                subject = "🎉 Félicitations - " + s.getNomComplet().trim();
                html = EmailTemplate.excellenceHtml(stored, s.getNomComplet().trim());
            } else {
                continue; // pas un type qui déclenche un email
            }

            String to = s.getEmail_parent().trim();
            SendResult res = brevo.sendEmail(to, "Parent de " + s.getNomComplet().trim(),
                    subject, html);

            // Log
            logService.log(to, subject, res.success,
                    res.success ? null : res.errorMessage,
                    stored.getId_a(), s.getId_s());

            if (res.success) {
                alerteService.marquerEmailEnvoye(stored.getId_a());
                r.sentEmails++;
            } else {
                r.failures++;
                System.err.println("AlerteNotifier échec : " + res.errorMessage);
            }
        }
        return r;
    }

    // ============================================================
    // Helpers
    // ============================================================

    /** Doit-on envoyer un email pour cette alerte ? */
    private boolean doitEnvoyerEmail(Alerte a) {
        if (a == null) return false;
        if (a.getStudentId() <= 0) return false; // alerte globale (matière classe) => pas de parent
        if (a.getSeverite() == Severite.CRITIQUE) return true;
        if (a.getTypeAlerte() == TypeAlerte.EXCELLENCE) return true;
        return false;
    }

    /**
     * Récupère l'alerte en DB en se basant sur student + type + année + matière + trimestre.
     * Nécessaire car les alertes en mémoire (sortie scan) n'ont pas encore l'id_a après insert
     * via addIfNotExists (qui retourne 0 en cas de doublon).
     */
    private Alerte retrouverAlerteEnDb(Alerte a) {
        for (Alerte db : alerteService.getByStudent(a.getStudentId())) {
            if (db.getTypeAlerte() == a.getTypeAlerte()
                    && eq(db.getMatiereId(), a.getMatiereId())
                    && eq(db.getTrimestre(), a.getTrimestre())
                    && safe(db.getAnneeScolaire()).equals(safe(a.getAnneeScolaire()))) {
                return db;
            }
        }
        return null;
    }

    private static boolean eq(Object x, Object y) {
        if (x == null && y == null) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
