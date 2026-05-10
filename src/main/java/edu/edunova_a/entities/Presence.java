package edu.edunova_a.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Presence {

    private int id_pr;
    private String statut_pr;             // PRESENT / ABSENT / RETARD / JUSTIFIE
    private LocalDate date_presence_pr;
    private LocalTime heure_arrivee_pr;   // nullable
    private String justificatif_path_pr;
    private String commentaire_pr;
    private int student_id;
    private Integer seance_id;            // nullable
    private Integer live_session_id;      // nullable

    // Champs joints (lecture seule, pour l'affichage)
    private String student_nom;
    private String seance_label;
    private String live_titre;

    public Presence() {}

    public Presence(int id_pr, String statut_pr, LocalDate date_presence_pr,
                    LocalTime heure_arrivee_pr, String justificatif_path_pr,
                    String commentaire_pr, int student_id,
                    Integer seance_id, Integer live_session_id) {
        this.id_pr                = id_pr;
        this.statut_pr            = statut_pr;
        this.date_presence_pr     = date_presence_pr;
        this.heure_arrivee_pr     = heure_arrivee_pr;
        this.justificatif_path_pr = justificatif_path_pr;
        this.commentaire_pr       = commentaire_pr;
        this.student_id           = student_id;
        this.seance_id            = seance_id;
        this.live_session_id      = live_session_id;
    }

    public int getId_pr() { return id_pr; }
    public void setId_pr(int id_pr) { this.id_pr = id_pr; }

    public String getStatut_pr() { return statut_pr; }
    public void setStatut_pr(String statut_pr) { this.statut_pr = statut_pr; }

    public LocalDate getDate_presence_pr() { return date_presence_pr; }
    public void setDate_presence_pr(LocalDate date_presence_pr) { this.date_presence_pr = date_presence_pr; }

    public LocalTime getHeure_arrivee_pr() { return heure_arrivee_pr; }
    public void setHeure_arrivee_pr(LocalTime heure_arrivee_pr) { this.heure_arrivee_pr = heure_arrivee_pr; }

    public String getJustificatif_path_pr() { return justificatif_path_pr; }
    public void setJustificatif_path_pr(String justificatif_path_pr) { this.justificatif_path_pr = justificatif_path_pr; }

    public String getCommentaire_pr() { return commentaire_pr; }
    public void setCommentaire_pr(String commentaire_pr) { this.commentaire_pr = commentaire_pr; }

    public int getStudent_id() { return student_id; }
    public void setStudent_id(int student_id) { this.student_id = student_id; }

    public Integer getSeance_id() { return seance_id; }
    public void setSeance_id(Integer seance_id) { this.seance_id = seance_id; }

    public Integer getLive_session_id() { return live_session_id; }
    public void setLive_session_id(Integer live_session_id) { this.live_session_id = live_session_id; }

    public String getStudent_nom() { return student_nom; }
    public void setStudent_nom(String student_nom) { this.student_nom = student_nom; }

    public String getSeance_label() { return seance_label; }
    public void setSeance_label(String seance_label) { this.seance_label = seance_label; }

    public String getLive_titre() { return live_titre; }
    public void setLive_titre(String live_titre) { this.live_titre = live_titre; }
}
