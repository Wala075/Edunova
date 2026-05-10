# 🔍 Guide Complet - Système d'Analyse de Risque avec IA

## 📋 Vue d'Ensemble

Le système d'analyse de risque utilise l'IA pour évaluer le niveau de sécurité de chaque connexion en analysant **6 facteurs de risque** et en calculant un **score 0-100** en temps réel.

---

## 🎯 Objectif

Détecter les connexions suspectes et bloquer les accès non autorisés avant qu'ils ne causent des dégâts.

---

## 📊 Les 6 Facteurs de Risque Analysés

### 1. 📍 **Localisation IP** (25 points max)
- **Qu'est-ce que c'est?** Vérifie si l'IP est nouvelle ou connue
- **Risque faible**: IP connue (5 points)
- **Risque élevé**: IP nouvelle (40 points)
- **Cas d'usage**: Détecte les connexions depuis des pays différents

### 2. 🖥️ **Nouvel Appareil** (20 points max)
- **Qu'est-ce que c'est?** Vérifie si l'appareil est nouveau
- **Risque faible**: Appareil connu (5 points)
- **Risque élevé**: Nouvel appareil (50 points)
- **Cas d'usage**: Détecte les connexions depuis des appareils inconnus

### 3. ⏱️ **Heure de Connexion** (15 points max)
- **Qu'est-ce que c'est?** Vérifie si l'heure est inhabituelle
- **Risque faible**: 8h-22h (10 points)
- **Risque élevé**: 0h-6h (60 points)
- **Cas d'usage**: Détecte les connexions à des heures suspectes

### 4. 🔁 **Tentatives Échouées** (20 points max)
- **Qu'est-ce que c'est?** Compte les tentatives de connexion échouées
- **Risque faible**: 0 tentatives (0 points)
- **Risque moyen**: 1-3 tentatives (20-50 points)
- **Risque élevé**: >3 tentatives (90 points)
- **Cas d'usage**: Détecte les attaques par force brute

### 5. 🌍 **Changement de Pays** (15 points max)
- **Qu'est-ce que c'est?** Vérifie si le pays a changé
- **Risque faible**: Même pays (5 points)
- **Risque élevé**: Pays différent (70 points)
- **Cas d'usage**: Détecte les connexions depuis des pays différents

### 6. ⚡ **Vitesse de Saisie** (5 points max)
- **Qu'est-ce que c'est?** Analyse la vitesse de saisie (détection de bot)
- **Vitesse normale**: 40-80 caractères/seconde (10 points)
- **Vitesse suspecte**: >100 caractères/seconde (80 points)
- **Cas d'usage**: Détecte les bots et les attaques automatisées

---

## 📈 Calcul du Score

```
Score Total = (IP + Device + Time + Attempts + Country + Typing) / 6

Exemple:
- IP: 40 points
- Device: 5 points
- Time: 10 points
- Attempts: 0 points
- Country: 5 points
- Typing: 10 points
─────────────────────
Score = (40 + 5 + 10 + 0 + 5 + 10) / 6 = 11.67 ≈ 12/100
```

---

## 🎯 Niveaux de Risque

| Score | Niveau | Emoji | Action |
|-------|--------|-------|--------|
| 0-30 | ✅ FAIBLE | ✅ | Connexion autorisée |
| 31-60 | ⚠️ MOYEN | ⚠️ | Alerte admin |
| 61-85 | 🔴 ÉLEVÉ | 🔴 | Vérification supplémentaire |
| 86-100 | 🚫 CRITIQUE | 🚫 | Connexion bloquée |

---

## 🔄 Flux de Connexion avec Analyse de Risque

