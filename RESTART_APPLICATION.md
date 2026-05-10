# 🔄 Instructions pour Relancer l'Application

## ✅ Corrections Appliquées

Les bugs suivants ont été corrigés:
1. ✅ URISyntaxException - Encodage des paramètres OAuth2
2. ✅ BindException - Gestion du port 8888 occupé

---

## 🚀 Étapes pour Relancer

### Étape 1: Arrêter l'Application Actuelle
- Fermer la fenêtre de l'application
- Attendre que le processus se termine
- Vérifier qu'il n'y a pas d'autres instances en cours d'exécution

### Étape 2: Nettoyer et Recompiler
```bash
# Nettoyer les fichiers compilés
mvn clean

# Recompiler le projet
mvn clean compile
```

### Étape 3: Relancer l'Application
```bash
# Option 1: Via Maven
mvn javafx:run

# Option 2: Via IntelliJ IDEA
# Cliquer sur le bouton "Run" ou appuyer sur Shift+F10
```

### Étape 4: Tester la Connexion Google OAuth2
1. Cliquer sur "Continuer avec Google"
2. Vérifier que la fenêtre GoogleOAuth2Window s'ouvre
3. Entrer votre email Google
4. Cliquer sur "Se connecter"
5. Vérifier que le navigateur s'ouvre
6. S'authentifier avec Google
7. Vérifier la redirection vers le Dashboard

---

## 🔍 Vérification

### Logs à Vérifier

**Succès:**
```
GoogleOAuth2WindowController: Initialisation
GoogleOAuth2WindowController: Serveur HTTP démarré sur le port 8888
GoogleOAuth2WindowController: Ouverture navigateur pour: [email]
GoogleOAuth2WindowController: Callback reçu
GoogleOAuth2WindowController: Code d'autorisation reçu
```

**Erreurs à Éviter:**
```
❌ URISyntaxException: Illegal character in query
❌ BindException: Address already in use
```

---

## 🛠️ Dépannage

### Problème: Port 8888 Toujours Occupé

**Solution:**
```bash
# Windows - Trouver le processus utilisant le port 8888
netstat -ano | findstr :8888

# Tuer le processus
taskkill /PID [PID] /F

# Puis relancer l'application
mvn javafx:run
```

### Problème: Erreur de Compilation

**Solution:**
```bash
# Nettoyer complètement
mvn clean

# Vérifier les erreurs
mvn compile -X

# Recompiler
mvn clean compile
```

### Problème: Navigateur ne s'Ouvre pas

**Solution:**
1. Vérifier que le navigateur par défaut est configuré
2. Vérifier que Desktop.isDesktopSupported() retourne true
3. Vérifier les logs de l'application

---

## 📊 Checklist de Vérification

- [ ] Application démarre sans erreurs
- [ ] Écran de connexion s'affiche
- [ ] Bouton "Continuer avec Google" fonctionne
- [ ] Fenêtre GoogleOAuth2Window s'ouvre
- [ ] Navigateur s'ouvre pour l'authentification
- [ ] Code d'autorisation est reçu
- [ ] Redirection vers Dashboard fonctionne
- [ ] Données sont enregistrées dans la BD
- [ ] Rapport de risque s'affiche
- [ ] Libellé "Tentatives Connexion" est correct

---

## 🎯 Résultat Attendu

Après les corrections et le redémarrage:

✅ **Connexion Google OAuth2 fonctionne correctement**
- Fenêtre s'ouvre
- Navigateur s'ouvre
- Authentification réussie
- Redirection vers Dashboard

✅ **Enregistrement des Données**
- Données enregistrées dans risk_analysis
- Score de risque calculé
- Niveau de risque défini

✅ **Rapport de Risque**
- Section affichée dans "Utilisateurs"
- Statistiques correctes
- Libellé "Tentatives Connexion" correct

---

## 📝 Notes

- Les corrections sont minimales et ne changent pas la logique métier
- Les corrections gèrent les cas d'erreur gracieusement
- Les logs sont améliorés pour le débogage
- Le port dynamique est utilisé en fallback si le port 8888 est occupé

---

## ✨ Conclusion

L'application est maintenant prête à être relancée avec les corrections appliquées.

**Prochaine étape:** Relancer l'application et tester la connexion Google OAuth2.

