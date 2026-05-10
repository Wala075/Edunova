# 🧪 Guide de Test - Système de Risque

## 📋 Avant de Commencer

Assurez-vous que:
- ✅ La base de données est accessible
- ✅ La table `risk` existe avec tous les champs
- ✅ Le projet compile sans erreurs
- ✅ Les fichiers FXML sont valides

---

## 🚀 Test 1: Compilation

### Commande
```bash
mvn clean compile
```

### Résultat Attendu
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXs
```

### Si Erreur
- Vérifier les imports
- Vérifier les chemins des fichiers
- Vérifier la syntaxe FXML

---

## 🔐 Test 2: Connexion Normale

### Étapes
1. Lancer l'application
2. Aller à l'écran de connexion
3. Entrer les identifiants valides
4. Répondre au captcha mathématique
5. Cliquer sur "Se connecter"

### Résultat Attendu
- ✅ Connexion réussie
- ✅ Dashboard s'ouvre
- ✅ Rapport de risque visible
- ✅ Score faible (< 30)

### Vérification BD
```sql
SELECT * FROM risk WHERE user_id = 1 ORDER BY date_analyse DESC LIMIT 1;
```

Vérifier:
- `score_risque` < 30
- `niveau_risque` = "✅ FAIBLE"
- `action_prise` = "AUTORISÉ"

---

## ⚠️ Test 3: Connexion à Risque Moyen

### Étapes
1. Modifier l'IP dans LoginController (ex: "203.0.113.1")
2. Modifier le pays (ex: "France")
3. Se connecter

### Résultat Attendu
- ✅ Connexion réussie
- ✅ Score moyen (31-60)
- ✅ Rapport affiche la connexion

### Vérification BD
```sql
SELECT * FROM risk WHERE user_id = 1 ORDER BY date_analyse DESC LIMIT 1;
```

Vérifier:
- `score_risque` entre 31 et 60
- `niveau_risque` = "⚠️ MOYEN"
- `action_prise` = "AUTORISÉ"

---

## 🔴 Test 4: Connexion à Risque Élevé

### Étapes
1. Modifier l'IP (ex: "192.0.2.1")
2. Modifier le pays (ex: "Russia")
3. Ajouter des tentatives échouées (modifier le code)
4. Se connecter

### Résultat Attendu
- ✅ Connexion réussie (score < 86)
- ✅ Score élevé (61-85)
- ✅ Rapport affiche la connexion

### Vérification BD
```sql
SELECT * FROM risk WHERE user_id = 1 ORDER BY date_analyse DESC LIMIT 1;
```

Vérifier:
- `score_risque` entre 61 et 85
- `niveau_risque` = "🔴 ÉLEVÉ"
- `action_prise` = "AUTORISÉ"

---

## 🚫 Test 5: Connexion Bloquée

### Étapes
1. Modifier le code pour forcer un score > 85
2. Ajouter plusieurs tentatives échouées
3. Se connecter

### Résultat Attendu
- ❌ Connexion bloquée
- ❌ Message d'erreur affiché
- ✅ Données enregistrées en BD

### Vérification BD
```sql
SELECT * FROM risk WHERE user_id = 1 ORDER BY date_analyse DESC LIMIT 1;
```

Vérifier:
- `score_risque` >= 86
- `niveau_risque` = "🚫 CRITIQUE"
- `action_prise` = "BLOQUÉ"

---

## 📊 Test 6: Rapport de Risque

### Étapes
1. Se connecter avec un compte admin
2. Aller au dashboard
3. Vérifier le rapport de risque

### Résultat Attendu
- ✅ Rapport visible entre overview et derniers utilisateurs
- ✅ Statistiques globales affichées
- ✅ Connexions à risque élevé listées
- ✅ Tableau avec détails

### Vérification Visuelle
- [ ] Titre "🛡️ Rapport de Risque" visible
- [ ] Statistiques globales affichées
- [ ] Connexions à risque élevé listées
- [ ] Tableau avec colonnes: Score, Utilisateur, IP, Pays, Raison, Action

---

## 🔍 Test 7: Historique Utilisateur

### Étapes
1. Se connecter 3 fois avec la même IP
2. Vérifier que le score baisse

### Résultat Attendu
- ✅ 1ère connexion: Score moyen (IP nouvelle)
- ✅ 2ème connexion: Score plus bas (IP connue)
- ✅ 3ème connexion: Score très bas (IP connue)

### Vérification BD
```sql
SELECT score_risque, adresse_ip FROM risk 
WHERE user_id = 1 
ORDER BY date_analyse DESC 
LIMIT 3;
```

Vérifier que les scores diminuent.

---

## 📈 Test 8: Statistiques Globales

### Étapes
1. Faire plusieurs connexions avec différents utilisateurs
2. Vérifier les statistiques globales

### Résultat Attendu
- ✅ Total connexions correct
- ✅ Connexions bloquées correct
- ✅ Score moyen correct
- ✅ Utilisateurs uniques correct

### Vérification BD
```sql
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN action_prise = 'BLOQUÉ' THEN 1 ELSE 0 END) as bloquees,
    AVG(score_risque) as score_moyen,
    COUNT(DISTINCT user_id) as utilisateurs
