# 🚀 Guide de Démarrage Rapide - Interface Enseignants

## ⏱️ 5 Minutes pour Intégrer l'Interface Enseignants

### Étape 1: Préparer la Base de Données (1 min)

#### Option A: Avec MySQL CLI
```bash
mysql -u root -p votre_base_de_donnees < sql/teachers_setup.sql
```

#### Option B: Avec MySQL Workbench
1. Ouvrez MySQL Workbench
2. Connectez-vous à votre serveur
3. Ouvrez le fichier `sql/teachers_setup.sql`
4. Exécutez le script (Ctrl+Shift+Enter)

#### Option C: Vérifier Manuellement
```sql
-- Vérifier que les tables existent
SHOW TABLES;

-- Vérifier les rôles
SELECT * FROM roles;

-- Vérifier la structure de teachers
DESCRIBE teachers;
```

---

### Étape 2: Ajouter le Bouton au Dashboard (2 min)

#### Modifier `DashboardController.java`

Ajoutez cette méthode:
```java
@FXML
private void handleTeachers() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/teachers.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle("Gestion des Enseignants");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(700);
        stage.show();
    } catch (IOException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("Impossible de charger l'interface enseignants.");
        alert.showAndWait();
        e.printStackTrace();
    }
}
```

#### Modifier `dashboard.fxml`

Ajoutez ce bouton dans le FXML:
```xml
<Button text="Gestion des Enseignants" onAction="#handleTeachers" 
        style="-fx-padding: 15 30; -fx-font-size: 14; -fx-background-color: #28a745; -fx-text-fill: white;" />
```

---

### Étape 3: Compiler et Exécuter (2 min)

#### Compiler
```bash
mvn clean compile
```

#### Exécuter
```bash
mvn javafx:run
```

---

## ✅ Vérification Rapide

### 1. Vérifier que les fichiers sont créés
```
✓ src/main/java/edunova/connexion/models/Teacher.java
✓ src/main/java/edunova/connexion/dao/TeacherDAO.java
✓ src/main/java/edunova/connexion/controllers/TeacherController.java
✓ src/main/resources/views/teachers.fxml
✓ sql/teachers_setup.sql
```

### 2. Vérifier la base de données
```sql
-- Vérifier les tables
SHOW TABLES LIKE '%teacher%';

-- Vérifier les rôles
SELECT * FROM roles WHERE nom = 'Enseignant';

-- Vérifier la structure
DESCRIBE teachers;
```

### 3. Tester l'interface
1. Lancez l'application
2. Cliquez sur "Gestion des Enseignants"
3. Ajoutez un enseignant de test
4. Vérifiez que les statistiques se mettent à jour

---

## 🎯 Cas d'Usage Rapides

### Ajouter un Enseignant
```
1. Cliquez sur "Gestion des Enseignants"
2. Remplissez le formulaire:
   - Nom: Dupont
   - Prénom: Jean
   - Email: jean.dupont@example.com
   - Téléphone: 0123456789
   - Spécialité: Mathématiques
   - Mot de passe: password123
   - Rôle: Enseignant
   - Actif: ☑
3. Cliquez sur "Ajouter"
```

### Rechercher un Enseignant
```
1. Entrez "Dupont" dans la barre de recherche
2. Cliquez sur "Rechercher"
3. Seul l'enseignant Dupont s'affiche
```

### Modifier un Enseignant
```
1. Cliquez sur un enseignant dans le tableau
2. Modifiez les champs
3. Cliquez sur "Modifier"
```

### Supprimer un Enseignant
```
1. Cliquez sur un enseignant dans le tableau
2. Cliquez sur "Supprimer"
3. Confirmez la suppression
```

---

## 🔧 Dépannage Rapide

### Erreur: "Cannot find resource '/views/teachers.fxml'"
**Solution**: Vérifiez que le fichier existe dans `src/main/resources/views/teachers.fxml`

