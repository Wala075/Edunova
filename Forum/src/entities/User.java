package entities;

/**
 * Entite User — reflete la vraie table `user` du collegue.
 *
 * Colonnes reelles :
 *   id_u | email_u | password_u | nom_u | prenom_u |
 *   telephone_u | photo_u | actif_u | date_creation_u | role_id | reputation
 */
public class User {

    private int    id;
    private String username;   // = email_u
    private String password;   // = password_u
    private String role;       // converti depuis role_id
    private String nom;        // = nom_u
    private String prenom;     // = prenom_u

    public User() {}

    public User(int id, String username, String password,
                String role, String nom, String prenom) {
        this.id = id; this.username = username; this.password = password;
        this.role = role; this.nom = nom; this.prenom = prenom;
    }

    public int    getId()               { return id; }
    public void   setId(int v)          { this.id = v; }
    public String getUsername()         { return username; }
    public void   setUsername(String v) { this.username = v; }
    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }
    public String getRole()             { return role; }
    public void   setRole(String v)     { this.role = v; }
    public String getNom()              { return nom; }
    public void   setNom(String v)      { this.nom = v; }
    public String getPrenom()           { return prenom; }
    public void   setPrenom(String v)   { this.prenom = v; }

    public String getNomComplet() {
        if (nom != null && prenom != null && !nom.isBlank())
            return prenom + " " + nom;
        return username != null ? username : "Utilisateur";
    }

    public String getInitiales() {
        String nc = getNomComplet().trim();
        String[] p = nc.split("\\s+");
        if (p.length >= 2)
            return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
        return nc.substring(0, Math.min(2, nc.length())).toUpperCase();
    }

    public boolean isAdmin()   { return "admin".equalsIgnoreCase(role); }
    public boolean isStudent() { return "student".equalsIgnoreCase(role); }
    public boolean isParent()  { return "parent".equalsIgnoreCase(role); }
    public boolean isTeacher() { return "teacher".equalsIgnoreCase(role); }
}