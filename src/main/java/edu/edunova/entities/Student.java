package edu.edunova.entities;

public class Student {
    private int id_s;
    private String nom_s;
    private String prenom_s;
    private String date_naissance_s;
    private int classe_id;   // 0 means not assigned

    // ===== Contact parents (Feature 3 / 4) =====
    private String email_parent;
    private String telephone_parent;

    public Student() {}

    public Student(String nom_s, String prenom_s) {
        this.nom_s = nom_s;
        this.prenom_s = prenom_s;
    }

    public Student(int id_s, String nom_s, String prenom_s) {
        this.id_s = id_s;
        this.nom_s = nom_s;
        this.prenom_s = prenom_s;
    }

    public Student(int id_s, String nom_s, String prenom_s, int classe_id) {
        this.id_s      = id_s;
        this.nom_s     = nom_s;
        this.prenom_s  = prenom_s;
        this.classe_id = classe_id;
    }

    public int getId_s() { return id_s; }
    public void setId_s(int id_s) { this.id_s = id_s; }

    public String getNom_s() { return nom_s; }
    public void setNom_s(String nom_s) { this.nom_s = nom_s; }

    public String getPrenom_s() { return prenom_s; }
    public void setPrenom_s(String prenom_s) { this.prenom_s = prenom_s; }

    public String getDate_naissance_s() { return date_naissance_s; }
    public void setDate_naissance_s(String date_naissance_s) { this.date_naissance_s = date_naissance_s; }

    public int getClasse_id() { return classe_id; }
    public void setClasse_id(int classe_id) { this.classe_id = classe_id; }

    public String getEmail_parent() { return email_parent; }
    public void setEmail_parent(String email_parent) { this.email_parent = email_parent; }

    public String getTelephone_parent() { return telephone_parent; }
    public void setTelephone_parent(String telephone_parent) { this.telephone_parent = telephone_parent; }

    public String getNomComplet() {
        return (nom_s == null ? "" : nom_s) + " " + (prenom_s == null ? "" : prenom_s);
    }

    @Override
    public String toString() {
        return getNomComplet().trim();
    }
}
