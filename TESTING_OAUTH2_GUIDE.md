# 🧪 Guide de Test - Connexion Google OAuth2 et Rapport de Risque

## 📋 Checklist de Test

### Phase 1: Compilation et Démarrage

- [ ] Compiler le projet: `mvn clean compile`
- [ ] Construire le JAR: `mvn clean package`
- [ ] Vérifier qu'il n'y a pas d'erreurs
- [ ] Démarrer l'application
- [ ] Vérifier que l'écran de connexion s'affiche

---

### Phase 2: Connexion Google OAuth2

#### Test 2.1: Ouverture de la Fenêtre
```
Étapes:
1. Cliquer sur le bouton "Continuer avec Google"
2. Vérifier que la fenêtre GoogleOAuth2Window s'ouvre
3. Vérifier que la fenêtre contient:
   - Titre "🔐 Connexion Google"
   - Champ email
   - Champ mot de passe
   - Boutons "Se connecter" et "Annuler"
   - Message informatif

Résultat attendu: ✅ Fenêtre affichée correctement
```

#### Test 2.2: Authentification Google
```
Étapes:
1. Entrer votre email Google
2. Cliquer sur "Se connecter"
3. Vérifier que le navigateur s'ouvre
4. Vérifier que l'URL est: https://accounts.google.com/o/oauth2/v2/auth?...
5. S'authentifier avec vos identifiants Google
6. Accepter les permissions
7. Vérifier que le navigateur redirige vers http://localhost:8888/Callback
8. Vérifier que la fenêtre affiche "✅ Authentification réussie!"

Résultat attendu: ✅ Redirection vers le Dashboard
```

#### Test 2.3: Annulation
```
Étapes:
1. Cliquer sur le bouton "Continuer avec Google"
2. Cliquer sur "Annuler"
3. Vérifier que la fenêtre se ferme
4. Vérifier que vous restez sur l'écran de connexion

Résultat attendu: ✅ Fenêtre fermée, retour à l'écran de connexion
```

---

### Phase 3: Enregistrement des Données

#### Test 3.1: Données de Connexion Google
```
Étapes:
1. Se connecter avec Google (voir Test 2.2)
2. Ouvrir la base de données
3. Exécuter la requête:
   SELECT * FROM risk_analysis 
   WHERE user_id = [votre_id] 
   ORDER BY created_at DESC 
   LIMIT 1;

Vérifier:
- ✓ user_id est correct
- ✓ ip_address = "127.0.0.1"
- ✓ country = "Local"
- ✓ device = "Desktop"
- ✓ login_time est récent
- ✓ risk_score est entre 0 et 100
- ✓ risk_level est défini (LOW/MEDIUM/HIGH)
- ✓ blocked est FALSE ou TRUE

Résultat attendu: ✅ Données enregistrées correctement
```

#### Test 3.2: Données de Connexion Normale
```
Étapes:
1. Se connecter avec email/mot de passe
2. Ouvrir la base de données
3. Exécuter la requête:
   SELECT * FROM risk_analysis 
   WHERE user_id = [votre_id] 
   ORDER BY created_at DESC 
   LIMIT 1;

Vérifier:
- ✓ Données enregistrées
- ✓ risk_score calculé
- ✓ risk_level défini

Résultat attendu: ✅ Données enregistrées correctement
```

---

### Phase 4: Rapport de Risque

#### Test 4.1: Affichage du Rapport
```
Étapes:
1. Se connecter à l'application
2. Aller à la section "Utilisateurs"
3. Vérifier que la section "Rapport de Score de Risque" s'affiche
4. Vérifier que le rapport contient:
   - 📊 Statistiques Globales
   - 📊 Total Connexions
   - 🚫 Connexions Bloquées
   - ⚠️ Score Moyen
   - 🔐 Tentatives Connexion (au lieu de "Tentatives de Connexion")
   - ⚡ Risque Élevé
   - ⌨️ Temps d'Écriture

Résultat attendu: ✅ Rapport affichée correctement
```

#### Test 4.2: Mise à Jour du Rapport
```
Étapes:
1. Noter le nombre de "Tentatives Connexion"
2. Se déconnecter
3. Se reconnecter
4. Aller à la section "Utilisateurs"
5. Vérifier que le nombre de "Tentatives Connexion" a augmenté

Résultat attendu: ✅ Rapport mis à jour après chaque connexion
```

