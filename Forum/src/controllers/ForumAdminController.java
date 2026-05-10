package controllers;

import entities.ForumPost;
import entities.ForumPost.Statut;
import entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.ModerationService;
import services.SignalementService;
import services.ForumService;
import services.PostValidator;
import utils.AlertHelper;
import utils.SceneManager;
import utils.SessionManager;
import utils.ThemeManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ForumAdminController implements Initializable {

    // Stats
    @FXML private Label lbTotalPosts;
    @FXML private Label lbEnAttente;
    @FXML private Label lbApprouves;
    @FXML private Label lbRejetes;

    // Filtres
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbFiltreStatut;

    // Cartes
    @FXML private ScrollPane spPosts;
    @FXML private VBox       vbCards;

    // Header
    @FXML private Label lbUserInitiales;
    @FXML private Label lbUserEmail;
    @FXML private Label lbUserRole;

    // Nouveau post admin
    @FXML private Button   btnTheme;
    @FXML private VBox      panelNouveauPost;
    @FXML private TextField tfAdminTitre;
    @FXML private TextArea  taAdminContenu;
    @FXML private Label     lbAdminFeedback;

    private final ForumService   service   = new ForumService();
    private final ModerationService   moderationService   = new ModerationService();
    private final SignalementService   signalementService  = new SignalementService();
    private final PostValidator  validator = new PostValidator();
    /** Tracks which post checkboxes are selected for bulk actions. */
    private final List<Integer> selectedPostIds = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbFiltreStatut.getItems().setAll("Tous", "En attente", "Acceptés", "Refusés");
        cbFiltreStatut.setValue("Tous");
        cbFiltreStatut.setOnAction(e -> filtrer());

        User u = SessionManager.getCurrentUser();
        if (u == null) {
            // Session expirée — rediriger vers le login
            javafx.application.Platform.runLater(() -> {
                Stage stage = (Stage) vbCards.getScene().getWindow();
                SceneManager.naviguer("Login.fxml", stage, "EduNova — Connexion");
            });
            return;
        }
        lbUserInitiales.setText(u.getInitiales());
        lbUserEmail.setText(u.getUsername());
        lbUserRole.setText("Administrateur");
        charger();
        rafraichirStats();
        javafx.application.Platform.runLater(() -> {
            ThemeManager.appliquer(vbCards.getScene());
            majBoutonTheme();
        });
    }

    // ── Recherche / Filtres ──────────────────────────────────
    @FXML private void rechercher() { filtrer(); }
    @FXML private void resetRecherche() { tfRecherche.clear(); cbFiltreStatut.setValue("Tous"); charger(); }

    private void filtrer() {
        String kw  = tfRecherche.getText().trim();
        String fil = cbFiltreStatut.getValue();
        try {
            List<ForumPost> res;
            if (!kw.isEmpty()) {
                res = service.rechercher(kw);
            } else {
                res = switch (fil) {
                    case "En attente" -> service.getParStatut(Statut.EN_ATTENTE);
                    case "Acceptés"   -> service.getParStatut(Statut.ACCEPTE);
                    case "Refusés"    -> service.getParStatut(Statut.REFUSE);
                    default           -> service.getTous();
                };
            }
            afficherCartes(res);
        } catch (Exception e) { AlertHelper.erreur("Filtre", e.getMessage()); }
    }

    private void charger() {
        try { afficherCartes(service.getTous()); }
        catch (Exception e) { AlertHelper.erreur("Chargement", e.getMessage()); }
    }

    // ── Affichage en cartes ──────────────────────────────────
    private void afficherCartes(List<ForumPost> posts) {
        selectedPostIds.clear();
        vbCards.getChildren().clear();
        if (posts.isEmpty()) {
            Label empty = new Label("Aucun post trouvé.");
            empty.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:13px;-fx-padding:24;");
            vbCards.getChildren().add(empty);
            return;
        }
        // ── Bulk action bar ──
        HBox bulkBar = new HBox(10);
        bulkBar.setAlignment(Pos.CENTER_LEFT);
        bulkBar.setPadding(new Insets(6, 4, 6, 4));
        bulkBar.setStyle(
            "-fx-background-color:white;-fx-background-radius:10px;" +
            "-fx-border-color:#e5e7eb;-fx-border-radius:10px;-fx-border-width:1px;"
        );

        Label lbSel = new Label("0 sélectionné(s)");
        lbSel.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");

        String btnBase = "-fx-font-size:12px;-fx-cursor:hand;-fx-background-radius:6px;" +
                         "-fx-border-radius:6px;-fx-border-width:1px;-fx-padding:4 12;";

        Button btnAccAll = new Button("✓ Accepter sélection");
        btnAccAll.setStyle(btnBase + "-fx-background-color:transparent;-fx-border-color:#374151;-fx-text-fill:#374151;");
        btnAccAll.setDisable(true);

        Button btnRefAll = new Button("✕ Refuser sélection");
        btnRefAll.setStyle(btnBase + "-fx-background-color:transparent;-fx-border-color:#6b7280;-fx-text-fill:#6b7280;");
        btnRefAll.setDisable(true);

        Button btnDelAll = new Button("🗑 Supprimer sélection");
        btnDelAll.setStyle(btnBase + "-fx-background-color:transparent;-fx-border-color:#9ca3af;-fx-text-fill:#9ca3af;");
        btnDelAll.setDisable(true);

        // Update label and button states when selection changes
        Runnable updateBulkBar = () -> {
            int n = selectedPostIds.size();
            lbSel.setText(n + " sélectionné" + (n > 1 ? "s" : ""));
            btnAccAll.setDisable(n == 0);
            btnRefAll.setDisable(n == 0);
            btnDelAll.setDisable(n == 0);
        };

        btnAccAll.setOnAction(e -> {
            if (!AlertHelper.confirmer("Accepter", "Accepter " + selectedPostIds.size() + " post(s) ?")) return;
            selectedPostIds.forEach(id -> { try { service.approuver(id); } catch (Exception ex) { /* ignore */ } });
            charger(); rafraichirStats();
        });
        btnRefAll.setOnAction(e -> {
            if (!AlertHelper.confirmer("Refuser", "Refuser " + selectedPostIds.size() + " post(s) ?")) return;
            selectedPostIds.forEach(id -> { try { service.rejeter(id); } catch (Exception ex) { /* ignore */ } });
            charger(); rafraichirStats();
        });
        btnDelAll.setOnAction(e -> {
            if (!AlertHelper.confirmer("Supprimer", "Supprimer définitivement " + selectedPostIds.size() + " post(s) ?")) return;
            selectedPostIds.forEach(id -> {
                try {
                    signalementService.supprimerSignalements(id);
                    service.supprimer(id);
                } catch (Exception ex) { /* ignore */ }
            });
            charger(); rafraichirStats();
        });

        bulkBar.getChildren().addAll(lbSel, btnAccAll, btnRefAll, btnDelAll);
        vbCards.getChildren().add(bulkBar);

        for (ForumPost p : posts) vbCards.getChildren().add(creerCarteAdmin(p, selectedPostIds, updateBulkBar));
    }

    private VBox creerCarteAdmin(ForumPost p, List<Integer> selectedIds, Runnable onSelectionChange) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;-fx-border-color:#e5e7eb;-fx-border-radius:16px;-fx-border-width:1px;");
        card.setPadding(new Insets(16, 20, 14, 20));
        card.setMaxWidth(Double.MAX_VALUE);

        // ── Header : checkbox + avatar + nom + rôle + date + badge statut ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        CheckBox cb = new CheckBox();
        cb.setStyle("-fx-cursor:hand;");
        cb.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) selectedIds.add(p.getId());
            else          selectedIds.remove((Integer) p.getId());
            onSelectionChange.run();
        });

        Label avatar = new Label(p.getInitiales());
        avatar.setStyle("-fx-background-color:" + roleColor(p.getAuteurRole()) + ";" +
                        "-fx-text-fill:white;-fx-font-weight:700;-fx-font-size:13px;" +
                        "-fx-background-radius:50%;-fx-min-width:40px;-fx-min-height:40px;" +
                        "-fx-pref-width:40px;-fx-pref-height:40px;-fx-alignment:center;");

        VBox infos = new VBox(2);
        Label nom  = new Label(p.getAuteurNom());
        nom.setStyle("-fx-font-weight:700;-fx-font-size:13px;-fx-text-fill:#1f2937;");
        Label sub  = new Label(p.getRoleIcon() + " " + capitalize(p.getAuteurRole()) + "  •  " + p.getDateFormatee());
        sub.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        infos.getChildren().addAll(nom, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge signalements
        try {
            int nbSig = signalementService.compterSignalements(p.getId());
            if (nbSig > 0) {
                Label lbSig = new Label("🚩 " + nbSig + " signalement" + (nbSig > 1 ? "s" : ""));
                lbSig.setStyle("-fx-background-color:#fef2f2;-fx-text-fill:#ef4444;" +
                    "-fx-font-size:10px;-fx-font-weight:700;-fx-background-radius:20px;" +
                    "-fx-border-color:#fecaca;-fx-border-radius:20px;-fx-border-width:1px;" +
                    "-fx-padding:2 8;-fx-cursor:hand;");
                // Tooltip avec les raisons
                lbSig.setOnMouseClicked(e -> {
                    AlertHelper.info("Signalements",
                        "Ce post a été signalé " + nbSig + " fois.\n" +
                        "Vous pouvez l'accepter, le refuser ou le supprimer.");
                });
                header.getChildren().add(lbSig);
            }
        } catch (Exception ignored) {}

        Label badge = new Label(p.getStatutLabel());
        String badgeStyle = switch (p.getStatut()) {
            case EN_ATTENTE -> "-fx-background-color:#f5f3ff;-fx-text-fill:#7c3aed;";
            case ACCEPTE    -> "-fx-background-color:#f0fdf4;-fx-text-fill:#374151;";
            case REFUSE     -> "-fx-background-color:#f9fafb;-fx-text-fill:#6b7280;";
        };
        badge.setStyle(badgeStyle + "-fx-font-size:11px;-fx-font-weight:700;" +
                       "-fx-background-radius:20px;-fx-border-radius:20px;-fx-padding:3 10;");

        header.getChildren().addAll(cb, avatar, infos, spacer, badge);

        // ── Titre ──
        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:#111827;");
        titre.setWrapText(true);

        // ── Contenu ──
        String contenuTexte = p.getContenu().length() > 200
            ? p.getContenu().substring(0, 200) + "…" : p.getContenu();
        Label contenu = new Label(contenuTexte);
        contenu.setStyle("-fx-font-size:12px;-fx-text-fill:#4b5563;-fx-line-spacing:3;");
        contenu.setWrapText(true);

        // ── Séparateur ──
        Separator sep = new Separator();

        // ── Zone résultat IA (cachée par défaut) ──
        Label lbIA = new Label();
        lbIA.setWrapText(true);
        lbIA.setVisible(false);
        lbIA.setManaged(false);
        lbIA.setStyle("-fx-font-size:12px;-fx-padding:8 12;-fx-background-radius:8px;-fx-background-color:#f5f3ff;-fx-text-fill:#4c1d95;");

        // ── Boutons d'action (style simple/neutre) ──
        String btnBase = "-fx-background-color:transparent;-fx-border-width:1px;-fx-border-radius:6px;" +
                         "-fx-background-radius:6px;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:5 14;";

        Button btnAccepter = new Button("✓  Accepter");
        btnAccepter.setStyle(btnBase + "-fx-border-color:#374151;-fx-text-fill:#374151;");

        Button btnRefuser = new Button("✕  Refuser");
        btnRefuser.setStyle(btnBase + "-fx-border-color:#6b7280;-fx-text-fill:#6b7280;");

        Button btnSupprimer = new Button("🗑  Supprimer");
        btnSupprimer.setStyle(btnBase + "-fx-border-color:#9ca3af;-fx-text-fill:#9ca3af;");

        Button btnIA = new Button("🤖  Analyse IA");
        btnIA.setStyle(btnBase + "-fx-border-color:#7c3aed;-fx-text-fill:#7c3aed;");

        HBox actions = new HBox(8, btnAccepter, btnRefuser, btnSupprimer, btnIA);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, titre, contenu, sep, lbIA, actions);

        // ── Handlers ──
        btnAccepter.setOnAction(e -> {
            if (p.getStatut() == Statut.ACCEPTE) { AlertHelper.info("Info","Post déjà accepté."); return; }
            try {
                service.approuver(p.getId());
                AlertHelper.info("Succès","Post accepté — visible par les membres.");
                charger(); rafraichirStats();
            } catch (Exception ex) { AlertHelper.erreur("Erreur", ex.getMessage()); }
        });

        btnRefuser.setOnAction(e -> {
            if (!AlertHelper.confirmer("Refuser","Refuser ce post de " + p.getAuteurNom() + " ?")) return;
            try { service.rejeter(p.getId()); charger(); rafraichirStats(); }
            catch (Exception ex) { AlertHelper.erreur("Erreur", ex.getMessage()); }
        });

        btnSupprimer.setOnAction(e -> {
            if (!AlertHelper.confirmer("Supprimer","Supprimer définitivement ce post ?")) return;
            try {
                signalementService.supprimerSignalements(p.getId());
                service.supprimer(p.getId());
                charger(); rafraichirStats();
            } catch (Exception ex) { AlertHelper.erreur("Erreur", ex.getMessage()); }
        });

        // ── Analyse IA en arrière-plan ──
        btnIA.setOnAction(e -> {
            btnIA.setText("⏳  Analyse...");
            btnIA.setDisable(true);
            lbIA.setVisible(false);
            lbIA.setManaged(false);

            Thread thread = new Thread(() -> {
                try {
                    ModerationService.AnalyseAdmin result = moderationService.analyserPourAdmin(p.getTitre(), p.getContenu());
                    Platform.runLater(() -> {
                        String bg = result.isAccepte()
                            ? "-fx-background-color:#f0fdf4;-fx-text-fill:#166534;"
                            : "-fx-background-color:#fef2f2;-fx-text-fill:#991b1b;";
                        lbIA.setText("🤖 " + result.getResume());
                        lbIA.setStyle("-fx-font-size:12px;-fx-padding:8 12;-fx-background-radius:8px;" + bg);
                        lbIA.setVisible(true);
                        lbIA.setManaged(true);
                        btnIA.setText("🤖  Analyse IA");
                        btnIA.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        lbIA.setText("❌ Erreur API : " + ex.getMessage() + "\nVérifiez HF_TOKEN dans ModerationService.java");
                        lbIA.setStyle("-fx-font-size:11px;-fx-padding:8 12;-fx-background-radius:8px;" +
                                      "-fx-background-color:#fef9f9;-fx-text-fill:#6b7280;");
                        lbIA.setVisible(true);
                        lbIA.setManaged(true);
                        btnIA.setText("🤖  Analyse IA");
                        btnIA.setDisable(false);
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        });

        return card;
    }

    // ── Nouveau post admin ───────────────────────────────────
    @FXML private void toggleTheme() {
        boolean dark = ThemeManager.toggle(vbCards.getScene());
        majBoutonTheme();
    }

    private void majBoutonTheme() {
        if (btnTheme != null)
            btnTheme.setText(ThemeManager.isDark() ? "☀  Mode clair" : "🌙  Mode sombre");
    }

    @FXML private void toggleNouveauPost() {
        boolean show = !panelNouveauPost.isVisible();
        panelNouveauPost.setVisible(show);
        panelNouveauPost.setManaged(show);
        if (!show) { tfAdminTitre.clear(); taAdminContenu.clear(); lbAdminFeedback.setText(""); }
    }

    @FXML private void publierAdmin() {
        String titre   = tfAdminTitre.getText().trim();
        String contenu = taAdminContenu.getText().trim();

        // Validation métier
        PostValidator.ResultatValidation rv = validator.valider(titre, contenu);
        if (!rv.valide) {
            lbAdminFeedback.setStyle("-fx-text-fill:#7c3aed;-fx-font-size:12px;");
            lbAdminFeedback.setText("⚠ " + rv.message);
            return;
        }
        try {
            User admin = SessionManager.getCurrentUser();
            if (admin == null) {
                lbAdminFeedback.setStyle("-fx-text-fill:#ef4444;-fx-font-size:12px;");
                lbAdminFeedback.setText("Session expirée. Veuillez vous reconnecter.");
                return;
            }
            ForumPost p = new ForumPost(titre, contenu, null, admin.getId(), admin.getNomComplet(), "admin");
            service.publierAdmin(p);
            lbAdminFeedback.setStyle("-fx-text-fill:#374151;-fx-font-size:12px;");
            lbAdminFeedback.setText("✓ Post publié.");
            tfAdminTitre.clear(); taAdminContenu.clear();
            charger(); rafraichirStats();
        } catch (Exception e) {
            lbAdminFeedback.setStyle("-fx-text-fill:#6b7280;-fx-font-size:12px;");
            lbAdminFeedback.setText("Erreur : " + e.getMessage());
        }
    }

    // ── Déconnexion ──────────────────────────────────────────
    @FXML private void seDeconnecter() {
        SessionManager.logout();
        SceneManager.naviguer("Login.fxml", (Stage) vbCards.getScene().getWindow(), "EduNova — Connexion");
    }

    // ── Stats ────────────────────────────────────────────────
    private void rafraichirStats() {
        try {
            lbTotalPosts.setText(String.valueOf(service.compterTous()));
            lbEnAttente.setText(String.valueOf(service.compterParStatut(Statut.EN_ATTENTE)));
            lbApprouves.setText(String.valueOf(service.compterParStatut(Statut.ACCEPTE)));
            lbRejetes.setText(String.valueOf(service.compterParStatut(Statut.REFUSE)));
        } catch (Exception ignored) {}
    }

    // ── Utils ────────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String roleColor(String role) {
        if (role == null) return "#6b7280";
        return switch (role.toLowerCase()) {
            case "admin"   -> "#7c3aed";
            case "student" -> "#2563eb";
            case "parent"  -> "#059669";
            case "teacher" -> "#d97706";
            default        -> "#6b7280";
        };
    }
}
