package controllers;

import edu.edunova.entities.Student;
import edu.edunova.services.StudentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page de gestion des coordonnées parents en cards éditables.
 */
public class GestionParents {

    @FXML private ListView<Student> lvEleves;
    @FXML private Label lbCompteur;
    @FXML private Label lbStatus;

    private final StudentService service = new StudentService();
    private final ObservableList<Student> data = FXCollections.observableArrayList();

    private final Set<Integer> dirty = new HashSet<>();
    private final Map<Integer, String> originalEmails = new HashMap<>();
    private final Map<Integer, String> originalTels   = new HashMap<>();

    @FXML
    public void initialize() {
        lvEleves.setItems(data);
        lvEleves.setCellFactory(lv -> new ParentCardCell());
        actualiser(null);
    }

    @FXML
    public void actualiser(ActionEvent e) {
        List<Student> list = service.getAllWithContacts();
        data.setAll(list);
        dirty.clear();
        originalEmails.clear();
        originalTels.clear();
        for (Student s : list) {
            originalEmails.put(s.getId_s(), safe(s.getEmail_parent()));
            originalTels.put(s.getId_s(), safe(s.getTelephone_parent()));
        }
        if (lbCompteur != null) lbCompteur.setText(list.size() + " élève(s)");
        updateStatus();
    }

    @FXML
    public void enregistrer(ActionEvent e) {
        if (dirty.isEmpty()) {
            showInfo("Rien à enregistrer", "Aucune modification détectée.");
            return;
        }

        // Validation rapide des emails
        StringBuilder warnings = new StringBuilder();
        for (Student s : data) {
            if (!dirty.contains(s.getId_s())) continue;
            String mail = safe(s.getEmail_parent());
            if (!mail.isEmpty() && !mail.contains("@")) {
                warnings.append("• ").append(s.getNomComplet().trim())
                        .append(" : email invalide\n");
            }
        }
        if (warnings.length() > 0) {
            Alert a = new Alert(Alert.AlertType.WARNING,
                    "Certains emails semblent invalides :\n\n" + warnings + "\nEnregistrer quand même ?",
                    ButtonType.OK, ButtonType.CANCEL);
            a.setHeaderText(null);
            if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) return;
        }

        int updated = 0;
        for (Student s : data) {
            if (!dirty.contains(s.getId_s())) continue;
            String email = safe(s.getEmail_parent());
            String tel   = safe(s.getTelephone_parent());

            if (!email.equals(originalEmails.getOrDefault(s.getId_s(), ""))) {
                service.updateEmailParent(s.getId_s(), email);
                originalEmails.put(s.getId_s(), email);
                updated++;
            }
            if (!tel.equals(originalTels.getOrDefault(s.getId_s(), ""))) {
                service.updateTelephoneParent(s.getId_s(), tel);
                originalTels.put(s.getId_s(), tel);
                updated++;
            }
        }
        dirty.clear();
        updateStatus();

        showInfo("Enregistrement", updated + " mise(s) à jour effectuée(s).");
    }

    @FXML
    public void retour(ActionEvent e) {
        if (!dirty.isEmpty()) {
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Tu as " + dirty.size() + " modification(s) non enregistrée(s). Quitter quand même ?",
                    ButtonType.OK, ButtonType.CANCEL);
            conf.setHeaderText(null);
            if (conf.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }
        Stage stage = (Stage) lvEleves.getScene().getWindow();
        stage.close();
    }

    // ============================================================
    // Helpers
    // ============================================================

    private void updateStatus() {
        if (lbStatus == null) return;
        if (dirty.isEmpty()) {
            lbStatus.setText("");
        } else {
            lbStatus.setText("● " + dirty.size() + " modification(s) non enregistrée(s)");
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.show();
    }

    // ============================================================
    //  CARTE PARENT - cellule éditable
    // ============================================================
    private class ParentCardCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student s, boolean empty) {
            super.updateItem(s, empty);
            if (empty || s == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            // ---- Avatar ----
            Label avatar = new Label(initiales(s.getNomComplet()));
            avatar.getStyleClass().add("pro-card-avatar");
            StackPane avatarPane = new StackPane(avatar);
            avatarPane.getStyleClass().add("pro-card-avatar-pane");

            // ---- Identité ----
            Label nom = new Label(s.getNomComplet().trim());
            nom.getStyleClass().add("pro-card-name");
            Label sub = new Label("ID #" + s.getId_s());
            sub.getStyleClass().add("pro-card-sub");
            VBox identity = new VBox(2, nom, sub);

            HBox left = new HBox(14, avatarPane, identity);
            left.setAlignment(Pos.CENTER_LEFT);
            left.setMinWidth(220);

            // ---- Email field ----
            Label emLab = new Label("📧  EMAIL PARENT");
            emLab.getStyleClass().add("pro-label");

            TextField tfEmail = new TextField(safe(s.getEmail_parent()));
            tfEmail.setPromptText("ex: parent@gmail.com");
            tfEmail.getStyleClass().add("pro-input");
            tfEmail.textProperty().addListener((obs, oldV, newV) -> {
                s.setEmail_parent(newV);
                if (!safe(newV).equals(originalEmails.getOrDefault(s.getId_s(), ""))) {
                    dirty.add(s.getId_s());
                } else {
                    // si revenu à la valeur originale, on retire de dirty si tel aussi original
                    if (safe(s.getTelephone_parent()).equals(originalTels.getOrDefault(s.getId_s(), ""))) {
                        dirty.remove(s.getId_s());
                    }
                }
                updateStatus();
            });

            VBox emailBox = new VBox(4, emLab, tfEmail);
            HBox.setHgrow(emailBox, Priority.ALWAYS);

            // ---- Tel field ----
            Label telLab = new Label("📱  TÉLÉPHONE PARENT");
            telLab.getStyleClass().add("pro-label");

            TextField tfTel = new TextField(safe(s.getTelephone_parent()));
            tfTel.setPromptText("ex: +216 22 333 444");
            tfTel.getStyleClass().add("pro-input");
            tfTel.setMaxWidth(220);
            tfTel.textProperty().addListener((obs, oldV, newV) -> {
                s.setTelephone_parent(newV);
                if (!safe(newV).equals(originalTels.getOrDefault(s.getId_s(), ""))) {
                    dirty.add(s.getId_s());
                } else {
                    if (safe(s.getEmail_parent()).equals(originalEmails.getOrDefault(s.getId_s(), ""))) {
                        dirty.remove(s.getId_s());
                    }
                }
                updateStatus();
            });

            VBox telBox = new VBox(4, telLab, tfTel);
            telBox.setMaxWidth(240);

            // ---- Card layout ----
            HBox card = new HBox(20, left, emailBox, telBox);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("pro-parent-card");
            card.setStyle("-fx-background-color: #16162e; -fx-background-radius: 12; "
                    + "-fx-padding: 18 22; "
                    + "-fx-border-color: #2d2d4e; -fx-border-width: 1; -fx-border-radius: 12;");

            setGraphic(card);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
        }

        private String initiales(String full) {
            if (full == null || full.isBlank()) return "?";
            String[] parts = full.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length && sb.length() < 2; i++) {
                if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
            return sb.length() == 0 ? "?" : sb.toString();
        }
    }
}