#### Test 4.3: Libellé "Tentatives Connexion"
```
Étapes:
1. Aller à la section "Utilisateurs"
2. Vérifier que le rapport affiche "🔐 Tentatives Connexion"
3. Vérifier que ce n'est pas "Tentatives de Connexion"

Résultat attendu: ✅ Libellé correct
```

---

### Phase 5: Intégration

#### Test 5.1: Connexion Normale Toujours Fonctionnelle
```
Étapes:
1. Aller à l'écran de connexion
2. Entrer email et mot de passe
3. Vérifier que la connexion fonctionne
4. Vérifier que vous êtes redirigé vers le Dashboard

Résultat attendu: ✅ Connexion normale fonctionne
```

#### Test 5.2: Historique de Connexion
```
Étapes:
1. Ouvrir la base de données
2. Exécuter la requête:
   SELECT * FROM login_history 
   WHERE user_id = [votre_id] 
   ORDER BY id DESC 
   LIMIT 5;

Vérifier:
- ✓ Historique enregistré pour chaque connexion
- ✓ Adresse IP enregistrée
- ✓ Statut de succès enregistré

Résultat attendu: ✅ Historique enregistré correctement
```

---

### Phase 6: Gestion des Erreurs

#### Test 6.1: Port 8888 Occupé
```
Étapes:
1. Occuper le port 8888 (ex: avec un autre service)
2. Cliquer sur "Continuer avec Google"
3. Vérifier le message d'erreur

Résultat attendu: ✅ Message d'erreur approprié
```

#### Test 6.2: Navigateur Non Disponible
```
Étapes:
1. Désactiver le navigateur par défaut
2. Cliquer sur "Continuer avec Google"
3. Vérifier le message d'erreur

Résultat attendu: ✅ Message d'erreur approprié
```

#### Test 6.3: Timeout
```
Étapes:
1. Cliquer sur "Continuer avec Google"
2. Ne pas s'authentifier pendant 2 minutes
3. Vérifier que la fenêtre affiche un message d'erreur

Résultat attendu: ✅ Timeout géré correctement
```

---

## 📊 Résultats Attendus

### Connexion Google OAuth2
- ✅ Fenêtre s'ouvre
- ✅ Navigateur s'ouvre
- ✅ Authentification réussie
- ✅ Redirection vers Dashboard
- ✅ Données enregistrées

### Rapport de Risque
- ✅ Section affichée dans "Utilisateurs"
- ✅ Libellé "🔐 Tentatives Connexion"
- ✅ Statistiques correctes
- ✅ Mise à jour après chaque connexion

### Enregistrement des Données
- ✅ Données enregistrées dans risk_analysis
- ✅ Score de risque calculé
- ✅ Niveau de risque défini
- ✅ Historique enregistré

---

## 🐛 Dépannage

### Problème: La fenêtre Google OAuth2 ne s'ouvre pas
**Solution:**
1. Vérifier que le port 8888 est disponible
2. Vérifier les logs de l'application
3. Vérifier que le fichier FXML est présent

### Problème: Le navigateur ne s'ouvre pas
**Solution:**
1. Vérifier que le navigateur par défaut est configuré
2. Vérifier que Desktop.isDesktopSupported() retourne true
3. Vérifier les logs de l'application

### Problème: Les données ne sont pas enregistrées
**Solution:**
1. Vérifier que la table risk_analysis existe
2. Vérifier que la connexion à la BD fonctionne
3. Vérifier les logs de l'application

### Problème: Le rapport de risque ne s'affiche pas
**Solution:**
1. Vérifier que le fichier users.fxml est modifié
2. Vérifier que le UserController charge le rapport
3. Vérifier les logs de l'application

---

## 📝 Notes

- Les tests doivent être effectués dans l'ordre
- Chaque test doit être validé avant de passer au suivant
- Les données de test peuvent être supprimées après les tests
- Les logs de l'application sont utiles pour le dépannage

---

## ✅ Validation Finale

Après tous les tests, vérifier:
- [ ] Compilation sans erreurs
- [ ] Connexion Google OAuth2 fonctionne
- [ ] Données enregistrées correctement
- [ ] Rapport de risque affichée
- [ ] Libellé "Tentatives Connexion" correct
- [ ] Connexion normale toujours fonctionnelle
- [ ] Pas de régressions

**Résultat:** ✅ Implémentation réussie