FROM risk;
```

Comparer avec le rapport.

---

## 🐛 Dépannage

### Problème: Rapport ne s'affiche pas
**Solution**:
1. Vérifier que `risk_report.fxml` existe
2. Vérifier que `RiskReportController.java` existe
3. Vérifier que `fx:include` est correct dans `dashboard.fxml`
4. Vérifier les logs de compilation

### Problème: Données non enregistrées
**Solution**:
1. Vérifier la connexion BD
2. Vérifier que la table `risk` existe
3. Vérifier les permissions BD
4. Vérifier les logs d'erreur

### Problème: Score incorrect
**Solution**:
1. Vérifier les constantes dans `RiskAnalyzerIA.java`
2. Vérifier la logique d'analyse
3. Vérifier les données d'entrée
4. Ajouter des logs de debug

### Problème: Connexion bloquée incorrectement
**Solution**:
1. Vérifier le seuil `RISK_HIGH = 85`
2. Vérifier la logique de blocage
3. Vérifier les données d'entrée
4. Tester avec des scores connus

---

## ✅ Checklist de Test Complète

- [ ] Compilation réussie
- [ ] Connexion normale fonctionne
- [ ] Score faible enregistré
- [ ] Connexion à risque moyen fonctionne
- [ ] Score moyen enregistré
- [ ] Connexion à risque élevé fonctionne
- [ ] Score élevé enregistré
- [ ] Connexion bloquée fonctionne
- [ ] Score critique enregistré
- [ ] Rapport visible sur dashboard
- [ ] Statistiques globales correctes
- [ ] Connexions à risque élevé listées
- [ ] Historique utilisateur fonctionne
- [ ] Scores diminuent avec IP connue
- [ ] Données BD correctes
- [ ] Pas d'erreurs en logs

---

## 📝 Logs à Vérifier

### Console
```
🔍 Analyse de risque pour l'utilisateur: 1
📊 Facteurs de Risque:
  📍 IP Location: 40/100
  🖥️  New Device: 50/100
  ⏱️  Unusual Time: 10/100
  🔁 Failed Attempts: 0/100
  🌍 Country Change: 70/100
  ⚡ Typing Speed: 10/100
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📈 Score Total: 30/100
🎯 Niveau: ✅ FAIBLE
🚫 Bloqué: NON
✅ Analyse de risque enregistrée: Score=30
```

### BD
```sql
SELECT * FROM risk WHERE user_id = 1 ORDER BY date_analyse DESC LIMIT 1;
```

Vérifier tous les champs sont remplis.

---

## 🎯 Résultat Final

Si tous les tests passent:
- ✅ Système de risque complètement intégré
- ✅ Données enregistrées correctement
- ✅ Rapport affiche correctement
- ✅ Blocage fonctionne
- ✅ Prêt pour la production

---

**Date**: May 7, 2026
**Version**: 1.0.0

