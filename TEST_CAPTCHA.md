# 🧪 Guide de Test - Captcha Interface

## ✅ Checklist de Test Complète

### 1. Démarrage de l'Application
- [ ] Application démarre sans erreur
- [ ] Écran de connexion s'affiche
- [ ] Tous les composants sont visibles

### 2. Checkbox "Je ne suis pas un robot"
- [ ] Checkbox visible sur l'interface
- [ ] Label "Je ne suis pas un robot" visible
- [ ] Icône 🛡️ visible

### 3. Comportement du Checkbox (Non Coché)
- [ ] Question captcha n'apparaît pas
- [ ] TextField réponse n'apparaît pas
- [ ] Bouton "Vérifier" n'apparaît pas
- [ ] Utilisateur peut entrer email/mot de passe

### 4. Comportement du Checkbox (Coché)
- [ ] Question captcha apparaît
- [ ] Question est lisible et claire
- [ ] TextField réponse apparaît
- [ ] Bouton "Vérifier" apparaît
- [ ] Focus automatique sur le TextField

### 5. Génération de Question
- [ ] Question est du format: "Combien font X + Y ?"
- [ ] X et Y sont entre 1 et 10
- [ ] Chaque fois qu'on coche, nouvelle question générée
- [ ] Questions varient (pas toujours la même)

### 6. Vérification - Bonne Réponse
**Test**: Cocher checkbox → Voir question "Combien font 5 + 3 ?" → Entrer "8" → Cliquer "Vérifier"
- [ ] Message ✅ "Vérification réussie" s'affiche
- [ ] Message en vert
- [ ] Question disparaît
- [ ] TextField disparaît
- [ ] Bouton "Vérifier" disparaît
- [ ] Checkbox reste coché
- [ ] Utilisateur peut maintenant se connecter

### 7. Vérification - Mauvaise Réponse
**Test**: Cocher checkbox → Voir question "Combien font 5 + 3 ?" → Entrer "7" → Cliquer "Vérifier"
- [ ] Message ⚠️ "Réponse incorrecte, réessayez" s'affiche
- [ ] Message en rouge
- [ ] TextField se vide
- [ ] Nouvelle question générée
- [ ] Focus revient au TextField
- [ ] Utilisateur peut réessayer

### 8. Vérification - Entrée Invalide
**Test**: Cocher checkbox → Entrer "abc" → Cliquer "Vérifier"
- [ ] Message ⚠️ "Veuillez entrer un nombre valide" s'affiche
- [ ] TextField se vide
- [ ] Focus revient au TextField

### 9. Vérification - Champ Vide
**Test**: Cocher checkbox → Laisser vide → Cliquer "Vérifier"
- [ ] Message ⚠️ "Veuillez entrer votre réponse" s'affiche
- [ ] Focus revient au TextField

### 10. Décocher le Checkbox
**Test**: Cocher → Voir question → Décocher
- [ ] Question disparaît
- [ ] TextField disparaît
- [ ] Bouton "Vérifier" disparaît
- [ ] Message d'erreur disparaît
- [ ] captchaValide = false

### 11. Connexion Sans Vérification
**Test**: Entrer email/mot de passe → Cliquer "Se connecter" (sans cocher captcha)
- [ ] Message d'erreur: "Veuillez cocher 'Je ne suis pas un robot'..."
- [ ] Connexion échouée
- [ ] Utilisateur reste sur l'écran de connexion

### 12. Connexion Avec Vérification Réussie
**Test**: Cocher → Résoudre question → Entrer email/mot de passe valides → Cliquer "Se connecter"
- [ ] Connexion réussie
- [ ] Redirection vers Dashboard
- [ ] Captcha réinitialisé

### 13. Connexion Avec Vérification Échouée
**Test**: Cocher → Mauvaise réponse → Entrer email/mot de passe → Cliquer "Se connecter"
- [ ] Message d'erreur: "Veuillez cocher 'Je ne suis pas un robot'..."
- [ ] Connexion échouée
- [ ] Utilisateur reste sur l'écran de connexion

### 14. Réinitialisation Après Erreur de Connexion
**Test**: Cocher → Résoudre question → Entrer mauvais mot de passe → Cliquer "Se connecter"
- [ ] Message d'erreur de mot de passe
- [ ] Captcha réinitialisé
- [ ] Checkbox décochée
- [ ] Question disparaît

### 15. Validation Email/Mot de Passe
- [ ] Email validation fonctionne
- [ ] Mot de passe validation fonctionne
- [ ] Messages d'erreur appropriés

