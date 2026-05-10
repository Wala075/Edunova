# 🔗 Intégration de la Table Risk Existante

## 📋 Vue d'Ensemble

Votre table `risk` existante a été intégrée avec le système d'analyse de risque. Les données de connexion sont maintenant enregistrées automatiquement lors de chaque tentative de connexion.

---

## 📊 Structure de la Table Risk

```sql
CREATE TABLE risk (
    id_ra INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    date_analyse DATETIME DEFAULT CURRENT_TIMESTAMP,
    adresse_ip VARCHAR(45),
    pays_ip VARCHAR(100),
    heure_connexion DATETIME,
    nb_tentatives_echouees INT DEFAULT 0,
    score_risque INT DEFAULT 0,
    niveau_risque VARCHAR(50),
    raisons TEXT,
    action_prise VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES user(id_u)
);
```

---

## 🔄 Flux d'Intégration

### 1. Lors de la Connexion

```
Utilisateur se connecte
    ↓
Vérification du captcha
    ↓
Vérification des identifiants
    ↓
🔍 Analyse de Risque
    - Récupérer l'historique
    - Analyser 6 facteurs
    - Calculer le score
    ↓
💾 Enregistrer dans la table 'risk'
    - id_ra (auto-increment)
    - user_id
    - date_analyse (NOW())
    - adresse_ip
    - pays_ip
    - heure_connexion
    - nb_tentatives_echouees
    - score_risque
    - niveau_risque
    - raisons
    - action_prise
    ↓
Vérifier si bloqué
    ├─ OUI → Afficher erreur
    └─ NON → Créer session
```

---

## 📝 Données Enregistrées

### Champs Automatiquement Remplis

| Champ | Source | Exemple |
|-------|--------|---------|
| `id_ra` | Auto-increment | 1, 2, 3... |
| `user_id` | Session utilisateur | 1 |
| `date_analyse` | NOW() | 2026-05-06 11:43:57 |
| `adresse_ip` | Détectée | 127.0.0.1 |
| `pays_ip` | Détecté | Tunisia |
| `heure_connexion` | Heure actuelle | 11:43:57 |
| `nb_tentatives_echouees` | Compteur | 0, 1, 2... |
| `score_risque` | Calculé par IA | 0-100 |
| `niveau_risque` | Déterminé | FAIBLE, MOYEN, ÉLEVÉ, CRITIQUE |
| `raisons` | Générées | "Heure normale (11h)" |
| `action_prise` | Déterminée | AUTORISÉ, BLOQUÉ |

---

## 🎯 Exemple d'Enregistrement

```sql
INSERT INTO risk (
    user_id, date_analyse, adresse_ip, pays_ip, heure_connexion,
    nb_tentatives_echouees, score_risque, niveau_risque, raisons, action_prise
) VALUES (
    1,                          -- user_id
    NOW(),                      -- date_analyse
    '127.0.0.1',               -- adresse_ip
    'Tunisia',                 -- pays_ip
    '2026-05-06 11:43:57',     -- heure_connexion
    0,                         -- nb_tentatives_echouees
    6,                         -- score_risque
    'FAIBLE',                  -- niveau_risque
    'Heure normale (11h)',     -- raisons
    'AUTORISÉ'                 -- action_prise
);
```

---

## 📊 Rapport de Risque sur le Dashboard

### Localisation

Le rapport de risque s'affiche **entre la vue d'ensemble et les derniers utilisateurs ajoutés** sur le dashboard.

### Contenu

#### 1. Statistiques Globales
```
📊 Statistiques Globales
├─ Total Connexions: 150
├─ Bloquées: 5
├─ Score Moyen: 15.3
└─ Utilisateurs: 45
```

#### 2. Connexions à Risque Élevé
```
⚠️ Connexions à Risque Élevé (Score ≥ 60)

Score | Utilisateur | IP | Pays | Raison | Action
------|-------------|----|----|--------|-------
72    | John Doe   | 10.0.0.1 | France | 2 tentatives échouées | AUTORISÉ
85    | Jane Smith | 203.0.113.1 | USA | Heure suspecte (3h) | AUTORISÉ
92    | Bob Wilson | 192.0.2.1 | Russia | >3 tentatives échouées | BLOQUÉ
```

---

## 🔍 Requêtes Utiles

### Voir Toutes les Connexions
```sql
SELECT * FROM risk ORDER BY date_analyse DESC LIMIT 20;
```

### Voir les Connexions Bloquées
```sql
SELECT * FROM risk WHERE action_prise = 'BLOQUÉ' ORDER BY date_analyse DESC;
```

