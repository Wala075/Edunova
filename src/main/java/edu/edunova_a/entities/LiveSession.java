package edu.edunova_a.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class LiveSession {

    private int id_ls;
    private String titre_ls;
    private LocalDate date_session_ls;
    private LocalTime heure_debut_ls;
    private LocalTime heure_fin_ls;
    private String lien_meet_ls;
    private String statut_ls;          // PROGRAMMEE / EN_COURS / TERMINEE / ANNULEE
    private String zoom_meeting_id;    // ID numérique Zoom (pour récupérer les participants)
    private Integer seance_id;         // nullable
    private int teacher_id;
    private int classe_id;
    private int matiere_id;

    // Champs joints (lecture seule)
    private String classe_nom;
    private String matiere_nom;
    private String teacher_nom;

    public LiveSession() {}

    public LiveSession(int id_ls, String titre_ls, LocalDate date_session_ls,
                       LocalTime heure_debut_ls, LocalTime heure_fin_ls,
                       String lien_meet_ls, String statut_ls,
                       Integer seance_id, int teacher_id, int classe_id, int matiere_id) {
        this.id_ls           = id_ls;
        this.titre_ls        = titre_ls;
        this.date_session_ls = date_session_ls;
        this.heure_debut_ls  = heure_debut_ls;
        this.heure_fin_ls    = heure_fin_ls;
        this.lien_meet_ls    = lien_meet_ls;
        this.statut_ls       = statut_ls;
        this.seance_id       = seance_id;
        this.teacher_id      = teacher_id;
        this.classe_id       = classe_id;
        this.matiere_id      = matiere_id;
    }

    public int getId_ls() { return id_ls; }
    public void setId_ls(int id_ls) { this.id_ls = id_ls; }

    public String getTitre_ls() { return titre_ls; }
    public void setTitre_ls(String titre_ls) { this.titre_ls = titre_ls; }

    public LocalDate getDate_session_ls() { return date_session_ls; }
    public void setDate_session_ls(LocalDate date_session_ls) { this.date_session_ls = date_session_ls; }

    public LocalTime getHeure_debut_ls() { return heure_debut_ls; }
    public void setHeure_debut_ls(LocalTime heure_debut_ls) { this.heure_debut_ls = heure_debut_ls; }

    public LocalTime getHeure_fin_ls() { return heure_fin_ls; }
    public void setHeure_fin_ls(LocalTime heure_fin_ls) { this.heure_fin_ls = heure_fin_ls; }

    public String getLien_meet_ls() { return lien_meet_ls; }
    public void setLien_meet_ls(String lien_meet_ls) { this.lien_meet_ls = lien_meet_ls; }

    public String getZoom_meeting_id() { return zoom_meeting_id; }
    public void setZoom_meeting_id(String zoom_meeting_id) { this.zoom_meeting_id = zoom_meeting_id; }

    public String getStatut_ls() { return statut_ls; }
    public void setStatut_ls(String statut_ls) { this.statut_ls = statut_ls; }

    public Integer getSeance_id() { return seance_id; }
    public void setSeance_id(Integer seance_id) { this.seance_id = seance_id; }

    public int getTeacher_id() { return teacher_id; }
    public void setTeacher_id(int teacher_id) { this.teacher_id = teacher_id; }

    public int getClasse_id() { return classe_id; }
    public void setClasse_id(int classe_id) { this.classe_id = classe_id; }

    public int getMatiere_id() { return matiere_id; }
    public void setMatiere_id(int matiere_id) { this.matiere_id = matiere_id; }

    public String getClasse_nom() { return classe_nom; }
    public void setClasse_nom(String classe_nom) { this.classe_nom = classe_nom; }

    public String getMatiere_nom() { return matiere_nom; }
    public void setMatiere_nom(String matiere_nom) { this.matiere_nom = matiere_nom; }

    public String getTeacher_nom() { return teacher_nom; }
    public void setTeacher_nom(String teacher_nom) { this.teacher_nom = teacher_nom; }

    @Override
    public String toString() {
        return titre_ls + " (" + date_session_ls + ")";
    }
}
