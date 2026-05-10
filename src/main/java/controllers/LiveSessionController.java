package controllers;

import edu.edunova_a.entities.Classe;
import edu.edunova_a.entities.LiveSession;
import edu.edunova_a.entities.Matiere;
import edu.edunova_a.entities.Teacher;
import edu.edunova_a.services.ClasseService;
import edu.edunova_a.services.LiveSessionService;
import edu.edunova_a.services.MatiereService;
import edu.edunova_a.services.TeacherService;
import edu.edunova_a.services.ZoomService;
import edu.edunova_a.utils.DialogUtils;
import edu.edunova_a.utils.StatusUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vue Cours en ligne refondue : header + stat cards + tabs filtre + cards.
 * Le formulaire d'ajout/modif s'ouvre dans un Dialog modal.
 * Toutes les actions (Rejoindre, Zoom, Pointage, Edit, Delete) sont sur la carte.
 */
public class LiveSessionController {

    /* ----- stat cards ----- */
    @FXML private Label lblStatTotal;
    @FXML private Label lblStatProgrammees;
    @FXML private Label lblStatEnCours;
    @FXML private Label lblStatTerminees;

    /* ----- filtre ----- */
    @FXML private TabPane   tabFilter;
    @FXML private TextField tfSearch;

    /* ----- container cards ----- */
    @FXML private FlowPane  cardsContainer;
    @FXML private Label     lblEmpty;

    /* ----- services ----- */
    private final LiveSessionService liveService    = new LiveSessionService();
    private final ClasseService      classeService  = new ClasseService();
    private final MatiereService     matiereService = new MatiereService();
    private final TeacherService     teacherService = new TeacherService();
    private final ZoomService        zoomService    = new ZoomService();

    private List<LiveSession> currentSessions = new java.util.ArrayList<>();

    private static final java.util.List<String> TIME_OPTIONS = java.util.List.of(
            "08:00","08:30","09:00","09:30","10:00","10:30",
            "11:00","11:30","12:00","12:30","13:00","13:30",
            "14:00","14:30","15:00","15:30","16:00","16:30",
            "17:00","17:30","18:00");

    /* ============== INIT ============== */

    @FXML
    public void initialize() {
        tabFilter.getSelectionModel().selectedIndexProperty()
                .addListener((obs, o, n) -> renderCards());
        loadAll();
    }

