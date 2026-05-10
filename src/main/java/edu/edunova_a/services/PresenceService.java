package edu.edunova_a.services;
import edu.edunova_a.entities.Presence;
import edu.edunova_a.interfaces.IService;
import edu.edunova_a.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresenceService implements IService<Presence> {

    private final Connection cnx = MyConnection.getInstance().getCnx();
    private final NotificationService notificationService = new NotificationService();

    private static final String SELECT_BASE =
            "SELECT p.id_pr, p.statut_pr, p.date_presence_pr, p.heure_arrivee_pr, " +
                    "p.justificatif_path_pr, p.commentaire_pr, p.student_id, p.seance_id, " +
                    "p.live_session_id, " +
                    "CONCAT(st.prenom_s, ' ', st.nom_s) AS student_nom, " +
                    "CONCAT(IFNULL(s.jour_se,''), ' ', IFNULL(TIME_FORMAT(s.heure_debut_se,'%H:%i'),''), " +
                    "       ' - ', IFNULL(m.nom_m,'')) AS seance_label, " +
                    "ls.titre_ls AS live_titre " +
                    "FROM presence p " +
                    "LEFT JOIN student st ON p.student_id = st.id_s " +
                    "LEFT JOIN seance s   ON p.seance_id  = s.id_se " +
                    "LEFT JOIN matiere m  ON s.matiere_id = m.id_m " +
                    "LEFT JOIN live_session ls ON p.live_session_id = ls.id_ls ";

    public void ajouter(Presence p) throws SQLException {
        validatePresence(p);
        String sql = "INSERT INTO presence " +
                "(statut_pr, date_presence_pr, heure_arrivee_pr, justificatif_path_pr, " +
                "commentaire_pr, student_id, seance_id, live_session_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getStatut_pr());
            ps.setDate(2, Date.valueOf(p.getDate_presence_pr()));
            if (p.getHeure_arrivee_pr() != null) ps.setTime(3, Time.valueOf(p.getHeure_arrivee_pr()));
            else ps.setNull(3, Types.TIME);
            ps.setString(4, p.getJustificatif_path_pr());
            ps.setString(5, p.getCommentaire_pr());
            ps.setInt(6, p.getStudent_id());
            if (p.getSeance_id() != null) ps.setInt(7, p.getSeance_id());
            else ps.setNull(7, Types.INTEGER);
            if (p.getLive_session_id() != null) ps.setInt(8, p.getLive_session_id());
            else ps.setNull(8, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId_pr(keys.getInt(1));
            }
        }

        // ── Hook : si l'élève est marqué ABSENT, on notifie le parent en async ──
        if ("ABSENT".equalsIgnoreCase(p.getStatut_pr())) {
            final int  presenceId    = p.getId_pr();
            final int  studentId     = p.getStudent_id();
            final Integer seanceId   = p.getSeance_id();
            final Integer liveId     = p.getLive_session_id();
            final java.time.LocalDate date = p.getDate_presence_pr();
            new Thread(() -> notificationService.notifierParentAbsence(
                    presenceId, studentId, seanceId, liveId, date),
                    "notif-sms-" + presenceId).start();
        }
    }

    public void modifier(Presence p) throws SQLException {
        validatePresence(p);
        String sql = "UPDATE presence SET statut_pr=?, date_presence_pr=?, " +
                "heure_arrivee_pr=?, justificatif_path_pr=?, commentaire_pr=?, " +
                "student_id=?, seance_id=?, live_session_id=? WHERE id_pr=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getStatut_pr());
            ps.setDate(2, Date.valueOf(p.getDate_presence_pr()));
            if (p.getHeure_arrivee_pr() != null) ps.setTime(3, Time.valueOf(p.getHeure_arrivee_pr()));
            else ps.setNull(3, Types.TIME);
            ps.setString(4, p.getJustificatif_path_pr());
            ps.setString(5, p.getCommentaire_pr());
            ps.setInt(6, p.getStudent_id());
            if (p.getSeance_id() != null) ps.setInt(7, p.getSeance_id());
            else ps.setNull(7, Types.INTEGER);
            if (p.getLive_session_id() != null) ps.setInt(8, p.getLive_session_id());
            else ps.setNull(8, Types.INTEGER);
            ps.setInt(9, p.getId_pr());
            ps.executeUpdate();
        }

        // ── Hook : déclenche aussi le SMS si on passe à ABSENT en modification ──
        // L'anti-doublon de NotificationService évite de spammer si on modifie plusieurs fois.
        if ("ABSENT".equalsIgnoreCase(p.getStatut_pr())) {
            final int  presenceId  = p.getId_pr();
            final int  studentId   = p.getStudent_id();
            final Integer seanceId = p.getSeance_id();
            final Integer liveId   = p.getLive_session_id();
            final java.time.LocalDate date = p.getDate_presence_pr();
            new Thread(() -> notificationService.notifierParentAbsence(
                    presenceId, studentId, seanceId, liveId, date),
                    "notif-sms-mod-" + presenceId).start();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM presence WHERE id_pr = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Presence> afficher() throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY p.date_presence_pr DESC, p.id_pr DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Presence> chercher(String mot) throws SQLException {
        List<Presence> list = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE p.statut_pr LIKE ? OR p.commentaire_pr LIKE ? " +
                "OR CONCAT(st.prenom_s,' ',st.nom_s) LIKE ? OR ls.titre_ls LIKE ? " +
                "OR m.nom_m LIKE ? " +
                "ORDER BY p.date_presence_pr DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String like = "%" + mot + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Presence map(ResultSet rs) throws SQLException {
        Time heureArr = rs.getTime("heure_arrivee_pr");
        Integer seanceId = rs.getObject("seance_id") != null ? rs.getInt("seance_id") : null;
        Integer liveId   = rs.getObject("live_session_id") != null ? rs.getInt("live_session_id") : null;

        // Gère les dates nulles ou "0000-00-00" (zeroDateTimeBehavior=CONVERT_TO_NULL)
        java.sql.Date sqlDate = rs.getDate("date_presence_pr");
        java.time.LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate()
                                                          : java.time.LocalDate.now();

        Presence p = new Presence(
                rs.getInt("id_pr"),
                rs.getString("statut_pr"),
                localDate,
                heureArr != null ? heureArr.toLocalTime() : null,
                rs.getString("justificatif_path_pr"),
                rs.getString("commentaire_pr"),
                rs.getInt("student_id"),
                seanceId,
                liveId
        );
        p.setStudent_nom(rs.getString("student_nom"));
        p.setSeance_label(rs.getString("seance_label"));
        p.setLive_titre(rs.getString("live_titre"));
        return p;
    }

    /* ====== VALIDATION ====== */

    private void validatePresence(Presence p) {
        if (p.getStatut_pr() == null || p.getStatut_pr().trim().isEmpty())
            throw new IllegalArgumentException("Le statut de présence est obligatoire.");
        if (p.getDate_presence_pr() == null)
            throw new IllegalArgumentException("La date de présence est obligatoire.");
        if (p.getStudent_id() <= 0)
            throw new IllegalArgumentException("L'étudiant est obligatoire.");
        if (p.getSeance_id() == null && p.getLive_session_id() == null)
            throw new IllegalArgumentException("Une séance ou session live est obligatoire.");
    }

    /* ====== IService ====== */

    @Override
    public void addEntity(Presence p) {
        try { ajouter(p); }
        catch (SQLException e) {
            System.err.println("[PresenceService] Erreur ajout : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout de la présence", e);
        }
    }

    @Override
    public void deleteEntity(Presence p) {
        try { supprimer(p.getId_pr()); }
        catch (SQLException e) {
            System.err.println("[PresenceService] Erreur suppression : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    @Override
    public void updateEntity(int id, Presence p) {
        p.setId_pr(id);
        try { modifier(p); }
        catch (SQLException e) {
            System.err.println("[PresenceService] Erreur modification : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la modification", e);
        }
    }

    @Override
    public List<Presence> getData() {
        try { return afficher(); }
        catch (SQLException e) {
            System.err.println("[PresenceService] Erreur chargement : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}