```
┌─────────────────────────────────────────┐
│ Utilisateur entre email + mot de passe  │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Vérification du captcha mathématique    │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Vérification des identifiants           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ 🔍 Analyse de Risque avec IA            │
│ - Récupérer l'historique utilisateur    │
│ - Analyser 6 facteurs de risque         │
│ - Calculer le score 0-100               │
│ - Déterminer le niveau de risque        │
└─────────────────────────────────────────┘
                    ↓
        ┌───────────┴───────────┐
        ↓                       ↓
   Score < 85            Score >= 85
        ↓                       ↓
   ✅ Connexion OK      🚫 Connexion Bloquée
        ↓                       ↓
   Créer session         Afficher erreur
   Ouvrir dashboard      Enregistrer tentative
   Enregistrer risque    Enregistrer risque
```

---

## 💾 Stockage en Base de Données

### Table: `risk_analysis`

```sql
CREATE TABLE risk_analysis (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    ip_address VARCHAR(45),
    country VARCHAR(100),
    device VARCHAR(100),
    login_time DATETIME,
    failed_attempts INT,
    typing_speed DOUBLE,
    risk_score INT,
    risk_level VARCHAR(50),
    blocked BOOLEAN,
    created_at DATETIME,
    
    FOREIGN KEY (user_id) REFERENCES user(id_u)
);
```

### Données Enregistrées
- ✅ Score de risque (0-100)
- ✅ Niveau de risque (FAIBLE/MOYEN/ÉLEVÉ/CRITIQUE)
- ✅ IP address
- ✅ Pays
- ✅ Appareil
- ✅ Nombre de tentatives échouées
- ✅ Vitesse de saisie
- ✅ Statut de blocage
- ✅ Timestamp

---

## 📊 Affichage sur le Dashboard

### Widget de Risque Actuel
```
┌─────────────────────────────────────────┐
│ 🛡️ Analyse de Risque                    │
├─────────────────────────────────────────┤
│ Score: 15/100                           │
│ Niveau: ✅ FAIBLE                       │
│                                         │
│ IP: 192.168.1.100                       │
│ Appareil: Windows                       │
│ Pays: Tunisia                           │
└─────────────────────────────────────────┘
```

### Historique des Analyses
```
┌─────────────────────────────────────────────────────────────┐
│ Historique des Connexions                                   │
├─────────────────────────────────────────────────────────────┤
│ ✅ 15  FAIBLE   192.168.1.100  Windows  Tunisia  ✅ OK      │
│ ⚠️  45  MOYEN    192.168.1.101  Windows  Tunisia  ✅ OK      │
│ 🔴 72  ÉLEVÉ    10.0.0.1       Linux    France   ✅ OK      │
│ 🚫 92  CRITIQUE 203.0.113.1    Mac      USA      🚫 BLOQUÉ  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Implémentation Technique

### Classes Principales

#### 1. **RiskAnalyzerIA.java**
- Analyse les 6 facteurs de risque
- Calcule le score total
- Détermine le niveau de risque
- Fournit les couleurs et emojis

#### 2. **RiskDAO.java**
- Enregistre les analyses en BD
- Récupère l'historique utilisateur
- Fournit les statistiques
- Gère les connexions bloquées

#### 3. **RiskData.java**
- Modèle de données pour le risque
- Stocke tous les facteurs
- Fournit les getters/setters

#### 4. **RiskAnalysisController.java**
- Affiche le score sur le dashboard
- Affiche l'historique
- Affiche les statistiques

### Intégration dans LoginController

```java
// Analyser le risque
RiskDAO riskDAO = new RiskDAO();
Map<String, Object> userHistory = riskDAO.getUserConnectionHistory(userId);

RiskData riskData = RiskAnalyzerIA.analyzeRisk(
    userId,
    ipAddress,
    country,
    device,
    failedAttempts,
    typingSpeed,
    userHistory
);

// Enregistrer l'analyse
riskDAO.insertRiskData(riskData);

// Vérifier si bloqué
if (riskData.isBlocked()) {
    showAlert("Connexion Bloquée - Score: " + riskData.getRiskScore());
    return;
}

