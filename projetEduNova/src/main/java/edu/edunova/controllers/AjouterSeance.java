package edu.edunova.controllers;

import edu.edunova.entities.Classe;
import edu.edunova.entities.Matiere;
import edu.edunova.entities.Seance;
import edu.edunova.entities.Teacher;
import edu.edunova.services.ClasseService;
import edu.edunova.services.MatiereService;
import edu.edunova.services.SeanceService;
import edu.edunova.services.TeacherService;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AjouterSeance {

    @FXML private Label resultatLabel;
    @FXML private Label weekRangeLabel;
    @FXML private Label classTitleLabel;
    @FXML private ComboBox<Classe> filtreClasseCombo;
    @FXML private ComboBox<Teacher> filtreTeacherCombo;
    @FXML private GridPane timetableGrid;

    private final SeanceService ss = new SeanceService();
    private final ClasseService cs = new ClasseService();
    private final MatiereService ms = new MatiereService();
    private final TeacherService ts = new TeacherService();

    private static final String[] JOURS = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"};

    private static final String[][] SLOTS = {
            {"08:30", "10:00"},
            {"10:15", "11:45"},
            {"13:30", "15:00"},
            {"15:15", "16:45"}
    };

    private static final int PAUSE_COL = 3;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter SHORT_FMT = DateTimeFormatter.ofPattern("dd/MM");

    private LocalDate weekStart = mondayOf(LocalDate.now());

    /** Internal clipboard used by the right-click Copier / Couper / Coller flow. */
    private static Seance clipboardSeance = null;
    private static boolean clipboardIsCut = false;

    @FXML
    public void initialize() {
        ObservableList<Classe> classes = FXCollections.observableArrayList(cs.getData());
        ObservableList<Teacher> teachers = FXCollections.observableArrayList(ts.getData());

        filtreClasseCombo.setItems(classes);
        filtreTeacherCombo.setItems(teachers);

        filtreClasseCombo.valueProperty().addListener((o, ov, nv) -> rafraichir());
        filtreTeacherCombo.valueProperty().addListener((o, ov, nv) -> rafraichir());

        rafraichir();
    }

    @FXML
    private void reinitialiserFiltres() {
        filtreClasseCombo.setValue(null);
        filtreTeacherCombo.setValue(null);
        rafraichir();
    }

    @FXML
    private void nouvelleSeance() {
        ouvrirDialog(null, null, -1);
    }

    @FXML
    private void semainePrecedente() {
        weekStart = weekStart.minusWeeks(1);
        rafraichir();
    }

    @FXML
    private void semaineSuivante() {
        weekStart = weekStart.plusWeeks(1);
        rafraichir();
    }

    @FXML
    private void semaineActuelle() {
        weekStart = mondayOf(LocalDate.now());
        rafraichir();
    }

    private void rafraichir() {
        List<Seance> seances = appliquerFiltres(ss.getData());
        construireGrille(seances);
        majResultatLabel(seances.size());
        majHeader();
    }

    private void majHeader() {
        if (weekRangeLabel != null) {
            LocalDate fin = weekStart.plusDays(5);
            int semestre = (weekStart.getMonthValue() >= 9 || weekStart.getMonthValue() <= 1) ? 1 : 2;
            weekRangeLabel.setText("Semestre " + semestre
                    + "    Semaine : " + weekStart.format(SHORT_FMT)
                    + " — " + fin.format(SHORT_FMT)
                    + "/" + weekStart.getYear());
        }
        if (classTitleLabel != null) {
            Classe c = filtreClasseCombo.getValue();
            classTitleLabel.setText("Classe : " + (c != null ? c.getNom() : "Toutes"));
        }
    }

    private List<Seance> appliquerFiltres(List<Seance> source) {
        Classe classeF = filtreClasseCombo.getValue();
        Teacher teacherF = filtreTeacherCombo.getValue();
        java.util.List<Seance> out = new java.util.ArrayList<>();
        for (Seance s : source) {
            if (classeF != null && s.getClasseId() != classeF.getId()) continue;
            if (teacherF != null && s.getTeacherId() != teacherF.getId()) continue;
            out.add(s);
        }
        return out;
    }

    private static LocalDate mondayOf(LocalDate d) {
        return d.minusDays((d.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue() + 7) % 7);
    }

    private void construireGrille(List<Seance> seances) {
        timetableGrid.getChildren().clear();
        timetableGrid.getColumnConstraints().clear();
        timetableGrid.getRowConstraints().clear();

        // ── Si aucune classe sélectionnée : afficher un message d'invite ──
        if (filtreClasseCombo.getValue() == null) {
            RowConstraints r = new RowConstraints();
            r.setMinHeight(400);
            timetableGrid.getRowConstraints().add(r);
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS);
            timetableGrid.getColumnConstraints().add(c);

            VBox invite = new VBox(12);
            invite.setAlignment(Pos.CENTER);
            invite.setMaxWidth(Double.MAX_VALUE);
            invite.setMaxHeight(Double.MAX_VALUE);
            invite.getStyleClass().add("tt-invite-box");

            Label icon = new Label("🏫");
            icon.setStyle("-fx-font-size: 48px;");

            Label msg = new Label("Sélectionnez une classe pour afficher\net remplir l'emploi du temps");
            msg.setStyle("-fx-font-size: 15px; -fx-text-fill: #a78bfa; -fx-text-alignment: center; -fx-font-weight: bold;");
            msg.setWrapText(true);
            msg.setAlignment(Pos.CENTER);

            Label hint = new Label("Utilisez le filtre « Filtrer par classe » ci-dessus");
            hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-style: italic;");

            invite.getChildren().addAll(icon, msg, hint);
            timetableGrid.add(invite, 0, 0);
            return;
        }

        // ----- Column constraints : day-label fixed, slots grow, pause fixed -----
        ColumnConstraints daysCol = new ColumnConstraints();
        daysCol.setMinWidth(140); daysCol.setPrefWidth(140);
        timetableGrid.getColumnConstraints().add(daysCol);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS); c.setMinWidth(170);
            timetableGrid.getColumnConstraints().add(c);
        }
        ColumnConstraints pauseColC = new ColumnConstraints();
        pauseColC.setMinWidth(40); pauseColC.setPrefWidth(40); pauseColC.setMaxWidth(40);
        timetableGrid.getColumnConstraints().add(pauseColC);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS); c.setMinWidth(170);
            timetableGrid.getColumnConstraints().add(c);
        }

        // ----- Row constraints : header fixed, day rows grow -----
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(50); headerRow.setPrefHeight(50);
        timetableGrid.getRowConstraints().add(headerRow);
        for (int i = 0; i < JOURS.length; i++) {
            RowConstraints r = new RowConstraints();
            r.setVgrow(Priority.ALWAYS); r.setMinHeight(80);
            timetableGrid.getRowConstraints().add(r);
        }

        // Row 0 : header
        Label corner = new Label("");
        corner.getStyleClass().add("tt-printed-corner");
        corner.setMinSize(140, 50);
        corner.setMaxWidth(Double.MAX_VALUE);
        corner.setMaxHeight(Double.MAX_VALUE);
        timetableGrid.add(corner, 0, 0);

        // Slot headers (left of pause)
        timetableGrid.add(slotHeader(SLOTS[0][0], SLOTS[0][1]), 1, 0);
        timetableGrid.add(slotHeader(SLOTS[1][0], SLOTS[1][1]), 2, 0);

        // Pause header column
        Label pauseHead = new Label("");
        pauseHead.getStyleClass().add("tt-printed-corner");
        pauseHead.setMinSize(40, 50);
        pauseHead.setMaxHeight(Double.MAX_VALUE);
        timetableGrid.add(pauseHead, PAUSE_COL, 0);

        // Slot headers (right of pause)
        timetableGrid.add(slotHeader(SLOTS[2][0], SLOTS[2][1]), 4, 0);
        timetableGrid.add(slotHeader(SLOTS[3][0], SLOTS[3][1]), 5, 0);

        // Pause column body : single label spanning all day rows
        Label pauseLbl = new Label("P\nA\nU\nS\nE");
        pauseLbl.getStyleClass().add("tt-printed-pause");
        pauseLbl.setMinSize(40, 0);
        pauseLbl.setMaxHeight(Double.MAX_VALUE);
        pauseLbl.setMaxWidth(Double.MAX_VALUE);
        pauseLbl.setAlignment(Pos.CENTER);
        timetableGrid.add(pauseLbl, PAUSE_COL, 1);
        GridPane.setRowSpan(pauseLbl, JOURS.length);

        // One row per day
        for (int d = 0; d < JOURS.length; d++) {
            int rowIdx = d + 1;
            LocalDate date = weekStart.plusDays(d);

            VBox dayCell = new VBox(2);
            dayCell.getStyleClass().add("tt-printed-day");
            dayCell.setAlignment(Pos.CENTER);
            dayCell.setMinSize(140, 80);
            dayCell.setMaxHeight(Double.MAX_VALUE);
            Label dayName = new Label(JOURS[d].toLowerCase());
            dayName.getStyleClass().add("tt-printed-day-name");
            Label dayDate = new Label(date.format(DATE_FMT));
            dayDate.getStyleClass().add("tt-printed-day-date");
            dayCell.getChildren().addAll(dayName, dayDate);
            timetableGrid.add(dayCell, 0, rowIdx);

            // Empty body cells for the 4 slots
            for (int slot = 0; slot < SLOTS.length; slot++) {
                int col = (slot < 2) ? slot + 1 : slot + 2; // skip pause col
                StackPane empty = creerCelluleVide(JOURS[d], SLOTS[slot][0], SLOTS[slot][1], date);
                timetableGrid.add(empty, col, rowIdx);
            }
        }

        // Place existing séances on top of the empty cells
        for (Seance s : seances) {
            placerSeance(s);
        }
    }

    private Label slotHeader(String h1, String h2) {
        Label l = new Label(h1.replace(":", "H:") + "    " + h2.replace(":", "H:"));
        l.getStyleClass().add("tt-printed-slot-header");
        l.setMinSize(170, 50);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMaxHeight(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        GridPane.setHalignment(l, HPos.CENTER);
        return l;
    }

    private StackPane creerCelluleVide(String jour, String hStart, String hEnd, LocalDate date) {
        StackPane cell = new StackPane();
        cell.setMinSize(170, 80);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setMaxWidth(Double.MAX_VALUE);

        // ── Si aucune classe sélectionnée : cellule verrouillée ──────
        boolean classeSelectionnee = filtreClasseCombo.getValue() != null;
        if (!classeSelectionnee) {
            cell.getStyleClass().add("tt-printed-locked");
            // Pas de clic, pas de drag, pas de menu
            return cell;
        }

        // ── Cellule normale cliquable ────────────────────────────────
        cell.getStyleClass().add("tt-printed-empty");

        Label plus = new Label("＋");
        plus.getStyleClass().add("tt-empty-plus");
        cell.getChildren().add(plus);

        // Direct single-click action for empty cells (no delay).

        // ----- Drop target -----
        cell.setOnDragOver(e -> {
            if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        cell.setOnDragEntered(e -> {
            if (e.getDragboard().hasString()
                    && !cell.getStyleClass().contains("tt-drop-target")) {
                cell.getStyleClass().add("tt-drop-target");
            }
        });
        cell.setOnDragExited(e -> cell.getStyleClass().remove("tt-drop-target"));
        cell.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean ok = false;
            if (db.hasString()) {
                try {
                    int id = Integer.parseInt(db.getString());
                    ok = handleSeanceDrop(id, jour, hStart);
                } catch (NumberFormatException ignored) {}
            }
            e.setDropCompleted(ok);
            e.consume();
        });

        // ----- Context menu : Nouvelle / Coller (double-clic OU clic droit) -----
        ContextMenu menu = new ContextMenu();
        MenuItem mNew = new MenuItem("＋  Nouvelle séance ici");
        mNew.setOnAction(ev -> ouvrirDialog(null, jour, slotIndexFromTime(hStart)));
        MenuItem mPaste = new MenuItem("Coller");
        mPaste.setOnAction(ev -> handlePaste(jour, hStart));
        menu.getItems().addAll(mNew, new SeparatorMenuItem(), mPaste);

        Runnable refreshAndShow = () -> {
            boolean has = clipboardSeance != null;
            mPaste.setDisable(!has);
            mPaste.setText(has
                    ? (clipboardIsCut ? "Coller (couper) ici" : "Coller (copier) ici")
                    : "Coller (rien à coller)");
        };

        cell.setOnContextMenuRequested(e -> {
            refreshAndShow.run();
            menu.show(cell, e.getScreenX(), e.getScreenY());
        });

        cell.setOnMouseClicked(e -> {
            if (e.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
            // Single-click opens the "Nouvelle séance" dialog immediately.
            ouvrirDialog(null, jour, slotIndexFromTime(hStart));
            e.consume();
        });
        return cell;
    }

    private int slotIndexFromTime(String hStart) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i][0].equals(hStart)) return i;
        }
        return -1;
    }

    private void placerSeance(Seance s) {
        int dayIdx = indexOfDay(s.getJour());
        if (dayIdx < 0) return;
        if (s.getHeureDebut() == null || s.getHeureFin() == null) return;

        int slot = slotForSeance(s);
        if (slot < 0) return;

        int row = dayIdx + 1;
        int col = (slot < 2) ? slot + 1 : slot + 2;

        VBox card = creerCarteSeance(s);
        card.setMinSize(170, 80);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setMaxWidth(Double.MAX_VALUE);

        final int colFinal = col;
        final int rowFinal = row;
        timetableGrid.getChildren().removeIf(node -> {
            Integer c = GridPane.getColumnIndex(node);
            Integer r = GridPane.getRowIndex(node);
            return c != null && r != null && c == colFinal && r == rowFinal;
        });

        timetableGrid.add(card, col, row);
        // Single-click delayed -> dialog ; double-click -> menu
        PauseTransition cardSinglePause = new PauseTransition(Duration.millis(230));
        cardSinglePause.setOnFinished(ev -> ouvrirDialog(s, null, -1));
        card.setOnMouseClicked(e -> {
            if (e.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
            if (!e.isStillSincePress()) return; // ignore drag-release click
            if (e.getClickCount() == 1) {
                cardSinglePause.playFromStart();
            }
        });

        // ----- Drag source (seulement si une classe est sélectionnée) -----
        card.setOnDragDetected(e -> {
            if (filtreClasseCombo.getValue() == null) { e.consume(); return; }
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(String.valueOf(s.getId()));
            db.setContent(cc);
            card.setOpacity(0.45);
            e.consume();
        });
        card.setOnDragDone(e -> {
            card.setOpacity(1.0);
            e.consume();
        });

        // ----- Drop target on a filled card : SWAP with the dragged séance -----
        card.setOnDragOver(e -> {
            if (e.getGestureSource() != card && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        card.setOnDragEntered(e -> {
            if (e.getGestureSource() != card && e.getDragboard().hasString()
                    && !card.getStyleClass().contains("tt-drop-target")) {
                card.getStyleClass().add("tt-drop-target");
            }
        });
        card.setOnDragExited(e -> card.getStyleClass().remove("tt-drop-target"));
        card.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean ok = false;
            if (db.hasString()) {
                try {
                    int sourceId = Integer.parseInt(db.getString());
                    if (sourceId != s.getId()) {
                        ok = handleSeanceSwap(sourceId, s.getId());
                    }
                } catch (NumberFormatException ignored) {}
            }
            e.setDropCompleted(ok);
            e.consume();
        });

        // ----- Right-click context menu : Modifier / Copier / Couper / Supprimer -----
        ContextMenu menu = new ContextMenu();
        MenuItem mEdit = new MenuItem("Modifier…");
        mEdit.setOnAction(ev -> ouvrirDialog(s, null, -1));
        MenuItem mCopy = new MenuItem("Copier");
        mCopy.setOnAction(ev -> {
            clipboardSeance = s;
            clipboardIsCut = false;
            rafraichir();
        });
        MenuItem mCut = new MenuItem("Couper");
        mCut.setOnAction(ev -> {
            clipboardSeance = s;
            clipboardIsCut = true;
            rafraichir();
        });
        MenuItem mDel = new MenuItem("Supprimer");
        mDel.setOnAction(ev -> demanderSupprimer(s));
        menu.getItems().addAll(mEdit, new SeparatorMenuItem(), mCopy, mCut, new SeparatorMenuItem(), mDel);
        card.setOnContextMenuRequested(e ->
                menu.show(card, e.getScreenX(), e.getScreenY()));

        // Double-click on a filled card opens the same menu (right-click alternative).
        card.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() != javafx.scene.input.MouseButton.PRIMARY) return;
            if (e.getClickCount() >= 2) {
                cardSinglePause.stop();
                menu.show(card, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        // Visual hint when this card is the one currently "cut"
        if (clipboardSeance != null && clipboardIsCut
                && clipboardSeance.getId() == s.getId()) {
            if (!card.getStyleClass().contains("tt-cut")) card.getStyleClass().add("tt-cut");
        }
    }

    private int slotForSeance(Seance s) {
        String hm = s.getHeureDebut().toString().substring(0, 5);
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i][0].equals(hm)) return i;
        }
        // Fallback : nearest by start hour
        int startMin = toMinutes(hm);
        int best = -1, bestDelta = Integer.MAX_VALUE;
        for (int i = 0; i < SLOTS.length; i++) {
            int slotMin = toMinutes(SLOTS[i][0]);
            int d = Math.abs(slotMin - startMin);
            if (d < bestDelta && d <= 60) {
                bestDelta = d; best = i;
            }
        }
        return best;
    }

    private static int toMinutes(String hm) {
        String[] p = hm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    /**
     * Move an existing seance to a new (jour, slot) cell.
     * Validates conflicts before persisting.
     */
    private boolean handleSeanceDrop(int seanceId, String newJour, String newSlotStart) {
        Seance original = ss.getById(seanceId);
        if (original == null) return false;

        int slotIdx = slotIndexFromTime(newSlotStart);
        if (slotIdx < 0) return false;

        // Already at this position : nothing to do
        if (newJour.equals(original.getJour())
                && SLOTS[slotIdx][0].equals(original.getHeureDebut().toString().substring(0, 5))) {
            return true;
        }

        Time tDeb = Time.valueOf(SLOTS[slotIdx][0] + ":00");
        Time tFin = Time.valueOf(SLOTS[slotIdx][1] + ":00");

        Seance moved = new Seance(
                newJour, tDeb, tFin,
                original.getSalle(),
                original.getTypeCours(),
                original.getAnneeScolaire(),
                original.getClasseId(),
                original.getMatiereId(),
                original.getTeacherId()
        );

        if (hasConflitForMove(moved, seanceId)) {
            afficher(AlertType.ERROR,
                    "Conflit horaire détecté — déplacement annulé.\n"
                            + "Une autre séance occupe déjà ce créneau pour le même prof, classe ou salle.");
            return false;
        }

        ss.updateEntity(seanceId, moved);
        rafraichir();
        return true;
    }

    /**
     * Swap the (jour, créneau) of two existing séances.
     * Each updated séance keeps its own subject / class / teacher / room.
     */
    private boolean handleSeanceSwap(int sourceId, int targetId) {
        Seance src = ss.getById(sourceId);
        Seance tgt = ss.getById(targetId);
        if (src == null || tgt == null) return false;

        // Build the swapped versions
        Seance newSrc = new Seance(
                tgt.getJour(), tgt.getHeureDebut(), tgt.getHeureFin(),
                src.getSalle(), src.getTypeCours(), src.getAnneeScolaire(),
                src.getClasseId(), src.getMatiereId(), src.getTeacherId()
        );
        Seance newTgt = new Seance(
                src.getJour(), src.getHeureDebut(), src.getHeureFin(),
                tgt.getSalle(), tgt.getTypeCours(), tgt.getAnneeScolaire(),
                tgt.getClasseId(), tgt.getMatiereId(), tgt.getTeacherId()
        );

        // Validate conflicts excluding both swapping rows
        if (hasConflitForMoveExcluding(newSrc, sourceId, targetId)
                || hasConflitForMoveExcluding(newTgt, sourceId, targetId)) {
            afficher(AlertType.ERROR,
                    "Échange impossible — conflit horaire avec une autre séance.");
            return false;
        }

        // IMPORTANT : we use updateEntityNoCheck because during the swap each
        // row must transiently occupy the other's slot, which would trip the
        // service-side conflict detection. The controller already validated.
        ss.updateEntityNoCheck(sourceId, newSrc);
        ss.updateEntityNoCheck(targetId, newTgt);
        afficher(AlertType.INFORMATION, "Séances échangées.");
        rafraichir();
        return true;
    }

    /** Variant of hasConflitForMove that excludes two ids (used during swap). */
    private boolean hasConflitForMoveExcluding(Seance moved, int exA, int exB) {
        for (Seance other : ss.getData()) {
            if (other.getId() == exA || other.getId() == exB) continue;
            if (other.getJour() == null || !other.getJour().equals(moved.getJour())) continue;
            if (other.getHeureDebut() == null || other.getHeureFin() == null) continue;
            boolean overlaps = other.getHeureDebut().before(moved.getHeureFin())
                    && moved.getHeureDebut().before(other.getHeureFin());
            if (!overlaps) continue;
            boolean sameTeacher = other.getTeacherId() == moved.getTeacherId();
            boolean sameClasse = other.getClasseId() == moved.getClasseId();
            boolean sameSalle = moved.getSalle() != null
                    && moved.getSalle().equals(other.getSalle());
            if (sameTeacher || sameClasse || sameSalle) return true;
        }
        return false;
    }

    /**
     * Paste the clipboard seance onto a new (jour, slot) cell.
     * COPY → insert a duplicate. CUT → update the original then clear clipboard.
     */
    private void handlePaste(String newJour, String newSlotStart) {
        if (clipboardSeance == null) return;
        int slotIdx = slotIndexFromTime(newSlotStart);
        if (slotIdx < 0) return;

        Time tDeb = Time.valueOf(SLOTS[slotIdx][0] + ":00");
        Time tFin = Time.valueOf(SLOTS[slotIdx][1] + ":00");

        Seance pasted = new Seance(
                newJour, tDeb, tFin,
                clipboardSeance.getSalle(),
                clipboardSeance.getTypeCours(),
                clipboardSeance.getAnneeScolaire(),
                clipboardSeance.getClasseId(),
                clipboardSeance.getMatiereId(),
                clipboardSeance.getTeacherId()
        );

        int excludeId = clipboardIsCut ? clipboardSeance.getId() : -1;
        if (hasConflitForMove(pasted, excludeId)) {
            afficher(AlertType.ERROR,
                    "Conflit horaire — collage annulé.\n"
                            + "Une autre séance occupe déjà ce créneau.");
            return;
        }

        if (clipboardIsCut) {
            ss.updateEntity(clipboardSeance.getId(), pasted);
            clipboardSeance = null;
            clipboardIsCut = false;
            afficher(AlertType.INFORMATION, "Séance déplacée.");
        } else {
            ss.addEntity(pasted);
            afficher(AlertType.INFORMATION, "Séance copiée.");
        }
        rafraichir();
    }

    /** Confirmation dialog + delete + refresh. */
    private void demanderSupprimer(Seance s) {
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Supprimer cette séance ?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                ss.deleteEntity(s);
                if (clipboardSeance != null && clipboardSeance.getId() == s.getId()) {
                    clipboardSeance = null;
                    clipboardIsCut = false;
                }
                afficher(AlertType.INFORMATION, "Séance supprimée.");
                rafraichir();
            }
        });
    }

    /**
     * True if 'moved' would clash with any other seance (excluding the row with id excludeId).
     * A clash is : same day + time overlap + (same teacher OR same class OR same room).
     */
    private boolean hasConflitForMove(Seance moved, int excludeId) {
        for (Seance other : ss.getData()) {
            if (other.getId() == excludeId) continue;
            if (other.getJour() == null || !other.getJour().equals(moved.getJour())) continue;
            if (other.getHeureDebut() == null || other.getHeureFin() == null) continue;

            boolean overlaps = other.getHeureDebut().before(moved.getHeureFin())
                    && moved.getHeureDebut().before(other.getHeureFin());
            if (!overlaps) continue;

            boolean sameTeacher = other.getTeacherId() == moved.getTeacherId();
            boolean sameClasse = other.getClasseId() == moved.getClasseId();
            boolean sameSalle = moved.getSalle() != null
                    && moved.getSalle().equals(other.getSalle());
            if (sameTeacher || sameClasse || sameSalle) return true;
        }
        return false;
    }

    private VBox creerCarteSeance(Seance s) {
        VBox box = new VBox(4);
        box.getStyleClass().add("tt-printed-cell");
        box.setAlignment(Pos.CENTER);

        Label matiere = new Label((s.getMatiereNom() != null ? s.getMatiereNom() : "—") + " /");
        matiere.getStyleClass().add("tt-printed-matiere");
        matiere.setWrapText(true);

        Label salle = new Label("salle: " + (s.getSalle() != null ? s.getSalle() : "—") + " /");
        salle.getStyleClass().add("tt-printed-salle");

        box.getChildren().addAll(matiere, salle);

        if (s.getTeacherNom() != null && !s.getTeacherNom().trim().isEmpty()) {
            Label prof = new Label(s.getTeacherNom() + " /");
            prof.getStyleClass().add("tt-printed-prof");
            box.getChildren().add(prof);
        }
        return box;
    }

    private static int indexOfDay(String jour) {
        for (int i = 0; i < JOURS.length; i++) if (JOURS[i].equals(jour)) return i;
        return -1;
    }

    private void ouvrirDialog(Seance existing, String prefilledJour, int slotIdx) {
        // Jour et créneau sont déterminés par la cellule cliquée (ou la séance existante)
        final String jour     = (existing != null) ? existing.getJour()
                              : (prefilledJour != null) ? prefilledJour : null;
        final int    slotFinal = (existing != null) ? slotForSeance(existing) : slotIdx;

        // Classe fixée par le filtre (ou par la séance existante)
        final Classe classeFixee = (existing != null)
                ? findById(cs.getData(), existing.getClasseId())
                : filtreClasseCombo.getValue();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle séance" : "Modifier la séance");
        dialog.initModality(Modality.APPLICATION_MODAL);

        DialogPane pane = dialog.getDialogPane();
        try {
            pane.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
        } catch (Exception ignored) {}
        // Fond sombre identique à l'interface
        pane.setStyle("-fx-background-color: #16213e; -fx-border-color: #2d2d4e; -fx-border-width: 1;");
        pane.getStyleClass().add("app-root");
        pane.setPrefWidth(480);

        // ── Champs du formulaire (sans Jour / Créneau / Classe) ──────

        ComboBox<Matiere> matiereC = new ComboBox<>(FXCollections.observableArrayList(ms.getData()));
        matiereC.getStyleClass().add("combo-pill");
        matiereC.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Teacher> teacherC = new ComboBox<>(FXCollections.observableArrayList(ts.getData()));
        teacherC.getStyleClass().add("combo-pill");
        teacherC.setMaxWidth(Double.MAX_VALUE);

        TextField salleF = new TextField();
        salleF.setPromptText("Ex : 2-10");
        salleF.getStyleClass().add("input-pill");
        salleF.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> typeC = new ComboBox<>(FXCollections.observableArrayList("PRESENTIEL", "DISTANCIEL"));
        typeC.setValue("PRESENTIEL");
        typeC.getStyleClass().add("combo-pill");
        typeC.setMaxWidth(Double.MAX_VALUE);

        // Pré-remplissage si modification
        if (existing != null) {
            salleF.setText(existing.getSalle());
            typeC.setValue(existing.getTypeCours());
            matiereC.setValue(findMatiereById(matiereC.getItems(), existing.getMatiereId()));
            teacherC.setValue(findTeacherById(teacherC.getItems(), existing.getTeacherId()));
        }

        // Désactiver salle si DISTANCIEL
        typeC.valueProperty().addListener((o, ov, nv) -> {
            boolean dist = "DISTANCIEL".equals(nv);
            salleF.setDisable(dist);
            if (dist) salleF.clear();
        });

        // ── Bandeau d'info (jour + créneau + classe) en haut ─────────
        String creneauTxt = (slotFinal >= 0 && slotFinal < SLOTS.length)
                ? SLOTS[slotFinal][0] + " — " + SLOTS[slotFinal][1] : "—";
        String dateTxt = "";
        if (jour != null) {
            int dIdx = indexOfDay(jour);
            if (dIdx >= 0) dateTxt = "  •  " + weekStart.plusDays(dIdx).format(DATE_FMT);
        }

        Label infoBar = new Label(
                "📅  " + (jour != null ? capitalize(jour) : "—")
                + dateTxt
                + "   ⏱  " + creneauTxt
                + "   🏫  " + (classeFixee != null ? classeFixee.getNom() : "—"));
        infoBar.setStyle(
                "-fx-background-color: #2d1b69; -fx-background-radius: 8; "
                + "-fx-padding: 8 14; -fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-text-fill: #c4b5fd;");
        infoBar.setMaxWidth(Double.MAX_VALUE);
        infoBar.setWrapText(true);

        // ── Grille formulaire ─────────────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.getColumnConstraints().addAll(
                colPct(30), colPct(70)
        );

        form.add(labelForm("MATIÈRE *"),     0, 0); form.add(matiereC,  1, 0);
        form.add(labelForm("ENSEIGNANT *"),  0, 1); form.add(teacherC,  1, 1);
        form.add(labelForm("SALLE *"),       0, 2); form.add(salleF,    1, 2);
        form.add(labelForm("TYPE *"),        0, 3); form.add(typeC,     1, 3);

        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("form-error");
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(16);

        VBox content = new VBox(14, infoBar, form, errorLabel);
        content.setStyle("-fx-padding: 6 0 0 0;");
        pane.setContent(content);

        // ── Boutons ───────────────────────────────────────────────────
        ButtonType saveBt   = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBt = new ButtonType("Annuler",     ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(cancelBt, saveBt);

        ButtonType deleteBt = null;
        if (existing != null) {
            deleteBt = new ButtonType("Supprimer", ButtonBar.ButtonData.LEFT);
            pane.getButtonTypes().add(deleteBt);
            pane.lookupButton(deleteBt).getStyleClass().add("btn-danger");
        }
        pane.lookupButton(saveBt).getStyleClass().add("btn-primary");
        pane.lookupButton(cancelBt).getStyleClass().add("btn-ghost");

        // Validation avant fermeture
        final ButtonType deleteFinal = deleteBt;
        pane.lookupButton(saveBt).addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            String err = validerSeanceSansJourClasse(
                    salleF.getText(), typeC.getValue(), matiereC.getValue(), teacherC.getValue());
            if (err != null) {
                errorLabel.setText(err);
                evt.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (!result.isPresent()) return;

        ButtonType clicked = result.get();
        if (clicked == saveBt) {
            Seance built = construireSansJourClasse(
                    jour, slotFinal, salleF.getText(), typeC.getValue(),
                    classeFixee, matiereC.getValue(), teacherC.getValue());

            if (existing == null) {
                String conflits = ss.detecterConflits(built);
                if (!conflits.isEmpty()) {
                    afficher(AlertType.ERROR, "Conflit horaire :\n" + conflits);
                    return;
                }
                ss.addEntity(built);
                afficher(AlertType.INFORMATION, "Séance ajoutée.");
            } else {
                ss.updateEntity(existing.getId(), built);
                afficher(AlertType.INFORMATION, "Séance modifiée.");
            }
            rafraichir();
        } else if (deleteFinal != null && clicked == deleteFinal) {
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Supprimer cette séance ?");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    ss.deleteEntity(existing);
                    afficher(AlertType.INFORMATION, "Séance supprimée.");
                    rafraichir();
                }
            });
        }
    }

    /** Contraintes de colonne en pourcentage pour le GridPane du dialog. */
    private javafx.scene.layout.ColumnConstraints colPct(double pct) {
        javafx.scene.layout.ColumnConstraints c = new javafx.scene.layout.ColumnConstraints();
        c.setPercentWidth(pct);
        c.setHgrow(Priority.ALWAYS);
        return c;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String validerSeanceSansJourClasse(String salle, String type,
                                               Matiere mat, Teacher prof) {
        if (mat == null)  return "Sélectionnez une matière.";
        if (prof == null) return "Sélectionnez un enseignant.";
        if (!"DISTANCIEL".equals(type)) {
            if (salle == null || salle.trim().isEmpty())
                return "Salle obligatoire en présentiel.";
            if (!salle.trim().matches("[A-Za-z0-9\\-]{1,10}"))
                return "Salle : 1 à 10 caractères alphanumériques (tirets autorisés).";
        }
        return null;
    }

    private Seance construireSansJourClasse(String jour, int slotIdx, String salle, String type,
                                            Classe classe, Matiere mat, Teacher prof) {
        String hStart = SLOTS[slotIdx][0] + ":00";
        String hEnd   = SLOTS[slotIdx][1] + ":00";
        Time tDeb = Time.valueOf(hStart);
        Time tFin = Time.valueOf(hEnd);
        String s = "DISTANCIEL".equals(type)
                ? (salle == null || salle.trim().isEmpty() ? "EN LIGNE" : salle.trim())
                : salle.trim();
        return new Seance(jour, tDeb, tFin, s, type, "2024-2025",
                classe.getId(), mat.getId(), prof.getId());
    }

    private static Classe findById(List<Classe> list, int id) {
        for (Classe c : list) if (c.getId() == id) return c;
        return null;
    }

    private static Matiere findMatiereById(List<Matiere> list, int id) {
        for (Matiere m : list) if (m.getId() == id) return m;
        return null;
    }

    private static Teacher findTeacherById(List<Teacher> list, int id) {
        for (Teacher t : list) if (t.getId() == id) return t;
        return null;
    }

    private Label labelForm(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-form");
        l.setMinWidth(110);
        return l;
    }

    private void majResultatLabel(int n) {
        if (resultatLabel == null) return;
        resultatLabel.setText("(" + n + " séance" + (n > 1 ? "s" : "") + ")");
    }

    @FXML
    private void exporterPDF() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le PDF");

        // Nom de fichier par défaut selon la classe filtrée
        Classe classeFiltre = filtreClasseCombo.getValue();
        String nomFichier = classeFiltre != null
                ? "emploi_" + classeFiltre.getNom().replaceAll("[^A-Za-z0-9]", "_") + ".pdf"
                : "emploi_du_temps.pdf";
        chooser.setInitialFileName(nomFichier);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        File fichier = chooser.showSaveDialog(timetableGrid.getScene().getWindow());
        if (fichier == null) return;

        List<Seance> seances = appliquerFiltres(ss.getData());

        if (seances.isEmpty()) {
            afficher(AlertType.WARNING, "Aucune séance à exporter.");
            return;
        }

        try {
            // Utilise le générateur en grille calendrier avec les dates de la semaine affichée
            edu.edunova.utils.EmploiDuTempsPdfGenerator.genererPourClasse(
                    classeFiltre, seances, fichier, weekStart);
            ouvrirPDF(fichier);
        } catch (Exception e) {
            afficher(AlertType.ERROR, "Erreur export PDF : " + e.getMessage());
        }
    }

    private void ouvrirPDF(File fichier) {
        try {
            if (Desktop.isDesktopSupported() && fichier.exists()) {
                Desktop.getDesktop().open(fichier);
            } else {
                afficher(AlertType.INFORMATION,
                        "PDF genere : " + fichier.getAbsolutePath()
                                + "\n(Ouverture automatique non supportee)");
            }
        } catch (Exception e) {
            afficher(AlertType.WARNING,
                    "PDF genere mais impossible d'ouvrir automatiquement : "
                            + fichier.getAbsolutePath());
        }
    }

    private void afficher(AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
