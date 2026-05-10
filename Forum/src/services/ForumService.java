package services;

import entities.ForumPost;
import entities.ForumPost.Statut;
import utils.DatabaseConnection;
import services.NotificationService;
import services.ReputationService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ForumService implements interfaces.IForumService {
    private final NotificationService notifService     = new NotificationService();
    private final ReputationService   reputationService = new ReputationService();

    // ── CREATE ──────────────────────────────────────────────
    /** Soumet un post (statut EN_ATTENTE par defaut). */
    public int soumettre(ForumPost p) throws SQLException {
        String sql = "INSERT INTO forum_post (titre,contenu,photo_path,auteur_id,auteur_nom,auteur_role,statut) VALUES (?,?,?,?,?,?,'EN_ATTENTE')";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getContenu());
            ps.setString(3, p.getPhotoPath());
            ps.setInt(4, p.getAuteurId());
            ps.setString(5, p.getAuteurNom());
            ps.setString(6, p.getAuteurRole());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { p.setId(k.getInt(1)); return p.getId(); }
            }
        }
        return -1;
    }

    /** Admin publie directement un post en ACCEPTE — visible immediatement. */
    public int publierAdmin(ForumPost p) throws SQLException {
        String sql = "INSERT INTO forum_post (titre,contenu,photo_path,auteur_id,auteur_nom,auteur_role,statut) VALUES (?,?,?,?,?,?,'ACCEPTE')";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getContenu());
            ps.setString(3, p.getPhotoPath());
            ps.setInt(4, p.getAuteurId());
            ps.setString(5, p.getAuteurNom());
            ps.setString(6, p.getAuteurRole());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { p.setId(k.getInt(1)); return p.getId(); }
            }
        }
        return -1;
    }

    // ── UPDATE STATUT (Admin) ────────────────────────────────
    public void changerStatut(int postId, Statut statut) throws SQLException {
        String sql = "UPDATE forum_post SET statut=? WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, postId);
            ps.executeUpdate();
        }
    }

    /** Accepte un post — statut passe a ACCEPTE (visible par tous les membres). */
    public void acceptPost(int postId) throws SQLException {
        ForumPost p = getById(postId);
        changerStatut(postId, Statut.ACCEPTE);
        if (p != null) {
            notifService.creer(p.getAuteurId(),
                "Votre post " + p.getTitre() + " a ete accepte et est maintenant visible.", "ACCEPTE");
            reputationService.ajouterPoints(p.getAuteurId(), ReputationService.Action.POST_ACCEPTE);
        }
    }

    public void refusePost(int postId) throws SQLException {
        ForumPost p = getById(postId);
        changerStatut(postId, Statut.REFUSE);
        if (p != null) {
            notifService.creer(p.getAuteurId(),
                "Votre post " + p.getTitre() + " a ete refuse par l'administrateur.", "REFUSE");
            reputationService.ajouterPoints(p.getAuteurId(), ReputationService.Action.POST_REFUSE);
        }
    }

    public void approuver(int postId) throws SQLException { acceptPost(postId); }
    public void rejeter(int postId)   throws SQLException { refusePost(postId); }

    /**
     * Modification d un post par son auteur.
     * Le post repasse en EN_ATTENTE pour re-validation par l admin.
     */
    public void modifierPost(int postId, String titre, String contenu, int userId) throws SQLException {
        String sql = "UPDATE forum_post SET titre=?, contenu=?, statut='EN_ATTENTE', date_modif=NOW() " +
                     "WHERE id=? AND auteur_id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, titre);
            ps.setString(2, contenu);
            ps.setInt(3, postId);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    public ForumPost getById(int id) {
        String sql = "SELECT * FROM forum_post WHERE id = ?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("ForumService.getById(" + id + ") : " + e.getMessage());
        }
        return null;
    }

    // ── DELETE ──────────────────────────────────────────────
    public void supprimer(int postId) throws SQLException {
        String sql = "DELETE FROM forum_post WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, postId); ps.executeUpdate();
        }
    }

    // ── READ (Admin) ─────────────────────────────────────────
    /** Tous les posts (admin voit tout). */
    public List<ForumPost> getTous() throws SQLException {
        return query("SELECT * FROM forum_post ORDER BY date_creation DESC");
    }

    /** Filtrer par statut. */
    public List<ForumPost> getParStatut(Statut statut) throws SQLException {
        String sql = "SELECT * FROM forum_post WHERE statut=? ORDER BY date_creation DESC";
        return queryParam(sql, statut.name());
    }

    /** Recherche dans titre + contenu (admin). */
    public List<ForumPost> rechercher(String kw) throws SQLException {
        String sql = "SELECT * FROM forum_post WHERE titre LIKE ? OR contenu LIKE ? ORDER BY date_creation DESC";
        String k = "%" + kw.trim() + "%";
        List<ForumPost> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    // ── READ (Etudiant/Parent) ───────────────────────────────
    /** Seulement les posts acceptes — visible par tous les connectes. */
    public List<ForumPost> getApprouves() throws SQLException {
        return query("SELECT * FROM forum_post WHERE statut='ACCEPTE' ORDER BY date_creation DESC");
    }

    /** Paginated approved posts — page is 0-based, pageSize is number of posts per page. */
    public List<ForumPost> getApprouvesPagines(int page, int pageSize) throws SQLException {
        String sql = "SELECT * FROM forum_post WHERE statut='ACCEPTE' ORDER BY date_creation DESC LIMIT ? OFFSET ?";
        List<ForumPost> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Total count of approved posts (for pagination). */
    public int compterApprouves() throws SQLException {
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM forum_post WHERE statut='ACCEPTE'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /** Alias conforme au prompt. */
    public List<ForumPost> getAcceptedPosts() throws SQLException { return getApprouves(); }

    /** Posts acceptes + recherche. */
    public List<ForumPost> rechercherApprouves(String kw) throws SQLException {
        String sql = "SELECT * FROM forum_post WHERE statut='ACCEPTE' AND (titre LIKE ? OR contenu LIKE ?) ORDER BY date_creation DESC";
        String k = "%" + kw.trim() + "%";
        List<ForumPost> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Posts d'un auteur specifique (pour "mes posts"). */
    public List<ForumPost> getMesPostsPar(int auteurId) throws SQLException {
        String sql = "SELECT * FROM forum_post WHERE auteur_id=? ORDER BY date_creation DESC";
        return queryParam(sql, auteurId);
    }

    // ── Compteurs pour dashboard admin ──────────────────────
    public int compterParStatut(Statut statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM forum_post WHERE statut=?";
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        return 0;
    }
    public int compterTous() throws SQLException {
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM forum_post");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Helpers ─────────────────────────────────────────────
    private List<ForumPost> query(String sql) throws SQLException {
        List<ForumPost> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
    private List<ForumPost> queryParam(String sql, Object param) throws SQLException {
        List<ForumPost> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }
    private ForumPost map(ResultSet rs) throws SQLException {
        ForumPost p = new ForumPost();
        p.setId(rs.getInt("id"));
        p.setTitre(rs.getString("titre"));
        p.setContenu(rs.getString("contenu"));
        p.setPhotoPath(rs.getString("photo_path"));
        p.setAuteurId(rs.getInt("auteur_id"));
        p.setAuteurNom(rs.getString("auteur_nom"));
        p.setAuteurRole(rs.getString("auteur_role"));
        String st = rs.getString("statut");
        p.setStatut(st != null ? Statut.valueOf(st) : Statut.EN_ATTENTE);
        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) p.setDateCreation(dc.toLocalDateTime());
        Timestamp dm = rs.getTimestamp("date_modif");
        if (dm != null) p.setDateModif(dm.toLocalDateTime());
        return p;
    }
}
