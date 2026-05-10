package edu.edunova.entities;

import java.sql.Time;

/**
 * Domain entity representing a single timetable slot ({@code seance}).
 * <p>
 * Maps to table {@code seance}. Foreign keys are stored as integers
 * ({@link #classeId}, {@link #matiereId}, {@link #teacherId}) while the
 * denormalised display fields ({@link #classeNom}, {@link #matiereNom},
 * {@link #teacherNom}) are populated by JOIN queries to avoid additional
 * round-trips when rendering tables.
 */
public class Seance {

    /** Day of week ({@code "LUNDI"}, {@code "MARDI"}, ..., {@code "SAMEDI"}). */
    private String jour;
    private int id;
    private Time heureDebut;
    private Time heureFin;
    private String salle;
    /** Course delivery mode ({@code "PRESENTIEL"} or {@code "DISTANCIEL"}). */
    private String typeCours;
    private String anneeScolaire;
    private int classeId;
    private int matiereId;
    private int teacherId;

    /** Resolved display name of the class (populated via JOIN, read-only). */
    private String classeNom;
    /** Resolved display name of the subject (populated via JOIN, read-only). */
    private String matiereNom;
    /** Resolved full name of the teacher (populated via JOIN, read-only). */
    private String teacherNom;

    /** Default constructor required by frameworks relying on reflection. */
    public Seance() {
    }

    /**
     * Builds a {@code Seance} without identifier (insert use case).
     */
    public Seance(String jour, Time heureDebut, Time heureFin,
                  String salle, String typeCours, String anneeScolaire,
                  int classeId, int matiereId, int teacherId) {
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
        this.typeCours = typeCours;
        this.anneeScolaire = anneeScolaire;
        this.classeId = classeId;
        this.matiereId = matiereId;
        this.teacherId = teacherId;
    }

    /**
     * Builds a fully-initialised {@code Seance} (read use case).
     */
    public Seance(int id, String jour, Time heureDebut, Time heureFin,
                  String salle, String typeCours, String anneeScolaire,
                  int classeId, int matiereId, int teacherId) {
        this.id = id;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
        this.typeCours = typeCours;
        this.anneeScolaire = anneeScolaire;
        this.classeId = classeId;
        this.matiereId = matiereId;
        this.teacherId = teacherId;
    }

    public int getId() { return id; }
    public String getJour() { return jour; }
    public Time getHeureDebut() { return heureDebut; }
    public Time getHeureFin() { return heureFin; }
    public String getSalle() { return salle; }
    public String getTypeCours() { return typeCours; }
    public String getAnneeScolaire() { return anneeScolaire; }
    public int getClasseId() { return classeId; }
    public int getMatiereId() { return matiereId; }
    public int getTeacherId() { return teacherId; }
    public String getClasseNom() { return classeNom; }
    public String getMatiereNom() { return matiereNom; }
    public String getTeacherNom() { return teacherNom; }

    public void setId(int id) { this.id = id; }
    public void setJour(String jour) { this.jour = jour; }
    public void setHeureDebut(Time heureDebut) { this.heureDebut = heureDebut; }
    public void setHeureFin(Time heureFin) { this.heureFin = heureFin; }
    public void setSalle(String salle) { this.salle = salle; }
    public void setTypeCours(String typeCours) { this.typeCours = typeCours; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    public void setClasseId(int classeId) { this.classeId = classeId; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    public void setTeacherNom(String teacherNom) { this.teacherNom = teacherNom; }

    @Override
    public String toString() {
        return jour + " " + heureDebut + "-" + heureFin
                + " | " + (matiereNom != null ? matiereNom : "Mat#" + matiereId)
                + " | " + (classeNom != null ? classeNom : "Cl#" + classeId)
                + " | " + (teacherNom != null ? teacherNom : "Prof#" + teacherId)
                + " | salle " + salle;
    }
}
