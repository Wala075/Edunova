package edu.edunova.entities;


import java.util.Date;

public class Note {
    private int id_n;
    private double valeur;
    private int coefficient;
    private String type_eval;
    private int trimestre;
    private Date date_saisie;
    private int student_id;
    private int matiere_id;
    private String annee_scolaire;

    // Constructeur par défaut
    public Note() {
    }

    // Constructeur sans l'ID (utile pour l'insertion/AJOUT)
    public Note(double valeur, int coefficient, String type_eval, int trimestre, Date date_saisie, int student_id, int matiere_id, String annee_scolaire) {
        this.valeur = valeur;
        this.coefficient = coefficient;
        this.type_eval = type_eval;
        this.trimestre = trimestre;
        this.date_saisie = date_saisie;
        this.student_id = student_id;
        this.matiere_id = matiere_id;
        this.annee_scolaire = annee_scolaire;
    }

    // Getters et Setters
    public int getId_n() {
        return id_n;
    }

    public void setId_n(int id_n) {
        this.id_n = id_n;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }

    public String getType_eval() {
        return type_eval;
    }

    public void setType_eval(String type_eval) {
        this.type_eval = type_eval;
    }

    public int getTrimestre() {
        return trimestre;
    }

    public void setTrimestre(int trimestre) {
        this.trimestre = trimestre;
    }

    public Date getDate_saisie() {
        return date_saisie;
    }

    public void setDate_saisie(Date date_saisie) {
        this.date_saisie = date_saisie;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public int getMatiere_id() {
        return matiere_id;
    }

    public void setMatiere_id(int matiere_id) {
        this.matiere_id = matiere_id;
    }

    public String getAnnee_scolaire() {
        return annee_scolaire;
    }

    public void setAnnee_scolaire(String annee_scolaire) {
        this.annee_scolaire = annee_scolaire;
    }

    // Méthode toString pour le débogage
    @Override
    public String toString() {
        return "Note{" +
                "id_n=" + id_n +
                ", valeur=" + valeur +
                ", coefficient=" + coefficient +
                ", type_eval='" + type_eval + '\'' +
                ", trimestre=" + trimestre +
                ", date_saisie=" + date_saisie +
                ", student_id=" + student_id +
                ", matiere_id=" + matiere_id +
                ", annee_scolaire='" + annee_scolaire + '\'' +
                '}';
    }
}