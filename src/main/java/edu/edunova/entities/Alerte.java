package edu.edunova.entities;

import java.time.LocalDateTime;

/**
 * Une alerte intelligente détectée par AlerteEngine.
 * Mappée sur la table SQL `alerte`.
 */
public class Alerte {

    // ---------- Enums ----------

    /** Sévérité de l'alerte (couleur d'affichage + déclenche ou non un SMS). */
    public enum Severite {
        INFO,        // bleu
        MOYENNE,     // orange
        CRITIQUE,    // rouge - SMS auto
        POSITIVE     // vert (excellence, progression)
    }

    /** Statut du suivi par l'enseignant. */
    public enum Statut {
        NOUVELLE,
        LUE,
        TRAITEE,
        IGNOREE
    }

    /** Types d'alerte gérés par AlerteEngine (les "règles métier"). */
    public enum TypeAlerte {
        CHUTE_NIVEAU,             // moyenne en baisse de >2pts vs trimestre précédent
        MOYENNE_FAIBLE,           // moyenne générale < 8
        ECHEC_MATIERE,            // moyenne d'une matière < 6
        PROGRESSION_POSITIVE,     // moyenne en hausse de +2pts
        EXCELLENCE,               // moyenne >= 16
        MATIERE_CRITIQUE_CLASSE,  // moyenne classe < 10 sur une matière
        NOTE_ANORMALE             // note isolée < 4 sur matière où l'élève va bien
    }

    // ---------- Champs ----------

    private int id_a;
    private TypeAlerte typeAlerte;
    private Severite severite;
    private String titre;
    private String message;

    private int studentId;        // 0 si non rattaché
    private Integer matiereId;    // null si non rattaché à une matière
    private Integer trimestre;    // null si non rattaché à un trimestre
    private String anneeScolaire;

    private LocalDateTime dateCreation;
    private Statut statut = Statut.NOUVELLE;
    private boolean emailEnvoye = false;
    private boolean smsEnvoye = false;

    // Champs "joints" (remplis par les requêtes JOIN, optionnels)
    private String studentNom;     // ex: "Ben Ali Mohamed" - pour l'affichage
    private String matiereNom;     // ex: "Mathématiques"

    // ---------- Constructeurs ----------

    public Alerte() {}

    public Alerte(TypeAlerte typeAlerte, Severite severite, String titre,
                  String message, int studentId, String anneeScolaire) {
        this.typeAlerte = typeAlerte;
        this.severite = severite;
        this.titre = titre;
        this.message = message;
        this.studentId = studentId;
        this.anneeScolaire = anneeScolaire;
        this.dateCreation = LocalDateTime.now();
    }

    // ---------- Getters / Setters ----------

    public int getId_a() { return id_a; }
    public void setId_a(int id_a) { this.id_a = id_a; }

    public TypeAlerte getTypeAlerte() { return typeAlerte; }
    public void setTypeAlerte(TypeAlerte typeAlerte) { this.typeAlerte = typeAlerte; }

    public Severite getSeverite() { return severite; }
    public void setSeverite(Severite severite) { this.severite = severite; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getMatiereId() { return matiereId; }
    public void setMatiereId(Integer matiereId) { this.matiereId = matiereId; }

    public Integer getTrimestre() { return trimestre; }
    public void setTrimestre(Integer trimestre) { this.trimestre = trimestre; }

    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public boolean isEmailEnvoye() { return emailEnvoye; }
    public void setEmailEnvoye(boolean emailEnvoye) { this.emailEnvoye = emailEnvoye; }

    public boolean isSmsEnvoye() { return smsEnvoye; }
    public void setSmsEnvoye(boolean smsEnvoye) { this.smsEnvoye = smsEnvoye; }

    public String getStudentNom() { return studentNom; }
    public void setStudentNom(String studentNom) { this.studentNom = studentNom; }

    public String getMatiereNom() { return matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }

    // ---------- Helpers UI ----------

    /** Couleur hexa associée à la sévérité (pour l'UI). */
    public String getCouleurSeverite() {
        if (severite == null) return "#9ca3af";
        switch (severite) {
            case CRITIQUE: return "#ef4444"; // rouge
            case MOYENNE:  return "#f59e0b"; // orange
            case POSITIVE: return "#10b981"; // vert
            case INFO:     return "#3b82f6"; // bleu
            default:       return "#9ca3af";
        }
    }

    /** Icône emoji pour l'affichage rapide. */
    public String getIcone() {
        if (severite == null) return "•";
        switch (severite) {
            case CRITIQUE: return "🔴";
            case MOYENNE:  return "🟠";
            case POSITIVE: return "🟢";
            case INFO:     return "🔵";
            default:       return "•";
        }
    }

    @Override
    public String toString() {
        return "[" + severite + "] " + titre + " - " + studentNom;
    }
}
