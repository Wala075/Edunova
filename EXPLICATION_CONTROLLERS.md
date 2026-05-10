# Explication des Controllers — EduNova

Ce document explique en détail le rôle de chaque controller, chaque méthode, et **pourquoi** on a choisi cette approche.

---

## 1. C'est quoi un Controller en JavaFX ?

JavaFX utilise le pattern **MVC** (Model-View-Controller) :

- **View** = le fichier `.fxml` (ce que voit l'utilisateur : boutons, tableaux, formulaires)
- **Model** = les entités (`Classe`, `Seance`, `Teacher`...) et les services (`ClasseService`, `SeanceService`)
- **Controller** = le fichier `.java` qui fait le lien entre les deux

Le controller :
1. Reçoit les actions de l'utilisateur (clic sur bouton, saisie dans un champ)
2. Appelle les services (qui parlent à la base de données)
3. Met à jour l'affichage

**Comment FXML et Controller sont liés ?**

Dans le FXML on a :
```xml
<ScrollPane fx:controller="edu.edunova.controllers.AjouterClasse">
    <TextField fx:id="nomField"/>
    <Button text="Ajouter" onAction="#ajouterClasse"/>
</ScrollPane>
```

Ça veut dire :
- `fx:controller` → JavaFX va instancier la classe `AjouterClasse` quand le FXML est chargé
- `fx:id="nomField"` → JavaFX va injecter ce TextField dans le champ `@FXML private TextField nomField` du controller (par leur nom)
- `onAction="#ajouterClasse"` → quand on clique, JavaFX appelle la méthode `ajouterClasse()` du controller

---

## 2. AjouterClasse.java — explication détaillée

### 2.1. Les champs `@FXML`

```java
@FXML private TextField nomField;
@FXML private ComboBox<String> niveauCombo;
@FXML private TextField capaciteField;
```

**Pourquoi `@FXML` ?**

L'annotation `@FXML` dit à JavaFX : "ce champ correspond à un élément du FXML, injecte-le ici automatiquement". Sans cette annotation, le champ resterait `null` et tu aurais une `NullPointerException`.

Le nom du champ Java **doit être identique** au `fx:id` du FXML.

**Pourquoi les regrouper par sections ?**

Pour la lisibilité. On groupe :
- Les champs du formulaire (saisie)
- Les labels d'erreur (validation)
- Les filtres de recherche
- La pagination
- Le tableau et ses colonnes

### 2.2. Les variables d'état

```java
private final ClasseService cs = new ClasseService();
private Classe classeSelectionnee = null;
private ObservableList<Classe> toutesLesClasses;
private FilteredList<Classe> classesFiltrees;
private int currentPage = 0;
private int itemsPerPage = 5;
```

- **`cs`** : le service qui parle à la base de données. On l'instancie une fois pour toute (final).
- **`classeSelectionnee`** : la classe sélectionnée dans le tableau. Sert à savoir laquelle modifier/supprimer.
- **`toutesLesClasses`** : `ObservableList` = liste qui notifie automatiquement la TableView quand elle change. Si on ajoute un élément, le tableau se met à jour tout seul.
- **`classesFiltrees`** : `FilteredList` = vue filtrée de `toutesLesClasses`. On change le **prédicat** (la règle de filtrage) et la liste s'adapte automatiquement.
- **`currentPage` / `itemsPerPage`** : pour la pagination.

**Pourquoi ObservableList et pas ArrayList ?**

`ObservableList` notifie l'UI quand son contenu change. Avec un `ArrayList` classique, il faudrait recharger manuellement le tableau à chaque modification.

### 2.3. Les styles de validation

```java
private static final String STYLE_VALID = "-fx-border-color: #16a34a; ...";
private static final String STYLE_INVALID = "-fx-border-color: #dc2626; ...";
```

`static final` = constantes. Pourquoi des constantes ? Pour éviter de recopier le même CSS dans chaque méthode. Si on veut changer la couleur du valide, on change un seul endroit.

### 2.4. La méthode `initialize()`

```java
@FXML
public void initialize() { ... }
```

**Pourquoi cette méthode ?**

JavaFX appelle automatiquement `initialize()` après avoir chargé le FXML et injecté tous les `@FXML`. C'est l'endroit où on configure :
- Les valeurs par défaut des ComboBox
- Les colonnes du tableau
- Les listeners (écoute des changements)
- Le chargement initial des données

**Pourquoi pas dans le constructeur ?**

Quand le constructeur est appelé, les `@FXML` sont encore `null` (l'injection n'a pas encore eu lieu). Si on essaie de les utiliser → `NullPointerException`.

### 2.5. Configuration des colonnes

```java
colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
```

`PropertyValueFactory<>("nom")` dit : "pour cette colonne, prends la valeur en appelant `getNom()` sur l'objet Classe".

Java va automatiquement chercher la méthode `getNom()` (par convention `get` + nom du champ avec majuscule).

**Cellule personnalisée pour la capacité :**

```java
colCapacite.setCellFactory(column -> new TableCell<Classe, Integer>() {
    @Override
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            // Afficher une ProgressBar + label avec couleur selon la capacité
        }
    }
});
```

**Pourquoi ?** Pour afficher la capacité comme une **barre de progression colorée** au lieu d'un simple chiffre. Vert si pleine (≥75%), orange (≥40%), rouge sinon. C'est plus visuel.

`setCellFactory` = comment dessiner la cellule.
`setCellValueFactory` = quelle valeur mettre dans la cellule.

### 2.6. Les listeners (validation temps réel)

```java
nomField.textProperty().addListener((obs, old, val) -> {
    validerNom();
    appliquerFiltres();
});
```

**Pourquoi un listener ?**

Pour réagir à chaque frappe au clavier. Dès que l'utilisateur tape une lettre, on valide le champ et on filtre le tableau. Pas besoin d'attendre qu'il clique sur "Valider".

`textProperty()` = la propriété observable du texte.
`addListener` = "appelle-moi quand ça change".
`(obs, old, val)` = lambda : `obs` (l'observable), `old` (ancienne valeur), `val` (nouvelle valeur).

### 2.7. Les méthodes de validation

```java
private boolean validerNom() {
    String txt = nomField.getText();
    if (txt == null || txt.trim().isEmpty()) {
        nomField.setStyle(STYLE_INVALID);
        nomError.setText("Le nom est obligatoire");
        return false;
    }
    // ... autres règles
    nomField.setStyle(STYLE_VALID);
    nomError.setText("");
    return true;
}
```

**Logique :**
1. Récupérer le texte
2. Tester chaque règle (vide, trop court, trop long, caractères spéciaux, doublon)
3. Si une règle est violée → bordure rouge + message d'erreur + `return false`
4. Sinon → bordure verte + message vide + `return true`

**Pourquoi `txt.trim()` ?** Pour ignorer les espaces au début et à la fin. "  6A  ".trim() → "6A".

**Pourquoi `matches("[A-Za-z0-9 \\-]+")` ?** C'est une **regex** (expression régulière) : seulement lettres, chiffres, espaces et tirets. On bloque les `@`, `#`, etc.

**Pourquoi la boucle pour le doublon ?**
```java
for (Classe c : cs.getData()) {
    if (c.getNom().equalsIgnoreCase(nom) && (classeSelectionnee == null || c.getId() != classeSelectionnee.getId())) {
        // c'est un doublon
    }
}
```
On parcourt toutes les classes existantes. Si on trouve une classe avec le même nom **MAIS** ce n'est pas la classe qu'on est en train de modifier → doublon. La condition `classeSelectionnee == null || c.getId() != classeSelectionnee.getId()` permet de modifier une classe sans qu'elle se considère comme son propre doublon.

### 2.8. Les méthodes CRUD

#### Ajouter
```java
@FXML
private void ajouterClasse() {
    if (!validerFormulaire()) return;  // si invalide, on arrête
    
    Classe c = new Classe(
        nomField.getText().trim(),
        niveauCombo.getValue(),
        Integer.parseInt(capaciteField.getText().trim())
    );
    cs.addEntity(c);  // sauvegarde en BD
    
    afficher(AlertType.INFORMATION, "Classe ajoutée!");
    viderForm();
    rafraichirListe();
}
```

**Étapes :**
1. Valider tous les champs
2. Construire l'objet `Classe`
3. Le sauvegarder via le service
4. Notifier l'utilisateur
5. Vider le formulaire
6. Rafraîchir le tableau

#### Modifier

Même chose mais on appelle `cs.updateEntity(id, c)` au lieu de `addEntity`. Et on vérifie avant qu'une classe est sélectionnée.

#### Supprimer

```java
String dependances = cs.getReferencesDetails(classeSelectionnee.getId());
if (!dependances.isEmpty()) {
    afficher(AlertType.ERROR, "Suppression impossible. Cette classe contient: " + dependances);
    return;
}
```

**Pourquoi vérifier les dépendances ?**

Si une classe a des séances/élèves liés, supprimer la classe casserait l'intégrité de la base (foreign keys). Donc on bloque la suppression et on dit à l'utilisateur pourquoi.

```java
Alert confirm = new Alert(AlertType.CONFIRMATION);
confirm.showAndWait().ifPresent(response -> {
    if (response == ButtonType.OK) {
        cs.deleteEntity(classeSelectionnee);
    }
});
```

**Pourquoi une confirmation ?** Pour éviter une suppression accidentelle. `showAndWait()` bloque jusqu'à ce que l'utilisateur clique OK ou Cancel.

### 2.9. Les filtres

```java
classesFiltrees.setPredicate(classe -> {
    if (!searchText.isEmpty() && !classe.getNom().toLowerCase().contains(searchText)) {
        return false;
    }
    if (niveauChoisi != null && !niveauChoisi.equals("Tous") && !niveauChoisi.equals(classe.getNiveau())) {
        return false;
    }
    return classe.getCapacite() >= capMin;
});
```

**Concept :** un **prédicat** = une fonction qui retourne `true` (garder) ou `false` (cacher).

Le `FilteredList` applique ce prédicat à chaque élément. Si la fonction retourne `true`, l'élément reste visible.

**Pourquoi `toLowerCase()` ?** Pour que la recherche soit insensible à la casse. "math" trouvera "Mathématiques" ET "MATHÉMATIQUES".

### 2.10. La pagination

```java
private void mettreAJourPage() {
    int totalPages = getTotalPages();
    int fromIndex = currentPage * itemsPerPage;
    int toIndex = Math.min(fromIndex + itemsPerPage, filteredAll.size());
    
    ObservableList<Classe> pageData = FXCollections.observableArrayList(
        filteredAll.subList(fromIndex, toIndex)
    );
    classesTable.setItems(pageData);
}
```

**Logique :** si on est page 2 avec 5 par page, on affiche les éléments 5 à 9 (indices `fromIndex=5`, `toIndex=10`).

`subList(from, to)` = vue de la liste de l'index `from` (inclus) à `to` (exclu).

`Math.min(...)` = pour ne pas dépasser la taille de la liste sur la dernière page.

**Pourquoi désactiver les boutons ?**
```java
btnPremierePage.setDisable(currentPage == 0);
```
Pour empêcher l'utilisateur de cliquer sur "page précédente" alors qu'il est déjà à la première.

### 2.11. L'export PDF

Utilise la librairie **iText** :
1. `Document` = le document PDF
2. `PdfWriter` = écrit le document dans un fichier
3. `PdfPTable` = un tableau dans le PDF
4. `Paragraph` = un paragraphe (titre, sous-titre)

**Pourquoi `FileChooser` ?** Pour laisser l'utilisateur choisir où enregistrer le PDF. Si null → l'utilisateur a annulé, on quitte.

**Pourquoi alterner les couleurs des lignes ?**
```java
BaseColor bg = (row % 2 == 0) ? BaseColor.WHITE : altBg;
```
Pour la lisibilité (effet "zebra"). `row % 2 == 0` = ligne paire.

### 2.12. La méthode `rafraichirListe()`

```java
private void rafraichirListe() {
    toutesLesClasses = FXCollections.observableArrayList(cs.getData());
    classesFiltrees = new FilteredList<>(toutesLesClasses, p -> true);
    appliquerFiltres();
}
```

**Quand l'appeler ?** Après chaque ajout/modif/suppression. Elle relit toutes les classes depuis la BD et reconstruit la liste.

`p -> true` = prédicat initial qui accepte tout (avant que les filtres soient appliqués).

### 2.13. La méthode `afficher()`

```java
private void afficher(AlertType type, String msg) {
    Alert alert = new Alert(type);
    alert.setContentText(msg);
    alert.show();
}
```

**Pourquoi factoriser ?** Pour ne pas répéter 3 lignes à chaque fois qu'on veut afficher un message. `INFORMATION`, `WARNING`, `ERROR` selon le contexte.

---

## 3. AjouterSeance.java — explication détaillée

Très similaire à `AjouterClasse`, mais avec quelques spécificités :

### 3.1. Détection des conflits

```java
String conflits = ss.detecterConflits(s);
if (!conflits.isEmpty()) {
    afficher(AlertType.ERROR, "⚠ Conflit horaire détecté!\n\n" + conflits);
    return;
}
ss.addEntity(s);
```

**Pourquoi ?** Une séance ne doit pas chevaucher :
- Une autre séance dans la **même salle** au même moment
- Une autre séance avec le **même prof** au même moment
- Une autre séance pour la **même classe** au même moment

Le service vérifie tout ça et retourne les conflits trouvés. Si la chaîne n'est pas vide, on bloque.

### 3.2. Les statistiques

```java
private void calculerStats() {
    List<Seance> all = ss.getData();
    long totalMin = 0;
    Set<String> sallesUniques = new HashSet<>();
    Set<Integer> profsUniques = new HashSet<>();
    
    for (Seance s : all) {
        if (s.getHeureDebut() != null && s.getHeureFin() != null) {
            long min = (s.getHeureFin().getTime() - s.getHeureDebut().getTime()) / 60000;
            totalMin += min;
        }
        if (s.getSalle() != null) sallesUniques.add(s.getSalle());
        profsUniques.add(s.getTeacherId());
    }
    
    statTotal.setText(String.valueOf(all.size()));
    statSemaine.setText((totalMin / 60) + "h");
    statSalles.setText(String.valueOf(sallesUniques.size()));
    statProfs.setText(String.valueOf(profsUniques.size()));
}
```

**Pourquoi `Set` (HashSet) ?** Un `Set` ne contient pas de doublons. Si la salle "S101" apparaît dans 5 séances, elle ne sera comptée qu'une fois → on a le **nombre de salles uniques**.

**Calcul des heures :** différence en millisecondes / 60000 = minutes. Puis / 60 = heures.

### 3.3. La validation des heures

```java
private boolean validerHeureDebut() {
    if (!txt.trim().matches("([01]?\\d|2[0-3]):[0-5]\\d")) {
        // format invalide
    }
    int[] hm = parseHM(txt.trim());
    int debMin = hm[0] * 60 + hm[1];
    if (debMin < 8 * 60 + 30 || debMin >= 18 * 60) {
        // hors plage
    }
}
```

**La regex** `([01]?\d|2[0-3]):[0-5]\d` :
- `[01]?\d` = 0 ou 1 suivi d'un chiffre (00-19)
- `|` = OU
- `2[0-3]` = 2 suivi de 0 à 3 (20-23)
- `:` = deux-points
- `[0-5]\d` = 0 à 5 suivi d'un chiffre (00-59)

Donc accepte 00:00 jusqu'à 23:59.

**Conversion en minutes :** `8h30 = 8*60 + 30 = 510 min`. Plus simple à comparer que des objets `Time`.

**Pourquoi valider début ET fin dans `validerHeureFin()` ?** Pour vérifier que `fin > début` ET que la durée est entre 30 min et 4h.

### 3.4. Les helpers `findClasse`, `findMatiere`, `findTeacher`

```java
private Classe findClasse(int id) {
    for (Classe c : classeCombo.getItems()) {
        if (c.getId() == id) return c;
    }
    return null;
}
```

**Pourquoi ?** Quand on sélectionne une séance dans le tableau, on veut pré-remplir le ComboBox des classes. Mais le ComboBox attend un objet `Classe`, pas un ID. Donc on cherche la `Classe` correspondante par son ID.

### 3.5. La construction de la séance

```java
private Seance construireSeance() {
    if (!validerFormulaire()) {
        afficher(AlertType.WARNING, "Veuillez corriger les erreurs.");
        return null;
    }
    
    Time hDebut = parseHeure(heureDebutField.getText().trim());
    Time hFin = parseHeure(heureFinField.getText().trim());
    
    return new Seance(
        jourCombo.getValue(),
        hDebut, hFin,
        salleField.getText().trim(),
        typeCoursCombo.getValue() != null ? typeCoursCombo.getValue() : "PRESENTIEL",
        "2024-2025",
        classeCombo.getValue().getId(),
        matiereCombo.getValue().getId(),
        teacherCombo.getValue().getId()
    );
}
```

**Pourquoi factoriser ?** Cette méthode est appelée par `ajouterSeance` ET `modifierSeance`. Pas besoin de répéter le code.

`return null` si invalide → les méthodes appelantes vérifient et arrêtent.

---

## 4. MainController.java — explication

```java
public class MainController {
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        chargerClasses();  // charge la vue Classes par défaut
    }

    @FXML
    private void chargerClasses() {
        chargerVue("/AjouterClasse.fxml");
    }

    @FXML
    private void chargerSeances() {
        chargerVue("/AjouterSeance.fxml");
    }

    private void chargerVue(String fxmlPath) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(vue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**Rôle :** gérer la navigation entre les écrans. C'est le "shell" de l'application.

**Comment ça marche ?**
- `contentArea` est un `StackPane` central dans le FXML principal
- Quand l'utilisateur clique sur "Classes" (sidebar), on charge le FXML correspondant et on l'injecte dans `contentArea`
- `setAll(vue)` remplace tout le contenu actuel par la nouvelle vue

**Pourquoi `FXMLLoader.load(...)` ?** C'est la fonction qui parse un fichier FXML, instancie le controller, et retourne l'arbre de composants prêt à être affiché.

---

## 5. Concepts clés à retenir

| Concept | Pourquoi |
|---------|----------|
| `@FXML` | Pour lier les composants FXML aux champs Java |
| `initialize()` | Pour configurer la vue après chargement |
| `ObservableList` | Pour que l'UI se mette à jour automatiquement |
| `FilteredList` + `Predicate` | Pour filtrer sans recharger |
| `PropertyValueFactory` | Pour relier une colonne à une propriété de l'objet |
| `setCellFactory` | Pour personnaliser l'affichage des cellules |
| Listeners (`addListener`) | Pour réagir aux changements en temps réel |
| Validation par méthode `valider...()` | Pour la modularité (chaque champ a sa règle) |
| `final` sur les services | Service partagé, pas réassignable |
| `Alert` | Pour communiquer avec l'utilisateur (info/warning/erreur) |
| `FileChooser` | Pour laisser l'utilisateur choisir un fichier |
| iText (PDF) | Pour générer des PDF formatés |

---

## 6. Le flux complet d'un ajout (exemple)

1. L'utilisateur tape "6A" dans `nomField`
2. `textProperty()` notifie le listener
3. Le listener appelle `validerNom()` → bordure verte
4. Idem pour niveau et capacité
5. L'utilisateur clique sur "Ajouter"
6. JavaFX appelle `ajouterClasse()` (via `onAction="#ajouterClasse"`)
7. `validerFormulaire()` re-vérifie tout (au cas où)
8. On construit l'objet `Classe`
9. `cs.addEntity(c)` → INSERT en base de données
10. `afficher(...)` → popup de confirmation
11. `viderForm()` → reset des champs
12. `rafraichirListe()` → relit la BD, met à jour le tableau

C'est ce qu'on appelle un cycle **input → validation → action → feedback**.
