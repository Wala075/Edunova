package edu.edunova_a.entities;

import java.time.LocalTime;

public class Seance {

    private int id_se;
    private String jour_se;            // Lundi, Mardi, ...
    private LocalTime heure_debut_se;
    private LocalTime heure_fin_se;
    private String salle_se;
    private String type_cours_se;      // PRESENTIEL / ONLINE / HYBRIDE
    private java.time.LocalDate date_seance; // date de la séance
    private int classe_id;
    private int matiere_id;
    private int teacher_id;

    // Champs joints (lecture seule, pour l'affichage)
    private String classe_nom;
    private String matiere_nom;
    private String teacher_nom;

    public Seance() {}

    public Seance(int id_se, String jour_se, LocalTime heure_debut_se, LocalTime heure_fin_se,
                  String salle_se, String type_cours_se, java.time.LocalDate date_seance,
                  int classe_id, int matiere_id, int teacher_id) {
        this.id_se          = id_se;
        this.jour_se        = jour_se;
        this.heure_debut_se = heure_debut_se;
        this.heure_fin_se   = heure_fin_se;
        this.salle_se       = salle_se;
        this.type_cours_se  = type_cours_se;
        this.date_seance    = date_seance;
        this.classe_id      = classe_id;
        this.matiere_id     = matiere_id;
        this.teacher_id     = teacher_id;
    }

    public int getId_se() { return id_se; }
    public void setId_se(int id_se) { this.id_se = id_se; }

    public String getJour_se() { return jour_se; }
    public void setJour_se(String jour_se) { this.jour_se = jour_se; }

    public LocalTime getHeure_debut_se() { return heure_debut_se; }
    public void setHeure_debut_se(LocalTime heure_debut_se) { this.heure_debut_se = heure_debut_se; }

    public LocalTime getHeure_fin_se() { return heure_fin_se; }
    public void setHeure_fin_se(LocalTime heure_fin_se) { this.heure_fin_se = heure_fin_se; }

    public String getSalle_se() { return salle_se; }
    public void setSalle_se(String salle_se) { this.salle_se = salle_se; }

    public String getType_cours_se() { return type_cours_se; }
    public void setType_cours_se(String type_cours_se) { this.type_cours_se = type_cours_se; }

    public java.time.LocalDate getDate_seance() { return date_seance; }
    public void setDate_seance(java.time.LocalDate date_seance) { this.date_seance = date_seance; }

    public int getClasse_id() { return classe_id; }
    public void setClasse_id(int classe_id) { this.classe_id = classe_id; }

    public int getMatiere_id() { return matiere_id; }
    public void setMatiere_id(int matiere_id) { this.matiere_id = matiere_id; }

    public int getTeacher_id() { return teacher_id; }
    public void setTeacher_id(int teacher_id) { this.teacher_id = teacher_id; }

    public String getClasse_nom() { return classe_nom; }
    public void setClasse_nom(String classe_nom) { this.classe_nom = classe_nom; }

    public String getMatiere_nom() { return matiere_nom; }
    public void setMatiere_nom(String matiere_nom) { this.matiere_nom = matiere_nom; }

    public String getTeacher_nom() { return teacher_nom; }
    public void setTeacher_nom(String teacher_nom) { this.teacher_nom = teacher_nom; }

    @Override
    public String toString() {
        return "[" + jour_se + " " + heure_debut_se + "-" + heure_fin_se + "] "
                + (matiere_nom != null ? matiere_nom : "Matière #" + matiere_id)
                + " - " + (classe_nom != null ? classe_nom : "Classe #" + classe_id)
                + (date_seance != null ? " (" + date_seance + ")" : "");
    }
}
