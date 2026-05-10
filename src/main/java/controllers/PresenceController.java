package controllers;

import edu.edunova_a.config.AppConfig;
import edu.edunova_a.entities.AttendanceToken;
import edu.edunova_a.entities.LiveSession;
import edu.edunova_a.entities.Presence;
import edu.edunova_a.entities.Seance;
import edu.edunova_a.entities.Student;
import edu.edunova_a.http.AttendanceHttpServer;
import edu.edunova_a.services.AttendanceStatsService;
import edu.edunova_a.services.AttendanceTokenService;
import edu.edunova_a.services.ExportService;
import edu.edunova_a.services.LiveSessionService;
import edu.edunova_a.services.NotificationService;
import edu.edunova_a.services.PresenceService;
import edu.edunova_a.services.SeanceService;
import edu.edunova_a.services.StudentService;
import edu.edunova_a.services.TwilioSmsService;
import edu.edunova_a.utils.QRCodeUtil;
import edu.edunova_a.utils.StatusUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PresenceController {

    /* ----- stat cards (haut) ----- */
    @FXML private Label lblStatPresents;
    @FXML private Label lblStatAbsents;
    @FXML private Label lblStatRetards;
    @FXML private Label lblStatTotal;

    /* ----- barre de filtres ----- */
    @FXML private DatePicker                 dpFilterDate;
    @FXML private ComboBox<SessionItem>      cbFilterSeance;
    @FXML private ComboBox<String>           cbFilterStatut;

    /* ----- panneau actions rapides (sous la table) ----- */
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField        tfJustificatif;
    @FXML private Label            lblActionInfo;
    @FXML private Label            lblSmsStatus;

    /* ----- liste de cartes (anciennement table globale) ----- */
    @FXML private javafx.scene.layout.FlowPane cardsContainer;
    @FXML private TextField tfSearch;

    /* ----- panneau QR Code (droite) ----- */
    @FXML private ImageView imgMiniQR;
    @FXML private Label     lblCodeSession;

    /* ----- fiche étudiant ----- */
    @FXML private ComboBox<Student>             cbDetailStudent;
    @FXML private Label                         lblDetailNom;
    @FXML private StackPane                     paneScorePill;
    @FXML private Label                         lblDetailScore;
    @FXML private Label                         lblDetailPresents;
    @FXML private Label                         lblDetailAbsents;
    @FXML private Label                         lblDetailRetards;
    @FXML private Label                         lblDetailJustifies;
    @FXML private TableView<Presence>           tableDetail;
    @FXML private TableColumn<Presence, String> colDetailDate;
    @FXML private TableColumn<Presence, String> colDetailContexte;
    @FXML private TableColumn<Presence, String> colDetailStatut;
    @FXML private TableColumn<Presence, String> colDetailNote;
    @FXML private PieChart                       pieDetailPresence;

    /* ----- services ----- */
    private final PresenceService         presenceService = new PresenceService();
    private final StudentService          studentService  = new StudentService();
    private final SeanceService           seanceService   = new SeanceService();
    private final LiveSessionService      liveService     = new LiveSessionService();
    private final AttendanceStatsService  statsService    = new AttendanceStatsService();
    private final AttendanceTokenService  tokenService    = new AttendanceTokenService();
    private final NotificationService     notifService    = new NotificationService();
    private final TwilioSmsService        twilioService   = new TwilioSmsService();
    private final ExportService           exportService   = new ExportService();

    /* ----- data ----- */
    private final ObservableList<Presence> data       = FXCollections.observableArrayList();
    private final ObservableList<Presence> detailData = FXCollections.observableArrayList();
    private Presence selected = null;

    /* ===================== INIT ===================== */

    @FXML
    public void initialize() {
        cbStatut.setItems(FXCollections.observableArrayList(
                "PRESENT", "ABSENT", "RETARD", "JUSTIFIE"));

        // Filtre par statut
        if (cbFilterStatut != null) {
            cbFilterStatut.setItems(FXCollections.observableArrayList(
                    "Tous", "PRESENT", "ABSENT", "RETARD", "JUSTIFIE"));
            cbFilterStatut.setValue("Tous");
        }

        // Filtre date par défaut : aujourd'hui
        dpFilterDate.setValue(LocalDate.now());

        try {
            // Charger les étudiants pour la fiche détail
            List<Student> students = studentService.afficher();
            cbDetailStudent.setItems(FXCollections.observableArrayList(students));
            cbDetailStudent.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Student s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : s.getPrenom_s() + " " + s.getNom_s());
                }
            });
            cbDetailStudent.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Student s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? "Choisir un étudiant..."
                                               : s.getPrenom_s() + " " + s.getNom_s());
                }
            });

            // Charger séances + live sessions dans le filtre
            loadSessionItems();

        } catch (Exception e) { showError(e.getMessage()); }

        initDetailTable();

        cbDetailStudent.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> { if (n != null) refreshDetail(n); });

        // Quand la séance change → on régénère le mini QR
        cbFilterSeance.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> { if (n != null) refreshMiniQR(n); });

        loadAll();
        refreshStats();
    }

    /** Charge séances + live sessions dans le ComboBox filtre. */
    private void loadSessionItems() throws Exception {
        List<SessionItem> items = new ArrayList<>();
        items.add(new SessionItem(0, "ALL", "Toutes les séances", null, null));
        for (Seance s : seanceService.afficher()) {
            String label = s.getJour_se() + " " + s.getHeure_debut_se()
                    + " — " + (s.getMatiere_nom() != null ? s.getMatiere_nom() : "?")
                    + " (" + (s.getClasse_nom() != null ? s.getClasse_nom() : "?") + ")";
            items.add(new SessionItem(s.getId_se(), "SEANCE", label,
                    s.getMatiere_nom(), s.getJour_se()));
        }
        for (LiveSession l : liveService.afficher()) {
            String label = "🎥 " + l.getTitre_ls() + " (" + l.getDate_session_ls() + ")";
            items.add(new SessionItem(l.getId_ls(), "LIVE", label,
                    l.getMatiere_nom(), l.getDate_session_ls() != null ? l.getDate_session_ls().toString() : null));
        }
        cbFilterSeance.setItems(FXCollections.observableArrayList(items));
        cbFilterSeance.getSelectionModel().selectFirst();
    }

    /* ============== RENDU CARTES PRÉSENCES ============== */

    private void renderCards() {
        cardsContainer.getChildren().clear();
        if (data.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label(
                    "📭 Aucune présence à afficher pour ces filtres.");
            empty.setStyle("-fx-text-fill: #6b6b85; -fx-font-size: 13; -fx-padding: 30;");
            cardsContainer.getChildren().add(empty);
            return;
        }
        for (Presence p : data) cardsContainer.getChildren().add(buildPresenceCard(p));
    }

    private javafx.scene.layout.VBox buildPresenceCard(Presence p) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(8);
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        String defaultStyle = "-fx-background-color: #13131f; -fx-background-radius: 12; "
                + "-fx-padding: 14; -fx-border-color: #1e1e2e; -fx-border-radius: 12; "
                + "-fx-border-width: 1; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);";
        String hoverStyle = "-fx-background-color: #17172a; -fx-background-radius: 12; "
                + "-fx-padding: 14; -fx-border-color: rgba(124,58,237,0.5); -fx-border-radius: 12; "
                + "-fx-border-width: 1; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(124,58,237,0.3), 14, 0, 0, 4);";
        String selectedStyle = "-fx-background-color: #1a1a2e; -fx-background-radius: 12; "
                + "-fx-padding: 14; -fx-border-color: #7c3aed; -fx-border-radius: 12; "
                + "-fx-border-width: 2; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(124,58,237,0.4), 16, 0, 0, 5);";

        boolean isSel = selected != null && selected.getId_pr() == p.getId_pr();
        card.setStyle(isSel ? selectedStyle : defaultStyle);
        card.setOnMouseEntered(e -> { if (selected != p) card.setStyle(hoverStyle); });
        card.setOnMouseExited(e  -> { if (selected != p) card.setStyle(defaultStyle); });
        card.setOnMouseClicked(e -> {
            selected = p;
            cbStatut.setValue(p.getStatut_pr());
            tfJustificatif.setText(p.getJustificatif_path_pr() != null
                    ? p.getJustificatif_path_pr() : "");
            lblActionInfo.setText("📌 " + p.getStudent_nom() + " — " + p.getDate_presence_pr());
            renderCards(); // refresh selection state
        });

        // Header : statut badge + méthode
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label statut = new Label(StatusUtils.libelleStatutPresence(p.getStatut_pr()));
        statut.setStyle("-fx-background-radius: 10; -fx-padding: 3 10; "
                + "-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: white; "
                + "-fx-background-color: " + StatusUtils.colorStatutPresence(p.getStatut_pr()) + ";");
        Region spacer = new Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        boolean qr = p.getHeure_arrivee_pr() != null
                && ("PRESENT".equalsIgnoreCase(p.getStatut_pr())
                    || "RETARD".equalsIgnoreCase(p.getStatut_pr()));
        Label methode = new Label(qr ? "📷 QR" : "✋ Manuel");
        methode.setStyle("-fx-font-size: 9; -fx-text-fill: " + (qr ? "#a78bfa" : "#8a8aa3") + "; "
                + "-fx-font-weight: bold;");
        header.getChildren().addAll(statut, spacer, methode);

        // Nom étudiant
        Label nom = new Label(p.getStudent_nom() != null ? p.getStudent_nom() : "—");
        nom.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #f4f4f9;");
        nom.setWrapText(true);

        // Date + heure
        Label date = new Label("📅 "
                + (p.getDate_presence_pr() != null ? p.getDate_presence_pr().toString() : "—")
                + (p.getHeure_arrivee_pr() != null ? "   🕐 " + p.getHeure_arrivee_pr() : ""));
        date.setStyle("-fx-font-size: 11; -fx-text-fill: #06b6d4;");

        // Contexte (séance ou live)
        String ctx = (p.getSeance_label() != null && !p.getSeance_label().isBlank())
                ? "📚 " + p.getSeance_label()
                : (p.getLive_titre() != null ? "🎥 " + p.getLive_titre() : "—");
        Label contexte = new Label(ctx);
        contexte.setWrapText(true);
        contexte.setMaxWidth(252);
        contexte.setStyle("-fx-font-size: 10; -fx-text-fill: #a8a8c0;");

        // Commentaire si présent
        if (p.getCommentaire_pr() != null && !p.getCommentaire_pr().isBlank()) {
            Label com = new Label("💬 " + p.getCommentaire_pr());
            com.setWrapText(true);
            com.setMaxWidth(252);
            com.setStyle("-fx-font-size: 10; -fx-text-fill: #8a8aa3; -fx-font-style: italic;");
            card.getChildren().addAll(header, nom, date, contexte, com);
        } else {
            card.getChildren().addAll(header, nom, date, contexte);
        }
        return card;
    }

    private void initDetailTable() {
        colDetailDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate_presence_pr() != null
                        ? c.getValue().getDate_presence_pr().toString() : ""));
        colDetailContexte.setCellValueFactory(c -> {
            String s = c.getValue().getSeance_label();
            String l = c.getValue().getLive_titre();
            return new SimpleStringProperty(
                    (s != null && !s.isBlank()) ? s : (l != null ? l : "—"));
        });
        colDetailStatut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatut_pr()));
        colDetailNote.setCellValueFactory(c ->
                new SimpleStringProperty(StatusUtils.noteFromStatut(c.getValue().getStatut_pr())));

        colDetailNote.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null); setGraphic(null);
                if (empty || item == null) return;
                Label pill = new Label(item);
                pill.setStyle("-fx-background-radius: 14; -fx-padding: 4 14; "
                        + "-fx-font-weight: bold; -fx-text-fill: white; "
                        + "-fx-background-color: " + StatusUtils.colorForNote(item) + ";");
                setGraphic(pill);
            }
        });
        colDetailStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null); setGraphic(null);
                if (empty || item == null) return;
                Label main = new Label(StatusUtils.libelleStatutPresence(item));
                main.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StatusUtils.colorStatutPresence(item) + ";");
                Label sub = new Label("Enregistré automatiquement");
                sub.setStyle("-fx-font-size: 10; -fx-text-fill: #a8a8c0;");
                javafx.scene.layout.VBox vb = new javafx.scene.layout.VBox(2, main, sub);
                setGraphic(vb);
            }
        });

        tableDetail.setItems(detailData);
    }

    /* ===================== STAT CARDS ===================== */

    private void refreshStats() {
        try {
            LocalDate date = dpFilterDate.getValue() != null ? dpFilterDate.getValue() : LocalDate.now();
            SessionItem sel = cbFilterSeance.getValue();
            Integer seanceId = (sel != null && "SEANCE".equals(sel.type)) ? sel.id : null;
            Integer liveId   = (sel != null && "LIVE".equals(sel.type))   ? sel.id : null;

            AttendanceStatsService.Stats st = statsService.forDate(date, seanceId, liveId);
            lblStatPresents.setText(String.valueOf(st.totalPresent));
            lblStatAbsents.setText( String.valueOf(st.totalAbsent));
            lblStatRetards.setText( String.valueOf(st.totalRetard));
            lblStatTotal.setText(   String.valueOf(st.total));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================== FILTRES ===================== */

    @FXML
    public void onFiltrer() {
        try {
            LocalDate date = dpFilterDate.getValue();
            SessionItem sel = cbFilterSeance.getValue();
            List<Presence> all = presenceService.afficher();

            List<Presence> filtered = all.stream().filter(p -> {
                if (date != null && !date.equals(p.getDate_presence_pr())) return false;
                // Filtre par statut
                if (cbFilterStatut != null && cbFilterStatut.getValue() != null
                        && !"Tous".equals(cbFilterStatut.getValue())) {
                    if (!cbFilterStatut.getValue().equalsIgnoreCase(p.getStatut_pr())) return false;
                }
                if (sel != null && !"ALL".equals(sel.type)) {
                    if ("SEANCE".equals(sel.type)) {
                        return p.getSeance_id() != null && p.getSeance_id() == sel.id;
                    } else if ("LIVE".equals(sel.type)) {
                        return p.getLive_session_id() != null && p.getLive_session_id() == sel.id;
                    }
                }
                return true;
            }).collect(Collectors.toList());

            data.setAll(filtered);
            renderCards();
            refreshStats();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    public void onResetFiltres() {
        dpFilterDate.setValue(LocalDate.now());
        cbFilterSeance.getSelectionModel().selectFirst();
        if (cbFilterStatut != null) cbFilterStatut.setValue("Tous");
        loadAll();
        refreshStats();
    }

    /* ===================== MINI QR + CODE SESSION ===================== */

    private void refreshMiniQR(SessionItem sel) {
        if (sel == null || "ALL".equals(sel.type)) {
            imgMiniQR.setImage(null);
            lblCodeSession.setText("—");
            return;
        }
        try {
            // Génère un code lisible : 3 lettres matière + date du jour (ex: ALG-16-05-24)
            String code = generateCodeSession(sel);
            lblCodeSession.setText(code);

            // Génère un token réel pour le QR
            AttendanceToken token = "SEANCE".equals(sel.type)
                    ? tokenService.generateForSeance(sel.id)
                    : tokenService.generateForLiveSession(sel.id);

            String baseUrl = AppConfig.get("attendance.public.url", "").trim();
            if (baseUrl.isEmpty()) {
                int port = AttendanceHttpServer.getInstance().getActualPort();
                baseUrl = "http://" + AppConfig.getLocalIp() + ":" + port;
            }
            String url = baseUrl + "/attend?token=" + token.getToken();
            imgMiniQR.setImage(QRCodeUtil.generateFxImage(url, 200));
        } catch (Exception e) {
            e.printStackTrace();
            lblCodeSession.setText("Erreur QR");
        }
    }

    private String generateCodeSession(SessionItem sel) {
        String prefix = "GEN";
        if (sel.matiereNom != null && !sel.matiereNom.isBlank()) {
            prefix = sel.matiereNom.replaceAll("[^A-Za-z]", "")
                    .toUpperCase();
            if (prefix.length() > 3) prefix = prefix.substring(0, 3);
            if (prefix.isEmpty()) prefix = "GEN";
        }
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy"));
        return prefix + "-" + date;
    }

    /* ===================== FICHE ÉTUDIANT ===================== */

    private void refreshDetail(Student s) {
        try {
            AttendanceStatsService.Stats st = statsService.forStudent(s.getId_s());
            int presence = st.scoreOver100();
            int absence  = st.absenceRate();

            lblDetailNom.setText(s.getPrenom_s() + " " + s.getNom_s()
                    + "   ✔ " + presence + "%  ✘ " + absence + "%");
            lblDetailScore.setText(presence + " %");
            lblDetailPresents.setText(st.totalPresent   + " présents");
            lblDetailAbsents.setText( st.totalAbsent    + " absents");
            lblDetailRetards.setText( st.totalRetard    + " retards");
            lblDetailJustifies.setText(st.totalJustifie + " justifiés");

            String bg;
            switch (st.level()) {
                case "GOOD": bg = "#10b981"; break;
                case "WARN": bg = "#f59e0b"; break;
                default:     bg = "#dc2626"; break;
            }
            paneScorePill.setStyle(
                    "-fx-background-color: " + bg + "; -fx-background-radius: 30; "
                    + "-fx-min-width: 110; -fx-min-height: 42; "
                    + "-fx-max-width: 110; -fx-max-height: 42;");

            List<Presence> filtered = presenceService.afficher().stream()
                    .filter(p -> p.getStudent_id() == s.getId_s())
                    .collect(Collectors.toList());
            detailData.setAll(filtered);

            // PieChart dans la fiche étudiant
            if (pieDetailPresence != null) {
                pieDetailPresence.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Présents (" + st.totalPresent + ")", st.totalPresent),
                        new PieChart.Data("Absents (" + st.totalAbsent + ")", st.totalAbsent),
                        new PieChart.Data("Retards (" + st.totalRetard + ")", st.totalRetard),
                        new PieChart.Data("Justifiés (" + st.totalJustifie + ")", st.totalJustifie)
                ));
                pieDetailPresence.setLabelsVisible(false);
                pieDetailPresence.setLegendVisible(true);
                Platform.runLater(() -> {
                    String[] colors = {"#10b981", "#ef4444", "#f59e0b", "#6366f1"};
                    int idx = 0;
                    for (PieChart.Data d : pieDetailPresence.getData()) {
                        if (d.getNode() != null)
                            d.getNode().setStyle("-fx-pie-color: " + colors[idx] + ";");
                        idx++;
                    }
                });
            }
        } catch (Exception e) {
            showError("Erreur chargement fiche : " + e.getMessage());
        }
    }

    @FXML
    public void onRafraichirDetail() {
        Student s = cbDetailStudent.getValue();
        if (s != null) refreshDetail(s);
    }

    /* ===================== ACTIONS LIGNE ===================== */

    @FXML
    public void onModifier() {
        if (selected == null) { warn("Sélectionnez d'abord une présence."); return; }
        if (cbStatut.getValue() == null) { warn("Choisissez un nouveau statut."); return; }
        try {
            selected.setStatut_pr(cbStatut.getValue());
            selected.setJustificatif_path_pr(tfJustificatif.getText().trim());
            presenceService.modifier(selected);
            loadAll(); onReset(); refreshStats();
            info("Présence mise à jour.");
            if (cbDetailStudent.getValue() != null) refreshDetail(cbDetailStudent.getValue());
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML
    public void onSupprimer() {
        if (selected == null) { warn("Sélectionnez une présence."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la présence #" + selected.getId_pr() + " ?",
                ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    presenceService.supprimer(selected.getId_pr());
                    loadAll(); onReset(); refreshStats();
                    info("Supprimée.");
                    if (cbDetailStudent.getValue() != null) refreshDetail(cbDetailStudent.getValue());
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    @FXML
    public void onReset() {
        cbStatut.getSelectionModel().clearSelection();
        tfJustificatif.clear();
        selected = null;
        lblActionInfo.setText("Cliquez sur une carte pour voir les actions");
        renderCards();
    }

    /* ===================== SMS ===================== */

    @FXML
    public void onEnvoyerSMSDetail() {
        Student s = cbDetailStudent.getValue();
        if (s == null) { warn("Sélectionnez un étudiant."); return; }
        Presence lastAbsent = detailData.stream()
                .filter(p -> "ABSENT".equalsIgnoreCase(p.getStatut_pr()))
                .findFirst().orElse(null);
        if (lastAbsent == null) { info("Aucune absence enregistrée."); return; }
        envoyerSMS(lastAbsent);
    }

    private void envoyerSMS(Presence p) {
        try {
            Student s = studentService.findById(p.getStudent_id());
            if (s == null) { showError("Étudiant introuvable."); return; }
            if (s.getTel_parent() == null || s.getTel_parent().isBlank()) {
                warn("Aucun numéro parent pour cet étudiant.");
                return;
            }

            String nomParent = s.getNom_parent() != null ? s.getNom_parent() : "Parent";
            String message = "Bonjour " + nomParent + ", votre enfant "
                    + s.getPrenom_s() + " " + s.getNom_s()
                    + " a été marqué " + p.getStatut_pr()
                    + " le " + p.getDate_presence_pr() + ". - EduNova";

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Envoyer SMS");
            confirm.setHeaderText("→ " + s.getTel_parent() + " (" + nomParent + ")");
            TextArea ta = new TextArea(message);
            ta.setEditable(false); ta.setWrapText(true); ta.setPrefRowCount(5);
            confirm.getDialogPane().setContent(ta);
            confirm.getButtonTypes().setAll(
                    new ButtonType("📱 Envoyer", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("Annuler",    ButtonBar.ButtonData.CANCEL_CLOSE));
            confirm.showAndWait().ifPresent(bt -> {
                if (bt.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
                lblSmsStatus.setText("⏳ Envoi...");
                final String tel = s.getTel_parent();
                final String msg = message;
                new Thread(() -> {
                    TwilioSmsService.SmsResult res = twilioService.sendSms(tel, msg);
                    Platform.runLater(() -> {
                        if (res.success) lblSmsStatus.setText("✅ SMS envoyé");
                        else { lblSmsStatus.setText("❌ " + res.errorMsg); showError("Erreur SMS : " + res.errorMsg); }
                    });
                }, "sms-thread").start();
            });
        } catch (Exception e) { showError(e.getMessage()); }
    }

    /* ===================== POINTAGE QR ===================== */

    @FXML
    public void onPointageQR() {
        SessionItem sel = cbFilterSeance.getValue();
        if (sel == null || "ALL".equals(sel.type)) {
            warn("Sélectionnez une séance précise dans le filtre du haut, puis relancez le pointage.");
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Pointage.fxml"));
            javafx.scene.Parent root = loader.load();
            PointageController ctrl = loader.getController();
            if ("SEANCE".equals(sel.type)) ctrl.initForSeance(sel.id, sel.label);
            else                            ctrl.initForLiveSession(sel.id, sel.label);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Pointage QR Code — " + sel.label);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    /* ===================== RECHERCHE ===================== */

    @FXML
    public void onChercher() {
        try {
            String m = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
            if (m.isEmpty()) { loadAll(); return; }
            data.setAll(presenceService.chercher(m));
            renderCards();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    /* ===================== EXPORT ===================== */

    @FXML
    public void onExportCSV() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exporter les présences en CSV");
            fc.setInitialFileName("presences_" + LocalDate.now() + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                exportService.exportPresencesCSV(new ArrayList<>(data), file);
                info("✅ Export CSV réussi : " + file.getName());
            }
        } catch (Exception e) {
            showError("Erreur export CSV : " + e.getMessage());
        }
    }

    @FXML
    public void onExportPDF() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exporter les présences en PDF");
            fc.setInitialFileName("presences_" + LocalDate.now() + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                exportService.exportPresencesPDF(new ArrayList<>(data), file);
                info("✅ Export PDF réussi : " + file.getName());
                // Ouvre le PDF dans le viewer système
                try { java.awt.Desktop.getDesktop().open(file); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            showError("Erreur export PDF : " + e.getMessage());
        }
    }

    /* ===================== HELPERS ===================== */

    private void loadAll() {
        try {
            data.setAll(presenceService.afficher());
            renderCards();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void showError(String msg) { new Alert(Alert.AlertType.ERROR,      msg).showAndWait(); }
    private void warn(String msg)      { new Alert(Alert.AlertType.WARNING,     msg).showAndWait(); }
    private void info(String msg)      { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }

    /* ===================== Wrapper interne pour le ComboBox ===================== */

    /** Élément du ComboBox de filtre : peut être une Seance, une LiveSession, ou "ALL". */
    public static class SessionItem {
        public final int    id;
        public final String type;       // "SEANCE" / "LIVE" / "ALL"
        public final String label;
        public final String matiereNom;
        public final String dateOrJour;

        public SessionItem(int id, String type, String label, String matiere, String dateOrJour) {
            this.id = id; this.type = type; this.label = label;
            this.matiereNom = matiere; this.dateOrJour = dateOrJour;
        }

        @Override public String toString() { return label; }
    }
}
