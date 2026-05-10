# Modifications - Connexion Google OAuth2 et Rapport de Risque

## 📋 Résumé des Modifications

### 1. **Rapport de Score de Risque dans Utilisateurs**
- ✅ Ajout d'une section "Rapport de Score de Risque" dans `users.fxml`
- ✅ Affichage des statistiques de risque sous le tableau des utilisateurs
- ✅ Changement du libellé "Tentatives de Connexion" par "🔐 Tentatives Connexion"

**Fichier modifié:**
- `src/main/resources/views/users.fxml`

---

### 2. **Connexion Google OAuth2 dans une Fenêtre Séparée**

#### Nouveaux Fichiers Créés:

**a) GoogleOAuth2WindowController.java**
- Contrôleur pour la fenêtre de connexion Google native
- Gère l'authentification OAuth2 sans navigateur intégré
- Ouvre le navigateur par défaut du système pour l'authentification
- Redirection directe vers l'application après succès
- Serveur HTTP local sur le port 8888 pour le callback

**b) google_oauth2_window.fxml**
- Interface FXML pour la fenêtre de connexion Google
- Champs pour email et mot de passe
- Boutons de connexion et annulation
- Messages de statut en temps réel

#### Fichiers Modifiés:

**c) LoginController.java**
- ✅ Nouvelle méthode `handleGoogleLogin()` - Ouvre la fenêtre Google OAuth2
- ✅ Nouvelle méthode `traiterCodeGoogleOAuth2()` - Traite le code d'autorisation
- ✅ Nouvelle méthode `enregistrerDonneesConnexion()` - Enregistre les données de connexion dans la BD
- ✅ Modification de `traiterConnexionGoogle()` - Appelle `enregistrerDonneesConnexion()`

---

### 3. **Enregistrement des Données de Connexion**

Les données de connexion sont maintenant enregistrées dans la table `risk_analysis` pour:
- **Connexion normale** (email/mot de passe)
- **Connexion Google OAuth2**

**Données enregistrées:**
- `user_id` - ID de l'utilisateur
- `ip_address` - Adresse IP (127.0.0.1 pour local)
- `country` - Pays (Local/Tunisia)
- `device` - Appareil (Desktop/Windows)
- `login_time` - Heure de connexion
- `failed_attempts` - Tentatives échouées
- `typing_speed` - Vitesse de saisie
- `risk_score` - Score de risque calculé
- `risk_level` - Niveau de risque
- `blocked` - Statut de blocage

---

## 🔄 Flux de Connexion Google OAuth2

```
1. Utilisateur clique sur "Continuer avec Google"
   ↓
2. Fenêtre GoogleOAuth2Window s'ouvre
   ↓
3. Utilisateur entre son email Google
   ↓
4. Navigateur par défaut s'ouvre pour l'authentification Google
   ↓
5. Après authentification, callback reçu sur http://localhost:8888/Callback
   ↓
6. Code d'autorisation extrait et traité
   ↓
7. Infos utilisateur récupérées via GoogleOAuth2Service
   ↓
8. Données de connexion enregistrées dans la BD
   ↓
9. Utilisateur redirigé vers le Dashboard
```

---

## 📊 Rapport de Risque Amélioré

Le rapport de risque dans la section "Utilisateurs" affiche maintenant:
- **Total Connexions** - Nombre total de connexions
- **Connexions Bloquées** - Nombre de connexions bloquées
- **Score Moyen** - Score de risque moyen
- **🔐 Tentatives Connexion** - Nombre de tentatives de connexion (remplace "Utilisateurs")
- **Risque Élevé** - Nombre de connexions à risque élevé
- **Temps d'Écriture** - Temps moyen de saisie

---

## 🔧 Configuration Requise

### Port 8888
- Utilisé pour le callback OAuth2
- Doit être disponible sur la machine locale
- Peut être modifié dans `GoogleOAuth2WindowController.java` et `LoginController.java`

### Navigateur par Défaut
- Utilisé pour l'authentification Google
- Doit être configuré sur la machine

---

## ✅ Tests Recommandés

1. **Connexion Google OAuth2**
   - Cliquer sur "Continuer avec Google"
   - Vérifier que la fenêtre s'ouvre
   - Vérifier que le navigateur s'ouvre
   - Vérifier la redirection vers le Dashboard

2. **Enregistrement des Données**
   - Vérifier que les données sont enregistrées dans `risk_analysis`
   - Vérifier que le rapport de risque affiche les données

3. **Rapport de Risque**
   - Vérifier que le rapport affiche "Tentatives Connexion"
   - Vérifier que les statistiques sont correctes

---

## 📝 Notes

- Les données de connexion sont enregistrées automatiquement lors de chaque connexion
- Le score de risque est calculé par `RiskAnalyzerIA`
- Les connexions à risque élevé peuvent être bloquées
- Le rapport de risque se met à jour automatiquement