// Créer la session
SessionManager.getInstance().setRiskScore(riskData.getRiskScore());
```

---

## 📈 Statistiques et Rapports

### Statistiques par Utilisateur
```sql
SELECT 
    COUNT(*) as total_logins,
    SUM(CASE WHEN blocked = TRUE THEN 1 ELSE 0 END) as blocked_logins,
    AVG(risk_score) as avg_risk_score,
    MAX(risk_score) as max_risk_score
FROM risk_analysis
WHERE user_id = ?;
```

### Connexions Bloquées
```sql
SELECT * FROM risk_analysis
WHERE blocked = TRUE
ORDER BY created_at DESC
LIMIT 10;
```

### Analyse par Pays
```sql
SELECT country, COUNT(*) as attempts, AVG(risk_score) as avg_score
FROM risk_analysis
GROUP BY country
ORDER BY avg_score DESC;
```

---

## 🔐 Sécurité

### Points Forts
- ✅ Analyse en temps réel
- ✅ Détection de bots
- ✅ Historique complet
- ✅ Blocage automatique
- ✅ Alertes admin

### Limitations
- ⚠️ Basé sur des heuristiques
- ⚠️ Nécessite un historique
- ⚠️ Peut avoir des faux positifs

### Recommandations
- Monitorer les faux positifs
- Ajuster les seuils si nécessaire
- Intégrer avec un système d'alertes
- Analyser les patterns de blocage

---

## 🧪 Exemples de Scénarios

### Scénario 1: Connexion Normale
```
IP: 192.168.1.100 (connue)
Device: Windows (connu)
Time: 14h (normal)
Attempts: 0
Country: Tunisia (même)
Typing: 50 chars/sec (normal)

Score: (5 + 5 + 10 + 0 + 5 + 10) / 6 = 5.8 ≈ 6/100
Niveau: ✅ FAIBLE
Action: Connexion autorisée
```

### Scénario 2: Connexion Suspecte
```
IP: 203.0.113.1 (nouvelle)
Device: Linux (nouveau)
Time: 3h (suspect)
Attempts: 2
Country: USA (changement)
Typing: 120 chars/sec (bot?)

Score: (40 + 50 + 60 + 50 + 70 + 80) / 6 = 58.3 ≈ 58/100
Niveau: ⚠️ MOYEN
Action: Alerte admin
```

### Scénario 3: Connexion Bloquée
```
IP: 10.0.0.1 (nouvelle)
Device: Mac (nouveau)
Time: 2h (très suspect)
Attempts: 5
Country: Russia (changement)
Typing: 150 chars/sec (bot)

Score: (40 + 50 + 60 + 90 + 70 + 80) / 6 = 65 ≈ 65/100
Niveau: 🔴 ÉLEVÉ
Action: Vérification supplémentaire
```

---

## 📚 Fichiers Créés

1. **RiskAnalyzerIA.java** - Service d'analyse
2. **RiskDAO.java** - Accès aux données
3. **RiskData.java** - Modèle de données
4. **RiskAnalysisController.java** - Affichage
5. **SQL_RISK_ANALYSIS.sql** - Schéma BD
6. **RISK_ANALYSIS_GUIDE.md** - Ce guide

---

## 🚀 Prochaines Étapes

1. **Exécuter le script SQL** pour créer la table
2. **Compiler le projet**
3. **Tester les connexions** et vérifier les scores
4. **Monitorer les faux positifs**
5. **Ajuster les seuils** si nécessaire
6. **Intégrer les alertes** pour l'admin

---

## 📞 Support

### Questions Fréquentes

**Q: Comment le score est-il calculé?**
A: Le score est la moyenne des 6 facteurs de risque (0-100).

**Q: Que se passe-t-il si le score est > 85?**
A: La connexion est bloquée et l'utilisateur reçoit un message d'erreur.

**Q: Comment puis-je voir l'historique?**
A: L'historique s'affiche sur le dashboard après la connexion.

**Q: Puis-je modifier les seuils?**
A: Oui, modifiez les constantes dans RiskAnalyzerIA.java.

---

**Status**: ✅ Implémenté et Prêt à Tester
**Dernière Mise à Jour**: May 7, 2026
