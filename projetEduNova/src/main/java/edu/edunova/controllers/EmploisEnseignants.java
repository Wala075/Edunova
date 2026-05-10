package edu.edunova.controllers;

import edu.edunova.entities.Seance;
import edu.edunova.entities.TeacherStat;
import edu.edunova.services.AnalyseEmploiService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmploisEnseignants {

    // ── Stats globales (supprimées du FXML) ──────────────────────────

    // ── Filtre enseignant ─────────────────────────────────────────────
    @FXML private ComboBox<TeacherStat> filtreTeacherCombo;

    // ── Grille emploi du temps ────────────────────────────────────────
    @FXML private GridPane timetableGrid;



    // ── Insights ─────────────────────────────────────────────────────
    @FXML private VBox insightsBox;

    private final AnalyseEmploiService analyse = new AnalyseEmploiService();
    private List<TeacherStat> tousLesStats;

    private static final String[] JOURS = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"};
    private static final String[][] SLOTS = {
            {"08:30", "10:00"},
            {"10:15", "11:45"},
            {"13:30", "15:00"},
            {"15:15", "16:45"}
    };
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SHORT_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final int PAUSE_COL = 3;

    private LocalDate weekStart = mondayOf(LocalDate.now());

    @FXML
    public void initialize() {
        // Converter combobox
        filtreTeacherCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(TeacherStat ts) {
                if (ts == null) return "— Sélectionner un enseignant —";
                var t = ts.getTeacher();
                return t.getPrenom() + " " + t.getNom() + "  ·  " + ts.getNbSeances() + " séances";
            }
            @Override public TeacherStat fromString(String s) { return null; }
        });

        filtreTeacherCombo.valueProperty().addListener((obs, o, v) -> {
            construireGrille();
        });

        rafraichir();
    }

    @FXML
    private void rafraichir() {
        tousLesStats = analyse.genererEmploisEnseignants();
        peuplerComboFiltre();
        afficherInsights();
        construireGrille();
    }

    @FXML
    private void semainePrecedente() { weekStart = weekStart.minusWeeks(1); construireGrille(); }
    @FXML
    private void semaineSuivante()   { weekStart = weekStart.plusWeeks(1);  construireGrille(); }
    @FXML
    private void semaineActuelle()   { weekStart = mondayOf(LocalDate.now()); construireGrille(); }

    // ── Grille calendrier ─────────────────────────────────────────────

    private void construireGrille() {
        timetableGrid.getChildren().clear();
        timetableGrid.getColumnConstraints().clear();
        timetableGrid.getRowConstraints().clear();

        TeacherStat ts = filtreTeacherCombo.getValue();

        // Aucun enseignant sélectionné → message d'invite
        if (ts == null) {
            RowConstraints r = new RowConstraints(); r.setMinHeight(300);
            timetableGrid.getRowConstraints().add(r);
            ColumnConstraints c = new ColumnConstraints(); c.setHgrow(Priority.ALWAYS);
            timetableGrid.getColumnConstraints().add(c);

            VBox invite = new VBox(12);
            invite.setAlignment(Pos.CENTER);
            invite.setMaxWidth(Double.MAX_VALUE); invite.setMaxHeight(Double.MAX_VALUE);
            invite.getStyleClass().add("tt-invite-box");
            Label icon = new Label("👨‍🏫"); icon.setStyle("-fx-font-size: 44px;");
            Label msg  = new Label("Sélectionnez un enseignant\npour afficher son emploi du temps");
            msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #a78bfa; -fx-text-alignment: center; -fx-font-weight: bold;");
            msg.setWrapText(true); msg.setAlignment(Pos.CENTER);
            invite.getChildren().addAll(icon, msg);
            timetableGrid.add(invite, 0, 0);
            return;
        }

        List<Seance> seances = ts.getSeances();

        // Contraintes colonnes : [jour] [slot0] [slot1] [PAUSE] [slot2] [slot3]
        ColumnConstraints dayCol = new ColumnConstraints();
        dayCol.setMinWidth(130); dayCol.setPrefWidth(130);
        timetableGrid.getColumnConstraints().add(dayCol);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS); c.setMinWidth(160);
            timetableGrid.getColumnConstraints().add(c);
        }
        ColumnConstraints pauseC = new ColumnConstraints();
        pauseC.setMinWidth(36); pauseC.setPrefWidth(36); pauseC.setMaxWidth(36);
        timetableGrid.getColumnConstraints().add(pauseC);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS); c.setMinWidth(160);
            timetableGrid.getColumnConstraints().add(c);
        }

        // Contraintes lignes
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(46); headerRow.setPrefHeight(46);
        timetableGrid.getRowConstraints().add(headerRow);
        for (int i = 0; i < JOURS.length; i++) {
            RowConstraints rr = new RowConstraints();
            rr.setVgrow(Priority.ALWAYS); rr.setMinHeight(75);
            timetableGrid.getRowConstraints().add(rr);
        }

        // En-tête ligne 0
        Label corner = new Label(""); corner.getStyleClass().add("tt-printed-corner");
        corner.setMinSize(130, 46); corner.setMaxWidth(Double.MAX_VALUE); corner.setMaxHeight(Double.MAX_VALUE);
        timetableGrid.add(corner, 0, 0);
        timetableGrid.add(slotHeader(SLOTS[0][0], SLOTS[0][1]), 1, 0);
        timetableGrid.add(slotHeader(SLOTS[1][0], SLOTS[1][1]), 2, 0);
        Label pauseHead = new Label(""); pauseHead.getStyleClass().add("tt-printed-corner");
        pauseHead.setMinSize(36, 46); pauseHead.setMaxHeight(Double.MAX_VALUE);
        timetableGrid.add(pauseHead, PAUSE_COL, 0);
        timetableGrid.add(slotHeader(SLOTS[2][0], SLOTS[2][1]), 4, 0);
        timetableGrid.add(slotHeader(SLOTS[3][0], SLOTS[3][1]), 5, 0);

        // Colonne PAUSE (corps)
        Label pauseLbl = new Label("P\nA\nU\nS\nE");
        pauseLbl.getStyleClass().add("tt-printed-pause");
        pauseLbl.setMinSize(36, 0); pauseLbl.setMaxHeight(Double.MAX_VALUE); pauseLbl.setMaxWidth(Double.MAX_VALUE);
        pauseLbl.setAlignment(Pos.CENTER);
        timetableGrid.add(pauseLbl, PAUSE_COL, 1);
        GridPane.setRowSpan(pauseLbl, JOURS.length);

        // Lignes des jours
        for (int d = 0; d < JOURS.length; d++) {
            int rowIdx = d + 1;
            LocalDate date = weekStart.plusDays(d);

            // Cellule jour
            VBox dayCell = new VBox(2);
            dayCell.getStyleClass().add("tt-printed-day");
            dayCell.setAlignment(Pos.CENTER);
            dayCell.setMinSize(130, 75); dayCell.setMaxHeight(Double.MAX_VALUE);
            Label dayName = new Label(capitalize(JOURS[d])); dayName.getStyleClass().add("tt-printed-day-name");
            Label dayDate = new Label(date.format(DATE_FMT)); dayDate.getStyleClass().add("tt-printed-day-date");
            dayCell.getChildren().addAll(dayName, dayDate);
            timetableGrid.add(dayCell, 0, rowIdx);

            // Cellules créneaux (lecture seule — pas de clic)
            for (int slot = 0; slot < SLOTS.length; slot++) {
                int col = (slot < 2) ? slot + 1 : slot + 2;
                Seance s = trouverSeance(seances, JOURS[d], SLOTS[slot][0]);
                StackPane cell;
                if (s != null) {
                    cell = creerCarteSeance(s);
                } else {
                    cell = new StackPane();
                    cell.getStyleClass().add("tt-printed-empty-ro"); // read-only, pas de +
                    cell.setMinSize(160, 75);
                    cell.setMaxHeight(Double.MAX_VALUE); cell.setMaxWidth(Double.MAX_VALUE);
                }
                timetableGrid.add(cell, col, rowIdx);
            }
        }
    }

    private Label slotHeader(String h1, String h2) {
        Label l = new Label(h1.replace(":", "H") + " — " + h2.replace(":", "H"));
        l.getStyleClass().add("tt-printed-slot-header");
        l.setMinSize(160, 46); l.setMaxWidth(Double.MAX_VALUE); l.setMaxHeight(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private StackPane creerCarteSeance(Seance s) {
        VBox box = new VBox(3);
        box.getStyleClass().add("tt-printed-cell");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinSize(160, 75); box.setMaxHeight(Double.MAX_VALUE); box.setMaxWidth(Double.MAX_VALUE);

        Label matiere = new Label(s.getMatiereNom() != null ? s.getMatiereNom() : "—");
        matiere.getStyleClass().add("tt-printed-matiere"); matiere.setWrapText(true);

        Label classe = new Label("🏫 " + (s.getClasseNom() != null ? s.getClasseNom() : "—"));
        classe.getStyleClass().add("tt-printed-salle");

        Label salle = new Label("📍 " + (s.getSalle() != null ? s.getSalle() : "—"));
        salle.getStyleClass().add("tt-printed-salle");

        box.getChildren().addAll(matiere, classe, salle);

        StackPane cell = new StackPane(box);
        cell.setMinSize(160, 75); cell.setMaxHeight(Double.MAX_VALUE); cell.setMaxWidth(Double.MAX_VALUE);
        return cell;
    }

    private Seance trouverSeance(List<Seance> seances, String jour, String heureDebut) {
        for (Seance s : seances) {
            if (s.getJour() == null || s.getHeureDebut() == null) continue;
            if (!s.getJour().equalsIgnoreCase(jour)) continue;
            if (s.getHeureDebut().toString().substring(0, 5).equals(heureDebut)) return s;
        }
        return null;
    }



    // ── Insights ──────────────────────────────────────────────────────

    private void afficherInsights() {
        if (insightsBox == null) return;
        insightsBox.getChildren().clear();
        List<String> recs = analyse.genererRecommendations(tousLesStats);
        for (String r : recs) {
            Label lbl = new Label(r);
            lbl.setWrapText(true); lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #b0b0c8; -fx-padding: 5 12; "
                    + "-fx-background-color: rgba(45,27,105,0.25); -fx-background-radius: 8;");
            insightsBox.getChildren().add(lbl);
        }
    }

    private void peuplerComboFiltre() {
        ObservableList<TeacherStat> items = FXCollections.observableArrayList();
        items.add(null);
        items.addAll(tousLesStats);
        filtreTeacherCombo.setItems(items);
        filtreTeacherCombo.setValue(null);
    }

    // ── Utilitaires ───────────────────────────────────────────────────

    private static LocalDate mondayOf(LocalDate d) {
        return d.minusDays((d.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue() + 7) % 7);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}