### 16. Styles et Apparence
- [ ] Checkbox bien aligné
- [ ] Question bien formatée
- [ ] TextField bien stylisé
- [ ] Bouton bien stylisé
- [ ] Messages bien visibles
- [ ] Couleurs cohérentes

### 17. Responsive Design
- [ ] Interface s'adapte à différentes tailles
- [ ] Tous les composants visibles
- [ ] Pas de débordement de texte

### 18. Performance
- [ ] Pas de lag lors du clic sur checkbox
- [ ] Pas de lag lors de la vérification
- [ ] Pas de lag lors de la génération de question

---

## 🎯 Cas d'Usage Complets

### Cas 1: Utilisateur Valide - Première Tentative
```
1. Ouvrir l'application
2. Cocher "Je ne suis pas un robot"
3. Voir question: "Combien font 7 + 4 ?"
4. Entrer "11"
5. Cliquer "Vérifier"
6. ✅ "Vérification réussie"
7. Entrer email: user@edunova.com
8. Entrer mot de passe: password123
9. Cliquer "Se connecter"
10. ✅ Redirection vers Dashboard
```

### Cas 2: Utilisateur Valide - Mauvaise Réponse Puis Bonne
```
1. Ouvrir l'application
2. Cocher "Je ne suis pas un robot"
3. Voir question: "Combien font 3 + 2 ?"
4. Entrer "6" (mauvais)
5. Cliquer "Vérifier"
6. ⚠️ "Réponse incorrecte, réessayez"
7. Voir nouvelle question: "Combien font 8 + 5 ?"
8. Entrer "13" (correct)
9. Cliquer "Vérifier"
10. ✅ "Vérification réussie"
11. Entrer email et mot de passe
12. Cliquer "Se connecter"
13. ✅ Redirection vers Dashboard
```

### Cas 3: Utilisateur Oublie de Cocher
```
1. Ouvrir l'application
2. Entrer email: user@edunova.com
3. Entrer mot de passe: password123
4. Cliquer "Se connecter"
5. ⚠️ "Veuillez cocher 'Je ne suis pas un robot'..."
6. Cocher "Je ne suis pas un robot"
7. Résoudre question
8. Cliquer "Se connecter"
9. ✅ Redirection vers Dashboard
```

### Cas 4: Utilisateur Décoche Après Vérification
```
1. Oubrir l'application
2. Cocher "Je ne suis pas un robot"
3. Résoudre question correctement
4. ✅ "Vérification réussie"
5. Décocher "Je ne suis pas un robot"
6. Entrer email et mot de passe
7. Cliquer "Se connecter"
8. ⚠️ "Veuillez cocher 'Je ne suis pas un robot'..."
```

---

## 📊 Résultats Attendus

### Succès
- ✅ Tous les tests passent
- ✅ Pas d'erreur dans la console
- ✅ Interface fluide et réactive
- ✅ Messages clairs et utiles
- ✅ Flux utilisateur logique

### Problèmes Potentiels
- ❌ Checkbox ne fonctionne pas
- ❌ Question n'apparaît pas
- ❌ Vérification ne fonctionne pas
- ❌ Erreurs dans la console
- ❌ Interface figée

---

## 🐛 Débogage

### Si le Checkbox ne Fonctionne Pas
1. Vérifier que `handleCaptchaCheckbox()` est appelée
2. Vérifier que `vboxCaptchaQuestion` existe dans le FXML
3. Vérifier les logs de la console

### Si la Question N'Apparaît Pas
1. Vérifier que `genererQuestionCaptcha()` est appelée
2. Vérifier que `lblCaptchaQuestion` existe dans le FXML
3. Vérifier que `vboxCaptchaQuestion.setVisible(true)` est appelée

### Si la Vérification Ne Fonctionne Pas
1. Vérifier que `handleVerifierCaptcha()` est appelée
2. Vérifier que `captchaReponseCorrecte` est correctement défini
3. Vérifier que la réponse est correctement parsée

### Si Erreur dans la Console
1. Lire le message d'erreur complètement
2. Vérifier la ligne de code mentionnée
3. Vérifier que tous les composants FXML existent
4. Vérifier que tous les IDs FXML correspondent

---

## 📝 Notes de Test

- Tester avec différents navigateurs (si applicable)
- Tester avec différentes résolutions d'écran
- Tester avec clavier et souris
- Tester les cas limites (nombres très grands, caractères spéciaux, etc.)
- Tester la performance avec plusieurs tentatives

---

**Status**: ✅ Prêt à Tester
**Dernière Mise à Jour**: May 7, 2026
