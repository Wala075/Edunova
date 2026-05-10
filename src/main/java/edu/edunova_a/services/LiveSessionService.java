package edu.edunova_a.services;

import edu.edunova_a.entities.LiveSession;
import edu.edunova_a.interfaces.IService;
import edu.edunova_a.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LiveSessionService implements IService<LiveSession> {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    private static final String SELECT_BASE =
            "SELECT ls.id_ls, ls.titre_ls, ls.date_session_ls, ls.heure_debut_ls, " +
                    "ls.heure_fin_ls, ls.lien_meet_ls, ls.statut_ls, ls.seance_id, " +
                    "ls.zoom_meeting_id, " +
                    "ls.teacher_id, ls.classe_id, ls.matiere_id, " +
                    "c.nom AS classe_nom, m.nom_m AS matiere_nom, " +
                    "CONCAT(t.prenom_t, ' ', t.nom_t) AS teacher_nom " +
                    "FROM live_session ls " +
                    "LEFT JOIN classe c ON ls.classe_id = c.id " +
                    "LEFT JOIN matiere m ON ls.matiere_id = m.id_m " +
                    "LEFT JOIN teacher t ON ls.teacher_id = t.id_t ";

    /* ====== VALIDATION ====== */

    private void validateLiveSession(LiveSession l) {
        if (l.getTitre_ls() == null || l.getTitre_ls().trim().isEmpty())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (l.getDate_session_ls() == null)
            throw new IllegalArgumentException("La date est obligatoire.");
        if (l.getHeure_debut_ls() == null || l.getHeure_fin_ls() == null)
            throw new IllegalArgumentException("Les heures de début et fin sont obligatoires.");
        if (!l.getHeure_fin_ls().isAfter(l.getHeure_debut_ls()))
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début.");
        if (l.getClasse_id() <= 0)
            throw new IllegalArgumentException("La classe est obligatoire.");
        if (l.getMatiere_id() <= 0)
            throw new IllegalArgumentException("La matière est obligatoire.");
        if (l.getTeacher_id() <= 0)
            throw new IllegalArgumentException("L'enseignant est obligatoire.");
    }

    /** Compte le nombre total de sessions live. */
    public int countAll() throws SQLException {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM live_session")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Sessions à venir (date >= aujourd'hui, statut PROGRAMMEE ou EN_COURS). */
    public List<LiveSession> findUpcoming(int limit) throws SQLException {
        List<LiveSession> list = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE ls.date_session_ls >= CURDATE() " +
                "AND ls.statut_ls IN ('PROGRAMMEE','EN_COURS') " +
                "ORDER BY ls.date_session_ls ASC, ls.heure_debut_ls ASC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /* ====== CRUD ====== */

    public void ajouter(LiveSession l) throws SQLException {
        validateLiveSession(l);
        String sql = "INSERT INTO live_session " +
                "(titre_ls, date_session_ls, heure_debut_ls, heure_fin_ls, lien_meet_ls, " +
                "statut_ls, seance_id, teacher_id, classe_id, matiere_id, zoom_meeting_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getTitre_ls());
            ps.setDate(2, Date.valueOf(l.getDate_session_ls()));
            ps.setTime(3, Time.valueOf(l.getHeure_debut_ls()));
            ps.setTime(4, Time.valueOf(l.getHeure_fin_ls()));
            ps.setString(5, l.getLien_meet_ls());
            ps.setString(6, l.getStatut_ls());
            if (l.getSeance_id() != null) ps.setInt(7, l.getSeance_id());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, l.getTeacher_id());
            ps.setInt(9, l.getClasse_id());
            ps.setInt(10, l.getMatiere_id());
            ps.setString(11, l.getZoom_meeting_id());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) l.setId_ls(keys.getInt(1));
            }
        }
    }

    public void modifier(LiveSession l) throws SQLException {
        validateLiveSession(l);
        String sql = "UPDATE live_session SET titre_ls=?, date_session_ls=?, " +
                "heure_debut_ls=?, heure_fin_ls=?, lien_meet_ls=?, statut_ls=?, " +
                "seance_id=?, teacher_id=?, classe_id=?, matiere_id=?, zoom_meeting_id=? WHERE id_ls=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, l.getTitre_ls());
            ps.setDate(2, Date.valueOf(l.getDate_session_ls()));
            ps.setTime(3, Time.valueOf(l.getHeure_debut_ls()));
            ps.setTime(4, Time.valueOf(l.getHeure_fin_ls()));
            ps.setString(5, l.getLien_meet_ls());
            ps.setString(6, l.getStatut_ls());
            if (l.getSeance_id() != null) ps.setInt(7, l.getSeance_id());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, l.getTeacher_id());
            ps.setInt(9, l.getClasse_id());
            ps.setInt(10, l.getMatiere_id());
            ps.setString(11, l.getZoom_meeting_id());
            ps.setInt(12, l.getId_ls());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM live_session WHERE id_ls = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<LiveSession> afficher() throws SQLException {
        List<LiveSession> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY ls.date_session_ls DESC, ls.heure_debut_ls DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<LiveSession> chercher(String mot) throws SQLException {
        List<LiveSession> list = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE ls.titre_ls LIKE ? OR ls.statut_ls LIKE ? OR ls.lien_meet_ls LIKE ? " +
                "OR c.nom LIKE ? OR m.nom_m LIKE ? OR CONCAT(t.prenom_t,' ',t.nom_t) LIKE ? " +
                "ORDER BY ls.date_session_ls DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String like = "%" + mot + "%";
            for (int i = 1; i <= 6; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private LiveSession map(ResultSet rs) throws SQLException {
        Integer seanceId = rs.getObject("seance_id") != null ? rs.getInt("seance_id") : null;
        LiveSession l = new LiveSession(
                rs.getInt("id_ls"),
                rs.getString("titre_ls"),
                rs.getDate("date_session_ls").toLocalDate(),
                rs.getTime("heure_debut_ls").toLocalTime(),
                rs.getTime("heure_fin_ls").toLocalTime(),
                rs.getString("lien_meet_ls"),
                rs.getString("statut_ls"),
                seanceId,
                rs.getInt("teacher_id"),
                rs.getInt("classe_id"),
                rs.getInt("matiere_id")
        );
        l.setClasse_nom(rs.getString("classe_nom"));
        l.setMatiere_nom(rs.getString("matiere_nom"));
        l.setTeacher_nom(rs.getString("teacher_nom"));
        l.setZoom_meeting_id(rs.getString("zoom_meeting_id"));
        return l;
    }

    /** Met à jour uniquement le lien Zoom et l'ID meeting après création via API */
    public void updateZoomInfo(int id, String lien, String zoomMeetingId) throws SQLException {
        String sql = "UPDATE live_session SET lien_meet_ls=?, zoom_meeting_id=? WHERE id_ls=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, lien);
            ps.setString(2, zoomMeetingId);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /* ====== IService ====== */

    @Override
    public void addEntity(LiveSession l) {
        try { ajouter(l); }
        catch (SQLException e) {
            System.err.println("[LiveSessionService] Erreur ajout : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout de la session", e);
        }
    }

    @Override
    public void deleteEntity(LiveSession l) {
        try { supprimer(l.getId_ls()); }
        catch (SQLException e) {
            System.err.println("[LiveSessionService] Erreur suppression : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    @Override
    public void updateEntity(int id, LiveSession l) {
        l.setId_ls(id);
        try { modifier(l); }
        catch (SQLException e) {
            System.err.println("[LiveSessionService] Erreur modification : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la modification", e);
        }
    }

    @Override
    public List<LiveSession> getData() {
        try { return afficher(); }
        catch (SQLException e) {
            System.err.println("[LiveSessionService] Erreur chargement : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
