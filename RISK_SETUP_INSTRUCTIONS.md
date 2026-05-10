# 🚀 Instructions d'Installation - Système de Risque avec IA

## 📋 Étapes d'Installation

### Étape 1: Exécuter le Script SQL

#### Option 1: Avec MySQL Workbench
1. Ouvrir MySQL Workbench
2. Se connecter à votre base de données
3. Ouvrir le fichier `SQL_RISK_ANALYSIS.sql`
4. Exécuter le script (Ctrl+Shift+Enter)

#### Option 2: Avec la Ligne de Commande
```bash
mysql -u root -p your_database < SQL_RISK_ANALYSIS.sql
```

#### Option 3: Avec phpMyAdmin
1. Ouvrir phpMyAdmin
2. Sélectionner votre base de données
3. Aller à l'onglet "SQL"
4. Copier-coller le contenu de `SQL_RISK_ANALYSIS.sql`
5. Cliquer sur "Exécuter"

### Vérification
```sql
-- Vérifier que la table a été créée
SHOW TABLES LIKE 'risk_analysis';

-- Vérifier la structure
DESCRIBE risk_analysis;

-- Vérifier les vues
SHOW VIEWS;
```

---

### Étape 2: Compiler le Projet

```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
```

### Vérification
- ✅ Pas d'erreurs de compilation
- ✅ Pas d'avertissements
- ✅ Tous les fichiers compilés

---

### Étape 3: Exécuter l'Application

```bash
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" javafx:run
```

Ou dans IntelliJ: Cliquer sur le bouton ▶️ Run

---

### Étape 4: Tester le Système

#### Test 1: Connexion Normale
1. Ouvrir l'application
2. Cocher "Je ne suis pas un robot"
3. Résoudre la question mathématique
4. Entrer email et mot de passe valides
5. Cliquer "Se connecter"
6. **Résultat attendu**: Connexion réussie, score < 30

#### Test 2: Vérifier le Score
1. Après la connexion, ouvrir le dashboard
2. Chercher le widget "Analyse de Risque"
3. Vérifier le score affiché
4. Vérifier le niveau de risque
5. Vérifier l'historique

#### Test 3: Vérifier la Base de Données
```sql
-- Vérifier les enregistrements
SELECT * FROM risk_analysis ORDER BY created_at DESC LIMIT 5;

-- Vérifier les statistiques
SELECT * FROM vw_user_risk_stats;

-- Vérifier les connexions bloquées
SELECT * FROM vw_blocked_connections;
```

---

## 🔧 Configuration

### Modifier les Seuils de Risque

Fichier: `RiskAnalyzerIA.java`

```java
// Seuils de risque (lignes 20-22)
private static final int RISK_LOW = 30;      // Changer ici
private static final int RISK_MEDIUM = 60;   // Changer ici
private static final int RISK_HIGH = 85;     // Changer ici
```

### Modifier les Facteurs de Risque

Fichier: `RiskAnalyzerIA.java`

```java
// Facteurs de risque (lignes 14-19)
private static final int FACTOR_IP_LOCATION = 25;      // Poids IP
private static final int FACTOR_NEW_DEVICE = 20;       // Poids Device
private static final int FACTOR_UNUSUAL_TIME = 15;     // Poids Time
private static final int FACTOR_FAILED_ATTEMPTS = 20;  // Poids Attempts
private static final int FACTOR_COUNTRY_CHANGE = 15;   // Poids Country
private static final int FACTOR_TYPING_SPEED = 5;      // Poids Typing
```

### Modifier les Heures Suspectes

Fichier: `RiskAnalyzerIA.java`, méthode `analyzeUnusualTime()`

```java
// Heures suspectes: 0h-6h (ligne 95)
if (currentTime.getHour() >= 0 && currentTime.getHour() < 6) {
    return 60; // Changer ici
}

// Heures normales: 8h-22h (ligne 99)
if (currentTime.getHour() >= 8 && currentTime.getHour() < 22) {
    return 10; // Changer ici
}
```

---

## 📊 Vérification de l'Installation

### Checklist
- [ ] Table `risk_analysis` créée
- [ ] Vues créées (4 vues)
- [ ] Procédures stockées créées (3 procédures)
- [ ] Projet compilé sans erreur
- [ ] Application démarre
- [ ] Connexion fonctionne
- [ ] Score affiché sur dashboard
- [ ] Historique affiché
- [ ] Données enregistrées en BD

### Commandes de Vérification

