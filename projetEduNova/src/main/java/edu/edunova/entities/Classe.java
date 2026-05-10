package edu.edunova.entities;

/**
 * Domain entity representing a school class.
 * <p>
 * Maps to table {@code classe} (columns {@code id}, {@code nom}, {@code niveau},
 * {@code capacite}). This is a plain POJO used for transferring data between
 * the persistence layer and the presentation layer.
 */
public class Classe {

    private int id;
    private String nom;
    private String niveau;
    private int capacite;

    /** Default constructor required by frameworks relying on reflection. */
    public Classe() {
    }

    /**
     * Builds a new {@code Classe} without identifier (insert use case).
     *
     * @param nom       class name (e.g. {@code "6A"})
     * @param niveau    school level (e.g. {@code "6ème"})
     * @param capacite  maximum number of students
     */
    public Classe(String nom, String niveau, int capacite) {
        this.nom = nom;
        this.niveau = niveau;
        this.capacite = capacite;
    }

    /**
     * Builds a fully-initialised {@code Classe} (read use case).
     *
     * @param id        primary key
     * @param nom       class name
     * @param niveau    school level
     * @param capacite  maximum number of students
     */
    public Classe(int id, String nom, String niveau, int capacite) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.capacite = capacite;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getNiveau() { return niveau; }
    public int getCapacite() { return capacite; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    @Override
    public String toString() {
        return nom + " — " + niveau + " (cap. " + capacite + ")";
    }
}
