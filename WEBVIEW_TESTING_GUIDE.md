# 🧪 Guide de Test - Google OAuth2 avec WebView

## 📋 Checklist de Test

### Phase 1: Compilation et Démarrage

- [ ] Recompiler le projet: `mvn clean compile`
- [ ] Construire le JAR: `mvn clean package`
- [ ] Vérifier qu'il n'y a pas d'erreurs
- [ ] Démarrer l'application
- [ ] Vérifier que l'écran de connexion s'affiche

---

### Phase 2: Ouverture de la Fenêtre WebView

#### Test 2.1: Ouverture de la Fenêtre
```
Étapes:
1. Cliquer sur le bouton "Continuer avec Google"
2. Vérifier que la fenêtre GoogleOAuth2Window s'ouvre
3. Vérifier que la fenêtre contient:
   - Barre de titre: "🔐 Connexion Google"
   - WebView affichant la page Google
   - Bouton "Annuler"

Résultat attendu: ✅ Fenêtre affichée correctement
```

#### Test 2.2: Affichage de la Page Google
```
Étapes:
1. Vérifier que la page Google s'affiche dans le WebView
2. Vérifier que le titre est "Se connecter avec Google"
3. Vérifier que les comptes disponibles s'affichent
4. Vérifier que le bouton "Utiliser un autre compte" est visible

Résultat attendu: ✅ Page Google affichée correctement
```

#### Test 2.3: Annulation
```
Étapes:
1. Cliquer sur le bouton "Annuler"
2. Vérifier que la fenêtre se ferme
3. Vérifier que vous restez sur l'écran de connexion

Résultat attendu: ✅ Fenêtre fermée, retour à l'écran de connexion
```

---

### Phase 3: Authentification Google

#### Test 3.1: Sélection d'un Compte
```
Étapes:
1. Cliquer sur un compte Google disponible
2. Vérifier que l'authentification se fait
3. Vérifier que la page change

Résultat attendu: ✅ Compte sélectionné, authentification en cours
```

#### Test 3.2: Authentification Réussie
```
Étapes:
1. Après sélection du compte, vérifier que:
   - La page affiche "✅ Connexion réussie!"
   - La fenêtre se ferme automatiquement
   - Vous êtes redirigé vers le Dashboard

Résultat attendu: ✅ Authentification réussie, redirection vers Dashboard
```

#### Test 3.3: Utiliser un Autre Compte
```
Étapes:
1. Cliquer sur "Utiliser un autre compte"
2. Entrer votre email Google
3. Entrer votre mot de passe
4. Vérifier que l'authentification réussit

Résultat attendu: ✅ Authentification avec un autre compte réussie
```

---

### Phase 4: Enregistrement des Données

#### Test 4.1: Données de Connexion Google
```
Étapes:
1. Se connecter avec Google (voir Test 3.2)
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

#### Test 4.2: Historique de Connexion
```
Étapes:
1. Ouvrir la base de données
2. Exécuter la requête:
   SELECT * FROM login_history 
   WHERE user_id = [votre_id] 
   ORDER BY id DESC 
   LIMIT 1;

Vérifier:
- ✓ user_id est correct
- ✓ adresse_ip_lh = "127.0.0.1"
- ✓ succes_lh = 1 (succès)

Résultat attendu: ✅ Historique enregistré correctement
```

---

### Phase 5: Rapport de Risque

#### Test 5.1: Affichage du Rapport
```
Étapes:
1. Se connecter à l'application
2. Aller à la section "Utilisateurs"
3. Vérifier que la section "Rapport de Score de Risque" s'affiche
4. Vérifier que le rapport contient:
   - 📊 Total Connexions
   - 🚫 Connexions Bloquées
   - ⚠️ Score Moyen
   - 🔐 Tentatives Connexion
   - ⚡ Risque Élevé
   - ⌨️ Temps d'Écriture

Résultat attendu: ✅ Rapport affichée correctement
```

#### Test 5.2: Mise à Jour du Rapport
```
Étapes:
1. Noter le nombre de "Tentatives Connexion"
2. Se déconnecter
3. Se reconnecter avec Google
4. Aller à la section "Utilisateurs"
5. Vérifier que le nombre de "Tentatives Connexion" a augmenté

Résultat attendu: ✅ Rapport mis à jour après chaque connexion
```

---

### Phase 6: Intégration

#### Test 6.1: Connexion Normale Toujours Fonctionnelle
```
Étapes:
1. Aller à l'écran de connexion
2. Entrer email et mot de passe
3. Vérifier que la connexion fonctionne
4. Vérifier que vous êtes redirigé vers le Dashboard

Résultat attendu: ✅ Connexion normale fonctionne
```

#### Test 6.2: Pas de Régressions
```
Étapes:
1. Tester toutes les fonctionnalités principales
2. Vérifier qu'aucune fonctionnalité n'est cassée
3. Vérifier que l'application fonctionne correctement

Résultat attendu: ✅ Pas de régressions
```

---

### Phase 7: Gestion des Erreurs

#### Test 7.1: Port 8888 Occupé
```
Étapes:
1. Occuper le port 8888 (ex: avec un autre service)
2. Cliquer sur "Continuer avec Google"
3. Vérifier que la fenêtre s'ouvre quand même
4. Vérifier que le serveur utilise un port dynamique

Résultat attendu: ✅ Fallback vers port dynamique
```

#### Test 7.2: Fermeture de la Fenêtre
```
Étapes:
1. Cliquer sur "Continuer avec Google"
2. Fermer la fenêtre (X)
3. Vérifier que vous restez sur l'écran de connexion
4. Vérifier qu'il n'y a pas d'erreurs

Résultat attendu: ✅ Fermeture gracieuse
```

---

## 📊 Résultats Attendus

### Connexion Google OAuth2 avec WebView
- ✅ Fenêtre s'ouvre
- ✅ Page Google affichée dans WebView
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

### Problème: La page Google ne s'affiche pas
**Solution:**
1. Vérifier que le WebView est correctement configuré
2. Vérifier que l'URL OAuth2 est correcte
3. Vérifier les logs de l'application
4. Vérifier l'accès à Internet

### Problème: Le callback n'est pas reçu
**Solution:**
1. Vérifier que le port 8888 est disponible
2. Vérifier que le serveur HTTP démarre correctement
3. Vérifier les logs de l'application
4. Vérifier que la redirection URI est correcte

### Problème: Les données ne sont pas enregistrées
**Solution:**
1. Vérifier que la table risk_analysis existe
2. Vérifier que la connexion à la BD fonctionne
3. Vérifier les logs de l'application
4. Vérifier que le RiskDAO fonctionne correctement

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
- [ ] WebView affiche la page Google
- [ ] Données enregistrées correctement
- [ ] Rapport de risque affichée
- [ ] Libellé "Tentatives Connexion" correct
- [ ] Connexion normale toujours fonctionnelle
- [ ] Pas de régressions

**Résultat:** ✅ Implémentation réussie

