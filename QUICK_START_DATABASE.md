# ⚡ Démarrage Rapide - Configuration Base de Données

## 🚨 PROBLÈME

L'application échoue avec l'erreur:
```
La table 'edunova.risk' n'existe pas
```

## ✅ SOLUTION EN 3 ÉTAPES

### Étape 1: Copier le Script SQL

Ouvrez votre client MySQL (phpMyAdmin, MySQL Workbench, etc.) et exécutez:

```sql
CREATE TABLE IF NOT EXISTS risk (
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
    temps_connexion_ms INT DEFAULT 0,
    vitesse_ecriture DOUBLE DEFAULT 0.0,
    device_type VARCHAR(100),
    navigateur VARCHAR(100),
    systeme_exploitation VARCHAR(100),
    localisation_precise VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES user(id_u) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_date_analyse (date_analyse),
    INDEX idx_score_risque (score_risque),
    INDEX idx_action_prise (action_prise)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Étape 2: Vérifier la Création

```sql
SHOW TABLES LIKE 'risk';
DESCRIBE risk;
```

Vous devriez voir la table avec tous les champs.

### Étape 3: Redémarrer l'Application

```bash
mvn javafx:run
```

---

## 🧪 TEST RAPIDE

1. **Se connecter**
   - Email: user@example.com
   - Password: password123
   - Captcha: Répondre à la question

2. **Vérifier les données**
   ```sql
   SELECT * FROM risk ORDER BY date_analyse DESC LIMIT 1;
   ```

3. **Voir le rapport**
   - Le rapport s'affiche sur le dashboard
   - Affiche 6 statistiques
   - Affiche les connexions à risque

---

## 📊 RAPPORT AMÉLIORÉ

### Statistiques Affichées
- 📊 Total Connexions
- 🚫 Connexions Bloquées
- ⚠️ Score Moyen
- 👥 Utilisateurs Uniques
- **⏱️ Temps Moyen de Connexion** (NOUVEAU)
- **⚡ Vitesse Moyenne d'Écriture** (NOUVEAU)

### Tableau des Connexions à Risque
- Score
- Utilisateur
- IP
- Pays
- **Temps de Connexion** (NOUVEAU)
- **Vitesse d'Écriture** (NOUVEAU)
- Raison
- Action

---

## 🎨 DESIGN

- ✅ 6 cartes colorées pour les statistiques
- ✅ Tableau détaillé des connexions
- ✅ Support dark/white mode
- ✅ Interface moderne et intuitive

---

## 📁 FICHIERS IMPORTANTS

- `CREATE_RISK_TABLE.sql` - Script de création
- `SETUP_DATABASE.md` - Guide détaillé
- `RAPPORT_AMELIORE_SUMMARY.md` - Résumé des améliorations

---

**Status**: ✅ **PRÊT À UTILISER**

