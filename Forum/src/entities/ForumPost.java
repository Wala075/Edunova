package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ForumPost {

    public enum Statut { EN_ATTENTE, ACCEPTE, REFUSE }

    private int           id;
    private String        titre;
    private String        contenu;
    private String        photoPath;
    private int           auteurId;
    private String        auteurNom;
    private String        auteurRole;
    private Statut        statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModif;

    public ForumPost() {}

    public ForumPost(String titre, String contenu, String photoPath,
                     int auteurId, String auteurNom, String auteurRole) {
        this.titre = titre; this.contenu = contenu; this.photoPath = photoPath;
        this.auteurId = auteurId; this.auteurNom = auteurNom; this.auteurRole = auteurRole;
        this.statut = Statut.EN_ATTENTE;
    }

    // Getters / Setters
    public int           getId()                     { return id; }
    public void          setId(int v)                { this.id = v; }
    public String        getTitre()                  { return titre; }
    public void          setTitre(String v)          { this.titre = v; }
    public String        getContenu()                { return contenu; }
    public void          setContenu(String v)        { this.contenu = v; }
    public String        getPhotoPath()              { return photoPath; }
    public void          setPhotoPath(String v)      { this.photoPath = v; }
    public int           getAuteurId()               { return auteurId; }
    public void          setAuteurId(int v)          { this.auteurId = v; }
    public String        getAuteurNom()              { return auteurNom; }
    public void          setAuteurNom(String v)      { this.auteurNom = v; }
    public String        getAuteurRole()             { return auteurRole; }
    public void          setAuteurRole(String v)     { this.auteurRole = v; }
    public Statut        getStatut()                 { return statut; }
    public void          setStatut(Statut v)         { this.statut = v; }
    public LocalDateTime getDateCreation()           { return dateCreation; }
    public void          setDateCreation(LocalDateTime v) { this.dateCreation = v; }
    public LocalDateTime getDateModif()              { return dateModif; }
    public void          setDateModif(LocalDateTime v)    { this.dateModif = v; }

    public String getStatutLabel() {
        if (statut == null) return "—";
        return switch (statut) {
            case EN_ATTENTE -> "⏳ En attente";
            case ACCEPTE    -> "✅ Accepté";
            case REFUSE     -> "❌ Refusé";
        };
    }

    public String getDateFormatee() {
        if (dateCreation == null) return "";
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getRoleIcon() {
        if (auteurRole == null) return "👤";
        return switch (auteurRole.toLowerCase()) {
            case "admin"   -> "🛡";
            case "student" -> "🎓";
            case "parent"  -> "👨‍👩‍👦";
            case "teacher" -> "👩‍🏫";
            default        -> "👤";
        };
    }

    public String getInitiales() {
        if (auteurNom == null || auteurNom.isBlank()) return "?";
        String[] parts = auteurNom.trim().split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return auteurNom.substring(0, Math.min(2, auteurNom.length())).toUpperCase();
    }
}