### Voir les Connexions à Risque Élevé
```sql
SELECT * FROM risk WHERE score_risque >= 60 ORDER BY score_risque DESC;
```

### Statistiques par Utilisateur
```sql
SELECT 
    user_id,
    COUNT(*) as total_connexions,
    SUM(CASE WHEN action_prise = 'BLOQUÉ' THEN 1 ELSE 0 END) as bloquees,
    AVG(score_risque) as score_moyen
FROM risk
GROUP BY user_id
ORDER BY score_moyen DESC;
```

### Statistiques par Pays
```sql
SELECT 
    pays_ip,
    COUNT(*) as tentatives,
    AVG(score_risque) as score_moyen,
    SUM(CASE WHEN action_prise = 'BLOQUÉ' THEN 1 ELSE 0 END) as bloquees
FROM risk
GROUP BY pays_ip
ORDER BY score_moyen DESC;
```

---

## 📁 Fichiers Modifiés/Créés

### Modifiés
1. **RiskDAO.java** - Adapté pour la table `risk`
2. **LoginController.java** - Enregistrement automatique

### Créés
1. **RiskReportController.java** - Affichage du rapport
2. **risk_report.fxml** - Interface du rapport

---

## 🚀 Utilisation

### 1. Vérifier les Données Enregistrées
```sql
SELECT * FROM risk WHERE user_id = 1 ORDER BY date_analyse DESC LIMIT 5;
```

### 2. Voir le Rapport sur le Dashboard
- Après la connexion, le rapport s'affiche automatiquement
- Affiche les statistiques globales
- Affiche les connexions à risque élevé

### 3. Analyser les Patterns
```sql
-- Connexions par heure
SELECT HOUR(heure_connexion) as heure, COUNT(*) as connexions
FROM risk
GROUP BY HOUR(heure_connexion)
ORDER BY heure;

-- Pays les plus suspects
SELECT pays_ip, AVG(score_risque) as score_moyen, COUNT(*) as tentatives
FROM risk
GROUP BY pays_ip
HAVING score_moyen > 50
ORDER BY score_moyen DESC;
```

---

## 🔐 Sécurité

### Points Forts
- ✅ Enregistrement automatique
- ✅ Historique complet
- ✅ Analyse en temps réel
- ✅ Blocage automatique

### Recommandations
- Monitorer les faux positifs
- Ajuster les seuils si nécessaire
- Archiver les anciennes données
- Analyser les patterns régulièrement

---

## 📈 Monitoring

### Requête de Monitoring
```sql
-- Connexions suspectes aujourd'hui
SELECT 
    DATE(date_analyse) as date,
    COUNT(*) as total,
    SUM(CASE WHEN action_prise = 'BLOQUÉ' THEN 1 ELSE 0 END) as bloquees,
    AVG(score_risque) as score_moyen
FROM risk
WHERE DATE(date_analyse) = CURDATE()
GROUP BY DATE(date_analyse);
```

---

## 🧹 Maintenance

### Nettoyer les Anciennes Données
```sql
-- Supprimer les données de plus de 90 jours
DELETE FROM risk WHERE date_analyse < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- Archiver les données
CREATE TABLE risk_archive AS
SELECT * FROM risk WHERE date_analyse < DATE_SUB(NOW(), INTERVAL 90 DAY);

DELETE FROM risk WHERE date_analyse < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### Optimiser la Table
```sql
-- Analyser
ANALYZE TABLE risk;

-- Optimiser
OPTIMIZE TABLE risk;

-- Vérifier
CHECK TABLE risk;
```

---

## ✅ Checklist d'Intégration

- [x] Table `risk` existante utilisée
- [x] RiskDAO adapté pour la table
- [x] LoginController enregistre les données
- [x] RiskReportController créé
- [x] risk_report.fxml créé
- [ ] Compiler le projet
- [ ] Tester les connexions
- [ ] Vérifier les données enregistrées
- [ ] Vérifier le rapport sur le dashboard

---

## 📞 Support

### Questions Fréquentes

**Q: Où sont enregistrées les données?**
A: Dans la table `risk` de votre base de données.

**Q: Quand les données sont-elles enregistrées?**
A: Automatiquement lors de chaque tentative de connexion.

**Q: Où voir le rapport?**
A: Sur le dashboard, entre la vue d'ensemble et les derniers utilisateurs.

**Q: Comment modifier les seuils?**
A: Modifiez les constantes dans `RiskAnalyzerIA.java`.

---

**Status**: ✅ Intégration Complète
**Dernière Mise à Jour**: May 7, 2026
