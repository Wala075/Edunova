package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment {
    private int           id;
    private int           postId;
    private int           auteurId;
    private String        auteurNom;
    private String        contenu;
    private LocalDateTime dateCreation;

    public Comment() {}
    public Comment(int postId, int auteurId, String auteurNom, String contenu) {
        this.postId = postId; this.auteurId = auteurId;
        this.auteurNom = auteurNom; this.contenu = contenu;
    }

    public int           getId()                       { return id; }
    public void          setId(int v)                  { this.id = v; }
    public int           getPostId()                   { return postId; }
    public void          setPostId(int v)              { this.postId = v; }
    public int           getAuteurId()                 { return auteurId; }
    public void          setAuteurId(int v)            { this.auteurId = v; }
    public String        getAuteurNom()                { return auteurNom; }
    public void          setAuteurNom(String v)        { this.auteurNom = v; }
    public String        getContenu()                  { return contenu; }
    public void          setContenu(String v)          { this.contenu = v; }
    public LocalDateTime getDateCreation()             { return dateCreation; }
    public void          setDateCreation(LocalDateTime v) { this.dateCreation = v; }

    public String getDateFormatee() {
        if (dateCreation == null) return "";
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public String getInitiales() {
        if (auteurNom == null || auteurNom.isBlank()) return "?";
        String[] p = auteurNom.trim().split("\\s+");
        if (p.length >= 2) return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
        return auteurNom.substring(0, Math.min(2, auteurNom.length())).toUpperCase();
    }
}
