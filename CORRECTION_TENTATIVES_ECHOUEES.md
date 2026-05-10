# Correction: Tentatives Échouées - Failed Password Attempts Tracking

## Problème Identifié
La statistique "Tentatives Échouées" affichait le nombre de connexions bloquées au lieu du nombre réel de tentatives de mot de passe échouées.

**Exemple**: Si l'utilisateur tape le mot de passe incorrectement 2 fois, la statistique devrait afficher "2", pas le nombre de connexions bloquées.

## Solution Appliquée

### 1. Modification de RiskDAO.getGlobalRiskStatistics()
Ajout d'une nouvelle requête pour compter les tentatives échouées depuis la table `login_history`:

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

### 2. Modification de DashboardController.afficherStatistiquesRisque()
Changement de la Stat 4 pour utiliser les données `failedAttempts`:

```java
// Avant:
String.valueOf(stats.getOrDefault("blockedLogins", 0))

// Après:
String.valueOf(stats.getOrDefault("failedAttempts", 0))
```

### 3. Mise à jour du Rapport Détaillé
Modification du rapport "Tentatives Échouées" pour utiliser les données correctes:

```java
case "Tentatives Échouées" -> {
    int failed = (int) stats.getOrDefault("failedAttempts", 0);  // Changé de blockedLogins
    int total = (int) stats.getOrDefault("totalLogins", 0);
    
    ajouterLigneRapport(contenuRapport, "Tentatives échouées", String.valueOf(failed), "#ef4444", textSub);
    ajouterLigneRapport(contenuRapport, "Total des tentatives", String.valueOf(total), textMain, textSub);
    
    double pourcentageEchec = total > 0 ? (failed * 100.0 / total) : 0;
    ajouterLigneRapport(contenuRapport, "Taux d'échec", String.format("%.1f%%", pourcentageEchec), "#ef4444", textSub);
}
```

## Fonctionnement

1. **L'utilisateur tape le mot de passe incorrectement** → Un enregistrement est créé dans `login_history` avec `succes_lh = 0`
2. **Les statistiques se rafraîchissent** → `getGlobalRiskStatistics()` compte tous les enregistrements dans `login_history` où `succes_lh = 0`
3. **La statistique s'affiche** → "Tentatives Échouées" affiche le nombre réel de tentatives échouées
4. **Le rapport détaillé montre** → Le nombre de tentatives échouées, le total des tentatives, et le taux d'échec

## Exemple de Scénario

**Actions de l'utilisateur**: 
- Tape le mot de passe incorrectement 2 fois
- Tape le mot de passe correctement à la 3ème tentative

**Résultat**:
- Total Connexions: 3
- Tentatives Échouées: 2 ✅ (maintenant correct!)
- Taux d'échec: 66.7%

## Fichiers Modifiés

1. **src/main/java/edunova/connexion/dao/RiskDAO.java**
   - Modification de `getGlobalRiskStatistics()` pour ajouter la requête `failedAttempts`

2. **src/main/java/edunova/connexion/controllers/DashboardController.java**
   - Mise à jour de la Stat 4 pour utiliser `failedAttempts` au lieu de `blockedLogins`
   - Mise à jour du rapport détaillé pour utiliser les données correctes

## Statut de Compilation
✅ **BUILD SUCCESS** - Toutes les modifications ont été compilées sans erreur

## Test de Vérification

Pour vérifier la correction:
1. Ouvrir l'application
2. Aller à la page de connexion
3. Taper le mot de passe incorrectement 2 fois
4. Taper le mot de passe correctement à la 3ème tentative
5. Vérifier les statistiques du tableau de bord
6. "Tentatives Échouées" devrait afficher "2"
7. Cliquer sur la statistique pour voir le rapport détaillé

## Différence Entre les Statistiques

| Statistique | Source | Signification |
|-------------|--------|---------------|
| **Tentatives Échouées** | `login_history` (succes_lh = 0) | Nombre de fois où le mot de passe a été tapé incorrectement |
| **Connexions Bloquées** | `risk_analysis` (action_prise = 'BLOQUÉ') | Nombre de connexions bloquées en raison d'un score de risque élevé |
| **Risque Élevé** | `risk_analysis` (score_risque >= 60) | Nombre de tentatives de connexion avec un score de risque élevé |

## Résumé

La correction assure que la statistique "Tentatives Échouées" affiche maintenant le nombre réel de tentatives de mot de passe échouées, ce qui correspond exactement à ce que l'utilisateur demandait. Si l'utilisateur tape le mot de passe incorrectement 2 fois, la statistique affichera "2".
