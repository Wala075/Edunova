# Exemple d'Intégration de l'Interface Enseignants

## 1. Ajouter un Bouton dans le Menu Principal

Si vous avez un `DashboardController.java`, ajoutez une méthode pour ouvrir l'interface enseignants:

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

## 2. Ajouter le Bouton dans le FXML du Dashboard

Dans votre fichier `dashboard.fxml`, ajoutez un bouton:

```xml
<Button text="Gestion des Enseignants" onAction="#handleTeachers" 
        style="-fx-padding: 10 20; -fx-font-size: 14;" />
```

## 3. Exemple Complet d'Intégration dans DashboardController

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

    // ... autres méthodes ...

    @FXML
    private void handleTeachers() {
        openTeachersWindow();
    }

    @FXML
    private void handleUsers() {
        openUsersWindow();
    }

    private void openTeachersWindow() {
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
            showError("Erreur", "Impossible de charger l'interface enseignants.");
            e.printStackTrace();
        }
    }

    private void openUsersWindow() {
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
            showError("Erreur", "Impossible de charger l'interface utilisateurs.");
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
```

## 4. Exemple de FXML Dashboard avec Boutons

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.text.Font?>

<BorderPane xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1" 
            fx:controller="edunova.connexion.controllers.DashboardController">
    <top>
        <VBox spacing="15" style="-fx-padding: 20; -fx-background-color: #f8f9fa;">
            <Label text="Tableau de Bord" style="-fx-font-size: 24; -fx-font-weight: bold;">
                <font>
                    <Font size="24.0" />
                </font>
            </Label>
        </VBox>
    </top>

    <center>
        <VBox spacing="20" style="-fx-padding: 30;">
            <Label text="Gestion" style="-fx-font-size: 18; -fx-font-weight: bold;" />
            
            <HBox spacing="15">
                <Button text="Gestion des Utilisateurs" onAction="#handleUsers" 
                        style="-fx-padding: 15 30; -fx-font-size: 14; -fx-background-color: #007bff; -fx-text-fill: white;" />
                
                <Button text="Gestion des Enseignants" onAction="#handleTeachers" 
                        style="-fx-padding: 15 30; -fx-font-size: 14; -fx-background-color: #28a745; -fx-text-fill: white;" />
            </HBox>
        </VBox>
    </center>
</BorderPane>
```

## 5. Étapes d'Intégration Complète

### Étape 1: Exécuter le Script SQL
```bash
# Connectez-vous à votre base de données MySQL
mysql -u root -p votre_base_de_donnees < sql/teachers_setup.sql
```

### Étape 2: Vérifier les Tables
```sql
-- Vérifier que les tables ont été créées
SHOW TABLES;

-- Vérifier la structure de la table teachers
DESCRIBE teachers;

-- Vérifier les rôles
SELECT * FROM roles;
```

### Étape 3: Compiler le Projet
```bash
mvn clean compile
```

### Étape 4: Exécuter l'Application
```bash
mvn javafx:run
```

### Étape 5: Tester l'Interface
1. Cliquez sur le bouton "Gestion des Enseignants"
2. Ajoutez un nouvel enseignant
3. Vérifiez que les statistiques se mettent à jour
4. Testez la recherche et la modification

## 6. Vérification de la Base de Données

Après avoir ajouté des enseignants, vérifiez les données:

```sql
-- Voir tous les enseignants
SELECT u.id, u.nom, u.prenom, u.email, t.specialite, u.actif
FROM users u
LEFT JOIN teachers t ON u.id = t.user_id
WHERE u.role_id = (SELECT id FROM roles WHERE nom = 'Enseignant');

-- Voir les statistiques
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN u.actif = TRUE THEN 1 ELSE 0 END) as actifs,
    SUM(CASE WHEN u.actif = FALSE THEN 1 ELSE 0 END) as inactifs
FROM users u
WHERE u.role_id = (SELECT id FROM roles WHERE nom = 'Enseignant');
```

## 7. Dépannage

### Problème: "Cannot find resource '/views/teachers.fxml'"
**Solution**: Assurez-vous que le fichier `teachers.fxml` est dans `src/main/resources/views/`

### Problème: "No role 'Enseignant' found"
**Solution**: Exécutez le script SQL pour insérer les rôles

### Problème: "Duplicate entry for key 'email'"
**Solution**: Utilisez un email unique pour chaque enseignant

### Problème: Les statistiques ne se mettent pas à jour
**Solution**: Vérifiez que la méthode `mettreAJourStatistiques()` est appelée après chaque opération

## 8. Personnalisation

### Changer les Couleurs
Modifiez les styles CSS dans `teachers.fxml`:
```xml
style="-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;"
```

### Ajouter des Colonnes au Tableau
Ajoutez une nouvelle `TableColumn` dans le FXML et une propriété dans le modèle `Teacher`

### Ajouter des Champs au Formulaire
Ajoutez un nouveau `TextField` dans le FXML et mettez à jour le contrôleur

## 9. Sécurité

- Les mots de passe sont stockés en base de données (considérez le hachage)
- Les requêtes utilisent des `PreparedStatement` pour éviter les injections SQL
- Les transactions garantissent la cohérence des données
- Les validations côté client et serveur sont en place

## 10. Performance

- Les index sont créés sur les colonnes fréquemment recherchées
- Les requêtes utilisent des `JOIN` pour minimiser les appels à la base de données
- La pagination peut être ajoutée pour les grandes listes
