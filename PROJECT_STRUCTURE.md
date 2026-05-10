# Structure du Projet - Gestion des Enseignants

## 📂 Arborescence Complète

```
Login/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edunova/connexion/
│   │   │       ├── config/
│   │   │       │   └── EnvLoader.java
│   │   │       ├── controllers/
│   │   │       │   ├── CaptchaController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   ├── ForgotPasswordController.java
│   │   │       │   ├── GoogleLoginController.java
│   │   │       │   ├── GoogleOAuth2WindowController.java
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── PhonePickerController.java
│   │   │       │   ├── RiskAnalysisController.java
│   │   │       │   ├── RiskReportController.java
│   │   │       │   ├── TeacherController.java          ✨ NOUVEAU
│   │   │       │   ├── UserController.java
│   │   │       │   └── UserFormController.java
│   │   │       ├── dao/
│   │   │       │   ├── RiskDAO.java
│   │   │       │   ├── TeacherDAO.java                 ✨ NOUVEAU
│   │   │       │   └── UserDAO.java
│   │   │       ├── models/
│   │   │       │   ├── RiskData.java
│   │   │       │   ├── Teacher.java                    ✨ NOUVEAU
│   │   │       │   └── User.java
│   │   │       ├── tests/
│   │   │       │   ├── Main.java
│   │   │       │   └── TestConnexion.java
│   │   │       └── tools/
│   │   │           ├── DatabaseConnection.java
│   │   │           ├── GoogleAuthService.java
│   │   │           ├── GoogleOAuth2Service.java
│   │   │           ├── HCaptchaServer.java
│   │   │           ├── HCaptchaService.java
│   │   │           ├── ImageGenerator.java
│   │   │           ├── ImageResizer.java
│   │   │           ├── PasswordUtils.java
│   │   │           ├── RiskAnalyzerIA.java
│   │   │           └── SessionManager.java
│   │   └── resources/
│   │       ├── config.properties
│   │       ├── images/
│   │       │   └── login_sidebar_bg.jpg
│   │       ├── styles/
│   │       │   └── style.css
│   │       └── views/
│   │           ├── captcha.html
│   │           ├── captcha_window.fxml
│   │           ├── dashboard.fxml
│   │           ├── forgot_password.fxml
│   │           ├── google_login.fxml
│   │           ├── google_oauth2_window.fxml
│   │           ├── login.fxml
│   │           ├── phone_picker.fxml
│   │           ├── risk_report.fxml
│   │           ├── teachers.fxml                       ✨ NOUVEAU
│   │           ├── users.fxml
│   │           └── user_form.fxml
│   └── test/
│       └── java/
├── sql/
│   └── teachers_setup.sql                              ✨ NOUVEAU
├── .env
├── .env.example
├── .gitignore
├── pom.xml
├── README.md
├── TEACHERS_INTEGRATION.md                            ✨ NOUVEAU
├── INTEGRATION_EXAMPLE.md                             ✨ NOUVEAU
├── TEACHERS_SUMMARY.md                                ✨ NOUVEAU
└── PROJECT_STRUCTURE.md                               ✨ NOUVEAU (ce fichier)
```

---

## 🆕 Fichiers Créés pour la Gestion des Enseignants

### 1. **Modèle (Model)**
```
src/main/java/edunova/connexion/models/Teacher.java
```
- Classe POJO représentant un enseignant
- Propriétés: id, email, password, nom, prenom, telephone, specialite, actif, roleId, roleNom
- Getters et setters pour tous les champs

### 2. **Accès aux Données (DAO)**
```
src/main/java/edunova/connexion/dao/TeacherDAO.java
```
- Classe d'accès aux données pour les enseignants
- Méthodes:
  - `findAll()`: Récupère tous les enseignants
  - `findById(int id)`: Récupère un enseignant par ID
  - `insert(Teacher teacher)`: Ajoute un nouvel enseignant
  - `update(Teacher teacher)`: Modifie un enseignant
  - `delete(int id)`: Supprime un enseignant
  - `search(String keyword)`: Recherche des enseignants
  - `getTotalTeachers()`: Nombre total d'enseignants
  - `getActiveTeachers()`: Nombre d'enseignants actifs
  - `getInactiveTeachers()`: Nombre d'enseignants inactifs
  - `findAllRoles()`: Récupère tous les rôles

