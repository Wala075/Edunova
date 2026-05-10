package edunova.connexion.dao;

import edunova.connexion.models.RiskData;
import edunova.connexion.tools.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class RiskDAO {

    /**
     * Enregistre une analyse de risque en base de données
     * Utilise la table 'risk_analysis' existante
     */
    public boolean insertRiskData(RiskData riskData) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Utiliser la table 'risk_analysis' existante
            // Note: date_analyse et heure_connexion utilisent NOW() (pas de placeholders)
            String sql = "INSERT INTO risk_analysis " +
                    "(user_id, date_analyse, adresse_ip, pays_ip, heure_connexion, " +
                    "nb_tentatives_echouees, score_risque, niveau_risque, raisons, action_prise) " +
                    "VALUES (?, NOW(), ?, ?, NOW(), ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Paramètres (8 au total, car date_analyse et heure_connexion utilisent NOW())
            stmt.setInt(1, riskData.getUserId());                    // ? 1 - user_id
            // NOW() pour date_analyse
            stmt.setString(2, riskData.getIpAddress());              // ? 2 - adresse_ip
            stmt.setString(3, riskData.getCountry());                // ? 3 - pays_ip
            // NOW() pour heure_connexion
            stmt.setInt(4, riskData.getFailedAttempts());            // ? 4 - nb_tentatives_echouees
            stmt.setInt(5, riskData.getRiskScore());                 // ? 5 - score_risque
            stmt.setString(6, riskData.getRiskLevel());              // ? 6 - niveau_risque
            stmt.setString(7, generateRiskReasons(riskData));        // ? 7 - raisons
            stmt.setString(8, riskData.isBlocked() ? "BLOQUÉ" : "AUTORISÉ"); // ? 8 - action_prise

            int result = stmt.executeUpdate();
            System.out.println("✅ Analyse de risque enregistrée: Score=" + riskData.getRiskScore() + 
                             ", Utilisateur=" + riskData.getUserId() + 
                             ", IP=" + riskData.getIpAddress());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement du risque: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère les raisons du risque
     */
    private String generateRiskReasons(RiskData riskData) {
        StringBuilder reasons = new StringBuilder();
        
        if (riskData.getFailedAttempts() > 0) {
            reasons.append(riskData.getFailedAttempts()).append(" tentative(s) échouée(s), ");
        }
        
        LocalDateTime loginTime = riskData.getLoginTime();
        int hour = loginTime.getHour();
        if (hour < 6 || hour > 22) {
            reasons.append("Heure suspecte (").append(hour).append("h), ");
        }
        
        if (reasons.length() > 0) {
            reasons.setLength(reasons.length() - 2); // Supprimer la dernière virgule
        } else {
            reasons.append("Heure normale (").append(hour).append("h)");
        }
        
        return reasons.toString();
    }

    /**
     * Récupère l'historique de connexion d'un utilisateur
     */
    public Map<String, Object> getUserConnectionHistory(int userId) {
        Map<String, Object> history = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Récupérer les IPs précédentes
            String sqlIPs = "SELECT DISTINCT adresse_ip FROM risk_analysis " +
                    "WHERE user_id = ? ORDER BY date_analyse DESC LIMIT 5";
            PreparedStatement stmtIPs = conn.prepareStatement(sqlIPs);
            stmtIPs.setInt(1, userId);
            ResultSet rsIPs = stmtIPs.executeQuery();

            List<String> previousIPs = new ArrayList<>();
            while (rsIPs.next()) {
                previousIPs.add(rsIPs.getString("adresse_ip"));
            }
            history.put("previousIPs", previousIPs);

            // Récupérer le dernier pays
            String sqlCountry = "SELECT pays_ip FROM risk_analysis " +
                    "WHERE user_id = ? ORDER BY date_analyse DESC LIMIT 1";
            PreparedStatement stmtCountry = conn.prepareStatement(sqlCountry);
            stmtCountry.setInt(1, userId);
            ResultSet rsCountry = stmtCountry.executeQuery();

            if (rsCountry.next()) {
                history.put("lastCountry", rsCountry.getString("pays_ip"));
            }

            return history;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'historique: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Récupère les analyses de risque d'un utilisateur
     */
    public List<RiskData> getUserRiskAnalyses(int userId, int limit) {
        List<RiskData> analyses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM risk_analysis " +
                    "WHERE user_id = ? ORDER BY date_analyse DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RiskData risk = new RiskData();
                risk.setId(rs.getInt("id_ra"));
                risk.setUserId(rs.getInt("user_id"));
                risk.setIpAddress(rs.getString("adresse_ip"));
                risk.setCountry(rs.getString("pays_ip"));
                risk.setLoginTime(rs.getTimestamp("heure_connexion").toLocalDateTime());
                risk.setFailedAttempts(rs.getInt("nb_tentatives_echouees"));
                risk.setRiskScore(rs.getInt("score_risque"));
                risk.setRiskLevel(rs.getString("niveau_risque"));
                risk.setBlocked("BLOQUÉ".equals(rs.getString("action_prise")));
                risk.setCreatedAt(rs.getTimestamp("date_analyse").toLocalDateTime());

                analyses.add(risk);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des analyses: " + e.getMessage());
            e.printStackTrace();
        }

        return analyses;
    }

    /**
     * Récupère les connexions bloquées
     */
    public List<RiskData> getBlockedConnections(int limit) {
        List<RiskData> blocked = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM risk_analysis " +
                    "WHERE action_prise = 'BLOQUÉ' ORDER BY date_analyse DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RiskData risk = new RiskData();
                risk.setId(rs.getInt("id_ra"));
                risk.setUserId(rs.getInt("user_id"));
                risk.setIpAddress(rs.getString("adresse_ip"));
                risk.setCountry(rs.getString("pays_ip"));
                risk.setRiskScore(rs.getInt("score_risque"));
                risk.setRiskLevel(rs.getString("niveau_risque"));
                risk.setBlocked(true);
                risk.setCreatedAt(rs.getTimestamp("date_analyse").toLocalDateTime());

                blocked.add(risk);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des connexions bloquées: " + e.getMessage());
            e.printStackTrace();
        }

        return blocked;
    }

    /**
     * Récupère les statistiques de risque pour un utilisateur
     */
    public Map<String, Object> getUserRiskStatistics(int userId) {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT " +
                    "COUNT(*) as total_logins, " +
                    "SUM(CASE WHEN action_prise = 'BLOQUÉ' THEN 1 ELSE 0 END) as blocked_logins, " +
                    "AVG(score_risque) as avg_risk_score, " +
                    "MAX(score_risque) as max_risk_score, " +
                    "MIN(score_risque) as min_risk_score " +
                    "FROM risk_analysis WHERE user_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("totalLogins", rs.getInt("total_logins"));
                stats.put("blockedLogins", rs.getInt("blocked_logins"));
                stats.put("avgRiskScore", rs.getDouble("avg_risk_score"));
                stats.put("maxRiskScore", rs.getInt("max_risk_score"));
                stats.put("minRiskScore", rs.getInt("min_risk_score"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Récupère les statistiques globales de risque
     */
    public Map<String, Object> getGlobalRiskStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Requête pour les statistiques de risque
            String sql = "SELECT " +
                    "COUNT(*) as total_logins, " +
                    "SUM(CASE WHEN action_prise = 'BLOQUÉ' THEN 1 ELSE 0 END) as blocked_logins, " +
                    "AVG(score_risque) as avg_risk_score, " +
                    "MAX(score_risque) as max_risk_score, " +
                    "MIN(score_risque) as min_risk_score, " +
                    "COUNT(DISTINCT user_id) as unique_users, " +
                    "SUM(CASE WHEN score_risque >= 60 THEN 1 ELSE 0 END) as high_risk_count, " +
                    "MAX(date_analyse) as last_connection_time " +
                    "FROM risk_analysis";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                stats.put("totalLogins", rs.getInt("total_logins"));
                stats.put("blockedLogins", rs.getInt("blocked_logins"));
                stats.put("avgRiskScore", rs.getDouble("avg_risk_score"));
                stats.put("maxRiskScore", rs.getInt("max_risk_score"));
                stats.put("minRiskScore", rs.getInt("min_risk_score"));
                stats.put("uniqueUsers", rs.getInt("unique_users"));
                stats.put("highRiskCount", rs.getInt("high_risk_count"));
                
                // Dernière connexion
                java.sql.Timestamp lastTime = rs.getTimestamp("last_connection_time");
                if (lastTime != null) {
                    stats.put("lastConnectionTime", lastTime.toLocalDateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                } else {
                    stats.put("lastConnectionTime", "N/A");
                }
                
                stats.put("avgConnectionTime", 0.0);
            }
            
            // Requête pour compter les tentatives échouées (failed password attempts)
            String sqlFailedAttempts = "SELECT COUNT(*) as failed_attempts FROM login_history WHERE succes_lh = 0";
            Statement stmtFailed = conn.createStatement();
            ResultSet rsFailed = stmtFailed.executeQuery(sqlFailedAttempts);
            
            if (rsFailed.next()) {
                stats.put("failedAttempts", rsFailed.getInt("failed_attempts"));
            } else {
                stats.put("failedAttempts", 0);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques globales: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Récupère les connexions à risque élevé
     */
    public List<Map<String, Object>> getHighRiskConnections(int limit) {
        List<Map<String, Object>> connections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT r.*, u.email_u, u.nom_u, u.prenom_u " +
                    "FROM risk_analysis r " +
                    "JOIN user u ON r.user_id = u.id_u " +
                    "WHERE r.score_risque >= 60 " +
                    "ORDER BY r.score_risque DESC, r.date_analyse DESC " +
                    "LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> connection = new HashMap<>();
                connection.put("id", rs.getInt("id_ra"));
                connection.put("userId", rs.getInt("user_id"));
                connection.put("email", rs.getString("email_u"));
                connection.put("nom", rs.getString("nom_u"));
                connection.put("prenom", rs.getString("prenom_u"));
                connection.put("ipAddress", rs.getString("adresse_ip"));
                connection.put("country", rs.getString("pays_ip"));
                connection.put("riskScore", rs.getInt("score_risque"));
                connection.put("riskLevel", rs.getString("niveau_risque"));
                connection.put("reasons", rs.getString("raisons"));
                connection.put("action", rs.getString("action_prise"));
                connection.put("date", rs.getTimestamp("date_analyse").toLocalDateTime());
                
                // Ajouter les nouveaux champs s'ils existent
                try {
                    connection.put("connectionTime", rs.getDouble("temps_connexion_ms"));
                    connection.put("typingSpeed", rs.getDouble("vitesse_ecriture"));
                } catch (SQLException ex) {
                    // Les champs n'existent pas, utiliser des valeurs par défaut
                    connection.put("connectionTime", 0.0);
                    connection.put("typingSpeed", 0.0);
                }

                connections.add(connection);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des connexions à risque: " + e.getMessage());
            e.printStackTrace();
        }

        return connections;
    }
}
