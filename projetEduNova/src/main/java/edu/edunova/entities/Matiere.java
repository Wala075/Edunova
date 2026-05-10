package edu.edunova.entities;

/**
 * Domain entity representing a teaching subject.
 * <p>
 * Maps to table {@code matiere} (columns {@code id_m}, {@code nom_m},
 * {@code coefficient_m}). The coefficient is the academic weight applied
 * when computing weighted averages.
 */
public class Matiere {

    private int id;
    private String nom;
    private int coefficient;

    /** Default constructor required by frameworks relying on reflection. */
    public Matiere() {
    }

    /**
     * Builds a fully-initialised {@code Matiere}.
     *
     * @param id           primary key
     * @param nom          subject name (e.g. {@code "Mathématiques"})
     * @param coefficient  academic weight ({@code &gt; 0})
     */
    public Matiere(int id, String nom, int coefficient) {
        this.id = id;
        this.nom = nom;
        this.coefficient = coefficient;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public int getCoefficient() { return coefficient; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setCoefficient(int coefficient) { this.coefficient = coefficient; }

    @Override
    public String toString() {
        return nom + " (coef. " + coefficient + ")";
    }
}
