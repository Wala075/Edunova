package services;

import utils.DatabaseConnection;
import java.sql.*;

/**
 * ReputationService — Systeme de points et badges.
 *
 * REGLES METIER :
 *   +10 pts  → post accepte par l admin
 *   + 2 pts  → like recu sur un post
 *   + 3 pts  → commentaire recu
 *   - 5 pts  → post refuse
 *
 * BADGES :
 *   0-19  pts → 🌱 Débutant
 *   20-49 pts → 📚 Apprenti
 *   50-99 pts → ⭐ Actif
 *   100+  pts → 🏆 Expert
 *   200+  pts → 👑 Modérateur
 */
public class ReputationService implements interfaces.IReputationService {

    public enum Action { POST_ACCEPTE, POST_REFUSE, LIKE_RECU, COMMENTAIRE_RECU }

    // Points par action
    private static final int PTS_POST_ACCEPTE    =  10;
    private static final int PTS_POST_REFUSE      =  -5;
    private static final int PTS_LIKE_RECU        =   2;
    private static final int PTS_COMMENTAIRE_RECU =   3;

    /** Ajoute ou retire des points a l utilisateur. */
    public void ajouterPoints(int userId, Action action) {
        int delta = switch (action) {
            case POST_ACCEPTE      -> PTS_POST_ACCEPTE;
            case POST_REFUSE       -> PTS_POST_REFUSE;
            case LIKE_RECU         -> PTS_LIKE_RECU;
            case COMMENTAIRE_RECU  -> PTS_COMMENTAIRE_RECU;
        };

        String sql = "UPDATE `user` SET reputation = GREATEST(0, COALESCE(reputation,0) + ?) WHERE id_u = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("ReputationService: " + e.getMessage());
        }
    }

    /** Retourne la reputation actuelle d un utilisateur. */
    public int getReputation(int userId) {
        String sql = "SELECT COALESCE(reputation,0) FROM `user` WHERE id_u = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("ReputationService: " + e.getMessage());
        }
        return 0;
    }

    /** Retourne le badge correspondant aux points. */
    public static String getBadge(int points) {
        if (points >= 200) return "👑 Modérateur";
        if (points >= 100) return "🏆 Expert";
        if (points >=  50) return "⭐ Actif";
        if (points >=  20) return "📚 Apprenti";
        return "🌱 Débutant";
    }

    /** Retourne la couleur du badge. */
    public static String getBadgeColor(int points) {
        if (points >= 200) return "#7c3aed";
        if (points >= 100) return "#d97706";
        if (points >=  50) return "#2563eb";
        if (points >=  20) return "#059669";
        return "#6b7280";
    }

    /** Retourne le prochain badge et les points manquants. */
    public static String getProgression(int points) {
        if (points >= 200) return "Niveau maximum atteint 🎉";
        if (points >= 100) return (200 - points) + " pts pour 👑 Modérateur";
        if (points >=  50) return (100 - points) + " pts pour 🏆 Expert";
        if (points >=  20) return (50  - points) + " pts pour ⭐ Actif";
        return (20 - points) + " pts pour 📚 Apprenti";
    }
}