### 3. **Contrôleur (Controller)**
```
src/main/java/edunova/connexion/controllers/TeacherController.java
```
- Contrôleur JavaFX pour l'interface enseignants
- Gère:
  - L'affichage des statistiques
  - La liaison des données au tableau
  - Les opérations CRUD
  - La validation des formulaires
  - La recherche et le filtrage

### 4. **Vue (FXML)**
```
src/main/resources/views/teachers.fxml
```
- Interface utilisateur en FXML
- Contient:
  - Panneau de statistiques (3 cartes)
  - Tableau des enseignants (7 colonnes)
  - Formulaire de gestion (8 champs)
  - Boutons d'action (4 boutons)
  - Barre de recherche

### 5. **Script SQL**
```
sql/teachers_setup.sql
```
- Script de création des tables
- Crée: roles, users, teachers
- Crée les index pour les performances
- Insère les rôles de base

### 6. **Documentation**
```
TEACHERS_INTEGRATION.md
INTEGRATION_EXAMPLE.md
TEACHERS_SUMMARY.md
PROJECT_STRUCTURE.md (ce fichier)
```

---

## 📊 Diagramme de Dépendances

```
TeacherController
    ├── TeacherDAO
    │   ├── DatabaseConnection
    │   └── Teacher (Model)
    └── FXML (teachers.fxml)
        └── CSS (style.css)
```

---

## 🔄 Flux de Données

```
Interface Utilisateur (FXML)
    ↓
TeacherController (Logique)
    ↓
TeacherDAO (Accès aux données)
    ↓
DatabaseConnection (Connexion BD)
    ↓
Base de Données MySQL
    ├── Table: roles
    ├── Table: users
    └── Table: teachers
```

---

## 📋 Détails des Fichiers Créés

### Teacher.java
```java
public class Teacher {
    private int id;
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String telephone;
    private String specialite;
    private boolean actif;
    private int roleId;
    private String roleNom;
    // Getters et setters...
}
```

### TeacherDAO.java
```java
public class TeacherDAO {
    public List<Teacher> findAll() { ... }
    public Teacher findById(int id) { ... }
    public boolean insert(Teacher teacher) { ... }
    public boolean update(Teacher teacher) { ... }
    public boolean delete(int id) { ... }
    public List<Teacher> search(String keyword) { ... }
    public int getTotalTeachers() { ... }
    public int getActiveTeachers() { ... }
    public int getInactiveTeachers() { ... }
    public List<String[]> findAllRoles() { ... }
}
```

### TeacherController.java
```java
public class TeacherController {
    @FXML private Label lblTotalEnseignants;
    @FXML private Label lblEnseignantsActifs;
    @FXML private Label lblEnseignantsInactifs;
    @FXML private TableView<Teacher> tableTeachers;
    @FXML private TextField txtNom, txtPrenom, txtEmail, txtTelephone, txtSpecialite;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox chkActif;
    
    @FXML public void initialize() { ... }
    @FXML private void handleAjouter() { ... }
    @FXML private void handleModifier() { ... }
    @FXML private void handleSupprimer() { ... }
    @FXML private void handleRecherche() { ... }
    @FXML private void handleActualiser() { ... }
    @FXML private void handleVider() { ... }
}
```

### teachers.fxml
```xml
<BorderPane>
    <top>
        <!-- Statistiques -->
        <VBox>
            <Label text="Gestion des Enseignants" />
            <HBox>
                <VBox> <!-- Total --> </VBox>
                <VBox> <!-- Actifs --> </VBox>
                <VBox> <!-- Inactifs --> </VBox>
            </HBox>
        </VBox>
    </top>
    <center>
        <HBox>
            <!-- Tableau -->
            <VBox>
                <TextField fx:id="txtRecherche" />
                <TableView fx:id="tableTeachers" />
            </VBox>
            <!-- Formulaire -->
            <VBox>
                <TextField fx:id="txtNom" />
                <TextField fx:id="txtPrenom" />
                <TextField fx:id="txtEmail" />
                <TextField fx:id="txtTelephone" />
                <TextField fx:id="txtSpecialite" />
                <PasswordField fx:id="txtPassword" />
                <ComboBox fx:id="cbRole" />
                <CheckBox fx:id="chkActif" />
                <HBox>
                    <Button text="Ajouter" />
                    <Button text="Modifier" />
                    <Button text="Supprimer" />
                    <Button text="Vider" />
                </HBox>
            </VBox>
        </HBox>
    </center>
</BorderPane>
```

