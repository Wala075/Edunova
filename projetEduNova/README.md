# 🎓 projetEduNova — Module M4 (Version Avancée)

**EduNova** — Application JavaFX de gestion scolaire — by **Abir Merseni**.

## ✅ Composants implémentés

| # | Composant | Status |
|---|-----------|--------|
| 1 | CRUD Classes | ✅ |
| 4 | Emploi du Temps (CRUD Séances) | ✅ |
| 5 | Détection des conflits horaires | ✅ |
| 6 | Anti-conflict Suppression (intégrité) | ✅ |

## ✨ Fonctionnalités avancées

### Module Classes
- 📊 **4 cartes de statistiques** (total, capacité moyenne, niveaux, places)
- 🔍 **Recherche en temps réel** par nom
- 🎚 **Filtre par niveau** (6ème, 5ème, ..., Terminale)
- 🔢 **Filtre par capacité minimum**
- 📥 **Export CSV** des résultats filtrés
- ✅ Validation des champs
- 🛡 Anti-conflict suppression (vérification d'intégrité)

### Module Emploi du Temps
- 📊 **4 cartes de statistiques** (total séances, heures/semaine, salles utilisées, profs actifs)
- 🔍 **Recherche multi-champs** (salle, matière, prof, classe...)
- 🎚 **3 filtres** (jour / classe / prof)
- 📥 **Export CSV** complet
- 🚨 **Détection automatique des conflits**
  - Même prof à 2 endroits en même temps
  - Même classe avec 2 cours
  - Même salle utilisée
- 🔄 Sélection d'une séance → form se remplit auto

## 🎨 Design

Thème **académique bleu marine**:
- `#0C447C` titres
- `#185FA5` boutons principaux
- `#378ADD` actions secondaires
- Cards blanches avec ombres
- Stats avec couleurs catégorisées

## 🚀 Comment lancer

1. **Décompresser** ZIP dans `IdeaProjects/`
2. **IntelliJ** → File → Open → sélectionner `pom.xml` → "Open as Project"
3. **Maven Reload** (auto, attendre 1-3 min)
4. **WampServer** allumé 🟢
5. **Lancer:**
   - Clic-droit `MainFX.java` → Run, OU
   - Maven panel → javafx → `javafx:run`

L'app va automatiquement insérer des données test au 1er lancement (5 matières + 4 profs + 2 classes).

## 📂 Structure

```
projetEduNova/
├── pom.xml
└── src/main/
    ├── java/edu/edunova/
    │   ├── MainFX.java                 🚀 launcher JavaFX
    │   ├── controllers/
    │   │   ├── MainController.java     menu navigation
    │   │   ├── AjouterClasse.java      module Classes (avec recherche, filtres, stats, export)
    │   │   └── AjouterSeance.java      module EDT (avec recherche, filtres, stats, export)
    │   ├── entities/
    │   │   ├── Classe.java
    │   │   ├── Matiere.java
    │   │   ├── Teacher.java
    │   │   └── Seance.java
    │   ├── interfaces/
    │   │   ├── IService.java
    │   │   ├── IClasseService.java
    │   │   └── ISeanceService.java
    │   ├── services/
    │   │   ├── ClasseService.java
    │   │   ├── MatiereService.java
    │   │   ├── TeacherService.java
    │   │   └── SeanceService.java
    │   ├── tests/
    │   │   └── MainClass.java
    │   └── utils/
    │       └── MyConnection.java       Singleton MySQL
    └── resources/
        ├── MainView.fxml
        ├── AjouterClasse.fxml
        └── AjouterSeance.fxml
```

## 🎓 Concepts utilisés (pour le jury)

- **Pattern MVC** (Model-View-Controller)
- **Pattern Singleton** (MyConnection)
- **Pattern DAO** (Service classes)
- **Generics** (`IService<T>`)
- **Héritage d'interfaces**
- **PreparedStatement** (anti-SQL injection)
- **JavaFX FXML** (UI déclarative)
- **FilteredList + Predicate** (recherche temps réel)
- **ObservableList listeners** (réactivité UI)
- **Jointures SQL multi-tables** (4 tables: seance + classe + matière + teacher)
- **Algorithme de détection de conflits horaires**
- **Export CSV avec FileChooser**

## 🐛 Erreurs courantes

| Erreur | Solution |
|--------|----------|
| Communications link failure | Démarrer WampServer 🟢 |
| Unknown database 'edunova' | Importer le SQL dans phpMyAdmin |
| JavaFX runtime missing | Maven plugin: `javafx:run` |
| ComboBox vide | Lancer 1ère fois insère données auto |

---

**EduNova — DevSprite Team** | ESPRIT 2024-2025
