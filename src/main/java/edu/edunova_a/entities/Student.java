package edu.edunova_a.entities;

public class Student {
    private int id_s;
    private String nom_s;
    private String prenom_s;
    private String date_naissance_s;
    private int classe_id;   // 0 means not assigned
    private String tel_parent;   // E.164, ex : +21698765432
    private String nom_parent;

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

    public String getTel_parent() { return tel_parent; }
    public void setTel_parent(String tel_parent) { this.tel_parent = tel_parent; }

    public String getNom_parent() { return nom_parent; }
    public void setNom_parent(String nom_parent) { this.nom_parent = nom_parent; }

    public String getNomComplet() {
        return (nom_s == null ? "" : nom_s) + " " + (prenom_s == null ? "" : prenom_s);
    }

    @Override
    public String toString() {
        return getNomComplet().trim();
    }
}
