package controllers;

import edu.edunova.entities.Note;
import edu.edunova.services.NoteService;
import edu.edunova.utils.MyConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ListeNotes {

    @FXML private ComboBox<String> cbEleve;
    @FXML private ComboBox<String> cbMatiere;
    @FXML private ListView<NoteRow> lvNotes;
    @FXML private Label lbCounter;

    private final Map<String, Integer> elevesMap = new HashMap<>();
    private final Map<String, Integer> matieresMap = new HashMap<>();
    private final ObservableList<NoteRow> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        chargerEleves();
        chargerMatieres();

        // ListView avec cellules cards
        lvNotes.setItems(data);
        lvNotes.setCellFactory(lv -> new NoteCardCell());
        lvNotes.getStyleClass().add("pro-cards-list");

        rechercher(null);
    }

    public void setMatiereFilter(String matiereName) {
        if (matiereName != null && cbMatiere.getItems().contains(matiereName)) {
            cbMatiere.setValue(matiereName);
            rechercher(null);
        }
    }

    public void setEleveFilter(String eleveNomComplet) {
        if (eleveNomComplet != null && cbEleve.getItems().contains(eleveNomComplet)) {
            cbEleve.setValue(eleveNomComplet);
            rechercher(null);
        }
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
    public void rechercher(ActionEvent event) {
        data.clear();

        String eleveSel = cbEleve.getValue();
        String matiereSel = cbMatiere.getValue();

        StringBuilder sql = new StringBuilder(
                "SELECT n.id_n, n.valeur, n.coefficient, n.type_eval, n.trimestre, " +
                "n.date_saisie, n.student_id, n.matiere_id, n.annee_scolaire, " +
                "s.nom_s, s.prenom_s, m.nom_m " +
                "FROM note n " +
                "JOIN student s ON n.student_id = s.id_s " +
                "JOIN matiere m ON n.matiere_id = m.id_m WHERE 1=1");

        if (eleveSel != null) sql.append(" AND n.student_id = ?");
        if (matiereSel != null) sql.append(" AND n.matiere_id = ?");
        sql.append(" ORDER BY n.date_saisie DESC");

        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql.toString())) {
            int idx = 1;
            if (eleveSel != null) pst.setInt(idx++, elevesMap.get(eleveSel));
            if (matiereSel != null) pst.setInt(idx++, matieresMap.get(matiereSel));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    NoteRow row = new NoteRow();
                    row.setIdN(rs.getInt("id_n"));
                    row.setEleve(rs.getString("nom_s") + " " + rs.getString("prenom_s"));
                    row.setMatiere(rs.getString("nom_m"));
                    row.setValeur(rs.getDouble("valeur"));
                    row.setCoefficient(rs.getInt("coefficient"));
                    row.setTypeEval(rs.getString("type_eval"));
                    row.setTrimestre(rs.getInt("trimestre"));
                    Date dt = rs.getDate("date_saisie");
                    row.setDateSaisie(dt != null ? dt.toString() : "");
                    row.setStudentId(rs.getInt("student_id"));
                    row.setMatiereId(rs.getInt("matiere_id"));
                    row.setAnneeScolaire(rs.getString("annee_scolaire"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de recherche : " + e.getMessage());
        }
        if (lbCounter != null) {
            lbCounter.setText(data.size() + " note(s)");
        }
    }

    @FXML
    public void reset(ActionEvent event) {
        cbEleve.setValue(null);
        cbMatiere.setValue(null);
        rechercher(null);
    }

    @FXML
    public void modifier(ActionEvent event) {
        NoteRow selected = lvNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Sélectionnez une note à modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierNote.fxml"));
            Parent root = loader.load();
            ModifierNote ctrl = loader.getController();
            ctrl.setNote(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier la note");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            rechercher(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'écran : " + e.getMessage());
        }
    }

    @FXML
    public void supprimer(ActionEvent event) {
        NoteRow selected = lvNotes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Sélectionnez une note à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cette note ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.setTitle("Confirmation");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            Note n = new Note();
            n.setId_n(selected.getIdN());
            new NoteService().deleteEntity(n);
            data.remove(selected);
            if (lbCounter != null) lbCounter.setText(data.size() + " note(s)");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Note supprimée.");
        }
    }

    @FXML
    public void retour(ActionEvent event) {
        Stage stage = (Stage) lvNotes.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    // ============================================================
    //  CARTE NOTE - cellule personnalisée
    // ============================================================
    private static class NoteCardCell extends ListCell<NoteRow> {
        @Override
        protected void updateItem(NoteRow n, boolean empty) {
            super.updateItem(n, empty);
            if (empty || n == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            // ---- Avatar (initiales) ----
            Label avatar = new Label(initiales(n.getEleve()));
            avatar.getStyleClass().add("pro-card-avatar");
            StackPane avatarPane = new StackPane(avatar);
            avatarPane.getStyleClass().add("pro-card-avatar-pane");

            // ---- Infos élève + matière ----
            Label nomEleve = new Label(n.getEleve());
            nomEleve.getStyleClass().add("pro-card-name");

            Label sousLigne = new Label(n.getMatiere() + "  ·  Trimestre " + n.getTrimestre()
                    + "  ·  " + safe(n.getTypeEval()));
            sousLigne.getStyleClass().add("pro-card-sub");

            VBox infos = new VBox(2, nomEleve, sousLigne);

            // ---- Note (gros chiffre coloré) ----
            String moyColor = couleurNote(n.getValeur());
            Label noteVal = new Label(String.format("%.1f", n.getValeur()));
            noteVal.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: " + moyColor + ";");

            Label noteSur = new Label("/ 20");
            noteSur.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

            VBox notePart = new VBox(2, noteVal, noteSur);
            notePart.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            // ---- Tags coef + date ----
            Label coefTag = pill("Coef " + n.getCoefficient(), "#2d1b69", "#c4b5fd");
            Label dateTag = pill("📅 " + safe(n.getDateSaisie()), "#1e1e3a", "#94a3b8");

            HBox tags = new HBox(8, coefTag, dateTag);
            tags.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            VBox infosFull = new VBox(8, infos, tags);
            HBox.setHgrow(infosFull, javafx.scene.layout.Priority.ALWAYS);

            // ---- Card layout ----
            HBox card = new HBox(16, avatarPane, infosFull, notePart);
            card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            card.getStyleClass().add("pro-note-card");
            card.setStyle("-fx-border-color: " + moyColor + "55; -fx-border-width: 1; "
                    + "-fx-background-radius: 12; -fx-border-radius: 12; "
                    + "-fx-background-color: #16162e; -fx-padding: 16 20;");

            setGraphic(card);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 6 0;");
        }

        // ----- Helpers card -----
        private String initiales(String full) {
            if (full == null || full.isBlank()) return "?";
            String[] parts = full.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length && sb.length() < 2; i++) {
                if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
            return sb.length() == 0 ? "?" : sb.toString();
        }

        private String couleurNote(double m) {
            if (m >= 16) return "#34d399";
            if (m >= 12) return "#38bdf8";
            if (m >= 10) return "#a78bfa";
            if (m >= 8)  return "#f59e0b";
            return "#f87171";
        }

        private Label pill(String text, String bg, String fg) {
            Label l = new Label(text);
            l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; "
                    + "-fx-font-size:10px; -fx-font-weight:600; -fx-padding:4 10; -fx-background-radius:8;");
            return l;
        }

        private String safe(String s) { return s == null ? "" : s; }
    }

    /** Classe ligne note (gardée pour ModifierNote). */
    public static class NoteRow {
        private int idN;
        private String eleve;
        private String matiere;
        private double valeur;
        private int coefficient;
        private String typeEval;
        private int trimestre;
        private String dateSaisie;
        private int studentId;
        private int matiereId;
        private String anneeScolaire;

        public int getIdN() { return idN; }
        public void setIdN(int idN) { this.idN = idN; }
        public String getEleve() { return eleve; }
        public void setEleve(String eleve) { this.eleve = eleve; }
        public String getMatiere() { return matiere; }
        public void setMatiere(String matiere) { this.matiere = matiere; }
        public double getValeur() { return valeur; }
        public void setValeur(double valeur) { this.valeur = valeur; }
        public int getCoefficient() { return coefficient; }
        public void setCoefficient(int coefficient) { this.coefficient = coefficient; }
        public String getTypeEval() { return typeEval; }
        public void setTypeEval(String typeEval) { this.typeEval = typeEval; }
        public int getTrimestre() { return trimestre; }
        public void setTrimestre(int trimestre) { this.trimestre = trimestre; }
        public String getDateSaisie() { return dateSaisie; }
        public void setDateSaisie(String dateSaisie) { this.dateSaisie = dateSaisie; }
        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }
        public int getMatiereId() { return matiereId; }
        public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
        public String getAnneeScolaire() { return anneeScolaire; }
        public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    }
}
