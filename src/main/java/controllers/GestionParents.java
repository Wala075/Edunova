package controllers;

import edu.edunova.entities.Student;
import edu.edunova.services.StudentService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page de gestion des coordonnées parents (email + téléphone).
 * Utilisée pour configurer rapidement les destinataires Brevo (F3) et Twilio (F4).
 */
public class GestionParents {

    @FXML private TableView<Student> tvEleves;
    @FXML private TableColumn<Student, String> colNom;
    @FXML private TableColumn<Student, String> colPrenom;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colTel;

    @FXML private Label lbCompteur;
    @FXML private Label lbStatus;

    private final StudentService service = new StudentService();
    private final ObservableList<Student> data = FXCollections.observableArrayList();

    /** IDs des élèves modifiés depuis le dernier save (pour ne pas tout réécrire). */
    private final Set<Integer> dirty = new HashSet<>();
    /** Sauvegarde des emails initiaux pour comparaison. */
    private final Map<Integer, String> originalEmails = new HashMap<>();
    private final Map<Integer, String> originalTels   = new HashMap<>();

    @FXML
    public void initialize() {
        // Colonnes lecture seule
        colNom.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getNom_s())));
        colPrenom.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getPrenom_s())));

        // Colonne email éditable
        colEmail.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getEmail_parent())));
        colEmail.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmail.setOnEditCommit(evt -> {
            Student s = evt.getRowValue();
            String newVal = evt.getNewValue() == null ? "" : evt.getNewValue().trim();
            s.setEmail_parent(newVal);
            dirty.add(s.getId_s());
            updateStatus();
        });

        // Colonne téléphone éditable
        colTel.setCellValueFactory(c ->
                new SimpleStringProperty(safe(c.getValue().getTelephone_parent())));
        colTel.setCellFactory(TextFieldTableCell.forTableColumn());
        colTel.setOnEditCommit(evt -> {
            Student s = evt.getRowValue();
            String newVal = evt.getNewValue() == null ? "" : evt.getNewValue().trim();
            s.setTelephone_parent(newVal);
            dirty.add(s.getId_s());
            updateStatus();
        });

        tvEleves.setItems(data);
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
        if (lbCompteur != null) {
            lbCompteur.setText(list.size() + " élève(s)");
        }
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
        Stage stage = (Stage) tvEleves.getScene().getWindow();
        stage.close();
    }

    // ================ helpers ================

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
}
