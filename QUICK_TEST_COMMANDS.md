# ⚡ Commandes Rapides de Test

## 🔨 Compilation et Build

### Compiler le projet
```bash
mvn clean compile
```

### Construire le JAR
```bash
mvn clean package
```

### Compiler et exécuter les tests
```bash
mvn clean test
```

### Construire sans tests
```bash
mvn clean package -DskipTests
```

---

## 🗄️ Requêtes SQL de Test

### Vérifier les données de connexion enregistrées
```sql
SELECT * FROM risk_analysis 
ORDER BY created_at DESC 
LIMIT 10;
```

### Vérifier les données pour un utilisateur spécifique
```sql
SELECT * FROM risk_analysis 
WHERE user_id = 1 
ORDER BY created_at DESC 
LIMIT 5;
```

### Compter les tentatives de connexion
```sql
SELECT user_id, COUNT(*) as tentatives 
FROM risk_analysis 
GROUP BY user_id 
ORDER BY tentatives DESC;
```

### Vérifier les connexions bloquées
```sql
SELECT * FROM risk_analysis 
WHERE blocked = 1 
ORDER BY created_at DESC;
```

### Vérifier les connexions à risque élevé
```sql
SELECT * FROM risk_analysis 
WHERE risk_score >= 60 
ORDER BY risk_score DESC;
```

### Statistiques globales
```sql
SELECT 
    COUNT(*) as total_connexions,
    SUM(CASE WHEN blocked = 1 THEN 1 ELSE 0 END) as connexions_bloquees,
    AVG(risk_score) as score_moyen,
    COUNT(DISTINCT user_id) as utilisateurs_uniques
FROM risk_analysis;
```

### Historique de connexion
```sql
SELECT * FROM login_history 
ORDER BY id DESC 
LIMIT 10;
```

---

## 🧪 Tests Manuels

### Test 1: Connexion Google OAuth2
```
1. Démarrer l'application
2. Cliquer sur "Continuer avec Google"
3. Vérifier que la fenêtre s'ouvre
4. Entrer votre email Google
5. Cliquer sur "Se connecter"
6. Vérifier que le navigateur s'ouvre
7. S'authentifier avec Google
8. Vérifier la redirection vers le Dashboard
```

### Test 2: Vérifier les données enregistrées
```
1. Après connexion Google, exécuter:
   SELECT * FROM risk_analysis 
   WHERE user_id = [votre_id] 
   ORDER BY created_at DESC 
   LIMIT 1;

2. Vérifier que les données sont présentes
```

### Test 3: Vérifier le rapport de risque
```
1. Aller à la section "Utilisateurs"
2. Vérifier que le rapport de risque s'affiche
3. Vérifier que le libellé est "🔐 Tentatives Connexion"
4. Vérifier que les statistiques sont correctes
```

### Test 4: Connexion normale
```
1. Se déconnecter
2. Se connecter avec email/mot de passe
3. Vérifier que la connexion fonctionne
4. Vérifier que les données sont enregistrées
```

---

## 🔍 Vérification des Fichiers

### Vérifier que les fichiers sont créés
```bash
# Vérifier GoogleOAuth2WindowController.java
ls -la src/main/java/edunova/connexion/controllers/GoogleOAuth2WindowController.java

# Vérifier google_oauth2_window.fxml
ls -la src/main/resources/views/google_oauth2_window.fxml
```

### Vérifier les modifications
```bash
# Vérifier les modifications du LoginController
grep -n "traiterCodeGoogleOAuth2" src/main/java/edunova/connexion/controllers/LoginController.java

# Vérifier les modifications du users.fxml
grep -n "riskScorePanel" src/main/resources/views/users.fxml

# Vérifier les modifications du risk_report.fxml
grep -n "Tentatives Connexion" src/main/resources/views/risk_report.fxml
```

---

## 📊 Logs et Débogage

### Activer les logs
```bash
# Ajouter au fichier de configuration
export LOG_LEVEL=DEBUG
```

### Vérifier les logs de l'application
```bash
# Chercher les erreurs
grep -i "error" application.log

# Chercher les messages Google OAuth2
grep -i "GoogleOAuth2" application.log

# Chercher les messages de connexion
grep -i "LoginController" application.log
```

---

## 🚀 Déploiement Rapide

### Déployer en développement
```bash
# Compiler et exécuter
mvn clean compile exec:java -Dexec.mainClass="edunova.connexion.tests.Main"
```

### Créer un JAR exécutable
```bash
# Construire le JAR
mvn clean package

# Exécuter le JAR
java -jar target/Login-1.0-SNAPSHOT.jar
```

---

## 🔧 Dépannage Rapide

### Problème: Port 8888 occupé
```bash
# Vérifier quel processus utilise le port 8888
netstat -ano | findstr :8888

# Tuer le processus (Windows)
taskkill /PID [PID] /F
```

### Problème: Erreur de compilation
```bash
# Nettoyer et recompiler
mvn clean compile

# Vérifier les erreurs
mvn compile 2>&1 | grep -i error
```

### Problème: Erreur de base de données
```bash
# Vérifier la connexion à la BD
mysql -u [user] -p [password] -h [host] [database]

# Vérifier que la table risk_analysis existe
SHOW TABLES LIKE 'risk_analysis';

# Vérifier la structure de la table
DESCRIBE risk_analysis;
```

---

## 📝 Checklist Rapide

- [ ] Compiler sans erreurs: `mvn clean compile`
- [ ] Construire le JAR: `mvn clean package`
- [ ] Démarrer l'application
- [ ] Tester connexion Google OAuth2
- [ ] Vérifier les données enregistrées
- [ ] Vérifier le rapport de risque
- [ ] Tester connexion normale
- [ ] Vérifier l'historique de connexion

---

## 💡 Astuces

### Réinitialiser les données de test
```sql
DELETE FROM risk_analysis WHERE user_id = 1;
DELETE FROM login_history WHERE user_id = 1;
```

### Voir les dernières connexions
```sql
SELECT * FROM risk_analysis 
ORDER BY created_at DESC 
LIMIT 5;
```

### Voir les connexions bloquées
```sql
SELECT * FROM risk_analysis 
WHERE blocked = 1;
```

### Voir les statistiques par utilisateur
```sql
SELECT 
    user_id,
    COUNT(*) as connexions,
    AVG(risk_score) as score_moyen,
    MAX(risk_score) as score_max,
    SUM(CASE WHEN blocked = 1 THEN 1 ELSE 0 END) as bloquees
FROM risk_analysis 
GROUP BY user_id;
```

---

## 🎯 Résumé

| Commande | Description |
|----------|-------------|
| `mvn clean compile` | Compiler le projet |
| `mvn clean package` | Construire le JAR |
| `mvn clean test` | Exécuter les tests |
| `java -jar target/Login-1.0-SNAPSHOT.jar` | Exécuter l'application |
| `SELECT * FROM risk_analysis` | Voir les données de risque |
| `SELECT * FROM login_history` | Voir l'historique de connexion |

