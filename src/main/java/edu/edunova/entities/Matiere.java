package edu.edunova.entities;

public class Matiere {
    private int id_m;
    private String nom_m;
    private int coefficient_m;

    public Matiere() {}

    public Matiere(String nom_m) {
        this.nom_m = nom_m;
    }

    public Matiere(int id_m, String nom_m) {
        this.id_m = id_m;
        this.nom_m = nom_m;
    }

    public Matiere(int id_m, String nom_m, int coefficient_m) {
        this.id_m          = id_m;
        this.nom_m         = nom_m;
        this.coefficient_m = coefficient_m;
    }

    public int getId_m() { return id_m; }
    public void setId_m(int id_m) { this.id_m = id_m; }

    public String getNom_m() { return nom_m; }
    public void setNom_m(String nom_m) { this.nom_m = nom_m; }

    public int getCoefficient_m() { return coefficient_m; }
    public void setCoefficient_m(int coefficient_m) { this.coefficient_m = coefficient_m; }

    @Override
    public String toString() {
        return nom_m;
    }
}