### teachers_setup.sql
```sql
CREATE TABLE roles (...)
CREATE TABLE users (...)
CREATE TABLE teachers (...)
CREATE INDEX idx_users_email ON users(email)
CREATE INDEX idx_users_role_id ON users(role_id)
CREATE INDEX idx_users_actif ON users(actif)
CREATE INDEX idx_teachers_user_id ON teachers(user_id)
INSERT INTO roles (nom) VALUES ('Admin'), ('Enseignant'), ('Étudiant'), ('Parent')
```

---

## 🔗 Relations entre les Fichiers

### Modèle → DAO
```
Teacher.java
    ↓ (utilisé par)
TeacherDAO.java
```

### DAO → Contrôleur
```
TeacherDAO.java
    ↓ (utilisé par)
TeacherController.java
```

### Contrôleur → Vue
```
TeacherController.java
    ↓ (contrôle)
teachers.fxml
```

### Vue → Base de Données
```
teachers.fxml
    ↓ (affiche les données de)
TeacherDAO.java
    ↓ (accède à)
Base de Données MySQL
```

---

## 📦 Dépendances Maven

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

---

## 🎯 Points d'Intégration

### 1. **Dashboard**
Ajouter un bouton pour ouvrir l'interface enseignants:
```java
@FXML
private void handleTeachers() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/teachers.fxml"));
    Parent root = loader.load();
    Scene scene = new Scene(root);
    Stage stage = new Stage();
    stage.setTitle("Gestion des Enseignants");
    stage.setScene(scene);
    stage.show();
}
```

### 2. **Menu Principal**
Ajouter un élément de menu pour accéder à l'interface enseignants

### 3. **Base de Données**
Exécuter le script SQL pour créer les tables:
```bash
mysql -u root -p votre_base < sql/teachers_setup.sql
```

---

## ✅ Checklist de Vérification

- [x] Modèle Teacher créé
- [x] DAO TeacherDAO créé
- [x] Contrôleur TeacherController créé
- [x] Vue teachers.fxml créée
- [x] Script SQL teachers_setup.sql créé
- [x] Documentation TEACHERS_INTEGRATION.md créée
- [x] Documentation INTEGRATION_EXAMPLE.md créée
- [x] Documentation TEACHERS_SUMMARY.md créée
- [x] Documentation PROJECT_STRUCTURE.md créée
- [ ] Script SQL exécuté
- [ ] Bouton ajouté au Dashboard
- [ ] Projet compilé
- [ ] Interface testée

---

## 🚀 Prochaines Étapes

1. **Exécuter le script SQL**
   ```bash
   mysql -u root -p votre_base < sql/teachers_setup.sql
   ```

2. **Ajouter le bouton au Dashboard**
   - Modifier `DashboardController.java`
   - Ajouter la méthode `handleTeachers()`
   - Ajouter le bouton dans `dashboard.fxml`

3. **Compiler le projet**
   ```bash
   mvn clean compile
   ```

4. **Exécuter l'application**
   ```bash
   mvn javafx:run
   ```

5. **Tester l'interface**
   - Ajouter un enseignant
   - Modifier un enseignant
   - Supprimer un enseignant
   - Rechercher un enseignant
   - Vérifier les statistiques

---

## 📞 Support

Pour toute question ou problème:
1. Consultez `TEACHERS_INTEGRATION.md`
2. Consultez `INTEGRATION_EXAMPLE.md`
3. Consultez `TEACHERS_SUMMARY.md`
4. Vérifiez les logs d'erreur

---

**Tous les fichiers sont prêts à être intégrés! 🎉**
