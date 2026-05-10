package edu.edunova.services;

import edu.edunova.entities.Seance;
import edu.edunova.entities.Teacher;
import edu.edunova.entities.TeacherStat;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AnalyseEmploiService {

    private final SeanceService seanceService = new SeanceService();
    private final TeacherService teacherService = new TeacherService();


    public List<TeacherStat> genererEmploisEnseignants() {
        List<Teacher> teachers = teacherService.getData();
        List<Seance> seances = seanceService.getData();

        Map<Integer, TeacherStat> stats = new HashMap<>();
        for (Teacher t : teachers) {
            stats.put(t.getId(), new TeacherStat(t));
        }

        for (Seance s : seances) {
            TeacherStat ts = stats.get(s.getTeacherId());
            if (ts != null) ts.addSeance(s);
        }

        for (TeacherStat ts : stats.values()) {
            detecterConflitsHoraires(ts);
        }

        List<TeacherStat> result = new ArrayList<>(stats.values());
        result.sort(Comparator.comparingDouble(TeacherStat::getTotalHeures).reversed());
        return result;
    }


    private void detecterConflitsHoraires(TeacherStat ts) {
        List<Seance> seances = ts.getSeances();
        for (int i = 0; i < seances.size(); i++) {
            for (int j = i + 1; j < seances.size(); j++) {
                Seance a = seances.get(i);
                Seance b = seances.get(j);
                if (!a.getJour().equals(b.getJour())) continue;

                if (chevauche(a.getHeureDebut(), a.getHeureFin(),
                              b.getHeureDebut(), b.getHeureFin())) {
                    ts.addConflit(
                            a.getJour() + " " + format(a.getHeureDebut()) + "-" + format(a.getHeureFin())
                            + " (" + a.getClasseNom() + ")  ↔  "
                            + format(b.getHeureDebut()) + "-" + format(b.getHeureFin())
                            + " (" + b.getClasseNom() + ")"
                    );
                }
            }
        }
    }


    private boolean chevauche(Time aDeb, Time aFin, Time bDeb, Time bFin) {
        if (aDeb == null || aFin == null || bDeb == null || bFin == null) return false;
        return aDeb.before(bFin) && bDeb.before(aFin);
    }


    private String format(Time t) {
        return t == null ? "" : t.toString().substring(0, 5);
    }


    public List<String> genererRecommendations(List<TeacherStat> stats) {
        List<String> recs = new ArrayList<>();

        if (stats.isEmpty()) {
            recs.add("ℹ Aucun enseignant trouvé dans la base.");
            return recs;
        }

        int totalConflits = 0;
        for (TeacherStat ts : stats) totalConflits += ts.getConflits().size();
        if (totalConflits > 0) {
            recs.add("⚠ " + totalConflits + " conflit(s) horaire(s) détecté(s) — un enseignant ne peut pas être à 2 endroits en même temps.");
        }

        List<TeacherStat> surcharges = new ArrayList<>();
        for (TeacherStat ts : stats) {
            if ("SURCHARGE".equals(ts.getNiveauCharge())) surcharges.add(ts);
        }
        if (!surcharges.isEmpty()) {
            StringBuilder sb = new StringBuilder("🔴 Enseignants surchargés (> "
                    + TeacherStat.SEUIL_CHARGE_MAX + "h/sem) : ");
            for (int i = 0; i < surcharges.size(); i++) {
                if (i > 0) sb.append(", ");
                Teacher t = surcharges.get(i).getTeacher();
                sb.append(t.getPrenom()).append(" ").append(t.getNom())
                  .append(" (").append(String.format("%.1f", surcharges.get(i).getTotalHeures())).append("h)");
            }
            recs.add(sb.toString());
        }

        List<TeacherStat> sousUtilises = new ArrayList<>();
        for (TeacherStat ts : stats) {
            if ("SOUS_UTILISE".equals(ts.getNiveauCharge()) && ts.getNbSeances() > 0) {
                sousUtilises.add(ts);
            }
        }
        if (!sousUtilises.isEmpty()) {
            StringBuilder sb = new StringBuilder("🟡 Enseignants sous-utilisés (< "
                    + TeacherStat.SEUIL_SOUS_UTILISE + "h/sem) : ");
            for (int i = 0; i < sousUtilises.size(); i++) {
                if (i > 0) sb.append(", ");
                Teacher t = sousUtilises.get(i).getTeacher();
                sb.append(t.getPrenom()).append(" ").append(t.getNom())
                  .append(" (").append(String.format("%.1f", sousUtilises.get(i).getTotalHeures())).append("h)");
            }
            recs.add(sb.toString());
        }

        List<Teacher> sansSeances = new ArrayList<>();
        for (TeacherStat ts : stats) {
            if (ts.getNbSeances() == 0) sansSeances.add(ts.getTeacher());
        }
        if (!sansSeances.isEmpty()) {
            StringBuilder sb = new StringBuilder("⚪ Sans aucune séance assignée : ");
            for (int i = 0; i < sansSeances.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(sansSeances.get(i).getPrenom()).append(" ").append(sansSeances.get(i).getNom());
            }
            recs.add(sb.toString());
        }

        if (!surcharges.isEmpty() && !sousUtilises.isEmpty()) {
            recs.add("💡 Suggestion : transférer des séances des enseignants surchargés "
                    + "vers les enseignants sous-utilisés pour mieux équilibrer la charge.");
        }

        if (recs.isEmpty()) {
            recs.add("✓ Charge équilibrée et aucun conflit détecté. Bravo !");
        }

        return recs;
    }


    public double moyenneHeures(List<TeacherStat> stats) {
        if (stats.isEmpty()) return 0;
        double total = 0;
        int count = 0;
        for (TeacherStat ts : stats) {
            if (ts.getNbSeances() > 0) {
                total += ts.getTotalHeures();
                count++;
            }
        }
        return count > 0 ? total / count : 0;
    }
}
