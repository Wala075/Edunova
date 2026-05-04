package edu.edunova.entities;

import java.util.ArrayList;
import java.util.List;

public class Bulletin {

    private Student student;
    private int trimestre;
    private String annee;
    private List<BulletinLigne> lignes = new ArrayList<>();
    private double moyenneGenerale;

    public Bulletin() {}

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public int getTrimestre() { return trimestre; }
    public void setTrimestre(int trimestre) { this.trimestre = trimestre; }

    public String getAnnee() { return annee; }
    public void setAnnee(String annee) { this.annee = annee; }

    public List<BulletinLigne> getLignes() { return lignes; }
    public void setLignes(List<BulletinLigne> lignes) { this.lignes = lignes; }
    public void addLigne(BulletinLigne l) { this.lignes.add(l); }

    public double getMoyenneGenerale() { return moyenneGenerale; }
    public void setMoyenneGenerale(double moyenneGenerale) { this.moyenneGenerale = moyenneGenerale; }

    public String getMention() {
        if (moyenneGenerale >= 16) return "Excellent";
        if (moyenneGenerale >= 14) return "Très Bien";
        if (moyenneGenerale >= 12) return "Bien";
        if (moyenneGenerale >= 10) return "Assez Bien";
        if (moyenneGenerale >= 8)  return "Passable";
        return "Insuffisant";
    }

    public String getDecision() {
        if (moyenneGenerale >= 10) return "Admis(e)";
        return "Non admis(e)";
    }

    /** Une ligne du bulletin = une matière */
    public static class BulletinLigne {
        private int matiereId;
        private String matiere;
        private int nbNotes;
        private double moyenne;

        public BulletinLigne() {}

        public BulletinLigne(int matiereId, String matiere, int nbNotes, double moyenne) {
            this.matiereId = matiereId;
            this.matiere = matiere;
            this.nbNotes = nbNotes;
            this.moyenne = moyenne;
        }

        public int getMatiereId() { return matiereId; }
        public void setMatiereId(int matiereId) { this.matiereId = matiereId; }

        public String getMatiere() { return matiere; }
        public void setMatiere(String matiere) { this.matiere = matiere; }

        public int getNbNotes() { return nbNotes; }
        public void setNbNotes(int nbNotes) { this.nbNotes = nbNotes; }

        public double getMoyenne() { return moyenne; }
        public void setMoyenne(double moyenne) { this.moyenne = moyenne; }

        public String getAppreciation() {
            if (moyenne >= 16) return "Excellent";
            if (moyenne >= 14) return "Très Bien";
            if (moyenne >= 12) return "Bien";
            if (moyenne >= 10) return "Assez Bien";
            if (moyenne >= 8)  return "Passable";
            return "Faible";
        }
    }
}
