package controllers;

import edu.edunova.entities.Classe;
import edu.edunova.entities.Matiere;
import edu.edunova.entities.Note;
import edu.edunova.entities.Student;
import edu.edunova.services.ClasseService;
import edu.edunova.services.NoteService;
import edu.edunova.utils.AnneeScolaire;
import edu.edunova.utils.InputUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjouterNote {

    // ── ComboBoxes ──────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cbClasse;
    @FXML private ComboBox<String> cbEleve;
    @FXML private ComboBox<String> cbMatiere;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbTrimestre;

    // ── Other fields ────────────────────────────────────────────────────────
    @FXML private TextField  tfValeur;
    @FXML private TextField  tfCoefficient;
    @FXML private DatePicker dpDate;
    @FXML private TextField  tfAnnee;

    // ── ID maps ─────────────────────────────────────────────────────────────
    private final Map<String, Integer> classesMap  = new HashMap<>();
    private final Map<String, Integer> elevesMap   = new HashMap<>();
    private final Map<String, Integer> matieresMap = new HashMap<>();

    private final ClasseService classeService = new ClasseService();

    // ────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        cbType.getItems().addAll("DEVOIR", "EXAMEN", "ORAL", "TP");
        cbTrimestre.getItems().addAll("1", "2", "3");
        dpDate.setValue(java.time.LocalDate.now());

        // Input controls
        InputUtils.applyDecimalFilter(tfValeur, 20.0);
        InputUtils.applyIntegerFilter(tfCoefficient, 100);
        InputUtils.applyDateFilter(dpDate);

        // Élève and Matière are disabled until a class is chosen
        cbEleve.setDisable(true);
        cbMatiere.setDisable(true);

        // ----- Année scolaire automatique : calculée depuis dpDate -----
        // Champ verrouillé (lecture seule) pour éviter toute saisie incohérente.
        tfAnnee.setEditable(false);
        tfAnnee.setFocusTraversable(false);
        tfAnnee.setStyle(tfAnnee.getStyle() + " -fx-opacity: 0.85;");
        tfAnnee.setTooltip(new Tooltip("Calculé automatiquement depuis la date de saisie"));
        // Sync initial
        tfAnnee.setText(AnneeScolaire.fromDate(dpDate.getValue()));
        // Re-calcul à chaque changement de date
        dpDate.valueProperty().addListener((obs, oldD, newD) ->
                tfAnnee.setText(AnneeScolaire.fromDate(newD)));

        chargerClasses();

        // Cascade: class → students + subjects
        cbClasse.setOnAction(e -> onClasseSelected());
    }

    // ── Loaders ─────────────────────────────────────────────────────────────

    private void chargerClasses() {
        classesMap.clear();
        cbClasse.getItems().clear();
        List<Classe> classes = classeService.getData();
        for (Classe c : classes) {
            cbClasse.getItems().add(c.getNom());
            classesMap.put(c.getNom(), c.getId());
        }
    }

    private void onClasseSelected() {
        String selected = cbClasse.getValue();

        // Reset dependent combos
        cbEleve.getItems().clear();
        cbEleve.setValue(null);
        cbMatiere.getItems().clear();
        cbMatiere.setValue(null);
        elevesMap.clear();
        matieresMap.clear();

        if (selected == null) {
            cbEleve.setDisable(true);
            cbMatiere.setDisable(true);
            return;
        }

        int classeId = classesMap.get(selected);

        // Load students for this class
        List<Student> students = classeService.getStudentsByClasse(classeId);
        for (Student s : students) {
            String label = s.getNomComplet().trim();
            cbEleve.getItems().add(label);
            elevesMap.put(label, s.getId_s());
        }

        // Load subjects for this class
        List<Matiere> matieres = classeService.getMatieresByClasse(classeId);
        for (Matiere m : matieres) {
            cbMatiere.getItems().add(m.getNom_m());
            matieresMap.put(m.getNom_m(), m.getId_m());
        }

        cbEleve.setDisable(students.isEmpty());
        cbMatiere.setDisable(matieres.isEmpty());

        if (students.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucun élève",
                    "Aucun élève n'est assigné à cette classe.");
        }
        if (matieres.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucune matière",
                    "Aucune matière n'est assignée à cette classe.");
        }
    }

    // ── Actions ─────────────────────────────────────────────────────────────

    @FXML
    public void ajouterNote(ActionEvent event) {
        try {
            // Validation
            if (cbClasse.getValue() == null || cbEleve.getValue() == null
                    || cbMatiere.getValue() == null || tfValeur.getText().isEmpty()
                    || cbType.getValue() == null || cbTrimestre.getValue() == null
                    || dpDate.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants",
                        "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            double valeur = Double.parseDouble(tfValeur.getText());
            if (valeur < 0 || valeur > 20) {
                showAlert(Alert.AlertType.ERROR, "Note invalide",
                        "La note doit être entre 0 et 20.");
                return;
            }

            int studentId = elevesMap.get(cbEleve.getValue());
            int matiereId = matieresMap.get(cbMatiere.getValue());
            java.sql.Date dateSql = java.sql.Date.valueOf(dpDate.getValue());

            Note nouvelleNote = new Note(
                    valeur,
                    Integer.parseInt(tfCoefficient.getText()),
                    cbType.getValue(),
                    Integer.parseInt(cbTrimestre.getValue()),
                    dateSql,
                    studentId,
                    matiereId,
                    tfAnnee.getText()
            );

            new NoteService().addEntity(nouvelleNote);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Note ajoutée avec succès !");
            resetForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "La note et le coefficient doivent être des nombres.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void annuler(ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) tfValeur.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void voirListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeNotes.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("EduNova - Liste des notes");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la liste : " + e.getMessage());
        }
    }

    @FXML
    public void retour(ActionEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) tfValeur.getScene().getWindow();
        stage.close();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void resetForm() {
        cbClasse.setValue(null);
        cbEleve.getItems().clear();
        cbEleve.setValue(null);
        cbEleve.setDisable(true);
        cbMatiere.getItems().clear();
        cbMatiere.setValue(null);
        cbMatiere.setDisable(true);
        elevesMap.clear();
        matieresMap.clear();
        tfValeur.clear();
        tfCoefficient.setText("1");
        cbType.setValue(null);
        cbTrimestre.setValue(null);
        dpDate.setValue(java.time.LocalDate.now());
        // L'année est recalculée automatiquement via le listener sur dpDate
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
