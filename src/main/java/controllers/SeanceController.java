package controllers;

import edu.edunova_a.entities.Classe;
import edu.edunova_a.entities.Matiere;
import edu.edunova_a.entities.Seance;
import edu.edunova_a.entities.Teacher;
import edu.edunova_a.services.ClasseService;
import edu.edunova_a.services.MatiereService;
import edu.edunova_a.services.SeanceService;
import edu.edunova_a.services.TeacherService;
import edu.edunova_a.utils.StatusUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SeanceController {

    /* ----- formulaire ----- */
    @FXML private ComboBox<String>  cbJour;
    @FXML private ComboBox<String>  cbHeureDebut;
    @FXML private ComboBox<String>  cbHeureFin;
    @FXML private TextField         tfSalle;
    @FXML private ComboBox<String>  cbTypeCours;
    @FXML private DatePicker        dpDateSeance;
    @FXML private ComboBox<Classe>  cbClasse;
    @FXML private ComboBox<Matiere> cbMatiere;
    @FXML private ComboBox<Teacher> cbTeacher;

    /* ----- cards container ----- */
    @FXML private VBox cardsContainer;
    @FXML private TextField tfSearch;

    private final SeanceService  seanceService  = new SeanceService();
    private final ClasseService  classeService  = new ClasseService();
    private final MatiereService matiereService = new MatiereService();
    private final TeacherService teacherService = new TeacherService();

    private final ObservableList<Seance> data = FXCollections.observableArrayList();
    private Seance selected = null;

    @FXML
    public void initialize() {
        cbJour.setItems(FXCollections.observableArrayList(
                "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));
        cbTypeCours.setItems(FXCollections.observableArrayList("PRESENTIEL", "ONLINE", "HYBRIDE"));
        ObservableList<String> times = FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
                "17:00", "17:30", "18:00");
        cbHeureDebut.setItems(times);
        cbHeureFin.setItems(times);

        try {
            cbClasse.setItems(FXCollections.observableArrayList(classeService.afficher()));
            cbMatiere.setItems(FXCollections.observableArrayList(matiereService.afficher()));
            cbTeacher.setItems(FXCollections.observableArrayList(teacherService.afficher()));
        } catch (Exception e) { showError(e.getMessage()); }

        loadAll();
    }

    /* ===================== CARD RENDERING ===================== */

    private void renderCards() {
        cardsContainer.getChildren().clear();

        if (data.isEmpty()) {
            Label empty = new Label("📭 Aucune séance trouvée.");
            empty.setStyle("-fx-text-fill: #6b6b85; -fx-font-size: 14; -fx-padding: 30;");
            cardsContainer.getChildren().add(empty);
            return;
        }

        int delay = 0;
        for (Seance s : data) {
            HBox card = buildSeanceCard(s);
            card.setOpacity(0);
            card.setScaleX(0.97); card.setScaleY(0.97);

            FadeTransition ft = new FadeTransition(Duration.millis(250), card);
            ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(delay));

            ScaleTransition st = new ScaleTransition(Duration.millis(250), card);
            st.setFromX(0.97); st.setFromY(0.97); st.setToX(1); st.setToY(1); st.setDelay(Duration.millis(delay));

            cardsContainer.getChildren().add(card);
            ft.play(); st.play();
            delay += 35;
        }
    }

    private HBox buildSeanceCard(Seance s) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        String defaultStyle = "-fx-background-color: #1a1a2e; -fx-background-radius: 12; "
                + "-fx-border-color: #2a2a3d; -fx-border-radius: 12; -fx-border-width: 1; "
                + "-fx-padding: 16; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);";
        String hoverStyle = "-fx-background-color: #1e1e34; -fx-background-radius: 12; "
                + "-fx-border-color: #7c3aed; -fx-border-radius: 12; -fx-border-width: 1; "
                + "-fx-padding: 16; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(124,58,237,0.3), 12, 0, 0, 3);";
        String selectedStyle = "-fx-background-color: #1e1e34; -fx-background-radius: 12; "
                + "-fx-border-color: #a78bfa; -fx-border-radius: 12; -fx-border-width: 2; "
                + "-fx-padding: 16; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(124,58,237,0.4), 14, 0, 0, 4);";

        card.setStyle(defaultStyle);
        card.setOnMouseEntered(e -> { if (selected != s) card.setStyle(hoverStyle); });
        card.setOnMouseExited(e -> { if (selected != s) card.setStyle(defaultStyle); });
        card.setOnMouseClicked(e -> {
            selected = s;
            populateForm(s);
            renderCards(); // re-render to highlight
        });
        if (selected != null && selected.getId_se() == s.getId_se()) card.setStyle(selectedStyle);

        // LEFT: Type badge + jour
        VBox left = new VBox(6);
        left.setAlignment(Pos.CENTER);
        left.setMinWidth(90);
        left.setMaxWidth(90);

        Label typeBadge = new Label(s.getType_cours_se() != null ? s.getType_cours_se() : "—");
        typeBadge.setStyle("-fx-background-radius: 8; -fx-padding: 4 12; "
                + "-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: white; "
                + "-fx-background-color: " + StatusUtils.colorTypeCours(s.getType_cours_se()) + ";");

        Label jourLabel = new Label(s.getJour_se() != null ? s.getJour_se() : "—");
        jourLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;");

        Label dateLabel = new Label(s.getDate_seance() != null ? s.getDate_seance().toString() : "");
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #8a8aa3;");

        left.getChildren().addAll(typeBadge, jourLabel, dateLabel);

        // CENTER: schedule info
        VBox center = new VBox(4);
        HBox.setHgrow(center, Priority.ALWAYS);

        Label timeLabel = new Label("🕐 " + (s.getHeure_debut_se() != null ? s.getHeure_debut_se().toString() : "")
                + " → " + (s.getHeure_fin_se() != null ? s.getHeure_fin_se().toString() : ""));
        timeLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #e0e0f0;");

        Label matiereLabel = new Label("📚 " + (s.getMatiere_nom() != null ? s.getMatiere_nom() : "Matière #" + s.getMatiere_id()));
        matiereLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #a78bfa;");

        Label classeLabel = new Label("🎓 " + (s.getClasse_nom() != null ? s.getClasse_nom() : "Classe #" + s.getClasse_id()));
        classeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #8a8aa3;");

        Label teacherLabel = new Label("👤 " + (s.getTeacher_nom() != null ? s.getTeacher_nom() : "—"));
        teacherLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6b6b85;");

        center.getChildren().addAll(timeLabel, matiereLabel, classeLabel, teacherLabel);

        // RIGHT: salle
        VBox right = new VBox(4);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setMinWidth(80);

        if (s.getSalle_se() != null && !s.getSalle_se().isBlank()) {
            Label salleLabel = new Label("🏛 " + s.getSalle_se());
            salleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #34d399; -fx-font-weight: bold;");
            right.getChildren().add(salleLabel);
        }

        Label idLabel = new Label("#" + s.getId_se());
        idLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #4a4a6a;");
        right.getChildren().add(idLabel);

        card.getChildren().addAll(left, center, right);
        return card;
    }

    /* ===================== ACTIONS ===================== */

    @FXML
    public void onAjouter() {
        try {
            Seance s = readForm();
            if (s == null) return;
            // Vérifier les conflits horaires
            if (seanceService.hasConflict(s)) {
                Alert conflict = new Alert(Alert.AlertType.WARNING,
                        "⚠️ Un conflit horaire a été détecté !\n\n"
                        + "Une séance existe déjà pour cette classe au même créneau.\n"
                        + "Voulez-vous quand même ajouter cette séance ?",
                        ButtonType.YES, ButtonType.NO);
                conflict.setTitle("Conflit horaire");
                conflict.setHeaderText("⚠️ Conflit détecté");
                if (conflict.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
            }
            seanceService.ajouter(s);
            loadAll();
            onReset();
            showInfo("✅ Séance ajoutée avec succès.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    public void onModifier() {
        if (selected == null) { showWarn("Sélectionnez une séance dans la liste."); return; }
        try {
            Seance s = readForm();
            if (s == null) return;
            s.setId_se(selected.getId_se());
            // Vérifier les conflits horaires (exclut la séance actuelle)
            if (seanceService.hasConflict(s)) {
                Alert conflict = new Alert(Alert.AlertType.WARNING,
                        "⚠️ Un conflit horaire a été détecté !\n\n"
                        + "Une autre séance existe pour cette classe au même créneau.\n"
                        + "Voulez-vous quand même modifier cette séance ?",
                        ButtonType.YES, ButtonType.NO);
                conflict.setTitle("Conflit horaire");
                if (conflict.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
            }
            seanceService.modifier(s);
            loadAll();
            onReset();
            showInfo("✅ Séance modifiée avec succès.");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    public void onSupprimer() {
        if (selected == null) { showWarn("Sélectionnez une séance dans la liste."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la séance " + selected.getJour_se() + " " + selected.getHeure_debut_se()
                + " - " + (selected.getMatiere_nom() != null ? selected.getMatiere_nom() : "")
                + " ?\n\nCette action est irréversible.",
                ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmation de suppression");
        a.setHeaderText("🗑 Supprimer la séance");
        a.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                try {
                    seanceService.supprimer(selected.getId_se());
                    loadAll();
                    onReset();
                    showInfo("Séance supprimée.");
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    @FXML
    public void onChercher() {
        try {
            String mot = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
            if (mot.isEmpty()) { loadAll(); return; }
            data.setAll(seanceService.chercher(mot));
            renderCards();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    public void onAfficherTout() {
        tfSearch.clear();
        loadAll();
    }

    @FXML
    public void onDemarrerPointage() {
        if (selected == null) {
            showWarn("Sélectionnez une séance dans la liste pour démarrer le pointage.");
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Pointage.fxml"));
            javafx.scene.Parent root = loader.load();
            PointageController ctrl = loader.getController();
            String label = selected.getJour_se() + " "
                    + selected.getHeure_debut_se() + " - "
                    + (selected.getMatiere_nom() != null ? selected.getMatiere_nom() : "Matière #" + selected.getMatiere_id())
                    + " (" + (selected.getClasse_nom() != null ? selected.getClasse_nom() : "Classe #" + selected.getClasse_id()) + ")";
            ctrl.initForSeance(selected.getId_se(), label);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Pointage - " + label);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir la fenêtre de pointage : " + e.getMessage());
        }
    }

    @FXML
    public void onReset() {
        cbJour.getSelectionModel().clearSelection();
        cbHeureDebut.getSelectionModel().clearSelection();
        cbHeureFin.getSelectionModel().clearSelection();
        tfSalle.clear();
        cbTypeCours.getSelectionModel().clearSelection();
        dpDateSeance.setValue(null);
        cbClasse.getSelectionModel().clearSelection();
        cbMatiere.getSelectionModel().clearSelection();
        cbTeacher.getSelectionModel().clearSelection();
        selected = null;
        renderCards();
    }

    /* ===================== HELPERS ===================== */

    private void loadAll() {
        try {
            data.setAll(seanceService.afficher());
            renderCards();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private Seance readForm() {
        String jour = cbJour.getValue();
        String type = cbTypeCours.getValue();
        Classe c = cbClasse.getValue();
        Matiere m = cbMatiere.getValue();
        Teacher t = cbTeacher.getValue();
        if (jour == null || type == null || c == null || m == null || t == null
                || cbHeureDebut.getValue() == null || cbHeureFin.getValue() == null) {
            showWarn("Tous les champs (sauf salle) sont obligatoires.");
            return null;
        }
        try {
            LocalTime hd = LocalTime.parse(cbHeureDebut.getValue().trim());
            LocalTime hf = LocalTime.parse(cbHeureFin.getValue().trim());
            if (!hf.isAfter(hd)) { showWarn("L'heure de fin doit être après l'heure de début."); return null; }
            return new Seance(0, jour, hd, hf,
                    tfSalle.getText(), type,
                    dpDateSeance.getValue(),
                    c.getId(), m.getId_m(), t.getId_t());
        } catch (Exception e) {
            showWarn("Format d'heure invalide (HH:mm). Ex: 08:00");
            return null;
        }
    }

    private void populateForm(Seance s) {
        cbJour.setValue(s.getJour_se());
        cbHeureDebut.setValue(s.getHeure_debut_se() != null ? s.getHeure_debut_se().toString() : null);
        cbHeureFin.setValue(s.getHeure_fin_se() != null ? s.getHeure_fin_se().toString() : null);
        tfSalle.setText(s.getSalle_se());
        cbTypeCours.setValue(s.getType_cours_se());
        dpDateSeance.setValue(s.getDate_seance());
        for (Classe c : cbClasse.getItems()) if (c.getId() == s.getClasse_id())   { cbClasse.setValue(c);  break; }
        for (Matiere m : cbMatiere.getItems()) if (m.getId_m() == s.getMatiere_id()) { cbMatiere.setValue(m); break; }
        for (Teacher t : cbTeacher.getItems()) if (t.getId_t() == s.getTeacher_id()) { cbTeacher.setValue(t); break; }
    }

    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
    private void showWarn(String msg)  { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showInfo(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
}
