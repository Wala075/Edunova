package controllers;

import edu.edunova.entities.Note;
import edu.edunova.services.NoteService;
import edu.edunova.utils.InputUtils;
import edu.edunova.utils.MyConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ModifierNote {

    @FXML private ComboBox<String> cbEleve;
    @FXML private ComboBox<String> cbMatiere;
    @FXML private TextField tfValeur;
    @FXML private TextField tfCoefficient;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbTrimestre;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfAnnee;

    private final Map<String, Integer> elevesMap = new HashMap<>();
    private final Map<String, Integer> matieresMap = new HashMap<>();
    private int idNote;

    @FXML
    public void initialize() {
        cbType.getItems().addAll("DEVOIR", "EXAMEN", "ORAL", "TP");
        cbTrimestre.getItems().addAll("1", "2", "3");

        // Input controls
        InputUtils.applyDecimalFilter(tfValeur, 20.0);
        InputUtils.applyIntegerFilter(tfCoefficient, 100);
        InputUtils.applyDateFilter(dpDate);

        chargerEleves();
        chargerMatieres();
    }

    public void setNote(ListeNotes.NoteRow row) {
        this.idNote = row.getIdN();
        cbEleve.setValue(row.getEleve());
        cbMatiere.setValue(row.getMatiere());
        tfValeur.setText(String.valueOf(row.getValeur()));
        tfCoefficient.setText(String.valueOf(row.getCoefficient()));
        cbType.setValue(row.getTypeEval());
        cbTrimestre.setValue(String.valueOf(row.getTrimestre()));
        if (row.getDateSaisie() != null && !row.getDateSaisie().isEmpty()) {
            try {
                dpDate.setValue(LocalDate.parse(row.getDateSaisie()));
            } catch (Exception ignored) {
                dpDate.setValue(LocalDate.now());
            }
        }
        tfAnnee.setText(row.getAnneeScolaire());
    }

    private void chargerEleves() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_s, nom_s, prenom_s FROM student")) {
            while (rs.next()) {
                int id = rs.getInt("id_s");
                String nomComplet = rs.getString("nom_s") + " " + rs.getString("prenom_s");
                cbEleve.getItems().add(nomComplet);
                elevesMap.put(nomComplet, id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement élèves : " + e.getMessage());
        }
    }

    private void chargerMatieres() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_m, nom_m FROM matiere")) {
            while (rs.next()) {
                int id = rs.getInt("id_m");
                String nom = rs.getString("nom_m");
                cbMatiere.getItems().add(nom);
                matieresMap.put(nom, id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement matières : " + e.getMessage());
        }
    }

    @FXML
    public void enregistrer(ActionEvent event) {
        try {
            if (cbEleve.getValue() == null || cbMatiere.getValue() == null
                    || tfValeur.getText().isEmpty() || cbType.getValue() == null
                    || cbTrimestre.getValue() == null || dpDate.getValue() == null) {
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

            Note n = new Note(
                    valeur,
                    Integer.parseInt(tfCoefficient.getText()),
                    cbType.getValue(),
                    Integer.parseInt(cbTrimestre.getValue()),
                    java.sql.Date.valueOf(dpDate.getValue()),
                    elevesMap.get(cbEleve.getValue()),
                    matieresMap.get(cbMatiere.getValue()),
                    tfAnnee.getText()
            );

            new NoteService().updateEntity(idNote, n);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Note modifiée avec succès !");
            ((Stage) tfValeur.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "La note et le coefficient doivent être des nombres.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void annuler(ActionEvent event) {
        ((Stage) tfValeur.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
