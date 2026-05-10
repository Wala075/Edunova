package edu.edunova_a.entities;

public class Classe {
    private int id;
    private String nom;
    private String niveau;
    private int capacite;

    public Classe() {}

    public Classe(int id, String nom) {
        this.id  = id;
        this.nom = nom;
    }

    public Classe(int id, String nom, String niveau, int capacite) {
        this.id       = id;
        this.nom      = nom;
        this.niveau   = niveau;
        this.capacite = capacite;
    }

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }

    public String getNom()              { return nom; }
    public void   setNom(String nom)    { this.nom = nom; }

    public String getNiveau()                { return niveau; }
    public void   setNiveau(String niveau)   { this.niveau = niveau; }

    public int getCapacite()               { return capacite; }
    public void setCapacite(int capacite)  { this.capacite = capacite; }

    @Override
    public String toString() { return nom; }
}
