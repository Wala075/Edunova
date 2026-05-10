package edu.edunova.services;

import edu.edunova.entities.Alerte;
import edu.edunova.entities.Alerte.Severite;
import edu.edunova.entities.Alerte.Statut;
import edu.edunova.entities.Alerte.TypeAlerte;
import edu.edunova.utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD + requêtes spécifiques pour les alertes.
 */
public class AlerteService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    // ============================================================
    // CREATE
    // ============================================================

    /** Insère une alerte et renvoie l'id généré (-1 si échec). */
    public int addAlerte(Alerte a) {
        String q = "INSERT INTO alerte (type_alerte, severite, titre, message, " +
                "student_id, matiere_id, trimestre, annee_scolaire, statut, " +
                "email_envoye, sms_envoye) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = cnx.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, a.getTypeAlerte() != null ? a.getTypeAlerte().name() : null);
            pst.setString(2, a.getSeverite()   != null ? a.getSeverite().name()   : Severite.INFO.name());
            pst.setString(3, a.getTitre());
            pst.setString(4, a.getMessage());
            // student_id : NULL pour les alertes globales (studentId <= 0)
            if (a.getStudentId() > 0) pst.setInt(5, a.getStudentId());
            else                      pst.setNull(5, Types.INTEGER);
            if (a.getMatiereId() != null) pst.setInt(6, a.getMatiereId()); else pst.setNull(6, Types.INTEGER);
            if (a.getTrimestre() != null) pst.setInt(7, a.getTrimestre()); else pst.setNull(7, Types.INTEGER);
            pst.setString(8, a.getAnneeScolaire());
            pst.setString(9, a.getStatut() != null ? a.getStatut().name() : Statut.NOUVELLE.name());
            pst.setBoolean(10, a.isEmailEnvoye());
            pst.setBoolean(11, a.isSmsEnvoye());

            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    a.setId_a(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("AlerteService.addAlerte : " + e.getMessage());
        }
        return -1;
    }

    /**
     * Évite les doublons : si une alerte du même type, même élève, même matière,
     * même trimestre, même année existe déjà => ne pas réinsérer.
     */
    public int addIfNotExists(Alerte a) {
        if (existsSimilar(a)) return 0;
        return addAlerte(a);
    }

    public boolean existsSimilar(Alerte a) {
        StringBuilder sb = new StringBuilder(
                "SELECT 1 FROM alerte WHERE type_alerte = ? " +
                        "AND IFNULL(annee_scolaire,'') = IFNULL(?, '')");
        // student_id : NULL si alerte globale
        if (a.getStudentId() > 0) sb.append(" AND student_id = ?");
        else sb.append(" AND student_id IS NULL");
        if (a.getMatiereId() != null) sb.append(" AND matiere_id = ?");
        else sb.append(" AND matiere_id IS NULL");
        if (a.getTrimestre() != null) sb.append(" AND trimestre = ?");
        else sb.append(" AND trimestre IS NULL");
        sb.append(" LIMIT 1");

        try (PreparedStatement pst = cnx.prepareStatement(sb.toString())) {
            int idx = 1;
            pst.setString(idx++, a.getTypeAlerte().name());
            pst.setString(idx++, a.getAnneeScolaire());
            if (a.getStudentId() > 0) pst.setInt(idx++, a.getStudentId());
            if (a.getMatiereId() != null) pst.setInt(idx++, a.getMatiereId());
            if (a.getTrimestre() != null) pst.setInt(idx++, a.getTrimestre());

            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("AlerteService.existsSimilar : " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // READ
    // ============================================================

    /** Toutes les alertes, plus récentes d'abord, avec nom élève + matière joints. */
    public List<Alerte> getAll() {
        return query(baseSelect() + " ORDER BY a.date_creation DESC", null);
    }

    public List<Alerte> getByStatut(Statut statut) {
        return query(baseSelect() + " WHERE a.statut = ? ORDER BY a.date_creation DESC",
                ps -> ps.setString(1, statut.name()));
    }

    public List<Alerte> getBySeverite(Severite severite) {
        return query(baseSelect() + " WHERE a.severite = ? ORDER BY a.date_creation DESC",
                ps -> ps.setString(1, severite.name()));
    }

    public List<Alerte> getByStudent(int studentId) {
        return query(baseSelect() + " WHERE a.student_id = ? ORDER BY a.date_creation DESC",
                ps -> ps.setInt(1, studentId));
    }

    public List<Alerte> getNouvelles() { return getByStatut(Statut.NOUVELLE); }

    public int countNouvelles() {
        try (PreparedStatement pst = cnx.prepareStatement(
                "SELECT COUNT(*) FROM alerte WHERE statut = 'NOUVELLE'")) {
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("AlerteService.countNouvelles : " + e.getMessage());
        }
        return 0;
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public void marquerStatut(int idAlerte, Statut s) {
        try (PreparedStatement pst = cnx.prepareStatement(
                "UPDATE alerte SET statut = ? WHERE id_a = ?")) {
            pst.setString(1, s.name());
            pst.setInt(2, idAlerte);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AlerteService.marquerStatut : " + e.getMessage());
        }
    }

    public void marquerEmailEnvoye(int idAlerte) {
        flagUpdate("email_envoye", idAlerte);
    }

    public void marquerSmsEnvoye(int idAlerte) {
        flagUpdate("sms_envoye", idAlerte);
    }

    private void flagUpdate(String col, int idAlerte) {
        try (PreparedStatement pst = cnx.prepareStatement(
                "UPDATE alerte SET " + col + " = TRUE WHERE id_a = ?")) {
            pst.setInt(1, idAlerte);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AlerteService.flagUpdate(" + col + ") : " + e.getMessage());
        }
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(int idAlerte) {
        try (PreparedStatement pst = cnx.prepareStatement(
                "DELETE FROM alerte WHERE id_a = ?")) {
            pst.setInt(1, idAlerte);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AlerteService.delete : " + e.getMessage());
        }
    }

    public void deleteAll() {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate("DELETE FROM alerte");
        } catch (SQLException e) {
            System.err.println("AlerteService.deleteAll : " + e.getMessage());
        }
    }

    // ============================================================
    // Helpers privés
    // ============================================================

    private String baseSelect() {
        return "SELECT a.*, " +
                "       CONCAT_WS(' ', s.nom_s, s.prenom_s) AS student_nom, " +
                "       m.nom_m AS matiere_nom " +
                "FROM alerte a " +
                "LEFT JOIN student s ON s.id_s = a.student_id " +
                "LEFT JOIN matiere m ON m.id_m = a.matiere_id";
    }

    /** Petit functional interface interne pour binder les params. */
    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Alerte> query(String sql, Binder binder) {
        List<Alerte> result = new ArrayList<>();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            if (binder != null) binder.bind(pst);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) result.add(rowToAlerte(rs));
            }
        } catch (SQLException e) {
            System.err.println("AlerteService.query : " + e.getMessage());
        }
        return result;
    }

    private Alerte rowToAlerte(ResultSet rs) throws SQLException {
        Alerte a = new Alerte();
        a.setId_a(rs.getInt("id_a"));
        a.setTypeAlerte(safeEnum(TypeAlerte.class, rs.getString("type_alerte")));
        a.setSeverite(safeEnum(Severite.class, rs.getString("severite")));
        a.setTitre(rs.getString("titre"));
        a.setMessage(rs.getString("message"));
        a.setStudentId(rs.getInt("student_id"));

        int matId = rs.getInt("matiere_id");
        a.setMatiereId(rs.wasNull() ? null : matId);

        int tri = rs.getInt("trimestre");
        a.setTrimestre(rs.wasNull() ? null : tri);

        a.setAnneeScolaire(rs.getString("annee_scolaire"));

        Timestamp ts = rs.getTimestamp("date_creation");
        a.setDateCreation(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());

        a.setStatut(safeEnum(Statut.class, rs.getString("statut")));
        a.setEmailEnvoye(rs.getBoolean("email_envoye"));
        a.setSmsEnvoye(rs.getBoolean("sms_envoye"));

        // Colonnes jointes (peuvent ne pas exister selon la requête)
        try { a.setStudentNom(rs.getString("student_nom")); } catch (SQLException ignore) {}
        try { a.setMatiereNom(rs.getString("matiere_nom")); } catch (SQLException ignore) {}

        return a;
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> cls, String value) {
        if (value == null) return null;
        try { return Enum.valueOf(cls, value); }
        catch (IllegalArgumentException e) { return null; }
    }
}
