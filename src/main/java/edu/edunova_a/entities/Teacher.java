package edu.edunova_a.entities;

public class Teacher {

    private int id_t;
    private String nom_t;
    private String prenom_t;
    private String email_t;
    private String telephone_t;
    private String specialite_t;
    private String diplome_t;
    private String date_embauche_t;   // String pour rester simple
    private String cv_path_t;
    private boolean actif_t;
    private int user_id;              // 0 si non assigné

    public Teacher(int id_t, String nom_t, String prenom_t, String email_t, String specialite_t) {
        this.id_t        = id_t;
        this.nom_t       = nom_t;
        this.prenom_t    = prenom_t;
        this.email_t     = email_t;
        this.specialite_t = specialite_t;
    }

    public int getId_t() { return id_t; }
    public void setId_t(int id_t) { this.id_t = id_t; }

    public String getNom_t() { return nom_t; }
    public void setNom_t(String nom_t) { this.nom_t = nom_t; }

    public String getPrenom_t() { return prenom_t; }
    public void setPrenom_t(String prenom_t) { this.prenom_t = prenom_t; }

    public String getEmail_t() { return email_t; }
    public void setEmail_t(String email_t) { this.email_t = email_t; }

    public String getTelephone_t() { return telephone_t; }
    public void setTelephone_t(String telephone_t) { this.telephone_t = telephone_t; }

    public String getSpecialite_t() { return specialite_t; }
    public void setSpecialite_t(String specialite_t) { this.specialite_t = specialite_t; }

    public String getDiplome_t() { return diplome_t; }

    public String getDate_embauche_t() { return date_embauche_t; }
    public void setDate_embauche_t(String date_embauche_t) { this.date_embauche_t = date_embauche_t; }

    public String getCv_path_t() { return cv_path_t; }

    public boolean isActif_t() { return actif_t; }
    public void setActif_t(boolean actif_t) { this.actif_t = actif_t; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    @Override
    public String toString() {
        return (prenom_t == null ? "" : prenom_t) + " " + (nom_t == null ? "" : nom_t);
    }
}
