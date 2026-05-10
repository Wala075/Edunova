# Fix: Failed Password Attempts Tracking

## Issue
The "Tentatives Échouées" (Failed Attempts) statistic was showing the count of blocked logins instead of the actual count of failed password attempts. 

**Example**: If a user typed the password incorrectly twice, the stat should show "2", not the number of blocked connections.

## Root Cause
The stat was using `blockedLogins` from the `risk_analysis` table (which counts high-risk blocked connections) instead of counting actual failed login attempts from the `login_history` table.

## Solution Implemented

### 1. Updated RiskDAO.getGlobalRiskStatistics() 
Added a new query to count failed login attempts from the `login_history` table:

```java
// Requête pour compter les tentatives échouées (failed password attempts)
String sqlFailedAttempts = "SELECT COUNT(*) as failed_attempts FROM login_history WHERE succes_lh = 0";
Statement stmtFailed = conn.createStatement();
ResultSet rsFailed = stmtFailed.executeQuery(sqlFailedAttempts);

if (rsFailed.next()) {
    stats.put("failedAttempts", rsFailed.getInt("failed_attempts"));
} else {
    stats.put("failedAttempts", 0);
}
```

### 2. Updated DashboardController.afficherStatistiquesRisque()
Changed Stat 4 to use the new `failedAttempts` data:

```java
// Before:
String.valueOf(stats.getOrDefault("blockedLogins", 0))

// After:
String.valueOf(stats.getOrDefault("failedAttempts", 0))
```

### 3. Updated Report Dialog
Modified the "Tentatives Échouées" report to use correct data:

```java
case "Tentatives Échouées" -> {
    int failed = (int) stats.getOrDefault("failedAttempts", 0);  // Changed from blockedLogins
    int total = (int) stats.getOrDefault("totalLogins", 0);
    
    ajouterLigneRapport(contenuRapport, "Tentatives échouées", String.valueOf(failed), "#ef4444", textSub);
    ajouterLigneRapport(contenuRapport, "Total des tentatives", String.valueOf(total), textMain, textSub);
    
    double pourcentageEchec = total > 0 ? (failed * 100.0 / total) : 0;
    ajouterLigneRapport(contenuRapport, "Taux d'échec", String.format("%.1f%%", pourcentageEchec), "#ef4444", textSub);
}
```

## How It Works Now

1. **User types password incorrectly** → `login_history` records entry with `succes_lh = 0`
2. **Statistics refresh** → `getGlobalRiskStatistics()` counts all rows in `login_history` where `succes_lh = 0`
3. **Stat displays** → "Tentatives Échouées" shows the actual count of failed password attempts
4. **Report shows** → Detailed breakdown including failed attempts, total attempts, and failure rate

## Example Scenario

**User Action**: Types password incorrectly 2 times, then correctly on 3rd attempt

**Result**:
- Total Connexions: 3
- Tentatives Échouées: 2 ✅ (now correct!)
- Taux d'échec: 66.7%

## Files Modified

1. **src/main/java/edunova/connexion/dao/RiskDAO.java**
   - Modified `getGlobalRiskStatistics()` to add `failedAttempts` query

2. **src/main/java/edunova/connexion/controllers/DashboardController.java**
   - Updated Stat 4 to use `failedAttempts` instead of `blockedLogins`
   - Updated report dialog to use correct data

## Build Status
✅ **BUILD SUCCESS** - All changes compiled without errors

## Testing
To verify the fix:
1. Open the application
2. Go to login page
3. Type password incorrectly 2 times
4. Type password correctly on 3rd attempt
5. Check dashboard statistics
6. "Tentatives Échouées" should show "2"
7. Click on the stat to see detailed report
