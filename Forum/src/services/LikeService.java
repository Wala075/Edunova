package services;

import utils.DatabaseConnection;
import services.NotificationService;
import services.ReputationService;
import java.sql.*;

public class LikeService implements interfaces.ILikeService {
    private final NotificationService notifService     = new NotificationService();
    private final ReputationService   reputationService = new ReputationService();

    public boolean toggleLike(int postId, int userId) throws SQLException {
        if (hasLiked(postId, userId)) { unlike(postId, userId); return false; }
        else                          { like(postId, userId);   return true;  }
    }

    public void like(int postId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO forum_like (post_id, user_id) VALUES (?, ?)";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId); ps.setInt(2, userId); ps.executeUpdate();
        }
        // Notifier l auteur du post (si ce n est pas lui meme qui like)
        try {
            String q = "SELECT auteur_id, auteur_nom, titre FROM forum_post WHERE id = ?";
            try (Connection c2 = DatabaseConnection.getInstance();
                 java.sql.PreparedStatement ps2 = c2.prepareStatement(q)) {
                ps2.setInt(1, postId);
                try (java.sql.ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) {
                        int auteurId = rs.getInt("auteur_id");
                        if (auteurId != userId) {
                            String titre = rs.getString("titre");
                            notifService.creer(auteurId,
                                "Quelqu un a aime votre post : " + titre, "LIKE");
                            reputationService.ajouterPoints(auteurId, ReputationService.Action.LIKE_RECU);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public void unlike(int postId, int userId) throws SQLException {
        String sql = "DELETE FROM forum_like WHERE post_id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId); ps.setInt(2, userId); ps.executeUpdate();
        }
    }

    public int countLikes(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_like WHERE post_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        return 0;
    }

    public boolean hasLiked(int postId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_like WHERE post_id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId); ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        }
        return false;
    }
}
