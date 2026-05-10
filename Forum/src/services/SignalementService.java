package services;

import utils.DatabaseConnection;
import java.sql.*;

/**
 * SignalementService — Signalement de posts inappropries.
 *
 * REGLES METIER :
 *   - Un utilisateur ne peut signaler un post qu une seule fois.
 *   - A partir de 3 signalements, le post passe automatiquement EN_ATTENTE
 *     pour re-validation par l admin.
 *   - L admin voit le nombre de signalements sur chaque carte.
 */
public class SignalementService implements interfaces.ISignalementService {

    private static final int SEUIL_AUTO_ATTENTE = 3;

    /**
     * Signale un post. Retourne true si signal enregistre, false si deja signale.
     * Si le seuil est atteint, le post repasse EN_ATTENTE automatiquement.
     */
    public boolean signaler(int postId, int userId, String raison) throws SQLException {
        if (aDejaSignale(postId, userId)) return false;

        String sql = "INSERT INTO forum_signalement (post_id, user_id, raison) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, raison);
            ps.executeUpdate();
        }

        // Verifier si le seuil est atteint
        if (compterSignalements(postId) >= SEUIL_AUTO_ATTENTE) {
            passerEnAttente(postId);
        }
        return true;
    }

    public boolean aDejaSignale(int postId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_signalement WHERE post_id=? AND user_id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int compterSignalements(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_signalement WHERE post_id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void supprimerSignalements(int postId) throws SQLException {
        String sql = "DELETE FROM forum_signalement WHERE post_id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.executeUpdate();
        }
    }

    private void passerEnAttente(int postId) {
        String sql = "UPDATE forum_post SET statut='EN_ATTENTE' WHERE id=? AND statut='ACCEPTE'";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("SignalementService.passerEnAttente: " + e.getMessage());
        }
    }
}
