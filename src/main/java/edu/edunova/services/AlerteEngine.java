package edu.edunova.services;

import edu.edunova.entities.Alerte;
import edu.edunova.entities.Alerte.Severite;
import edu.edunova.entities.Alerte.TypeAlerte;
import edu.edunova.entities.Student;
import edu.edunova.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Le "cerveau" du système d'alerte intelligent.
 *
 * Implémente 7 règles métier qui scannent les notes pour générer
 * automatiquement des alertes. Évite les doublons via {@link AlerteService#addIfNotExists}.
 *
 * Usage typique :
 *   AlerteEngine engine = new AlerteEngine();
 *   ScanReport r = engine.scanComplet("2025-2026");
 *   System.out.println(r.totalDetectees() + " alertes détectées");
 */
public class AlerteEngine {

    private final Connection cnx = MyConnection.getInstance().getCnx();
    private final AlerteService alerteService = new AlerteService();
    private final StudentService studentService = new StudentService();
    private final NoteService noteService = new NoteService();

    // ============================================================
    // SEUILS (constantes métier)
    // ============================================================
    public static final double SEUIL_MOYENNE_FAIBLE   = 8.0;
    public static final double SEUIL_ECHEC_MATIERE    = 6.0;
    public static final double SEUIL_EXCELLENCE       = 16.0;
    public static final double SEUIL_CHUTE_POINTS     = 2.0;   // -2 pts entre trimestres
    public static final double SEUIL_PROGRESSION_PTS  = 2.0;   // +2 pts entre trimestres
    public static final double SEUIL_NOTE_ANORMALE    = 4.0;
    public static final double SEUIL_BIEN_EN_MATIERE  = 12.0;  // pour détecter note anormale
    public static final double SEUIL_CLASSE_CRITIQUE  = 10.0;

    // ============================================================
    // RÉSULTAT D'UN SCAN
    // ============================================================

    /** Résumé d'un scan, retourné à l'UI pour affichage. */
    public static class ScanReport {
        public int eleves       = 0;
        public int detectees    = 0;
        public int inserees     = 0;
        public int doublonsIgnores = 0;
        public int critiques    = 0;
        public int moyennes     = 0;
        public int positives    = 0;
        public int infos        = 0;
        public List<Alerte> alertes = new ArrayList<>();

        public int totalDetectees() { return detectees; }

        @Override
        public String toString() {
            return String.format(
                    "Scan: %d élèves analysés | %d alertes (%d nouvelles, %d doublons) | " +
                            "🔴 %d critiques  🟠 %d moyennes  🟢 %d positives  🔵 %d infos",
                    eleves, detectees, inserees, doublonsIgnores,
                    critiques, moyennes, positives, infos);
        }
    }

    // ============================================================
    // ENTRÉES PUBLIQUES
    // ============================================================

    /** Scan toute l'école pour une année donnée. */
    public ScanReport scanComplet(String annee) {
        ScanReport report = new ScanReport();
        List<Student> students = studentService.getData();

        for (Student s : students) {
            report.eleves++;
            List<Alerte> alertes = analyserEleve(s.getId_s(), annee);
            for (Alerte a : alertes) {
                accumuler(report, a);
                int id = alerteService.addIfNotExists(a);
                if (id > 0) report.inserees++;
                else if (id == 0) report.doublonsIgnores++;
            }
        }

        // Règles "globales" (au niveau classe/matière, pas par élève)
        for (Alerte a : detecterMatieresCritiquesClasse(annee)) {
            accumuler(report, a);
            int id = alerteService.addIfNotExists(a);
            if (id > 0) report.inserees++;
            else if (id == 0) report.doublonsIgnores++;
        }

        return report;
    }

    /** Analyse complète d'un élève (toutes les règles individuelles). */
    public List<Alerte> analyserEleve(int studentId, String annee) {
        List<Alerte> all = new ArrayList<>();
        all.addAll(detecterMoyenneFaibleOuExcellence(studentId, annee));
        all.addAll(detecterEchecMatiere(studentId, annee));
        all.addAll(detecterChuteOuProgressionEntreTrimestres(studentId, annee));
        all.addAll(detecterNoteAnormale(studentId, annee));
        return all;
    }

    // ============================================================
    // RÈGLE 1 + 5 : MOYENNE FAIBLE (<8) ou EXCELLENCE (>=16)
    // ============================================================

    public List<Alerte> detecterMoyenneFaibleOuExcellence(int studentId, String annee) {
        List<Alerte> out = new ArrayList<>();
        Double moy = moyenneGenerale(studentId, annee);
        if (moy == null) return out;

        String nom = nomEleve(studentId);

        if (moy < SEUIL_MOYENNE_FAIBLE) {
            Alerte a = new Alerte(
                    TypeAlerte.MOYENNE_FAIBLE,
                    Severite.CRITIQUE,
                    "Moyenne générale en difficulté",
                    String.format("L'élève %s a une moyenne générale de %.2f/20 (seuil critique < %.0f). " +
                                    "Une intervention pédagogique est recommandée.",
                            nom, moy, SEUIL_MOYENNE_FAIBLE),
                    studentId,
                    annee);
            out.add(a);
        } else if (moy >= SEUIL_EXCELLENCE) {
            Alerte a = new Alerte(
                    TypeAlerte.EXCELLENCE,
                    Severite.POSITIVE,
                    "Élève en excellence",
                    String.format("Bravo ! %s atteint une moyenne générale de %.2f/20. À encourager.",
                            nom, moy),
                    studentId,
                    annee);
            out.add(a);
        }
        return out;
    }

    // ============================================================
    // RÈGLE 2 : ÉCHEC MATIÈRE (moyenne matière < 6)
    // ============================================================

    public List<Alerte> detecterEchecMatiere(int studentId, String annee) {
        List<Alerte> out = new ArrayList<>();
        String q = "SELECT m.id_m, m.nom_m, " +
                "       SUM(n.valeur * n.coefficient) / SUM(n.coefficient) AS moy " +
                "FROM note n JOIN matiere m ON m.id_m = n.matiere_id " +
                "WHERE n.student_id = ? AND n.annee_scolaire = ? " +
                "GROUP BY m.id_m, m.nom_m " +
                "HAVING moy < ?";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setInt(1, studentId);
            pst.setString(2, annee);
            pst.setDouble(3, SEUIL_ECHEC_MATIERE);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int matId = rs.getInt("id_m");
                    String matNom = rs.getString("nom_m");
                    double moy = rs.getDouble("moy");
                    String nom = nomEleve(studentId);

                    Alerte a = new Alerte(
                            TypeAlerte.ECHEC_MATIERE,
                            Severite.CRITIQUE,
                            "Échec en " + matNom,
                            String.format("%s a une moyenne de %.2f/20 en %s (seuil critique < %.0f). " +
                                            "Soutien à mettre en place.",
                                    nom, moy, matNom, SEUIL_ECHEC_MATIERE),
                            studentId,
                            annee);
                    a.setMatiereId(matId);
                    out.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("AlerteEngine.detecterEchecMatiere : " + e.getMessage());
        }
        return out;
    }

    // ============================================================
    // RÈGLE 3 + 4 : CHUTE / PROGRESSION ENTRE TRIMESTRES
    // ============================================================

    public List<Alerte> detecterChuteOuProgressionEntreTrimestres(int studentId, String annee) {
        List<Alerte> out = new ArrayList<>();
        Double t1 = moyenneTrimestre(studentId, 1, annee);
        Double t2 = moyenneTrimestre(studentId, 2, annee);
        Double t3 = moyenneTrimestre(studentId, 3, annee);

        out.addAll(comparerTrimestres(studentId, annee, t1, t2, 1, 2));
        out.addAll(comparerTrimestres(studentId, annee, t2, t3, 2, 3));
        return out;
    }

    private List<Alerte> comparerTrimestres(int studentId, String annee,
                                            Double moyAvant, Double moyApres,
                                            int triAvant, int triApres) {
        List<Alerte> out = new ArrayList<>();
        if (moyAvant == null || moyApres == null) return out;

        double diff = moyApres - moyAvant;
        String nom = nomEleve(studentId);

        if (diff <= -SEUIL_CHUTE_POINTS) {
            Alerte a = new Alerte(
                    TypeAlerte.CHUTE_NIVEAU,
                    Severite.MOYENNE,
                    "Chute du niveau (T" + triAvant + " → T" + triApres + ")",
                    String.format("%s : la moyenne est passée de %.2f/20 (T%d) à %.2f/20 (T%d) (-%.2f points).",
                            nom, moyAvant, triAvant, moyApres, triApres, Math.abs(diff)),
                    studentId,
                    annee);
            a.setTrimestre(triApres);
            out.add(a);
        } else if (diff >= SEUIL_PROGRESSION_PTS) {
            Alerte a = new Alerte(
                    TypeAlerte.PROGRESSION_POSITIVE,
                    Severite.POSITIVE,
                    "Belle progression (T" + triAvant + " → T" + triApres + ")",
                    String.format("Bravo %s ! Moyenne en hausse de +%.2f points entre T%d (%.2f) et T%d (%.2f).",
                            nom, diff, triAvant, moyAvant, triApres, moyApres),
                    studentId,
                    annee);
            a.setTrimestre(triApres);
            out.add(a);
        }
        return out;
    }

    // ============================================================
    // RÈGLE 6 : MATIÈRE CRITIQUE AU NIVEAU CLASSE
    // ============================================================

    public List<Alerte> detecterMatieresCritiquesClasse(String annee) {
        List<Alerte> out = new ArrayList<>();
        String q = "SELECT m.id_m, m.nom_m, AVG(n.valeur) AS moy, COUNT(DISTINCT n.student_id) AS nb " +
                "FROM note n JOIN matiere m ON m.id_m = n.matiere_id " +
                "WHERE n.annee_scolaire = ? " +
                "GROUP BY m.id_m, m.nom_m " +
                "HAVING moy < ? AND nb >= 2";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setString(1, annee);
            pst.setDouble(2, SEUIL_CLASSE_CRITIQUE);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int matId = rs.getInt("id_m");
                    String matNom = rs.getString("nom_m");
                    double moy = rs.getDouble("moy");
                    int nb = rs.getInt("nb");

                    Alerte a = new Alerte(
                            TypeAlerte.MATIERE_CRITIQUE_CLASSE,
                            Severite.MOYENNE,
                            "Difficultés générales en " + matNom,
                            String.format("Moyenne classe en %s : %.2f/20 (%d élèves). " +
                                            "Réviser la pédagogie de cette matière.",
                                    matNom, moy, nb),
                            0,
                            annee);
                    a.setMatiereId(matId);
                    out.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("AlerteEngine.detecterMatieresCritiquesClasse : " + e.getMessage());
        }
        return out;
    }

    // ============================================================
    // RÈGLE 7 : NOTE ANORMALE (note < 4 sur matière où l'élève va bien >=12)
    // ============================================================

    public List<Alerte> detecterNoteAnormale(int studentId, String annee) {
        List<Alerte> out = new ArrayList<>();
        // 1. Trouver les matières où l'élève a une moyenne globale >= SEUIL_BIEN_EN_MATIERE
        // 2. Vérifier s'il a une note < SEUIL_NOTE_ANORMALE dans ces matières
        String q = "SELECT m.id_m, m.nom_m, " +
                "       SUM(n.valeur * n.coefficient) / SUM(n.coefficient) AS moy_mat, " +
                "       MIN(n.valeur) AS min_note " +
                "FROM note n JOIN matiere m ON m.id_m = n.matiere_id " +
                "WHERE n.student_id = ? AND n.annee_scolaire = ? " +
                "GROUP BY m.id_m, m.nom_m " +
                "HAVING moy_mat >= ? AND min_note < ?";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setInt(1, studentId);
            pst.setString(2, annee);
            pst.setDouble(3, SEUIL_BIEN_EN_MATIERE);
            pst.setDouble(4, SEUIL_NOTE_ANORMALE);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int matId = rs.getInt("id_m");
                    String matNom = rs.getString("nom_m");
                    double moyMat = rs.getDouble("moy_mat");
                    double minNote = rs.getDouble("min_note");
                    String nom = nomEleve(studentId);

                    Alerte a = new Alerte(
                            TypeAlerte.NOTE_ANORMALE,
                            Severite.INFO,
                            "Note isolée à vérifier en " + matNom,
                            String.format("%s a une note de %.2f/20 en %s alors que sa moyenne y est de %.2f/20. " +
                                            "Vérifier la copie ou un évènement particulier.",
                                    nom, minNote, matNom, moyMat),
                            studentId,
                            annee);
                    a.setMatiereId(matId);
                    out.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("AlerteEngine.detecterNoteAnormale : " + e.getMessage());
        }
        return out;
    }

    // ============================================================
    // Helpers SQL
    // ============================================================

    /** Moyenne générale d'un élève (toutes matières, tous trimestres) sur une année. */
    private Double moyenneGenerale(int studentId, String annee) {
        // moyenne des moyennes par matière (cohérent avec BulletinService)
        String q = "SELECT AVG(moy) FROM ( " +
                "  SELECT SUM(valeur * coefficient) / SUM(coefficient) AS moy " +
                "  FROM note WHERE student_id = ? AND annee_scolaire = ? " +
                "  GROUP BY matiere_id " +
                ") sub";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setInt(1, studentId);
            pst.setString(2, annee);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    return rs.wasNull() ? null : v;
                }
            }
        } catch (SQLException e) {
            System.err.println("AlerteEngine.moyenneGenerale : " + e.getMessage());
        }
        return null;
    }

    /** Moyenne générale d'un élève pour un trimestre précis. */
    private Double moyenneTrimestre(int studentId, int trimestre, String annee) {
        String q = "SELECT AVG(moy) FROM ( " +
                "  SELECT SUM(valeur * coefficient) / SUM(coefficient) AS moy " +
                "  FROM note WHERE student_id = ? AND annee_scolaire = ? AND trimestre = ? " +
                "  GROUP BY matiere_id " +
                ") sub";
        try (PreparedStatement pst = cnx.prepareStatement(q)) {
            pst.setInt(1, studentId);
            pst.setString(2, annee);
            pst.setInt(3, trimestre);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    return rs.wasNull() ? null : v;
                }
            }
        } catch (SQLException e) {
            System.err.println("AlerteEngine.moyenneTrimestre : " + e.getMessage());
        }
        return null;
    }

    private String nomEleve(int studentId) {
        Student s = studentService.findById(studentId);
        return s == null ? ("élève #" + studentId) : s.getNomComplet().trim();
    }

    private void accumuler(ScanReport r, Alerte a) {
        r.detectees++;
        r.alertes.add(a);
        if (a.getSeverite() == null) return;
        switch (a.getSeverite()) {
            case CRITIQUE: r.critiques++; break;
            case MOYENNE:  r.moyennes++;  break;
            case POSITIVE: r.positives++; break;
            case INFO:     r.infos++;     break;
        }
    }
}
