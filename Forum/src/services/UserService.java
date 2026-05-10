package services;

import entities.User;
import utils.DatabaseConnection;

import java.security.MessageDigest;
import java.sql.*;

/**
 * UserService — schema REEL de la table `user` du collegue :
 *
 *   id_u | email_u | password_u | nom_u | prenom_u |
 *   telephone_u | photo_u | actif_u | date_creation_u | role_id | reputation
 *
 * FIX : La requête originale comparait le mot de passe dans le SQL
 *       (WHERE password_u = ?). Si les mots de passe sont hashés
 *       (MD5, SHA-1, bcrypt…) dans la base, la comparaison échoue toujours.
 *       → On récupère le user par email puis on compare côté Java.
 */
public class UserService implements interfaces.IUserService {

    /**
     * Authentifie l'utilisateur.
     * Supporte automatiquement : texte clair, MD5, SHA-1, SHA-256.
     * Pour bcrypt : décommentez la section bcrypt ci-dessous et ajoutez
     * jBCrypt dans pom.xml.
     */
    public User login(String email, String password) throws SQLException {

        // FIX #1 : chercher uniquement par email (+ actif_u = 1).
        // La vérification du mot de passe se fait côté Java.
        String sql = "SELECT * FROM `user` WHERE email_u = ? AND actif_u = 1 LIMIT 1";

        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;   // email inconnu ou compte inactif

                String storedPassword = rs.getString("password_u");

                if (!passwordMatch(password, storedPassword)) {
                    return null;               // mauvais mot de passe
                }

                return mapUser(rs);
            }
        }
    }

    /**
     * FIX #2 : Détection automatique du format de hachage.
     * Teste dans l'ordre : clair → MD5 → SHA-1 → SHA-256.
     *
     * Pour identifier le format dans phpMyAdmin :
     *   SELECT password_u FROM `user` LIMIT 1;
     *  • 32 hex  → MD5
     *  • 40 hex  → SHA-1
     *  • 64 hex  → SHA-256
     *  • $2y$…   → bcrypt (voir commentaire ci-dessous)
     *  • autre   → texte clair
     */
    private boolean passwordMatch(String plainPassword, String storedPassword) {
        if (storedPassword == null) return false;

        if (storedPassword.equals(plainPassword))              return true; // clair
        if (storedPassword.equals(hash("MD5",     plainPassword))) return true;
        if (storedPassword.equals(hash("SHA-1",   plainPassword))) return true;
        if (storedPassword.equals(hash("SHA-256", plainPassword))) return true;

        // bcrypt — décommentez + ajoutez jBCrypt dans pom.xml :
        // <dependency>
        //     <groupId>org.mindrot</groupId>
        //     <artifactId>jbcrypt</artifactId>
        //     <version>0.4</version>
        // </dependency>
        // if (storedPassword.startsWith("$2")) {
        //     return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, storedPassword);
        // }

        return false;
    }

    private String hash(String algorithm, String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id_u"));
        u.setUsername(rs.getString("email_u"));
        u.setPassword(rs.getString("password_u"));
        u.setNom(rs.getString("nom_u"));
        u.setPrenom(rs.getString("prenom_u"));
        u.setRole(convertirRole(rs.getInt("role_id")));
        return u;
    }

    /**
     * FIX #3 : Vérifiez les IDs de rôles dans votre base :
     *   SELECT * FROM role;
     * Adaptez les cases si vos IDs sont différents.
     */
    private String convertirRole(int roleId) {
        return switch (roleId) {
            case 1  -> "admin";
            case 2  -> "student";
            case 3  -> "parent";
            case 4  -> "teacher";
            default -> "student";
        };
    }

    /**
     * Returns quick stats for a user profile popup.
     * Returns int[]{totalPosts, acceptedPosts, reputation}
     */
    public int[] getUserStats(int userId) {
        int total = 0, accepted = 0, reputation = 0;
        try (Connection c = DatabaseConnection.getInstance()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM forum_post WHERE auteur_id=?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total = rs.getInt(1); }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM forum_post WHERE auteur_id=? AND statut='ACCEPTE'")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) accepted = rs.getInt(1); }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COALESCE(reputation,0) FROM `user` WHERE id_u=?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) reputation = rs.getInt(1); }
            }
        } catch (Exception ignored) {}
        return new int[]{total, accepted, reputation};
    }
}