package edu.edunova.entities;

/**
 * Domain entity representing a teacher.
 * <p>
 * Maps to table {@code teacher} (columns {@code id_t}, {@code nom_t},
 * {@code prenom_t}, {@code specialite_t}). The complete persistence row also
 * carries email/phone/active flag, but only the fields exposed below are
 * required by the current business operations.
 */
public class Teacher {

    private int id;
    private String nom;
    private String prenom;
    private String specialite;

    /** Default constructor required by frameworks relying on reflection. */
    public Teacher() {
    }

    /**
     * Builds a fully-initialised {@code Teacher}.
     *
     * @param id          primary key
     * @param nom         last name
     * @param prenom      first name
     * @param specialite  teaching speciality (may be {@code null})
     */
    public Teacher(int id, String nom, String prenom, String specialite) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getSpecialite() { return specialite; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    @Override
    public String toString() {
        return prenom + " " + nom + (specialite != null ? " (" + specialite + ")" : "");
    }
}
