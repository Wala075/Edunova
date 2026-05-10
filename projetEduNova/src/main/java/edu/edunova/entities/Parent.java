package edu.edunova.entities;

/**
 * Domain entity representing a student's parent.
 * <p>
 * Maps to table {@code parent}. The link to a {@link Classe} is optional
 * ({@code classeId} may be {@code null}) and {@link #classeNom} is a
 * denormalised display field populated through a JOIN.
 */
public class Parent {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    /** Foreign key to {@code classe.id}; {@code null} when no class is assigned. */
    private Integer classeId;
    /** Resolved display name of the linked class (read-only, set by JOIN). */
    private String classeNom;

    /** Default constructor required by frameworks relying on reflection. */
    public Parent() {
    }

    /**
     * Builds a {@code Parent} without identifier (insert use case).
     */
    public Parent(String nom, String prenom, String email, Integer classeId) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.classeId = classeId;
    }

    /**
     * Builds a fully-initialised {@code Parent} without join data.
     */
    public Parent(int id, String nom, String prenom, String email, Integer classeId) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.classeId = classeId;
    }

    /**
     * Builds a fully-initialised {@code Parent} including denormalised join data.
     */
    public Parent(int id, String nom, String prenom, String email, Integer classeId, String classeNom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.classeId = classeId;
        this.classeNom = classeNom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public Integer getClasseId() { return classeId; }
    public String getClasseNom() { return classeNom; }

    /**
     * Returns the parent's display name as {@code "Prenom Nom"}, trimmed.
     *
     * @return concatenated full name, never {@code null}
     */
    public String getNomComplet() {
        StringBuilder sb = new StringBuilder();
        if (prenom != null && !prenom.isEmpty()) sb.append(prenom).append(" ");
        if (nom != null) sb.append(nom);
        return sb.toString().trim();
    }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setClasseId(Integer classeId) { this.classeId = classeId; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    @Override
    public String toString() {
        return getNomComplet() + " <" + email + ">";
    }
}