    private void loadAll() {
        try {
            currentSessions = liveService.afficher();
            updateStats();
            renderCards();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void updateStats() {
        long prog = currentSessions.stream().filter(l -> "PROGRAMMEE".equalsIgnoreCase(l.getStatut_ls())).count();
        long enc  = currentSessions.stream().filter(l -> "EN_COURS".equalsIgnoreCase(l.getStatut_ls())).count();
        long ter  = currentSessions.stream().filter(l -> "TERMINEE".equalsIgnoreCase(l.getStatut_ls())).count();
        lblStatTotal.setText(String.valueOf(currentSessions.size()));
        lblStatProgrammees.setText(String.valueOf(prog));
        lblStatEnCours.setText(String.valueOf(enc));
        lblStatTerminees.setText(String.valueOf(ter));
    }

    /* ============== FILTRE + RECHERCHE ============== */

    @FXML
    public void onChercher() { renderCards(); }

    private List<LiveSession> applyFilter() {
        int tab = tabFilter.getSelectionModel().getSelectedIndex();
        String q = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        return currentSessions.stream()
                .filter(l -> {
                    String st = l.getStatut_ls() == null ? "" : l.getStatut_ls().toUpperCase();
                    if (tab == 1 && !"PROGRAMMEE".equals(st)) return false;
                    if (tab == 2 && !"EN_COURS".equals(st))   return false;
                    if (tab == 3 && !"TERMINEE".equals(st))   return false;
                    if (q.isEmpty()) return true;
                    return contains(l.getTitre_ls(), q)
                            || contains(l.getStatut_ls(), q)
                            || contains(l.getMatiere_nom(), q)
                            || contains(l.getClasse_nom(), q)
                            || contains(l.getTeacher_nom(), q);
                })
                .collect(Collectors.toList());
    }

    /* ============== RENDU CARTES ============== */

    private void renderCards() {
        List<LiveSession> list = applyFilter();
        cardsContainer.getChildren().clear();
        if (list.isEmpty()) {
            lblEmpty.setVisible(true); lblEmpty.setManaged(true);
            return;
        }
        lblEmpty.setVisible(false); lblEmpty.setManaged(false);

        int delay = 0;
        for (LiveSession ls : list) {
            VBox card = buildCard(ls);
            card.setOpacity(0);
            card.setScaleX(0.96); card.setScaleY(0.96);

            FadeTransition ft = new FadeTransition(Duration.millis(280), card);
            ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(delay));
            ScaleTransition st = new ScaleTransition(Duration.millis(280), card);
            st.setFromX(0.96); st.setFromY(0.96); st.setToX(1); st.setToY(1); st.setDelay(Duration.millis(delay));

            cardsContainer.getChildren().add(card);
            ft.play(); st.play();
            delay += 50;
        }
    }

    private VBox buildCard(LiveSession ls) {
        VBox card = new VBox(10);
        card.setPrefWidth(360);
        card.setMaxWidth(360);
        card.setStyle("-fx-background-color: #13131f; -fx-background-radius: 14; "
                + "-fx-padding: 18; -fx-border-color: #1e1e2e; -fx-border-radius: 14; "
                + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);");

        String hoverStyle = "-fx-background-color: #17172a; -fx-background-radius: 14; "
                + "-fx-padding: 18; -fx-border-color: rgba(6,182,212,0.5); -fx-border-radius: 14; "
                + "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(6,182,212,0.3), 16, 0, 0, 5);";
        String defaultStyle = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e  -> card.setStyle(defaultStyle));

        // Header : statut badge + ID
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label statut = new Label(StatusUtils.libelleStatutLive(ls.getStatut_ls()));
        statut.setStyle("-fx-background-radius: 8; -fx-padding: 4 12; "
                + "-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: white; "
                + "-fx-background-color: " + StatusUtils.colorStatutLive(ls.getStatut_ls()) + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label id = new Label("#" + ls.getId_ls());
        id.setStyle("-fx-font-size: 10; -fx-text-fill: #4a4a6a;");
        header.getChildren().addAll(statut, spacer, id);

        // Titre
        Label titre = new Label(ls.getTitre_ls() != null ? ls.getTitre_ls() : "—");
        titre.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #f4f4f9;");
        titre.setWrapText(true);
        titre.setMaxWidth(320);

        // Date + heures
        Label dateHeure = new Label("📅 "
                + (ls.getDate_session_ls() != null ? ls.getDate_session_ls().toString() : "—")
                + "   🕐 " + (ls.getHeure_debut_ls() != null ? ls.getHeure_debut_ls() : "")
                + " → "    + (ls.getHeure_fin_ls()   != null ? ls.getHeure_fin_ls()   : ""));
        dateHeure.setStyle("-fx-font-size: 12; -fx-text-fill: #06b6d4; -fx-font-weight: bold;");

        // Matière + Classe
        Label info = new Label("📚 " + (ls.getMatiere_nom() != null ? ls.getMatiere_nom() : "—")
                + "  ·  🎓 " + (ls.getClasse_nom() != null ? ls.getClasse_nom() : "—"));
        info.setStyle("-fx-font-size: 11; -fx-text-fill: #a8a8c0;");

        // Enseignant
        Label teacher = new Label("👤 " + (ls.getTeacher_nom() != null ? ls.getTeacher_nom() : "—"));
        teacher.setStyle("-fx-font-size: 11; -fx-text-fill: #8a8aa3;");

        // Badge Zoom si meeting créé
        HBox zoomBadgeBox = new HBox(6);
        if (ls.getZoom_meeting_id() != null && !ls.getZoom_meeting_id().isBlank()) {
            Label zb = new Label("🎥 Zoom prêt");
            zb.setStyle("-fx-background-color: rgba(59,130,246,0.18); -fx-text-fill: #60a5fa; "
                    + "-fx-padding: 3 10; -fx-background-radius: 8; -fx-font-size: 10; -fx-font-weight: bold;");
            zoomBadgeBox.getChildren().add(zb);
        }

        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #1e1e2e;");

        // Boutons : Rejoindre + Zoom + Pointage
        HBox actions1 = new HBox(8);
        actions1.setAlignment(Pos.CENTER_LEFT);

        if (ls.getLien_meet_ls() != null && !ls.getLien_meet_ls().isBlank()) {
            Button btnJoin = new Button("🔗 Rejoindre");
            btnJoin.setStyle("-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2); "
                    + "-fx-text-fill: white; -fx-padding: 7 14; -fx-background-radius: 8; "
                    + "-fx-font-size: 11; -fx-font-weight: bold; -fx-cursor: hand;");
            btnJoin.setOnAction(e -> {
                try { java.awt.Desktop.getDesktop().browse(new java.net.URI(ls.getLien_meet_ls())); }
                catch (Exception ex) { showError("Impossible d'ouvrir le lien."); }
            });
            actions1.getChildren().add(btnJoin);
        }

        Button btnZoom = new Button(ls.getZoom_meeting_id() == null || ls.getZoom_meeting_id().isBlank()
                ? "🎥 Créer Zoom" : "🎥 Recréer Zoom");
        btnZoom.setStyle("-fx-background-color: rgba(59,130,246,0.18); -fx-text-fill: #60a5fa; "
                + "-fx-padding: 7 14; -fx-background-radius: 8; -fx-font-size: 11; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: rgba(59,130,246,0.4); "
                + "-fx-border-radius: 8; -fx-border-width: 1;");
        btnZoom.setOnAction(e -> creerZoom(ls, btnZoom));
        actions1.getChildren().add(btnZoom);

        // Boutons : Participants + Pointage + Edit + Delete
        HBox actions2 = new HBox(8);
        actions2.setAlignment(Pos.CENTER_LEFT);

        Button btnParticipants = new Button("👥");
        btnParticipants.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: #d4d4e0; "
                + "-fx-padding: 7 12; -fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand;");
        btnParticipants.setTooltip(new Tooltip("Participants Zoom"));
        btnParticipants.setOnAction(e -> voirParticipants(ls, btnParticipants));

        Button btnPointage = new Button("📷 Pointage");
        btnPointage.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); "
                + "-fx-text-fill: white; -fx-padding: 7 14; -fx-background-radius: 8; "
                + "-fx-font-size: 11; -fx-font-weight: bold; -fx-cursor: hand;");
        btnPointage.setOnAction(e -> demarrerPointage(ls));

        Region s2 = new Region();
        HBox.setHgrow(s2, Priority.ALWAYS);

        Button btnEdit = new Button("✎");
        btnEdit.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: #d4d4e0; "
                + "-fx-padding: 7 12; -fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand;");
        btnEdit.setTooltip(new Tooltip("Modifier"));
        btnEdit.setOnAction(e -> onShowFormDialog(ls));

        Button btnDel = new Button("✖");
        btnDel.setStyle("-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #f87171; "
                + "-fx-padding: 7 12; -fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand; "
                + "-fx-border-color: rgba(239,68,68,0.4); -fx-border-radius: 8; -fx-border-width: 1;");
        btnDel.setTooltip(new Tooltip("Supprimer"));
        btnDel.setOnAction(e -> onSupprimer(ls));

        actions2.getChildren().addAll(btnParticipants, btnPointage, s2, btnEdit, btnDel);

        card.getChildren().addAll(header, titre, dateHeure, info, teacher);
        if (!zoomBadgeBox.getChildren().isEmpty()) card.getChildren().add(zoomBadgeBox);
        card.getChildren().addAll(sep, actions1, actions2);
        return card;
    }

    /* ============== ACTIONS ZOOM (par carte) ============== */

    /** Création Zoom auto déclenchée par la checkbox du form. Pas de bouton à toggle. */
    private void creerZoomAuto(LiveSession session) {
        new Thread(() -> {
            try {
                int dureeMins = (int) java.time.Duration.between(
                        session.getHeure_debut_ls(), session.getHeure_fin_ls()).toMinutes();
                ZoomService.MeetingResult result = zoomService.createMeeting(
                        session.getTitre_ls(), session.getDate_session_ls(),
                        session.getHeure_debut_ls(), dureeMins);
                liveService.updateZoomInfo(session.getId_ls(), result.joinUrl, result.meetingId);
                Platform.runLater(() -> {
                    loadAll();
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Session + Réunion Zoom créées");
                    ok.setHeaderText("✅ Tout est prêt !");
                    ok.setContentText("Lien Zoom : " + result.joinUrl
                            + "\n\nID réunion : " + result.meetingId);
                    DialogUtils.applyProTheme(ok);
                    ok.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Session créée, mais erreur Zoom : " + e.getMessage()));
            }
        }).start();
    }

    private void creerZoom(LiveSession session, Button btnZoom) {
        if (session.getZoom_meeting_id() != null && !session.getZoom_meeting_id().isBlank()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cette session a déjà un lien Zoom.\nVoulez-vous en créer un nouveau ?",
                    ButtonType.YES, ButtonType.NO);
            DialogUtils.applyProTheme(confirm);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }
        btnZoom.setDisable(true);
        btnZoom.setText("⏳...");

        new Thread(() -> {
            try {
                int dureeMins = (int) java.time.Duration.between(
                        session.getHeure_debut_ls(), session.getHeure_fin_ls()).toMinutes();

                ZoomService.MeetingResult result = zoomService.createMeeting(
                        session.getTitre_ls(), session.getDate_session_ls(),
                        session.getHeure_debut_ls(), dureeMins);

                liveService.updateZoomInfo(session.getId_ls(), result.joinUrl, result.meetingId);

                Platform.runLater(() -> {
                    btnZoom.setDisable(false);
                    loadAll();
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Réunion Zoom créée");
                    ok.setHeaderText("✅ Réunion créée avec succès !");
                    ok.setContentText("Lien : " + result.joinUrl
                            + "\n\nID : " + result.meetingId);
                    DialogUtils.applyProTheme(ok);
                    ok.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnZoom.setDisable(false);
                    btnZoom.setText("🎥 Créer Zoom");
                    showError("Erreur Zoom : " + e.getMessage());
                });
            }
        }).start();
    }

    private void voirParticipants(LiveSession session, Button btn) {
        if (session.getZoom_meeting_id() == null || session.getZoom_meeting_id().isBlank()) {
            warn("Cette session n'a pas de réunion Zoom associée.\n"
               + "Créez d'abord une réunion avec '🎥 Créer Zoom'.");
            return;
        }
        btn.setDisable(true);
        new Thread(() -> {
            try {
                List<ZoomService.ZoomParticipant> participants =
                        zoomService.getParticipants(session.getZoom_meeting_id());
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    if (participants.isEmpty()) {
                        info("Aucun participant trouvé.\n(La réunion doit être terminée)");
                        return;
                    }
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Participants — " + session.getTitre_ls());
                    dialog.setHeaderText("👥 " + participants.size() + " participant(s)");
                    ListView<String> listView = new ListView<>();
                    for (ZoomService.ZoomParticipant p : participants) {
                        String entry = p.name
                                + (p.email != null && !p.email.isEmpty() ? "  |  " + p.email : "")
                                + "  |  Connexion : " + p.joinTime;
                        listView.getItems().add(entry);
                    }
                    listView.setPrefSize(700, 350);
                    dialog.getDialogPane().setContent(listView);
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                    DialogUtils.applyProTheme(dialog);
                    dialog.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    showError("Erreur participants : " + e.getMessage());
                });
            }
        }).start();
    }

    private void demarrerPointage(LiveSession ls) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Pointage.fxml"));
            javafx.scene.Parent root = loader.load();
            PointageController ctrl = loader.getController();
            String label = ls.getTitre_ls() + " (" + ls.getDate_session_ls() + ")";
            ctrl.initForLiveSession(ls.getId_ls(), label);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Pointage - " + label);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'ouvrir le pointage : " + e.getMessage());
        }
    }

    /* ============== FORM DIALOG (création / édition) ============== */

    @FXML
    public void onShowFormDialog() { onShowFormDialog(null); }

    private void onShowFormDialog(LiveSession existing) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existing == null ? "Nouvelle session live" : "Modifier session #" + existing.getId_ls());

            TextField tfTitre  = new TextField(); tfTitre.setPromptText("Ex: Cours Maths - Algèbre");
            DatePicker dpDate  = new DatePicker();
            ComboBox<String> cbStatut    = new ComboBox<>(FXCollections.observableArrayList("PROGRAMMEE","EN_COURS","TERMINEE","ANNULEE"));
            ComboBox<String> cbHd        = new ComboBox<>(FXCollections.observableArrayList(TIME_OPTIONS));
            ComboBox<String> cbHf        = new ComboBox<>(FXCollections.observableArrayList(TIME_OPTIONS));
            TextField tfLien   = new TextField(); tfLien.setPromptText("https://meet.google.com/...");
            ComboBox<Classe>  cbClasse  = new ComboBox<>(FXCollections.observableArrayList(classeService.afficher()));
            ComboBox<Matiere> cbMatiere = new ComboBox<>(FXCollections.observableArrayList(matiereService.afficher()));
            ComboBox<Teacher> cbTeacher = new ComboBox<>(FXCollections.observableArrayList(teacherService.afficher()));

            // Largeur uniforme
            for (Control c : new Control[]{tfTitre, dpDate, cbStatut, cbHd, cbHf, tfLien, cbClasse, cbMatiere, cbTeacher}) {
                c.setPrefWidth(280);
            }

            // Checkbox création Zoom auto (seulement à la création)
            CheckBox cbCreerZoom = new CheckBox("🎥 Créer automatiquement une réunion Zoom");
            cbCreerZoom.setSelected(existing == null);
            cbCreerZoom.setStyle("-fx-text-fill: #60a5fa; -fx-font-weight: bold;");
            if (existing != null && existing.getZoom_meeting_id() != null
                    && !existing.getZoom_meeting_id().isBlank()) {
                cbCreerZoom.setText("🎥 Zoom déjà créé (recréer un nouveau lien)");
                cbCreerZoom.setSelected(false);
            }

            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(10);
            grid.add(new Label("Titre"),       0, 0); grid.add(tfTitre,   1, 0);
            grid.add(new Label("Date"),        0, 1); grid.add(dpDate,    1, 1);
            grid.add(new Label("Statut"),      0, 2); grid.add(cbStatut,  1, 2);
            grid.add(new Label("Heure début"), 0, 3); grid.add(cbHd,      1, 3);
            grid.add(new Label("Heure fin"),   0, 4); grid.add(cbHf,      1, 4);
            grid.add(new Label("Lien Meet"),   0, 5); grid.add(tfLien,    1, 5);
            grid.add(new Label("Classe"),      0, 6); grid.add(cbClasse,  1, 6);
            grid.add(new Label("Matière"),     0, 7); grid.add(cbMatiere, 1, 7);
            grid.add(new Label("Enseignant"),  0, 8); grid.add(cbTeacher, 1, 8);
            grid.add(cbCreerZoom,                     1, 9);
            for (javafx.scene.Node n : grid.getChildren()) {
                if (n instanceof Label) ((Label) n).setStyle("-fx-text-fill: #a8a8c0; -fx-font-size: 12;");
            }

            // Pré-remplissage en édition
            if (existing != null) {
                tfTitre.setText(existing.getTitre_ls());
                dpDate.setValue(existing.getDate_session_ls());
                cbStatut.setValue(existing.getStatut_ls());
                if (existing.getHeure_debut_ls() != null) cbHd.setValue(existing.getHeure_debut_ls().toString());
                if (existing.getHeure_fin_ls()   != null) cbHf.setValue(existing.getHeure_fin_ls().toString());
                tfLien.setText(existing.getLien_meet_ls() != null ? existing.getLien_meet_ls() : "");
                for (Classe x  : cbClasse.getItems())  if (x.getId() == existing.getClasse_id())  { cbClasse.setValue(x);  break; }
                for (Matiere x : cbMatiere.getItems()) if (x.getId_m() == existing.getMatiere_id()){ cbMatiere.setValue(x); break; }
                for (Teacher x : cbTeacher.getItems()) if (x.getId_t() == existing.getTeacher_id()){ cbTeacher.setValue(x); break; }
            } else {
                cbStatut.setValue("PROGRAMMEE");
                dpDate.setValue(java.time.LocalDate.now());
            }

            dialog.getDialogPane().setContent(grid);
            ButtonType ok = new ButtonType(existing == null ? "Créer" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
            DialogUtils.applyProTheme(dialog);

            dialog.showAndWait().ifPresent(bt -> {
                if (bt != ok) return;
                if (tfTitre.getText().isBlank() || dpDate.getValue() == null
                        || cbStatut.getValue() == null
                        || cbHd.getValue() == null || cbHf.getValue() == null
                        || cbClasse.getValue() == null || cbMatiere.getValue() == null
                        || cbTeacher.getValue() == null) {
                    warn("Tous les champs (sauf Lien) sont obligatoires.");
                    return;
                }
                try {
                    LocalTime hd = LocalTime.parse(cbHd.getValue());
                    LocalTime hf = LocalTime.parse(cbHf.getValue());
                    if (!hf.isAfter(hd)) { warn("L'heure de fin doit être après l'heure de début."); return; }

                    LiveSession l = new LiveSession(0,
                            tfTitre.getText().trim(),
                            dpDate.getValue(), hd, hf,
                            tfLien.getText(),
                            cbStatut.getValue(),
                            null,
                            cbTeacher.getValue().getId_t(),
                            cbClasse.getValue().getId(),
                            cbMatiere.getValue().getId_m());

                    boolean isCreate = (existing == null);
                    if (isCreate) {
                        liveService.ajouter(l);
                    } else {
                        l.setId_ls(existing.getId_ls());
                        l.setZoom_meeting_id(existing.getZoom_meeting_id());
                        liveService.modifier(l);
                    }
                    loadAll();

                    // Déclenchement Zoom auto si checkbox cochée
                    if (cbCreerZoom.isSelected()) {
                        // Recharge la session fraîchement créée pour avoir l'id
                        LiveSession finalSession = isCreate
                                ? liveService.afficher().stream()
                                    .filter(x -> x.getTitre_ls().equals(l.getTitre_ls())
                                              && x.getDate_session_ls().equals(l.getDate_session_ls()))
                                    .findFirst().orElse(null)
                                : existing;
                        if (finalSession != null) {
                            creerZoomAuto(finalSession);
                        }
                    } else {
                        info(isCreate ? "✅ Session créée." : "✅ Session modifiée.");
                    }
                } catch (Exception e) { showError(e.getMessage()); }
            });
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void onSupprimer(LiveSession ls) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la session « " + ls.getTitre_ls() + " » ?\n\nCette action est irréversible.",
                ButtonType.YES, ButtonType.NO);
        a.setHeaderText("🗑 Supprimer la session live");
        DialogUtils.applyProTheme(a);
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    liveService.supprimer(ls.getId_ls());
                    loadAll();
                    info("Session supprimée.");
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    /* ============== HELPERS ============== */

    private boolean contains(String s, String q) { return s != null && s.toLowerCase().contains(q); }

    private void showError(String msg) { showAlert(Alert.AlertType.ERROR, msg); }
    private void warn(String msg)      { showAlert(Alert.AlertType.WARNING, msg); }
    private void info(String msg)      { showAlert(Alert.AlertType.INFORMATION, msg); }
    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg);
        DialogUtils.applyProTheme(a);
        a.showAndWait();
    }
}
