# ⚡ Commandes Rapides

## 🚀 Démarrer l'Application

### Option 1: IntelliJ IDEA (Recommandé)
```
1. Ouvrir le projet dans IntelliJ
2. Cliquer sur le bouton ▶️ Run (ou Shift+F10)
3. L'application démarre
```

### Option 2: Ligne de Commande
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" javafx:run
```

### Option 3: Avec Maven Wrapper (si disponible)
```bash
cd c:\Users\PC\IdeaProjects\Login
mvnw javafx:run
```

---

## 🔧 Compiler le Projet

### Compilation Complète
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
```

### Compilation Rapide (sans nettoyer)
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" compile
```

---

## 🧹 Nettoyer le Projet

### Nettoyer les Fichiers Compilés
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean
```

### Nettoyer et Compiler
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
```

---

## 📦 Construire le Projet

### Construire le JAR
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean package
```

---

## 🧪 Tester le Captcha

### Flux de Test Complet
1. Compiler: `mvn clean compile`
2. Exécuter: `mvn javafx:run`
3. Cocher "Je ne suis pas un robot"
4. Voir la question mathématique
5. Entrer la bonne réponse
6. Cliquer "Vérifier"
7. Voir le message de succès
8. Entrer email et mot de passe
9. Cliquer "Se connecter"

---

## 📋 Alias Utiles (PowerShell)

### Créer des Alias
```powershell
# Ajouter à votre profil PowerShell
Set-Alias -Name mvn -Value "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd"
```

### Utiliser les Alias
```bash
cd c:\Users\PC\IdeaProjects\Login
mvn clean compile
mvn javafx:run
```

---

## 🔍 Vérifier l'Installation

### Vérifier Maven
```bash
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" --version
```

### Vérifier Java
```bash
java -version
```

### Vérifier JDK
```bash
javac -version
```

---

## 📁 Fichiers Importants

### Fichiers Modifiés
- `src/main/java/edunova/connexion/controllers/LoginController.java`
- `src/main/resources/views/login.fxml`

### Fichiers de Configuration
- `pom.xml` - Configuration Maven
- `src/main/resources/config.properties` - Configuration API

### Fichiers de Documentation
- `CAPTCHA_INTERFACE.md` - Guide complet
- `TEST_CAPTCHA.md` - Checklist de test
- `RESUME_IMPLEMENTATION.md` - Résumé des changements
- `VISUAL_GUIDE.md` - Guide visuel
- `FINAL_SUMMARY.md` - Synthèse finale

---

## 🐛 Débogage

### Voir les Logs Détaillés
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile -X
```

### Voir les Erreurs de Compilation
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile -e
```

---

## 💾 Sauvegarder et Committer

### Vérifier le Statut Git
```bash
cd c:\Users\PC\IdeaProjects\Login
git status
```

### Ajouter les Fichiers
```bash
git add src/main/java/edunova/connexion/controllers/LoginController.java
git add src/main/resources/views/login.fxml
```

### Committer les Changements
```bash
git commit -m "Implémentation du captcha mathématique sur l'interface login"
```

### Pousser vers GitHub
```bash
git push origin main
```

---

## 🎯 Workflow Complet

### 1. Compiler et Tester
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" javafx:run
```

### 2. Vérifier les Changements
```bash
git status
git diff
```

### 3. Committer et Pousser
```bash
git add .
git commit -m "Implémentation du captcha mathématique"
git push origin main
```

---

## 📊 Commandes Utiles

### Lister les Dépendances
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" dependency:tree
```

### Mettre à Jour les Dépendances
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" versions:display-dependency-updates
```

### Générer la Documentation
```bash
cd c:\Users\PC\IdeaProjects\Login
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" javadoc:javadoc
```

---

## 🚨 Résolution de Problèmes

### Erreur: "No compiler is provided"
```bash
# Solution: Vérifier que JDK est installé
java -version
javac -version

# Si JDK n'est pas trouvé, définir JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

### Erreur: "Maven not found"
```bash
# Solution: Utiliser le chemin complet
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean compile
```

### Erreur: "Port already in use"
```bash
# Solution: Tuer le processus existant
netstat -ano | findstr :8888
taskkill /PID <PID> /F
```

---

## 📞 Aide Rapide

### Besoin d'Aide?
1. Lire `FINAL_SUMMARY.md` pour une vue d'ensemble
2. Lire `CAPTCHA_INTERFACE.md` pour les détails techniques
3. Lire `TEST_CAPTCHA.md` pour les scénarios de test
4. Vérifier les logs de la console pour les erreurs

### Fichiers à Consulter
- `QUICK_COMMANDS.md` - Ce fichier (commandes rapides)
- `VISUAL_GUIDE.md` - Guide visuel de l'interface
- `RESUME_IMPLEMENTATION.md` - Résumé des changements

---

## ⏱️ Temps Estimé

| Tâche | Temps |
|-------|-------|
| Compiler | 30-60 secondes |
| Exécuter | 5-10 secondes |
| Tester le captcha | 2-3 minutes |
| Tester la connexion | 1-2 minutes |
| **Total** | **5-10 minutes** |

---

## ✅ Checklist Rapide

- [ ] Compiler sans erreur
- [ ] Exécuter l'application
- [ ] Cocher le checkbox
- [ ] Voir la question
- [ ] Entrer la réponse
- [ ] Cliquer "Vérifier"
- [ ] Voir le succès
- [ ] Se connecter
- [ ] Voir le dashboard

---

**Status**: ✅ Prêt à Utiliser
**Dernière Mise à Jour**: May 7, 2026
