package edu.edunova_a.services;

import edu.edunova_a.entities.Seance;
import edu.edunova_a.interfaces.IService;
import edu.edunova_a.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceService implements IService<Seance> {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    private static final String SELECT_BASE =
            "SELECT s.id_se, s.jour_se, s.heure_debut_se, s.heure_fin_se, s.salle_se, " +
                    "s.type_cours_se, s.date_seance, s.classe_id, s.matiere_id, s.teacher_id, " +
                    "c.nom AS classe_nom, m.nom_m AS matiere_nom, " +
                    "CONCAT(t.prenom_t, ' ', t.nom_t) AS teacher_nom " +
                    "FROM seance s " +
                    "LEFT JOIN classe c ON s.classe_id = c.id " +
                    "LEFT JOIN matiere m ON s.matiere_id = m.id_m " +
                    "LEFT JOIN teacher t ON s.teacher_id = t.id_t ";

    /* ====== VALIDATION ====== */

    private void validateSeance(Seance s) {
        if (s.getJour_se() == null || s.getJour_se().trim().isEmpty())
            throw new IllegalArgumentException("Le jour est obligatoire.");
        if (s.getHeure_debut_se() == null || s.getHeure_fin_se() == null)
            throw new IllegalArgumentException("Les heures de début et fin sont obligatoires.");
        if (!s.getHeure_fin_se().isAfter(s.getHeure_debut_se()))
            throw new IllegalArgumentException("L'heure de fin doit être après l'heure de début.");
        if (s.getClasse_id() <= 0)
            throw new IllegalArgumentException("La classe est obligatoire.");
        if (s.getMatiere_id() <= 0)
            throw new IllegalArgumentException("La matière est obligatoire.");
        if (s.getTeacher_id() <= 0)
            throw new IllegalArgumentException("L'enseignant est obligatoire.");
    }

    /** Vérifie s'il existe un conflit horaire pour la même classe, le même jour. */
    public boolean hasConflict(Seance s) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seance " +
                "WHERE classe_id = ? AND jour_se = ? AND id_se != ? " +
                "AND heure_debut_se < ? AND heure_fin_se > ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getClasse_id());
            ps.setString(2, s.getJour_se());
            ps.setInt(3, s.getId_se()); // 0 pour nouvelle séance
            ps.setTime(4, java.sql.Time.valueOf(s.getHeure_fin_se()));
            ps.setTime(5, java.sql.Time.valueOf(s.getHeure_debut_se()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Compte le nombre total de séances. */
    public int countAll() throws SQLException {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM seance")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Séances à venir (date >= aujourd'hui). */
    public List<Seance> findUpcoming(int limit) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE s.date_seance >= CURDATE() ORDER BY s.date_seance ASC, s.heure_debut_se ASC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /* ====== CRUD utilisé par les contrôleurs ====== */

    public void ajouter(Seance s) throws SQLException {
        validateSeance(s);
        String sql = "INSERT INTO seance " +
                "(jour_se, heure_debut_se, heure_fin_se, salle_se, type_cours_se, " +
                "date_seance, classe_id, matiere_id, teacher_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getJour_se());
            ps.setTime(2, Time.valueOf(s.getHeure_debut_se()));
            ps.setTime(3, Time.valueOf(s.getHeure_fin_se()));
            ps.setString(4, s.getSalle_se());
            ps.setString(5, s.getType_cours_se());
            if (s.getDate_seance() != null) ps.setDate(6, Date.valueOf(s.getDate_seance()));
            else ps.setNull(6, Types.DATE);
            ps.setInt(7, s.getClasse_id());
            ps.setInt(8, s.getMatiere_id());
            ps.setInt(9, s.getTeacher_id());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId_se(keys.getInt(1));
            }
        }
    }

    public void modifier(Seance s) throws SQLException {
        validateSeance(s);
        String sql = "UPDATE seance SET jour_se=?, heure_debut_se=?, heure_fin_se=?, " +
                "salle_se=?, type_cours_se=?, date_seance=?, classe_id=?, " +
                "matiere_id=?, teacher_id=? WHERE id_se=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getJour_se());
            ps.setTime(2, Time.valueOf(s.getHeure_debut_se()));
            ps.setTime(3, Time.valueOf(s.getHeure_fin_se()));
            ps.setString(4, s.getSalle_se());
            ps.setString(5, s.getType_cours_se());
            if (s.getDate_seance() != null) ps.setDate(6, Date.valueOf(s.getDate_seance()));
            else ps.setNull(6, Types.DATE);
            ps.setInt(7, s.getClasse_id());
            ps.setInt(8, s.getMatiere_id());
            ps.setInt(9, s.getTeacher_id());
            ps.setInt(10, s.getId_se());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM seance WHERE id_se = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Seance> afficher() throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY s.id_se DESC";
        try (Statement st = cnx.createStatement()) {
            try (ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Seance> chercher(String mot) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = SELECT_BASE +
                "WHERE s.jour_se LIKE ? OR s.salle_se LIKE ? OR s.type_cours_se LIKE ? " +
                "OR c.nom LIKE ? OR m.nom_m LIKE ? " +
                "OR CONCAT(t.prenom_t,' ',t.nom_t) LIKE ? " +
                "ORDER BY s.id_se DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String like = "%" + mot + "%";
            for (int i = 1; i <= 6; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Seance map(ResultSet rs) throws SQLException {
        java.sql.Date sqlDate = rs.getDate("date_seance");
        Seance s = new Seance(
                rs.getInt("id_se"),
                rs.getString("jour_se"),
                rs.getTime("heure_debut_se").toLocalTime(),
                rs.getTime("heure_fin_se").toLocalTime(),
                rs.getString("salle_se"),
                rs.getString("type_cours_se"),
                sqlDate != null ? sqlDate.toLocalDate() : null,
                rs.getInt("classe_id"),
                rs.getInt("matiere_id"),
                rs.getInt("teacher_id")
        );
        s.setClasse_nom(rs.getString("classe_nom"));
        s.setMatiere_nom(rs.getString("matiere_nom"));
        s.setTeacher_nom(rs.getString("teacher_nom"));
        return s;
    }

    /* ====== Méthodes IService (délèguent vers les méthodes ci-dessus) ====== */

    @Override
    public void addEntity(Seance seance) {
        try { ajouter(seance); }
        catch (SQLException e) {
            System.err.println("[SeanceService] Erreur ajout : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout de la séance", e);
        }
    }

    @Override
    public void deleteEntity(Seance seance) {
        try { supprimer(seance.getId_se()); }
        catch (SQLException e) {
            System.err.println("[SeanceService] Erreur suppression : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de la séance", e);
        }
    }

    @Override
    public void updateEntity(int id, Seance seance) {
        seance.setId_se(id);
        try { modifier(seance); }
        catch (SQLException e) {
            System.err.println("[SeanceService] Erreur modification : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la modification de la séance", e);
        }
    }

    @Override
    public List<Seance> getData() {
        try { return afficher(); }
        catch (SQLException e) {
            System.err.println("[SeanceService] Erreur chargement : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
