# Exact Changes Made - Line by Line

## File 1: RiskDAO.java

### Location: `getGlobalRiskStatistics()` method (Line 223-273)

**REMOVED**:
```java
"AVG(COALESCE(vitesse_ecriture, 0)) as avg_typing_speed " +
```

**REMOVED**:
```java
// Temps d'écriture moyen
double avgTypingSpeed = rs.getDouble("avg_typing_speed");
stats.put("avgTypingSpeed", avgTypingSpeed);
```

**FINAL SQL QUERY**:
```java
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
```

---

## File 2: RiskReportController.java

### Location: Field Declarations (Line 24-31)

**REMOVED**:
```java
@FXML private Label lblAvgTypingSpeed;
```

**FINAL FIELDS**:
```java
@FXML private VBox riskReportPanel;
@FXML private Label lblTotalLogins;
@FXML private Label lblBlockedLogins;
@FXML private Label lblAvgRiskScore;
@FXML private Label lblConnectionAttempts;
@FXML private Label lblHighRiskConnectionsCount;
@FXML private VBox highRiskConnectionsPanel;
```

### Location: `displayGlobalStatistics()` method (Line 72-120)

**REMOVED**:
```java
// Temps d'écriture moyen
double avgTypingSpeed = (Double) stats.getOrDefault("avgTypingSpeed", 0.0);
if (lblAvgTypingSpeed != null) {
    if (avgTypingSpeed > 0) {
        lblAvgTypingSpeed.setText(String.format("%.1f car/s", avgTypingSpeed));
    } else {
        lblAvgTypingSpeed.setText("N/A");
    }
}
```

**REMOVED FROM CONSOLE OUTPUT**:
```java
System.out.println("  Temps d'écriture moyen: " + String.format("%.1f", avgTypingSpeed));
```

**FINAL METHOD**:
```java
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
```

---

## File 3: dashboard.fxml

### Location: Risk Report Section (Around Line 200)

**REMOVED**:
```xml
<!-- Rapport de Risque -->
<fx:include source="risk_report.fxml" />

```

**BEFORE**:
```xml
                        <!-- Rapport de Risque -->
                        <fx:include source="risk_report.fxml" />

                        <!-- Derniers utilisateurs en cartes -->
                        <VBox fx:id="cardDerniers" style="-fx-background-radius: 12;
```

**AFTER**:
```xml
                        <!-- Derniers utilisateurs en cartes -->
                        <VBox fx:id="cardDerniers" style="-fx-background-radius: 12;
```

---

## Summary of Changes

### Total Lines Changed: ~30 lines
### Total Files Modified: 3 files
### Total Errors Fixed: 1 critical error

### Change Breakdown:
- **RiskDAO.java**: 2 removals (SQL column + data mapping)
- **RiskReportController.java**: 3 removals (field + logic + console output)
- **dashboard.fxml**: 1 removal (fx:include)

### Impact:
- ✅ Fixes SQL error: "Champ 'vitesse_ecriture' inconnu"
- ✅ Removes risk report from dashboard
- ✅ Maintains all other functionality
- ✅ No breaking changes

---

## Verification

### Compile Check
```bash
mvn clean compile
```
**Expected**: BUILD SUCCESS

### Runtime Check
```bash
mvn javafx:run
```
**Expected**: No SQL errors, statistics display correctly

### Database Check
```sql
SELECT * FROM risk_analysis ORDER BY date_analyse DESC LIMIT 1;
```
**Expected**: New record with all 10 columns populated

---

## Rollback Instructions (if needed)

### To Revert Changes:
1. Restore RiskDAO.java from git
2. Restore RiskReportController.java from git
3. Restore dashboard.fxml from git
4. Run: `mvn clean compile`

---

**Changes Made**: May 8, 2026
**Status**: ✅ COMPLETE
**Ready for**: Testing and Deployment
