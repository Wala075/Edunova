package services;

import entities.ChatMessage;
import entities.User;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatService — private messaging between users.
 *
 * Features:
 *  - Send a message (auto-moderated via ModerationService)
 *  - Load conversation between two users
 *  - Mark messages as read
 *  - Count unread messages
 *  - Get all users with their online status and last seen time
 *  - Update own presence (called on login and periodically)
 */
public class ChatService {

    private final ModerationService moderationService = new ModerationService();

    // ── Presence ─────────────────────────────────────────────

    /** Call on login and every 30s to mark user as online. */
    public void updatePresence(int userId) {
        String sql = "INSERT INTO user_presence (user_id, last_seen, is_online) VALUES (?,NOW(),1) " +
                     "ON DUPLICATE KEY UPDATE last_seen=NOW(), is_online=1";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    /** Call on logout to mark user as offline. */
    public void setOffline(int userId) {
        String sql = "UPDATE user_presence SET is_online=0, last_seen=NOW() WHERE user_id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    /**
     * Returns all users (except self) with presence info.
     * Each row: { id_u, nom_u, prenom_u, role_id, is_online, last_seen }
     * Users not in user_presence are treated as offline.
     */
    public List<UserPresence> getUtilisateursAvecPresence(int selfId) {
        List<UserPresence> list = new ArrayList<>();
        // Mark anyone whose last_seen > 2 minutes ago as offline
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE user_presence SET is_online=0 WHERE last_seen < DATE_SUB(NOW(), INTERVAL 2 MINUTE)")) {
            ps.executeUpdate();
        } catch (Exception ignored) {}

        String sql =
            "SELECT u.id_u, u.nom_u, u.prenom_u, u.role_id, " +
            "       COALESCE(p.is_online, 0) AS is_online, " +
            "       p.last_seen " +
            "FROM `user` u " +
            "LEFT JOIN user_presence p ON p.user_id = u.id_u " +
            "WHERE u.id_u <> ? AND u.actif_u = 1 " +
            "ORDER BY is_online DESC, u.nom_u ASC";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, selfId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserPresence up = new UserPresence();
                    up.userId   = rs.getInt("id_u");
                    up.nom      = rs.getString("prenom_u") + " " + rs.getString("nom_u");
                    up.roleId   = rs.getInt("role_id");
                    up.isOnline = rs.getBoolean("is_online");
                    Timestamp ts = rs.getTimestamp("last_seen");
                    up.lastSeen = ts != null ? ts.toLocalDateTime() : null;
                    list.add(up);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    // ── Messages ─────────────────────────────────────────────

    /**
     * Sends a message. Runs AI moderation first.
     * Returns the moderation result so the UI can show feedback.
     * If moderation passes, message is saved with statut=ACCEPTE.
     * If it fails, message is saved with statut=REFUSE (stored but not shown to receiver).
     */
    public SendResult envoyerMessage(int senderId, int receiverId, String texte) {
        if (texte == null || texte.isBlank())
            return new SendResult(false, "Message vide.", null);

        // Moderation
        try {
            ModerationService.ResultatModeration mod = moderationService.moderer("", texte);
            String statut = mod.isOk() ? "ACCEPTE" : "REFUSE";

            String sql = "INSERT INTO chat_message (sender_id, receiver_id, contenu, statut) VALUES (?,?,?,?)";
            try (Connection c = DatabaseConnection.getInstance();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.setString(3, texte.trim());
                ps.setString(4, statut);
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) {
                        if (!mod.isOk()) {
                            return new SendResult(false,
                                "Message bloque : " + mod.explication, null);
                        }
                        return new SendResult(true, "OK", getById(k.getInt(1)));
                    }
                }
            }
        } catch (Exception e) {
            return new SendResult(false, "Erreur : " + e.getMessage(), null);
        }
        return new SendResult(false, "Erreur inconnue.", null);
    }

    /** Returns accepted messages between two users, oldest first. */
    public List<ChatMessage> getConversation(int userId1, int userId2) {
        List<ChatMessage> list = new ArrayList<>();
        String sql =
            "SELECT * FROM chat_message " +
            "WHERE statut='ACCEPTE' AND (" +
            "  (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)" +
            ") ORDER BY date_creation ASC";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId1); ps.setInt(2, userId2);
            ps.setInt(3, userId2); ps.setInt(4, userId1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception ignored) {}
        return list;
    }

    /** Marks all messages from sender to receiver as read. */
    public void marquerLus(int senderId, int receiverId) {
        String sql = "UPDATE chat_message SET lu=1 WHERE sender_id=? AND receiver_id=? AND lu=0";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, senderId); ps.setInt(2, receiverId);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    /** Count unread messages sent to userId. */
    public int compterNonLus(int userId) {
        String sql = "SELECT COUNT(*) FROM chat_message WHERE receiver_id=? AND lu=0 AND statut='ACCEPTE'";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    /** Count unread messages from a specific sender to receiver. */
    public int compterNonLusDepuis(int senderId, int receiverId) {
        String sql = "SELECT COUNT(*) FROM chat_message WHERE sender_id=? AND receiver_id=? AND lu=0 AND statut='ACCEPTE'";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, senderId); ps.setInt(2, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // ── Helpers ──────────────────────────────────────────────

    private ChatMessage getById(int id) {
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM chat_message WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ChatMessage map(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setContenu(rs.getString("contenu"));
        m.setStatut(rs.getString("statut"));
        m.setLu(rs.getBoolean("lu"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) m.setDateCreation(ts.toLocalDateTime());
        return m;
    }

    // ── Inner classes ─────────────────────────────────────────

    public static class UserPresence {
        public int           userId;
        public String        nom;
        public int           roleId;
        public boolean       isOnline;
        public LocalDateTime lastSeen;

        public String getInitiales() {
            if (nom == null || nom.isBlank()) return "?";
            String[] p = nom.trim().split("\\s+");
            if (p.length >= 2) return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
            return nom.substring(0, Math.min(2, nom.length())).toUpperCase();
        }

        public String getLastSeenLabel() {
            if (isOnline) return "En ligne";
            if (lastSeen == null) return "Jamais connecte";
            // Use UTC now to match the DB timezone set in DatabaseConnection URL
            LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
            long minutes = java.time.Duration.between(lastSeen, now).toMinutes();
            if (minutes < 1)    return "A l'instant";
            if (minutes < 60)   return "Il y a " + minutes + " min";
            long hours = minutes / 60;
            if (hours < 24)     return "Il y a " + hours + "h";
            long days = hours / 24;
            return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
        }

        public String getRoleColor() {
            return switch (roleId) {
                case 1 -> "#7c3aed";
                case 2 -> "#2563eb";
                case 3 -> "#059669";
                case 4 -> "#d97706";
                default -> "#6b7280";
            };
        }
    }

    public static class SendResult {
        public final boolean     ok;
        public final String      message;
        public final ChatMessage chatMessage;
        public SendResult(boolean ok, String message, ChatMessage chatMessage) {
            this.ok = ok; this.message = message; this.chatMessage = chatMessage;
        }
    }
}
