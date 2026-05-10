package services;

import utils.DatabaseConnection;
import java.sql.*;

/**
 * CommentLikeService — likes on individual comments.
 * Uses the forum_comment_like table.
 */
public class CommentLikeService {

    public boolean toggleLike(int commentId, int userId) throws SQLException {
        if (hasLiked(commentId, userId)) { unlike(commentId, userId); return false; }
        else                             { like(commentId, userId);   return true;  }
    }

    public void like(int commentId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO forum_comment_like (comment_id, user_id) VALUES (?, ?)";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void unlike(int commentId, int userId) throws SQLException {
        String sql = "DELETE FROM forum_comment_like WHERE comment_id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public int countLikes(int commentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_comment_like WHERE comment_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean hasLiked(int commentId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_comment_like WHERE comment_id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
