package edu.edunova.notifications;

import edu.edunova.utils.MyConnection;

import java.sql.*;

/**
 * Log des emails envoyés via Brevo dans la table `email_log`.
 */
public class EmailLogService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    /**
     * Enregistre un envoi (succès ou échec).
     *
     * @param destinataire email destinataire
     * @param sujet        sujet de l'email
     * @param succes       true si envoyé, false sinon
     * @param erreur       message d'erreur (null si succès)
     * @param alerteId     id alerte associée (null sinon)
     * @param studentId    id élève (0 si aucun)
     * @return id généré (-1 si échec d'écriture)
     */
    public int log(String destinataire, String sujet, boolean succes,
                   String erreur, Integer alerteId, int studentId) {
        String q = "INSERT INTO email_log " +
                "(destinataire, sujet, statut, erreur, alerte_id, student_id) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement pst = cnx.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, destinataire);
            pst.setString(2, sujet);
            pst.setString(3, succes ? "ENVOYE" : "ECHEC");
            pst.setString(4, erreur);
            if (alerteId != null) pst.setInt(5, alerteId); else pst.setNull(5, Types.INTEGER);
            if (studentId > 0)    pst.setInt(6, studentId); else pst.setNull(6, Types.INTEGER);
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Si la table n'existe pas, on log juste en console (pas de cascade d'erreur)
            System.err.println("EmailLogService.log : " + e.getMessage());
        }
        return -1;
    }

    public int countSuccess() {
        return countByStatus("ENVOYE");
    }

    public int countFailure() {
        return countByStatus("ECHEC");
    }

    private int countByStatus(String s) {
        try (PreparedStatement pst = cnx.prepareStatement(
                "SELECT COUNT(*) FROM email_log WHERE statut = ?")) {
            pst.setString(1, s);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("EmailLogService.countByStatus : " + e.getMessage());
        }
        return 0;
    }
}
