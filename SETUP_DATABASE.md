# 🗄️ Configuration de la Base de Données - Table Risk

## ⚠️ PROBLÈME IDENTIFIÉ

La table `risk` n'existe pas dans votre base de données. C'est pourquoi l'application échoue avec l'erreur:
```
La table 'edunova.risk' n'existe pas
```

---

## ✅ SOLUTION

### Étape 1: Créer la Table

Exécutez ce script SQL dans votre base de données:

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
    
    -- Nouveaux champs pour le rapport amélioré
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
-- Vérifier que la table existe
SHOW TABLES LIKE 'risk';

-- Voir la structure
DESCRIBE risk;
```

### Étape 3: Redémarrer l'Application

Après avoir créé la table, redémarrez l'application. Elle devrait maintenant fonctionner correctement.

---

## 📊 Structure de la Table

### Champs Principaux
| Champ | Type | Description |
|-------|------|-------------|
| `id_ra` | INT | ID auto-increment |
| `user_id` | INT | Référence à l'utilisateur |
| `date_analyse` | DATETIME | Timestamp de l'analyse |
| `adresse_ip` | VARCHAR(45) | Adresse IP |
| `pays_ip` | VARCHAR(100) | Pays détecté |
| `heure_connexion` | DATETIME | Heure de connexion |
| `nb_tentatives_echouees` | INT | Nombre de tentatives échouées |
| `score_risque` | INT | Score 0-100 |
| `niveau_risque` | VARCHAR(50) | FAIBLE, MOYEN, ÉLEVÉ, CRITIQUE |
| `raisons` | TEXT | Raisons du score |
| `action_prise` | VARCHAR(50) | AUTORISÉ ou BLOQUÉ |

### Champs Supplémentaires (Rapport Amélioré)
| Champ | Type | Description |
|-------|------|-------------|
| `temps_connexion_ms` | INT | Temps de connexion en ms |
| `vitesse_ecriture` | DOUBLE | Vitesse d'écriture (car/s) |
| `device_type` | VARCHAR(100) | Type d'appareil |
| `navigateur` | VARCHAR(100) | Navigateur utilisé |
| `systeme_exploitation` | VARCHAR(100) | Système d'exploitation |
| `localisation_precise` | VARCHAR(255) | Localisation précise |

---

## 🔍 Vérification

### Vérifier que la table est créée
```sql
SELECT * FROM risk LIMIT 1;
```

### Vérifier la structure
```sql
DESCRIBE risk;
```

### Vérifier les index
```sql
SHOW INDEX FROM risk;
```

---

## 📝 Exemple d'Enregistrement

Après la première connexion, vous devriez voir:

```sql
SELECT * FROM risk ORDER BY date_analyse DESC LIMIT 1;
```

Résultat:
```
id_ra: 1
user_id: 1
date_analyse: 2026-05-07 14:30:00
adresse_ip: 127.0.0.1
pays_ip: Tunisia
heure_connexion: 2026-05-07 14:30:00
nb_tentatives_echouees: 0
score_risque: 12
niveau_risque: ✅ FAIBLE
raisons: Heure normale (14h)
action_prise: AUTORISÉ
temps_connexion_ms: 245
vitesse_ecriture: 50.5
device_type: Desktop
navigateur: Chrome
systeme_exploitation: Windows 10
localisation_precise: NULL
```

---

## 🧪 Test Rapide

### 1. Créer la table
```sql
-- Exécuter le script SQL ci-dessus
```

### 2. Redémarrer l'application
```bash
mvn javafx:run
```

### 3. Se connecter
- Email: user@example.com
- Password: password123
- Captcha: Répondre à la question

### 4. Vérifier les données
```sql
SELECT * FROM risk ORDER BY date_analyse DESC LIMIT 5;
```

---

## 🎯 Rapport Amélioré

Le rapport affiche maintenant:

### Statistiques Globales
- ✅ Total Connexions
- ✅ Connexions Bloquées
- ✅ Score Moyen
- ✅ Utilisateurs Uniques
- ✅ **Temps Moyen de Connexion** (NOUVEAU)
- ✅ **Vitesse Moyenne d'Écriture** (NOUVEAU)

### Connexions à Risque Élevé
Tableau avec colonnes:
- Score
- Utilisateur
- IP
- Pays
- **Temps de Connexion** (NOUVEAU)
- **Vitesse d'Écriture** (NOUVEAU)
- Raison
- Action

---

## 🎨 Design Dark/White Mode

Le rapport s'adapte automatiquement au thème:

### Mode Dark
- Fond sombre
- Texte clair
- Couleurs adaptées

### Mode White
- Fond clair
- Texte foncé
- Couleurs adaptées

---

## 🔧 Maintenance

### Nettoyer les Anciennes Données
```sql
DELETE FROM risk WHERE date_analyse < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

### Optimiser la Table
```sql
OPTIMIZE TABLE risk;
```

### Vérifier l'Intégrité
```sql
CHECK TABLE risk;
```

---

## 📞 Dépannage

### Erreur: Table n'existe pas
**Solution**: Exécuter le script SQL de création

### Erreur: Clé étrangère
**Solution**: Vérifier que la table `user` existe

### Erreur: Permissions
**Solution**: Vérifier les permissions de l'utilisateur MySQL

---

## ✅ Checklist

- [ ] Script SQL exécuté
- [ ] Table créée avec succès
- [ ] Structure vérifiée
- [ ] Application redémarrée
- [ ] Première connexion testée
- [ ] Données enregistrées
- [ ] Rapport visible
- [ ] Statistiques correctes

---

**Status**: ✅ **PRÊT À UTILISER**
**Date**: May 7, 2026