```sql
-- Vérifier la table
SELECT COUNT(*) FROM risk_analysis;

-- Vérifier les vues
SELECT * FROM vw_user_risk_stats;
SELECT * FROM vw_blocked_connections;
SELECT * FROM vw_risk_by_country;
SELECT * FROM vw_risk_by_device;

-- Vérifier les procédures
CALL sp_get_system_risk_stats();
CALL sp_get_suspicious_connections(10);
CALL sp_get_user_risk_history(1, 10);
```

---

## 🐛 Dépannage

### Erreur: "Table 'risk_analysis' doesn't exist"
**Solution**: Exécuter le script SQL

```bash
mysql -u root -p your_database < SQL_RISK_ANALYSIS.sql
```

### Erreur: "Cannot find symbol: RiskAnalyzerIA"
**Solution**: Vérifier que le fichier existe
```bash
ls src/main/java/edunova/connexion/tools/RiskAnalyzerIA.java
```

### Erreur: "Compilation failure"
**Solution**: Nettoyer et recompiler
```bash
mvn clean compile
```

### Score toujours 0
**Solution**: Vérifier que l'analyse est appelée dans LoginController

### Données non enregistrées
**Solution**: Vérifier la connexion à la base de données

```java
// Vérifier la connexion
Connection conn = DatabaseConnection.getConnection();
if (conn != null) {
    System.out.println("✅ Connexion OK");
} else {
    System.out.println("❌ Connexion échouée");
}
```

---

## 📈 Monitoring

### Logs à Vérifier

```
🔍 Analyse de risque pour l'utilisateur: 1
📊 Facteurs de Risque:
  📍 IP Location: 5/100
  🖥️  New Device: 5/100
  ⏱️  Unusual Time: 10/100
  🔁 Failed Attempts: 0/100
  🌍 Country Change: 5/100
  ⚡ Typing Speed: 10/100
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📈 Score Total: 6/100
🎯 Niveau: ✅ FAIBLE
🚫 Bloqué: NON
✅ Analyse de risque enregistrée: Score=6
```

### Requêtes de Monitoring

```sql
-- Connexions bloquées aujourd'hui
SELECT COUNT(*) FROM risk_analysis 
WHERE blocked = TRUE AND DATE(created_at) = CURDATE();

-- Score moyen aujourd'hui
SELECT AVG(risk_score) FROM risk_analysis 
WHERE DATE(created_at) = CURDATE();

-- Pays avec le plus de connexions bloquées
SELECT country, COUNT(*) as blocked_count 
FROM risk_analysis 
WHERE blocked = TRUE 
GROUP BY country 
ORDER BY blocked_count DESC;

-- Appareils suspects
SELECT device, AVG(risk_score) as avg_score, COUNT(*) as attempts 
FROM risk_analysis 
GROUP BY device 
HAVING avg_score > 50;
```

---

## 🔄 Maintenance

### Nettoyage des Anciennes Données

```sql
-- Supprimer les données de plus de 90 jours
DELETE FROM risk_analysis 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- Archiver les données
CREATE TABLE risk_analysis_archive AS
SELECT * FROM risk_analysis 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

DELETE FROM risk_analysis 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### Optimisation de la Base de Données

```sql
-- Analyser la table
ANALYZE TABLE risk_analysis;

-- Optimiser la table
OPTIMIZE TABLE risk_analysis;

-- Vérifier l'intégrité
CHECK TABLE risk_analysis;
```

---

## 📚 Fichiers Importants

### Fichiers Créés
- `RiskAnalyzerIA.java` - Service d'analyse
- `RiskDAO.java` - Accès aux données
- `RiskAnalysisController.java` - Affichage
- `SQL_RISK_ANALYSIS.sql` - Schéma BD
- `RISK_ANALYSIS_GUIDE.md` - Guide complet
- `RISK_IMPLEMENTATION_SUMMARY.md` - Résumé
- `RISK_SETUP_INSTRUCTIONS.md` - Ce fichier

### Fichiers Modifiés
- `RiskData.java` - Modèle de données
- `LoginController.java` - Intégration
- `SessionManager.java` - Stockage du score

---

## ✅ Checklist Finale

- [ ] Script SQL exécuté
- [ ] Table créée
- [ ] Vues créées
- [ ] Procédures créées
- [ ] Projet compilé
- [ ] Application démarre
- [ ] Connexion fonctionne
- [ ] Score affiché
- [ ] Historique affiché
- [ ] Données enregistrées
- [ ] Tests passés
- [ ] Monitoring en place

---

## 🎉 Prêt!

Votre système de risque avec IA est maintenant installé et prêt à être utilisé!

---

**Status**: ✅ Installation Complète
**Dernière Mise à Jour**: May 7, 2026
**Prochaine Action**: Exécuter le script SQL et compiler le projet
