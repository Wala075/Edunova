package edunova.connexion.controllers;

import edunova.connexion.dao.RiskDAO;
import edunova.connexion.models.RiskData;
import edunova.connexion.tools.RiskAnalyzerIA;
import edunova.connexion.tools.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour afficher l'analyse de risque sur le dashboard
 */
public class RiskAnalysisController {

    @FXML private VBox riskAnalysisPanel;
    @FXML private Label lblRiskScore;
    @FXML private Label lblRiskLevel;
    @FXML private Label lblLastIP;
    @FXML private Label lblLastDevice;
    @FXML private Label lblLastCountry;
    @FXML private VBox riskHistoryPanel;

    private RiskDAO riskDAO = new RiskDAO();

    @FXML
    public void initialize() {
        displayCurrentRiskAnalysis();
        displayRiskHistory();
    }

    /**
     * Affiche l'analyse de risque actuelle
     */
    private void displayCurrentRiskAnalysis() {
        SessionManager session = SessionManager.getInstance();
        int userId = session.getUserId();
        int riskScore = session.getRiskScore();

        if (riskScore == 0) {
            riskAnalysisPanel.setVisible(false);
            return;
        }

        // Afficher le score
        lblRiskScore.setText(riskScore + "/100");
        lblRiskScore.setStyle("-fx-font-size: 24; -fx-font-weight: bold; " +
                "-fx-text-fill: " + RiskAnalyzerIA.getScoreColor(riskScore) + ";");

        // Afficher le niveau
        String riskLevel = RiskAnalyzerIA.getScoreEmoji(riskScore) + " " +
                (riskScore < 30 ? "FAIBLE" :
                 riskScore < 60 ? "MOYEN" :
                 riskScore < 85 ? "ÉLEVÉ" : "CRITIQUE");
        lblRiskLevel.setText(riskLevel);

        // Récupérer les dernières données
        List<RiskData> analyses = riskDAO.getUserRiskAnalyses(userId, 1);
        if (!analyses.isEmpty()) {
            RiskData latest = analyses.get(0);
            lblLastIP.setText("IP: " + latest.getIpAddress());
            lblLastDevice.setText("Appareil: " + latest.getDevice());
            lblLastCountry.setText("Pays: " + latest.getCountry());
        }
    }

    /**
     * Affiche l'historique des analyses de risque
     */
    private void displayRiskHistory() {
        SessionManager session = SessionManager.getInstance();
        int userId = session.getUserId();

        List<RiskData> analyses = riskDAO.getUserRiskAnalyses(userId, 10);

        riskHistoryPanel.getChildren().clear();

        if (analyses.isEmpty()) {
            Label noData = new Label("Aucune analyse de risque");
            noData.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
            riskHistoryPanel.getChildren().add(noData);
            return;
        }

        for (RiskData risk : analyses) {
            HBox row = createRiskHistoryRow(risk);
            riskHistoryPanel.getChildren().add(row);
        }
    }

    /**
     * Crée une ligne d'historique de risque
     */
    private HBox createRiskHistoryRow(RiskData risk) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 0 0 1 0; -fx-background-color: " +
                (risk.isBlocked() ? "#fef2f2" : "#f8fafc") + ";");

        // Score
        Label scoreLabel = new Label(RiskAnalyzerIA.getScoreEmoji(risk.getRiskScore()) +
                " " + risk.getRiskScore());
        scoreLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; " +
                "-fx-text-fill: " + RiskAnalyzerIA.getScoreColor(risk.getRiskScore()) + "; " +
                "-fx-min-width: 50;");

        // Niveau
        Label levelLabel = new Label(risk.getRiskLevel());
        levelLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b; -fx-min-width: 80;");

        // IP
        Label ipLabel = new Label(risk.getIpAddress());
        ipLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b; -fx-min-width: 120;");

        // Appareil
        Label deviceLabel = new Label(risk.getDevice());
        deviceLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b; -fx-min-width: 100;");

        // Pays
        Label countryLabel = new Label(risk.getCountry());
        countryLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b; -fx-min-width: 80;");

        // Statut
        Label statusLabel = new Label(risk.isBlocked() ? "🚫 BLOQUÉ" : "✅ OK");
        statusLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; " +
                "-fx-text-fill: " + (risk.isBlocked() ? "#ef4444" : "#16a34a") + ";");

        row.getChildren().addAll(scoreLabel, levelLabel, ipLabel, deviceLabel, countryLabel, statusLabel);
        return row;
    }

    /**
     * Affiche les statistiques de risque
     */
    public void displayRiskStatistics() {
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
