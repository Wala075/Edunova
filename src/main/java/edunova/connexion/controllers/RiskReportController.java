package edunova.connexion.controllers;

import edunova.connexion.dao.RiskDAO;
import edunova.connexion.tools.RiskAnalyzerIA;
import edunova.connexion.tools.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour afficher le rapport de risque sur le dashboard
 * Affiche les statistiques de risque et les connexions à risque élevé
 * Supporte le mode dark et white avec mise à jour en temps réel
 */
public class RiskReportController {

    @FXML private VBox riskReportPanel;
    @FXML private Label lblTotalLogins;
    @FXML private Label lblBlockedLogins;
    @FXML private Label lblAvgRiskScore;
    @FXML private Label lblConnectionAttempts;
    @FXML private Label lblHighRiskConnectionsCount;
    @FXML private VBox highRiskConnectionsPanel;
    @FXML private VBox failedAttemptsPanel;

    private RiskDAO riskDAO = new RiskDAO();
    private Timeline updateTimeline;

    @FXML
    public void initialize() {
        displayRiskReport();
        startAutoRefresh();
    }

    /**
     * Démarre la mise à jour automatique du rapport toutes les 5 secondes
     */
    private void startAutoRefresh() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            displayRiskReport();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }

    /**
     * Affiche le rapport de risque complet
     */
    private void displayRiskReport() {
        SessionManager session = SessionManager.getInstance();
        int userId = session.getUserId();

        // Afficher les statistiques globales
        displayGlobalStatistics();

        // Afficher les connexions à risque élevé
        displayHighRiskConnections();

        // Afficher les tentatives échouées par compte
        displayFailedLoginAttempts();
    }

    /**
     * Affiche les statistiques globales de risque
     */
    private void displayGlobalStatistics() {
        Map<String, Object> stats = riskDAO.getGlobalRiskStatistics();

        if (stats.isEmpty()) {
            riskReportPanel.setVisible(false);
            return;
        }

        riskReportPanel.setVisible(true);

        // Total de connexions
        int totalLogins = (Integer) stats.getOrDefault("totalLogins", 0);
        lblTotalLogins.setText(String.valueOf(totalLogins));

        // Connexions bloquées
        int blockedLogins = (Integer) stats.getOrDefault("blockedLogins", 0);
        lblBlockedLogins.setText(String.valueOf(blockedLogins));
        double blockPercentage = totalLogins > 0 ? (blockedLogins * 100.0 / totalLogins) : 0;
        lblBlockedLogins.setStyle("-fx-text-fill: " + (blockedLogins > 0 ? "#ef4444" : "#16a34a") + "; " +
                "-fx-font-weight: bold; -fx-font-size: 14;");

        // Score moyen
        double avgScore = (Double) stats.getOrDefault("avgRiskScore", 0.0);
        lblAvgRiskScore.setText(String.format("%.1f", avgScore));
        lblAvgRiskScore.setStyle("-fx-text-fill: " + RiskAnalyzerIA.getScoreColor((int) avgScore) + "; " +
                "-fx-font-weight: bold; -fx-font-size: 14;");

        // Tentatives de connexion (total)
        int totalAttempts = (Integer) stats.getOrDefault("totalLogins", 0);
        lblConnectionAttempts.setText(String.valueOf(totalAttempts));

        // Nombre de connexions à risque élevé
        int highRiskCount = (Integer) stats.getOrDefault("highRiskCount", 0);
        if (lblHighRiskConnectionsCount != null) {
            lblHighRiskConnectionsCount.setText(String.valueOf(highRiskCount));
        }

        System.out.println("📊 Statistiques Globales de Risque (Mise à jour):");
        System.out.println("  Total de connexions: " + totalLogins);
        System.out.println("  Connexions bloquées: " + blockedLogins + " (" + String.format("%.1f", blockPercentage) + "%)");
        System.out.println("  Score moyen: " + String.format("%.1f", avgScore));
        System.out.println("  Tentatives de connexion: " + totalAttempts);
        System.out.println("  Connexions à risque élevé: " + highRiskCount);
    }

    /**
     * Affiche les connexions à risque élevé
     */
    private void displayHighRiskConnections() {
        List<Map<String, Object>> connections = riskDAO.getHighRiskConnections(10);

        highRiskConnectionsPanel.getChildren().clear();

        if (connections.isEmpty()) {
            Label noData = new Label("✅ Aucune connexion à risque élevé");
            noData.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12; -fx-font-weight: bold;");
            highRiskConnectionsPanel.getChildren().add(noData);
            return;
        }

        // Ajouter un en-tête
        HBox header = createHeaderRow();
        highRiskConnectionsPanel.getChildren().add(header);

        // Ajouter les connexions
        for (Map<String, Object> connection : connections) {
            HBox row = createConnectionRow(connection);
            highRiskConnectionsPanel.getChildren().add(row);
        }
    }

    /**
     * Affiche les tentatives de connexion échouées par compte
     */
    private void displayFailedLoginAttempts() {
        if (failedAttemptsPanel == null) {
            return; // Le panneau n'existe pas dans le FXML
        }

        List<Map<String, Object>> failedAttempts = riskDAO.getFailedLoginAttemptsByUser(15);

        failedAttemptsPanel.getChildren().clear();

        if (failedAttempts.isEmpty()) {
            Label noData = new Label("✅ Aucune tentative échouée");
            noData.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12; -fx-font-weight: bold;");
            failedAttemptsPanel.getChildren().add(noData);
            return;
        }

        // Ajouter un titre
        Label title = new Label("📊 Tentatives de Connexion Échouées par Compte");
        title.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-padding: 10 0 10 0;");
        failedAttemptsPanel.getChildren().add(title);

        // Ajouter un en-tête
        HBox header = createFailedAttemptsHeaderRow();
        failedAttemptsPanel.getChildren().add(header);

        // Ajouter les tentatives échouées
        for (Map<String, Object> attempt : failedAttempts) {
            HBox row = createFailedAttemptRow(attempt);
            failedAttemptsPanel.getChildren().add(row);
        }
    }

    /**
     * Crée la ligne d'en-tête pour les connexions à risque
     */
    private HBox createHeaderRow() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 12; -fx-background-color: #f1f5f9; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");

        Label scoreLabel = new Label("Score");
        scoreLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 50;");

        Label userLabel = new Label("Utilisateur");
        userLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 120;");

        Label ipLabel = new Label("IP");
        ipLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 120;");

        Label countryLabel = new Label("Pays");
        countryLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 80;");

        Label connTimeLabel = new Label("Temps");
        connTimeLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 80;");

        Label typingSpeedLabel = new Label("Vitesse");
        typingSpeedLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 80;");

        Label reasonLabel = new Label("Raison");
        reasonLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 150;");

        Label actionLabel = new Label("Action");
        actionLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 80;");

        header.getChildren().addAll(scoreLabel, userLabel, ipLabel, countryLabel, connTimeLabel, typingSpeedLabel, reasonLabel, actionLabel);
        return header;
    }

    /**
     * Crée la ligne d'en-tête pour les tentatives échouées
     */
    private HBox createFailedAttemptsHeaderRow() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 12; -fx-background-color: #fef2f2; " +
                "-fx-border-color: #fecaca; -fx-border-width: 0 0 2 0;");

        Label countLabel = new Label("Tentatives");
        countLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-min-width: 80;");

        Label userLabel = new Label("Utilisateur");
        userLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 150;");

        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 200;");

        Label lastAttemptLabel = new Label("Dernière Tentative");
        lastAttemptLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 150;");

        header.getChildren().addAll(countLabel, userLabel, emailLabel, lastAttemptLabel);
        return header;
    }

    /**
     * Crée une ligne de connexion à risque
     */
    private HBox createConnectionRow(Map<String, Object> connection) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 0 0 1 0; -fx-background-color: #fef2f2;");

        // Score
        int riskScore = (Integer) connection.get("riskScore");
        Label scoreLabel = new Label(RiskAnalyzerIA.getScoreEmoji(riskScore) + " " + riskScore);
        scoreLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; " +
                "-fx-text-fill: " + RiskAnalyzerIA.getScoreColor(riskScore) + "; -fx-min-width: 50;");

        // Utilisateur
        String nom = (String) connection.get("nom");
        String prenom = (String) connection.get("prenom");
        String email = (String) connection.get("email");
        Label userLabel = new Label(prenom + " " + nom + "\n(" + email + ")");
        userLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 120; -fx-wrap-text: true;");

        // IP
        String ipAddress = (String) connection.get("ipAddress");
        Label ipLabel = new Label(ipAddress);
        ipLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 120;");

        // Pays
        String country = (String) connection.get("country");
        Label countryLabel = new Label(country);
        countryLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 80;");

        // Temps de connexion
        Object connTimeObj = connection.get("connectionTime");
        String connTimeStr = connTimeObj != null ? String.format("%.0f ms", ((Number) connTimeObj).doubleValue()) : "N/A";
        Label connTimeLabel = new Label(connTimeStr);
        connTimeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 80;");

        // Vitesse d'écriture
        Object typingSpeedObj = connection.get("typingSpeed");
        String typingSpeedStr = typingSpeedObj != null ? String.format("%.1f car/s", ((Number) typingSpeedObj).doubleValue()) : "N/A";
        Label typingSpeedLabel = new Label(typingSpeedStr);
        typingSpeedLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 80;");

        // Raison
        String reasons = (String) connection.get("reasons");
        Label reasonLabel = new Label(reasons);
        reasonLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 150; -fx-wrap-text: true;");

        // Action
        String action = (String) connection.get("action");
        Label actionLabel = new Label(action);
        actionLabel.setStyle("-fx-font-size: 10; -fx-font-weight: bold; " +
                "-fx-text-fill: " + ("BLOQUÉ".equals(action) ? "#ef4444" : "#16a34a") + "; -fx-min-width: 80;");

        row.getChildren().addAll(scoreLabel, userLabel, ipLabel, countryLabel, connTimeLabel, typingSpeedLabel, reasonLabel, actionLabel);
        return row;
    }

    /**
     * Crée une ligne de tentative échouée
     */
    private HBox createFailedAttemptRow(Map<String, Object> attempt) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12; -fx-border-color: #fecaca; " +
                "-fx-border-width: 0 0 1 0; -fx-background-color: #fef2f2;");

        // Nombre de tentatives
        int failedCount = (Integer) attempt.get("failedCount");
        Label countLabel = new Label("❌ " + failedCount);
        countLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; " +
                "-fx-text-fill: " + (failedCount > 5 ? "#dc2626" : "#f97316") + "; -fx-min-width: 80;");

        // Utilisateur
        String nom = (String) attempt.get("nom");
        String prenom = (String) attempt.get("prenom");
        Label userLabel = new Label(prenom + " " + nom);
        userLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-min-width: 150;");

        // Email
        String email = (String) attempt.get("email");
        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 200;");

        // Dernière tentative
        java.time.LocalDateTime lastAttempt = (java.time.LocalDateTime) attempt.get("lastFailedAttempt");
        String lastAttemptStr = lastAttempt != null ? 
            lastAttempt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A";
        Label lastAttemptLabel = new Label(lastAttemptStr);
        lastAttemptLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-min-width: 150;");

        row.getChildren().addAll(countLabel, userLabel, emailLabel, lastAttemptLabel);
        return row;
    }

    /**
     * Affiche les statistiques de risque pour l'utilisateur actuel
     */
    public void displayUserRiskStatistics() {
        SessionManager session = SessionManager.getInstance();
        int userId = session.getUserId();

        Map<String, Object> stats = riskDAO.getUserRiskStatistics(userId);

        System.out.println("📊 Statistiques de Risque pour l'utilisateur " + userId + ":");
        System.out.println("  Total de connexions: " + stats.get("totalLogins"));
        System.out.println("  Connexions bloquées: " + stats.get("blockedLogins"));
        System.out.println("  Score moyen: " + String.format("%.2f", stats.get("avgRiskScore")));
        System.out.println("  Score max: " + stats.get("maxRiskScore"));
        System.out.println("  Score min: " + stats.get("minRiskScore"));
    }
}
