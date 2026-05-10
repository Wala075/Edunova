# ⚡ Démarrage Rapide - Google OAuth2 avec WebView

## 🚀 Commandes Rapides

### 1. Recompiler le Projet
```bash
mvn clean compile
```

### 2. Construire le JAR
```bash
mvn clean package
```

### 3. Lancer l'Application
```bash
mvn javafx:run
```

---

## 🧪 Test Rapide

### Étape 1: Démarrer l'Application
```bash
mvn javafx:run
```

### Étape 2: Tester la Connexion Google
1. Cliquer sur "Continuer avec Google"
2. Vérifier que la fenêtre s'ouvre
3. Vérifier que la page Google s'affiche
4. Sélectionner un compte
5. Vérifier la redirection vers Dashboard

### Étape 3: Vérifier les Données
```sql
SELECT * FROM risk_analysis 
ORDER BY created_at DESC 
LIMIT 1;
```

---

## 📊 Vérification

### Logs à Vérifier
```
✅ GoogleOAuth2WindowController: Initialisation
✅ GoogleOAuth2WindowController: Chargement URL
✅ GoogleOAuth2WindowController: Serveur HTTP démarré
✅ GoogleOAuth2WindowController: Callback reçu
✅ GoogleOAuth2WindowController: Code d'autorisation reçu
```

### Pas d'Erreurs
```
❌ URISyntaxException
❌ BindException
❌ Autres exceptions
```

---

## 🎯 Résultat Attendu

✅ **Fenêtre s'ouvre**
- Barre de titre: "🔐 Connexion Google"
- WebView affiche la page Google
- Bouton "Annuler"

✅ **Authentification**
- Utilisateur sélectionne un compte
- Authentification réussie
- Fenêtre se ferme automatiquement

✅ **Redirection**
- Utilisateur redirigé vers Dashboard
- Données enregistrées dans la BD
- Rapport de risque affichée

---

## 🐛 Dépannage Rapide

### Problème: Erreur de Compilation
```bash
# Nettoyer et recompiler
mvn clean compile -X
```

### Problème: Port 8888 Occupé
```bash
# Windows - Trouver le processus
netstat -ano | findstr :8888

# Tuer le processus
taskkill /PID [PID] /F
```

### Problème: WebView ne s'affiche pas
```bash
# Vérifier les logs
mvn javafx:run 2>&1 | grep -i error
```

---

## 📝 Fichiers Modifiés

1. **GoogleOAuth2WindowController.java**
   - Suppression des champs email/mot de passe
   - Ajout du WebView
   - Chargement de la page Google

2. **google_oauth2_window.fxml**
   - Suppression des champs de saisie
   - Ajout du WebView
   - Styling professionnel

---

## ✅ Checklist

- [ ] Compilation sans erreurs
- [ ] Fenêtre s'ouvre
- [ ] Page Google affichée
- [ ] Authentification réussie
- [ ] Redirection vers Dashboard
- [ ] Données enregistrées
- [ ] Rapport de risque affichée

---

## 🎉 Résumé

**Implémentation:** ✅ COMPLÈTE  
**Prêt pour:** TESTS ET DÉPLOIEMENT  
**Commande de démarrage:** `mvn javafx:run`