### Erreur: "No role 'Enseignant' found"
**Solution**: Exécutez le script SQL `sql/teachers_setup.sql`

### Erreur: "Duplicate entry for key 'email'"
**Solution**: Utilisez un email unique pour chaque enseignant

### Erreur: "Connection refused"
**Solution**: Vérifiez que MySQL est en cours d'exécution et que les identifiants sont corrects

### Les statistiques ne se mettent pas à jour
**Solution**: Vérifiez que la méthode `mettreAJourStatistiques()` est appelée après chaque opération

---

## 📊 Statistiques Disponibles

| Statistique | Description |
|-------------|-------------|
| **Total Enseignants** | Nombre total d'enseignants |
| **Enseignants Actifs** | Nombre d'enseignants avec le statut actif |
| **Enseignants Inactifs** | Nombre d'enseignants avec le statut inactif |

---

## 🎨 Personnalisation Rapide

### Changer la Couleur du Bouton "Ajouter"
Dans `teachers.fxml`, modifiez:
```xml
<Button text="Ajouter" onAction="#handleAjouter" 
        style="-fx-padding: 10 20; -fx-font-size: 12; -fx-background-color: #28a745; -fx-text-fill: white;" />
```

Changez `#28a745` (vert) par une autre couleur:
- Bleu: `#007bff`
- Rouge: `#dc3545`
- Orange: `#fd7e14`
- Violet: `#6f42c1`

### Ajouter une Colonne au Tableau
1. Ajoutez une `TableColumn` dans le FXML
2. Ajoutez une propriété dans le modèle `Teacher`
3. Liez la colonne à la propriété dans le contrôleur

---

## 📚 Documentation Complète

Pour plus de détails, consultez:
- `TEACHERS_INTEGRATION.md` - Documentation technique complète
- `INTEGRATION_EXAMPLE.md` - Exemples de code détaillés
- `TEACHERS_SUMMARY.md` - Résumé des fonctionnalités
- `PROJECT_STRUCTURE.md` - Structure du projet

---

## 🎓 Exemple Complet d'Intégration

### Fichier: `DashboardController.java`
```java
package edunova.connexion.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private void handleTeachers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/teachers.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gestion des Enseignants");
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(700);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger l'interface enseignants.");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/users.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Gestion des Utilisateurs");
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(700);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger l'interface utilisateurs.");
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
```

---

## 🚀 Commandes Utiles

### Compiler le projet
```bash
mvn clean compile
```

### Exécuter l'application
```bash
mvn javafx:run
```

### Nettoyer les fichiers compilés
```bash
mvn clean
```

### Construire le projet
```bash
mvn package
```

---

## 📋 Checklist d'Intégration

- [ ] Exécuter le script SQL
- [ ] Vérifier que les tables sont créées
- [ ] Ajouter la méthode `handleTeachers()` au contrôleur
- [ ] Ajouter le bouton dans le FXML
- [ ] Compiler le projet
- [ ] Exécuter l'application
- [ ] Tester l'ajout d'un enseignant
- [ ] Tester la modification
- [ ] Tester la suppression
- [ ] Tester la recherche
- [ ] Vérifier les statistiques

---

## 💡 Conseils

1. **Sauvegardez votre travail** avant de compiler
2. **Vérifiez les logs** en cas d'erreur
3. **Testez chaque fonctionnalité** après l'intégration
4. **Utilisez des emails uniques** pour chaque enseignant
5. **Activez les enseignants** après leur création

---

## 🎉 Vous Êtes Prêt!

L'interface enseignants est maintenant intégrée et prête à l'emploi!

**Bon développement! 🚀**

---

## 📞 Besoin d'Aide?

1. Consultez la documentation complète
2. Vérifiez les logs d'erreur
3. Assurez-vous que MySQL est en cours d'exécution
4. Vérifiez que les fichiers sont au bon endroit
5. Vérifiez que le script SQL a été exécuté

**Tout problème peut être résolu! 💪**
