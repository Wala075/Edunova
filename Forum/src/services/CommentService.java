package services;

import entities.Comment;
import utils.DatabaseConnection;
import services.NotificationService;
import services.ReputationService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService implements interfaces.ICommentService {
    private final NotificationService notifService     = new NotificationService();
    private final ReputationService   reputationService = new ReputationService();

    public void addComment(Comment comment) throws SQLException {
        String sql = "INSERT INTO forum_comment (post_id, auteur_id, auteur_nom, contenu) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, comment.getPostId());
            ps.setInt(2, comment.getAuteurId());
            ps.setString(3, comment.getAuteurNom());
            ps.setString(4, comment.getContenu());
            ps.executeUpdate();
        }
        // Notifier l auteur du post
        try {
            String q = "SELECT auteur_id, titre FROM forum_post WHERE id = ?";
            try (Connection c2 = DatabaseConnection.getInstance();
                 java.sql.PreparedStatement ps2 = c2.prepareStatement(q)) {
                ps2.setInt(1, comment.getPostId());
                try (java.sql.ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) {
                        int auteurId = rs.getInt("auteur_id");
                        if (auteurId != comment.getAuteurId()) {
                            String titre = rs.getString("titre");
                            notifService.creer(auteurId,
                                comment.getAuteurNom() + " a commente votre post : " + titre,
                                "COMMENTAIRE");
                            reputationService.ajouterPoints(auteurId, ReputationService.Action.COMMENTAIRE_RECU);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public List<Comment> getByPost(int postId) throws SQLException {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT * FROM forum_comment WHERE post_id = ? ORDER BY date_creation ASC";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public int countComments(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_comment WHERE post_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        return 0;
    }

    private Comment map(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setPostId(rs.getInt("post_id"));
        c.setAuteurId(rs.getInt("auteur_id"));
        c.setAuteurNom(rs.getString("auteur_nom"));
        c.setContenu(rs.getString("contenu"));
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) c.setDateCreation(dc.toLocalDateTime());
        return c;
    }
}
