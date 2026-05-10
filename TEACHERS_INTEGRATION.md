# Intégration de l'Interface Enseignants

## Fichiers Créés

### 1. Modèle (Model)
- **Fichier**: `src/main/java/edunova/connexion/models/Teacher.java`
- **Description**: Classe modèle représentant un enseignant avec les propriétés:
  - id, email, password, nom, prenom, telephone, specialite, actif, roleId, roleNom

### 2. Accès aux Données (DAO)
- **Fichier**: `src/main/java/edunova/connexion/dao/TeacherDAO.java`
- **Description**: Classe d'accès aux données pour les enseignants avec les méthodes:
  - `findAll()`: Récupère tous les enseignants
  - `findById(int id)`: Récupère un enseignant par ID
  - `insert(Teacher teacher)`: Ajoute un nouvel enseignant
  - `update(Teacher teacher)`: Modifie un enseignant
  - `delete(int id)`: Supprime un enseignant
  - `search(String keyword)`: Recherche des enseignants
  - `getTotalTeachers()`: Nombre total d'enseignants
  - `getActiveTeachers()`: Nombre d'enseignants actifs
  - `getInactiveTeachers()`: Nombre d'enseignants inactifs
  - `findAllRoles()`: Récupère tous les rôles disponibles

### 3. Contrôleur (Controller)
- **Fichier**: `src/main/java/edunova/connexion/controllers/TeacherController.java`
- **Description**: Contrôleur JavaFX pour gérer l'interface enseignants avec:
  - Affichage des statistiques (total, actifs, inactifs)
  - Tableau des enseignants
  - Formulaire CRUD (Créer, Lire, Mettre à jour, Supprimer)
  - Recherche et filtrage
  - Validation des données

### 4. Vue (FXML)
- **Fichier**: `src/main/resources/views/teachers.fxml`
- **Description**: Interface utilisateur avec:
  - Panneau de statistiques en haut
  - Tableau des enseignants à gauche
  - Formulaire de gestion à droite
  - Boutons d'action (Ajouter, Modifier, Supprimer, Vider)
  - Barre de recherche

## Prérequis Base de Données

Assurez-vous que votre base de données contient les tables suivantes:

### Table `users`
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    actif BOOLEAN DEFAULT TRUE,
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Table `teachers` (nouvelle)
```sql
CREATE TABLE teachers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    specialite VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Table `roles`
```sql
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50) UNIQUE NOT NULL
);
```

Assurez-vous qu'un rôle "Enseignant" existe:
```sql
INSERT INTO roles (nom) VALUES ('Enseignant');
```

## Intégration dans le Menu Principal

Pour ajouter l'interface enseignants au menu principal, modifiez votre contrôleur principal (par exemple `DashboardController.java`) pour ajouter un bouton ou un menu qui charge la vue `teachers.fxml`:

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
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## Fonctionnalités

### Statistiques
- **Total Enseignants**: Affiche le nombre total d'enseignants
- **Enseignants Actifs**: Affiche le nombre d'enseignants actifs
- **Enseignants Inactifs**: Affiche le nombre d'enseignants inactifs

### Gestion des Enseignants
- **Ajouter**: Crée un nouvel enseignant avec tous les champs requis
- **Modifier**: Met à jour les informations d'un enseignant sélectionné
- **Supprimer**: Supprime un enseignant après confirmation
- **Rechercher**: Filtre les enseignants par nom, prénom, email ou spécialité
- **Actualiser**: Recharge la liste complète des enseignants

### Validation
- Tous les champs obligatoires sont validés
- L'email doit être au format valide
- Le mot de passe est obligatoire pour les nouveaux enseignants
- Les doublons d'email sont évités

## Notes Importantes

1. **Transactions**: Le DAO utilise des transactions pour garantir la cohérence des données lors de l'insertion et de la suppression.

2. **Sécurité**: Les requêtes utilisent des `PreparedStatement` pour éviter les injections SQL.

3. **Spécialité**: Le champ spécialité est optionnel et peut être utilisé pour indiquer la matière enseignée (Mathématiques, Français, etc.).

4. **Rôle**: Les enseignants doivent avoir le rôle "Enseignant" assigné.

5. **Statut Actif**: Les enseignants peuvent être activés ou désactivés sans être supprimés.

## Dépannage

### Erreur: "Table 'teachers' doesn't exist"
- Créez la table `teachers` en utilisant le script SQL fourni ci-dessus.

### Erreur: "No role 'Enseignant' found"
- Insérez le rôle "Enseignant" dans la table `roles`.

### Erreur: "Duplicate entry for key 'email'"
- L'email existe déjà dans la base de données. Utilisez un email unique.

## Améliorations Futures

- Ajouter un système de permissions pour les enseignants
- Ajouter des classes/cours associés aux enseignants
- Ajouter un historique des modifications
- Ajouter un système de notifications
- Exporter les données en PDF/Excel
