package edu.edunova.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Aggregated workload statistics for a single teacher over the current school
 * year. Instances are produced by the analysis service from raw {@link Seance}
 * rows; no direct persistence is involved.
 * <p>
 * The class also encodes the workload classification thresholds
 * ({@link #SEUIL_SOUS_UTILISE}, {@link #SEUIL_NORMAL_MAX}, {@link #SEUIL_CHARGE_MAX})
 * exposed through {@link #getNiveauCharge()}.
 */
public class TeacherStat {

    /** Strict upper bound of the under-utilised band (hours/week). */
    public static final int SEUIL_SOUS_UTILISE = 6;
    /** Inclusive upper bound of the normal band (hours/week). */
    public static final int SEUIL_NORMAL_MAX   = 18;
    /** Inclusive upper bound of the heavy-load band (hours/week). Beyond is overload. */
    public static final int SEUIL_CHARGE_MAX   = 25;

    private final Teacher teacher;
    private final List<Seance> seances = new ArrayList<>();
    private final Set<Integer> classes = new HashSet<>();
    private final Set<Integer> matieres = new HashSet<>();
    private final Set<String> jours = new HashSet<>();
    private int totalMinutes = 0;
    private final List<String> conflits = new ArrayList<>();

    /**
     * @param teacher  underlying teacher; required, immutable for the lifetime
     *                 of the stat object
     */
    public TeacherStat(Teacher teacher) {
        this.teacher = teacher;
    }

    public Teacher getTeacher() { return teacher; }
    public List<Seance> getSeances() { return seances; }
    public int getNbSeances() { return seances.size(); }
    public int getNbClasses() { return classes.size(); }
    public int getNbMatieres() { return matieres.size(); }
    public int getNbJours() { return jours.size(); }
    public int getTotalMinutes() { return totalMinutes; }
    public double getTotalHeures() { return totalMinutes / 60.0; }
    public List<String> getConflits() { return conflits; }
    public boolean hasConflits() { return !conflits.isEmpty(); }

    /**
     * Registers a session into this aggregate, updating distinct sets
     * (classes, subjects, days) and the cumulative duration.
     *
     * @param s  session to add; ignored fields are tolerated as {@code null}
     */
    public void addSeance(Seance s) {
        seances.add(s);
        classes.add(s.getClasseId());
        matieres.add(s.getMatiereId());
        if (s.getJour() != null) jours.add(s.getJour());
        if (s.getHeureDebut() != null && s.getHeureFin() != null) {
            long min = (s.getHeureFin().getTime() - s.getHeureDebut().getTime()) / 60000;
            totalMinutes += (int) min;
        }
    }

    /**
     * Records a scheduling conflict description for later reporting.
     *
     * @param description human-readable conflict description
     */
    public void addConflit(String description) {
        conflits.add(description);
    }

    /**
     * Classifies the teacher's weekly load.
     *
     * @return one of {@code "SOUS_UTILISE"}, {@code "NORMAL"}, {@code "CHARGE"},
     *         {@code "SURCHARGE"}
     */
    public String getNiveauCharge() {
        double h = getTotalHeures();
        if (h < SEUIL_SOUS_UTILISE) return "SOUS_UTILISE";
        if (h <= SEUIL_NORMAL_MAX) return "NORMAL";
        if (h <= SEUIL_CHARGE_MAX) return "CHARGE";
        return "SURCHARGE";
    }

    /**
     * Returns a localised display label for the workload classification.
     *
     * @return French label suitable for table cells
     */
    public String getNiveauChargeLibelle() {
        switch (getNiveauCharge()) {
            case "SOUS_UTILISE": return "Sous-utilisé";
            case "NORMAL":       return "Normal";
            case "CHARGE":       return "Chargé";
            case "SURCHARGE":    return "Surchargé";
            default:             return "—";
        }
    }
}
