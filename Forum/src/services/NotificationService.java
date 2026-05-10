package services;

import entities.Notification;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService — gestion des notifications in-app.
 * Notifie les membres quand leur post est accepte ou refuse par l'admin.
 */
public class NotificationService implements interfaces.INotificationService {

    /** Creer une notification pour un utilisateur. */
    public void creer(int userId, String message, String type) {
        String sql = "INSERT INTO forum_notification (user_id, message, type) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("NotificationService.creer : " + e.getMessage());
        }
    }

    /** Toutes les notifications d'un utilisateur (les plus recentes en premier). */
    public List<Notification> getByUser(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM forum_notification WHERE user_id = ? ORDER BY date_creation DESC LIMIT 20";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Nombre de notifications non lues. */
    public int compterNonLues(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_notification WHERE user_id = ? AND lue = 0";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Marquer toutes les notifications comme lues. */
    public void marquerToutesLues(int userId) throws SQLException {
        String sql = "UPDATE forum_notification SET lue = 1 WHERE user_id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setLue(rs.getBoolean("lue"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) n.setDateCreation(ts.toLocalDateTime());
        return n;
    }
}
