package controllers;

import entities.Comment;
import entities.ForumPost;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CommentService;
import services.CommentLikeService;
import services.ChatService;
import services.ModerationService;
import services.TranslationService;
import services.ForumService;
import services.LikeService;
import utils.AlertHelper;
import utils.SceneManager;
import utils.SessionManager;
import utils.ThemeManager;
import services.ReputationService;
import services.SignalementService;
import services.NotificationService;
import entities.Notification;
import entities.ChatMessage;

import java.io.File;
import java.awt.Desktop;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import services.UserService;

public class ForumMembreController implements Initializable {

    @FXML private Label    lbInitiales;
    @FXML private Label    lbNomUser;
    @FXML private Label    lbRoleUser;
    @FXML private Label    lbNbMesPosts;
    @FXML private Label    lbBadgeReputation;
    @FXML private Label    lbPointsReputation;
    @FXML private Button   btnTheme;
    @FXML private Label    lbBadge;
    @FXML private VBox     vbFil;
    @FXML private ScrollPane spFil;
    @FXML private TextField  tfTitre;
    @FXML private TextArea   taContenu;
    @FXML private TextField  tfPhotoPath;
    @FXML private Label      lbFeedback;
    @FXML private TextField  tfRecherche;
    @FXML private VBox       vbMesPosts;
    @FXML private Label      lbInitialesPost;
    @FXML private VBox       panelContenu;
    @FXML private VBox       panelNouveauPost;
    @FXML private VBox       vbContacts;
    @FXML private Label      lbChatUnread;

    private final ForumService       forumService       = new ForumService();
    private final ReputationService  reputationService  = new ReputationService();
    private final SignalementService signalementService = new SignalementService();
    private final NotificationService notificationService = new NotificationService();
    private final TranslationService translationService = new TranslationService();
    private final ModerationService  moderationService  = new ModerationService();
    private final LikeService        likeService        = new LikeService();
    private final CommentService     commentService     = new CommentService();
    private final CommentLikeService commentLikeService = new CommentLikeService();
    private final UserService        userService        = new UserService();
    private final ChatService        chatService        = new ChatService();
    private User   moi;
    private String photoChoisie;
    private final Map<Integer, String> reactionMap = new HashMap<>();
    private static final int PAGE_SIZE = 5;
    private int currentPage = 0;
    private int totalApprouves = 0;
    private Timeline presenceTimeline;
    // Open chat windows keyed by partner userId
    private final Map<Integer, javafx.stage.Stage> chatWindows = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        moi = SessionManager.getCurrentUser();
        if (moi == null) {
            // Session expirée ou accès direct — rediriger vers le login
            javafx.application.Platform.runLater(() -> {
                Stage stage = (Stage) vbFil.getScene().getWindow();
                utils.SceneManager.naviguer("Login.fxml", stage, "EduNova");
            });
            return;
        }
        lbInitiales.setText(moi.getInitiales());
        lbNomUser.setText(moi.getNomComplet());
        lbRoleUser.setText(capitalize(moi.getRole()));
        if (lbInitialesPost != null) lbInitialesPost.setText(moi.getInitiales());
        lbFeedback.setText("");
        chargerFil();
        chargerMesPosts();
        chargerReputation();
        chargerNotifications();
        // Chat: mark presence and start polling
        chatService.updatePresence(moi.getId());
        chargerContacts();
        demarrerPollingPresence();
        javafx.application.Platform.runLater(() -> {
            ThemeManager.appliquer(vbFil.getScene());
            majBoutonTheme();
        });
    }

    @FXML private void rechercher() {
        String kw = tfRecherche.getText().trim();
        try {
            List<ForumPost> res = kw.isEmpty() ? forumService.getApprouves() : forumService.rechercherApprouves(kw);
            afficherFil(res);
        } catch (Exception e) { AlertHelper.erreur("Recherche", e.getMessage()); }
    }

    @FXML private void resetRecherche() { tfRecherche.clear(); chargerFil(); }

    /** Opens the post form panel (triggered by "Quoi de neuf?" button). */
    @FXML private void ouvrirFormulairePost() {
        if (panelContenu != null) {
            panelContenu.setVisible(true);
            panelContenu.setManaged(true);
        }
        if (tfTitre != null) tfTitre.requestFocus();
    }

    /** Cancels and collapses the post form panel. */
    @FXML private void annulerPost() {
        if (panelContenu != null) {
            panelContenu.setVisible(false);
            panelContenu.setManaged(false);
            panelContenu.getChildren().removeIf(n -> "photo-preview".equals(n.getUserData()));
        }
        if (tfTitre    != null) tfTitre.clear();
        if (taContenu  != null) taContenu.clear();
        if (tfPhotoPath != null) tfPhotoPath.clear();
        if (lbFeedback != null) lbFeedback.setText("");
        photoChoisie = null;
    }

    @FXML private void choisirPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.gif"));
        File f = fc.showOpenDialog(tfTitre.getScene().getWindow());
        if (f != null) {
            photoChoisie = f.getAbsolutePath();
            tfPhotoPath.setText(f.getName());
            // ── Show inline preview ──
            afficherPreviewPhoto(f);
        }
    }

    private void afficherPreviewPhoto(File f) {
        if (panelContenu == null) return;
        // Remove old preview if any
        panelContenu.getChildren().removeIf(n -> "photo-preview".equals(n.getUserData()));
        try {
            ImageView iv = new ImageView(new Image(f.toURI().toString()));
            iv.setFitWidth(260);
            iv.setPreserveRatio(true);
            iv.setStyle("-fx-background-radius:8px;");
            iv.setUserData("photo-preview");

            Button btnRetirer = new Button("✕ Retirer");
            btnRetirer.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:#ef4444;-fx-font-size:11px;" +
                "-fx-cursor:hand;-fx-border-color:#fecaca;-fx-border-radius:6px;" +
                "-fx-background-radius:6px;-fx-padding:2 8;"
            );
            btnRetirer.setOnAction(e -> {
                panelContenu.getChildren().removeIf(n -> "photo-preview".equals(n.getUserData()));
                tfPhotoPath.clear();
                photoChoisie = null;
            });

            VBox previewBox = new VBox(4, iv, btnRetirer);
            previewBox.setUserData("photo-preview");
            previewBox.setAlignment(Pos.TOP_LEFT);

            // Insert before the lbFeedback label
            int idx = panelContenu.getChildren().indexOf(lbFeedback);
            if (idx >= 0) panelContenu.getChildren().add(idx, previewBox);
            else          panelContenu.getChildren().add(previewBox);
        } catch (Exception ignored) {}
    }

    @FXML private void publier() {
        String titre   = tfTitre.getText().trim();
        String contenu = taContenu.getText().trim();
        if (titre.isEmpty() || contenu.isEmpty()) { feedback("Titre et contenu obligatoires.", false); return; }

        // Désactiver le bouton pendant l'analyse IA
        feedback("🤖 Analyse du contenu en cours...", true);

        Thread thread = new Thread(() -> {
            try {
                // ── Modération IA avant soumission ──
                ModerationService.ResultatModeration mod = moderationService.moderer(titre, contenu);

                javafx.application.Platform.runLater(() -> {
                    if (!mod.isOk()) {
                        // Contenu refusé par l'IA
                        String msg = mod.getIcone() + " Publication refusée automatiquement.\n"
                                   + "Raison : " + mod.explication;
                        feedback(msg, false);
                        return;
                    }
                    // Contenu OK → soumettre
                    try {
                        ForumPost p = new ForumPost(titre, contenu, photoChoisie,
                            moi.getId(), moi.getNomComplet(), moi.getRole());
                        forumService.soumettre(p);
                        feedback("✅ Post soumis ! En attente de validation par l'admin.", true);
                        tfTitre.clear(); taContenu.clear(); tfPhotoPath.clear(); photoChoisie = null;
                        annulerPost();
                        chargerMesPosts();
                    } catch (Exception ex) { feedback("Erreur : " + ex.getMessage(), false); }
                });

            } catch (Exception ex) {
                // Si l'API IA echoue → afficher l'erreur, ne pas soumettre
                javafx.application.Platform.runLater(() -> {
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("404")) {
                        feedback("⚠ Service IA indisponible (modèle introuvable). Réessayez.", false);
                    } else if (msg != null && msg.contains("401")) {
                        feedback("⚠ Clé HuggingFace invalide. Vérifiez HF_TOKEN.", false);
                    } else if (msg != null && msg.contains("503")) {
                        feedback("⚠ Modèle IA en cours de chargement, réessayez dans 20 secondes.", false);
                    } else {
                        feedback("⚠ Service IA indisponible : " + msg + ". Réessayez.", false);
                    }
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void chargerFil() {
        currentPage = 0;
        vbFil.getChildren().clear();
        try {
            totalApprouves = forumService.compterApprouves();
            List<ForumPost> posts = forumService.getApprouvesPagines(0, PAGE_SIZE);
            if (posts.isEmpty()) {
                Label empty = new Label("Aucun post pour le moment.");
                empty.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:14px;");
                empty.setPadding(new Insets(40));
                vbFil.getChildren().add(empty);
            } else {
                for (ForumPost p : posts) vbFil.getChildren().add(creerCartePost(p));
                if (totalApprouves > PAGE_SIZE) vbFil.getChildren().add(creerBoutonChargerPlus());
            }
        } catch (Exception e) { AlertHelper.erreur("Chargement", e.getMessage()); }
    }

    private void chargerPageSuivante() {
        currentPage++;
        try {
            List<ForumPost> posts = forumService.getApprouvesPagines(currentPage, PAGE_SIZE);
            // Remove the "Charger plus" button (always last child)
            if (!vbFil.getChildren().isEmpty()) {
                vbFil.getChildren().remove(vbFil.getChildren().size() - 1);
            }
            for (ForumPost p : posts) vbFil.getChildren().add(creerCartePost(p));
            int loaded = (currentPage + 1) * PAGE_SIZE;
            if (loaded < totalApprouves) vbFil.getChildren().add(creerBoutonChargerPlus());
        } catch (Exception e) { AlertHelper.erreur("Chargement", e.getMessage()); }
    }

    private javafx.scene.Node creerBoutonChargerPlus() {
        Button btn = new Button("Charger plus de posts ↓");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
            "-fx-background-color:white;-fx-text-fill:#7c3aed;-fx-font-size:13px;" +
            "-fx-font-weight:600;-fx-background-radius:12px;-fx-border-color:#e5e7eb;" +
            "-fx-border-radius:12px;-fx-border-width:1px;-fx-cursor:hand;-fx-padding:12 0;"
        );
        btn.setOnAction(e -> chargerPageSuivante());
        return btn;
    }

    private void afficherFil(List<ForumPost> posts) {
        // Used only for search results (no pagination needed)
        vbFil.getChildren().clear();
        if (posts.isEmpty()) {
            Label empty = new Label("Aucun post trouvé.");
            empty.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:14px;");
            empty.setPadding(new Insets(40));
            vbFil.getChildren().add(empty);
            return;
        }
        for (ForumPost p : posts) vbFil.getChildren().add(creerCartePost(p));
    }

    private VBox creerCartePost(ForumPost p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white;-fx-background-radius:16px;-fx-border-color:#e5e7eb;-fx-border-radius:16px;-fx-border-width:1px;");
        card.setPadding(new Insets(18, 20, 14, 20));
        card.setMaxWidth(Double.MAX_VALUE);

        // ── Header auteur ──
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(p.getInitiales());
        avatar.setStyle("-fx-background-color:" + roleColor(p.getAuteurRole()) + ";" +
                        "-fx-text-fill:white;-fx-font-weight:700;-fx-font-size:14px;" +
                        "-fx-background-radius:50%;-fx-min-width:44px;-fx-min-height:44px;" +
                        "-fx-pref-width:44px;-fx-pref-height:44px;-fx-alignment:center;-fx-cursor:hand;");
        // ── Profile popup on avatar click ──
        avatar.setOnMouseClicked(e -> afficherProfilUtilisateur(p.getAuteurId(), p.getAuteurNom(), p.getAuteurRole()));
        VBox infos = new VBox(2);
        Label nomLabel  = new Label(p.getAuteurNom());
        nomLabel.setStyle("-fx-font-weight:700;-fx-font-size:14px;-fx-text-fill:#1f2937;");
        Label roleDate  = new Label(p.getRoleIcon() + " " + capitalize(p.getAuteurRole()) + "  •  " + p.getDateFormatee());
        roleDate.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        infos.getChildren().addAll(nomLabel, roleDate);
        header.getChildren().addAll(avatar, infos);

        // ── Titre ──
        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:#0f1419;");
        titre.setWrapText(true);

        // ── Contenu ──
        Label contenu = new Label(p.getContenu());
        contenu.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-line-spacing:4;");
        contenu.setWrapText(true);
        contenu.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, titre, contenu);

        // ── Image si presente ──
        if (p.getPhotoPath() != null && !p.getPhotoPath().isBlank()) {
            File f = new File(p.getPhotoPath());
            if (f.exists()) {
                try {
                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setFitWidth(500); iv.setPreserveRatio(true);
                    card.getChildren().add(iv);
                } catch (Exception ignored) {}
            }
        }

        // ── Séparateur ──
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#f3f4f6;");
        card.getChildren().add(sep);

        // ── Barre d'actions : Réactions | Commenter | Partager ──
        try {
            int     nbLikes   = likeService.countLikes(p.getId());
            boolean jaime     = likeService.hasLiked(p.getId(), moi.getId());
            int     nbComments = commentService.countComments(p.getId());

            // ── Reaction popup — colored circles (Facebook style) ──
            // reactions array: { emoji, label, circle-bg, text-color, active-btn-bg }
            String[][] reactions = {
                {"👍", "J'aime",  "#1877f2", "white",   "#e7f0fd"},
                {"❤",  "J'adore", "#f33e58", "white",   "#fde8ec"},
                {"😂", "Haha",    "#f7b928", "#7a5c00", "#fef9e7"},
                {"😮", "Wow",     "#f7b928", "#7a5c00", "#fef9e7"},
                {"😢", "Triste",  "#6eb5f5", "white",   "#e8f4fd"},
                {"😡", "Grrr",    "#e9710f", "white",   "#fef0e6"},
            };

            String curEmoji  = jaime ? reactionMap.getOrDefault(p.getId(), "👍") : null;
            String curLabel  = curEmoji == null ? "J'aime" : reactionLabel(curEmoji, reactions);
            String curColor  = curEmoji == null ? "#6b7280" : reactionColor(curEmoji, reactions);
            String curBg     = curEmoji == null ? "#f9fafb" : reactionBg(curEmoji, reactions);

            // ── Main reaction button ──
            Button btnReact = new Button(
                (curEmoji != null ? curEmoji + " " : "👍 ") + curLabel + "  " + nbLikes
            );
            btnReact.setStyle(
                "-fx-background-color:" + curBg + ";-fx-text-fill:" + curColor + ";" +
                "-fx-font-size:13px;-fx-font-weight:600;" +
                "-fx-background-radius:20px;-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 16;"
            );

            // ── Popup: white pill with colored circle buttons ──
            HBox emojiPopup = new HBox(6);
            emojiPopup.setAlignment(Pos.CENTER_LEFT);
            emojiPopup.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:30px;" +
                "-fx-border-color:#e5e7eb;-fx-border-radius:30px;-fx-border-width:1px;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.22),14,0,0,4);" +
                "-fx-padding:8 12;"
            );
            emojiPopup.setVisible(false);
            emojiPopup.setManaged(false);

            for (String[] r : reactions) {
                final String emoji    = r[0];
                final String label    = r[1];
                final String circleBg = r[2];
                final String txtColor = r[3];
                final String activeBg = r[4];

                // Colored circle label (not a button — styled as a circle)
                Label circle = new Label(emoji);
                circle.setStyle(
                    "-fx-background-color:" + circleBg + ";" +
                    "-fx-text-fill:" + txtColor + ";" +
                    "-fx-font-size:18px;" +
                    "-fx-background-radius:50%;" +
                    "-fx-min-width:40px;-fx-min-height:40px;" +
                    "-fx-pref-width:40px;-fx-pref-height:40px;" +
                    "-fx-alignment:center;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),4,0,0,2);"
                );

                // Tooltip
                Tooltip tip = new Tooltip(label);
                tip.setStyle("-fx-font-size:11px;");
                Tooltip.install(circle, tip);

                // Hover: scale up by increasing font + size
                circle.setOnMouseEntered(ev -> circle.setStyle(
                    "-fx-background-color:" + circleBg + ";" +
                    "-fx-text-fill:" + txtColor + ";" +
                    "-fx-font-size:22px;" +
                    "-fx-background-radius:50%;" +
                    "-fx-min-width:46px;-fx-min-height:46px;" +
                    "-fx-pref-width:46px;-fx-pref-height:46px;" +
                    "-fx-alignment:center;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.25),6,0,0,3);"
                ));
                circle.setOnMouseExited(ev -> circle.setStyle(
                    "-fx-background-color:" + circleBg + ";" +
                    "-fx-text-fill:" + txtColor + ";" +
                    "-fx-font-size:18px;" +
                    "-fx-background-radius:50%;" +
                    "-fx-min-width:40px;-fx-min-height:40px;" +
                    "-fx-pref-width:40px;-fx-pref-height:40px;" +
                    "-fx-alignment:center;-fx-cursor:hand;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),4,0,0,2);"
                ));

                // Click
                circle.setOnMouseClicked(ev -> {
                    emojiPopup.setVisible(false);
                    emojiPopup.setManaged(false);
                    try {
                        boolean nowLiked = likeService.hasLiked(p.getId(), moi.getId());
                        if (!nowLiked) likeService.like(p.getId(), moi.getId());
                        reactionMap.put(p.getId(), emoji);
                        int nb = likeService.countLikes(p.getId());
                        btnReact.setText(emoji + " " + label + "  " + nb);
                        btnReact.setStyle(
                            "-fx-background-color:" + activeBg + ";-fx-text-fill:" + circleBg + ";" +
                            "-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:20px;-fx-border-color:" + activeBg + ";" +
                            "-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 16;"
                        );
                        chargerReputation();
                    } catch (Exception ex) { AlertHelper.erreur("Réaction", ex.getMessage()); }
                });

                emojiPopup.getChildren().add(circle);
            }

            // Show popup on hover over main button
            btnReact.setOnMouseEntered(ev -> {
                emojiPopup.setVisible(true);
                emojiPopup.setManaged(true);
            });
            emojiPopup.setOnMouseExited(ev -> {
                emojiPopup.setVisible(false);
                emojiPopup.setManaged(false);
            });

            // ── Click main button = toggle like (👍) ──
            btnReact.setOnAction(ev -> {
                try {
                    boolean liked = likeService.toggleLike(p.getId(), moi.getId());
                    int nb = likeService.countLikes(p.getId());
                    if (liked) {
                        String em = reactionMap.getOrDefault(p.getId(), "👍");
                        String lb = reactionLabel(em, reactions);
                        String co = reactionColor(em, reactions);
                        String bg2 = reactionBg(em, reactions);
                        btnReact.setText(em + " " + lb + "  " + nb);
                        btnReact.setStyle(
                            "-fx-background-color:" + bg2 + ";-fx-text-fill:" + co + ";" +
                            "-fx-font-size:13px;-fx-font-weight:700;" +
                            "-fx-background-radius:20px;-fx-border-color:" + bg2 + ";" +
                            "-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 16;"
                        );
                    } else {
                        reactionMap.remove(p.getId());
                        btnReact.setText("👍 J'aime  " + nb);
                        btnReact.setStyle(
                            "-fx-background-color:#f9fafb;-fx-text-fill:#6b7280;" +
                            "-fx-font-size:13px;-fx-font-weight:600;" +
                            "-fx-background-radius:20px;-fx-border-color:#e5e7eb;" +
                            "-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 16;"
                        );
                    }
                    chargerReputation();
                } catch (Exception ex) { AlertHelper.erreur("Like", ex.getMessage()); }
            });

            // Section commentaires (cachée par défaut)
            VBox sectionComments = new VBox(8);
            sectionComments.setVisible(false);
            sectionComments.setManaged(false);
            sectionComments.setPadding(new Insets(8, 0, 0, 0));

            // Bouton Commenter
            Button btnComment = new Button("💬 " + nbComments + " Commentaire" + (nbComments > 1 ? "s" : ""));
            btnComment.setStyle("-fx-background-color:#f9fafb;-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-font-weight:600;-fx-background-radius:20px;-fx-border-color:#e5e7eb;-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 14;");

            // Barre d'actions (emoji popup sits above it via VBox stacking)
            VBox reactionWrapper = new VBox(0, emojiPopup);
            reactionWrapper.setAlignment(Pos.BOTTOM_LEFT);

            HBox actionBar = new HBox(10, btnReact, btnComment);
            actionBar.setAlignment(Pos.CENTER_LEFT);

            // ── Action Commenter ──
            btnComment.setOnAction(e -> {
                boolean show = !sectionComments.isVisible();
                sectionComments.setVisible(show);
                sectionComments.setManaged(show);
                if (show) chargerCommentaires(sectionComments, p, btnComment);
            });

            // ── Bouton Signaler (visible seulement sur les posts des autres) ──
            if (moi.getId() != p.getAuteurId()) {
                try {
                    boolean dejaSignale = signalementService.aDejaSignale(p.getId(), moi.getId());
                    Button btnSignaler = new Button(dejaSignale ? "🚩 Signalé" : "🚩 Signaler");
                    btnSignaler.setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:" + (dejaSignale ? "#ef4444" : "#9ca3af") + ";" +
                        "-fx-font-size:11px;-fx-cursor:hand;-fx-border-color:" + (dejaSignale ? "#fecaca" : "#e5e7eb") + ";" +
                        "-fx-border-radius:20px;-fx-background-radius:20px;-fx-padding:5 12;");
                    if (dejaSignale) btnSignaler.setDisable(true);
                    actionBar.getChildren().add(btnSignaler);

                    btnSignaler.setOnAction(e -> {
                        // Boite de dialogue pour choisir la raison
                        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                            "Contenu inapproprié",
                            "Contenu inapproprié", "Spam ou publicité",
                            "Harcèlement ou insulte", "Désinformation", "Autre"
                        );
                        dialog.setTitle("Signaler ce post");
                        dialog.setHeaderText("Pourquoi signalez-vous ce post ?");
                        dialog.setContentText("Raison :");
                        dialog.showAndWait().ifPresent(raison -> {
                            try {
                                boolean ok = signalementService.signaler(p.getId(), moi.getId(), raison);
                                if (ok) {
                                    btnSignaler.setText("🚩 Signalé");
                                    btnSignaler.setStyle(
                                        "-fx-background-color:transparent;-fx-text-fill:#ef4444;" +
                                        "-fx-font-size:11px;-fx-border-color:#fecaca;" +
                                        "-fx-border-radius:20px;-fx-background-radius:20px;-fx-padding:5 12;");
                                    btnSignaler.setDisable(true);
                                    AlertHelper.info("Signalement envoyé",
                                        "Merci. L'administrateur examinera ce post.");
                                }
                            } catch (Exception ex) {
                                AlertHelper.erreur("Erreur", ex.getMessage());
                            }
                        });
                    });
                } catch (Exception ignored) {}
            }

            // ── Bouton Partager : cache si on est l'auteur ──
            if (moi.getId() != p.getAuteurId()) {
                Button btnShare = new Button("↗ Partager");
                btnShare.setStyle("-fx-background-color:#f9fafb;-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-font-weight:600;-fx-background-radius:20px;-fx-border-color:#e5e7eb;-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 14;");
                actionBar.getChildren().add(btnShare);

                btnShare.setOnAction(e -> {
                    if (!AlertHelper.confirmer("Partager",
                            "Republier le post de " + p.getAuteurNom() + " sur votre fil ?\nIl sera soumis a la validation admin.")) return;
                    try {
                        ForumPost partage = new ForumPost(
                            "🔁 " + p.getTitre(),
                            "Partage depuis le post de " + p.getAuteurNom() + " :\n\n" + p.getContenu(),
                            p.getPhotoPath(),
                            moi.getId(),
                            moi.getNomComplet(),
                            moi.getRole()
                        );
                        forumService.soumettre(partage);
                        btnShare.setText("✅ Partagé !");
                        btnShare.setDisable(true);
                        chargerMesPosts();
                    } catch (Exception ex) { AlertHelper.erreur("Partage", ex.getMessage()); }
                });
            }

            // ── Bouton Traduire ──
            Button btnTraduire = new Button("🌐 Traduire");
            btnTraduire.setStyle("-fx-background-color:#f9fafb;-fx-text-fill:#6b7280;-fx-font-size:12px;" +
                "-fx-font-weight:600;-fx-background-radius:20px;-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:20px;-fx-cursor:hand;-fx-padding:6 14;");

            // Panel traduction (caché par défaut)
            VBox panelTrad = new VBox(8);
            panelTrad.setVisible(false);
            panelTrad.setManaged(false);
            panelTrad.setPadding(new Insets(10, 0, 0, 0));

            // Sélecteur de langue
            ComboBox<String> cbLangue = new ComboBox<>();
            cbLangue.getItems().addAll("Français 🇫🇷", "عربي 🇹🇳", "English 🇬🇧");
            cbLangue.setValue("Français 🇫🇷");
            cbLangue.setStyle("-fx-font-size:12px;-fx-background-radius:8px;-fx-border-radius:8px;");

            Button btnLancer = new Button("Traduire");
            btnLancer.setStyle("-fx-background-color:#1f2937;-fx-text-fill:white;-fx-font-size:12px;" +
                "-fx-font-weight:600;-fx-background-radius:8px;-fx-cursor:hand;-fx-padding:5 14;");

            HBox langRow = new HBox(8, cbLangue, btnLancer);
            langRow.setAlignment(Pos.CENTER_LEFT);

            Label lbTraduit = new Label();
            lbTraduit.setWrapText(true);
            lbTraduit.setStyle("-fx-font-size:13px;-fx-text-fill:#1f2937;-fx-background-color:#f5f3ff;" +
                "-fx-background-radius:10px;-fx-padding:10 12;");
            lbTraduit.setVisible(false);
            lbTraduit.setManaged(false);
            lbTraduit.setMaxWidth(Double.MAX_VALUE);

            panelTrad.getChildren().addAll(langRow, lbTraduit);
            actionBar.getChildren().add(btnTraduire);

            btnTraduire.setOnAction(e -> {
                boolean show = !panelTrad.isVisible();
                panelTrad.setVisible(show);
                panelTrad.setManaged(show);
            });

            btnLancer.setOnAction(e -> {
                String choix = cbLangue.getValue();
                TranslationService.Langue langue = choix.startsWith("عربي")
                    ? TranslationService.Langue.ARABE
                    : choix.startsWith("English")
                        ? TranslationService.Langue.ANGLAIS
                        : TranslationService.Langue.FRANCAIS;

                btnLancer.setText("...");
                btnLancer.setDisable(true);
                lbTraduit.setVisible(false);
                lbTraduit.setManaged(false);

                String texteATraduire = p.getTitre() + "\n\n" + p.getContenu();

                Thread t = new Thread(() -> {
                    try {
                        TranslationService.ResultatTraduction res =
                            translationService.traduire(texteATraduire, langue);
                        javafx.application.Platform.runLater(() -> {
                            lbTraduit.setText(res.texteTraduit);
                            lbTraduit.setVisible(true);
                            lbTraduit.setManaged(true);
                            btnLancer.setText("Traduire");
                            btnLancer.setDisable(false);
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            lbTraduit.setText("Erreur : " + ex.getMessage());
                            lbTraduit.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;" +
                                "-fx-background-color:#f9fafb;-fx-background-radius:10px;-fx-padding:8 12;");
                            lbTraduit.setVisible(true);
                            lbTraduit.setManaged(true);
                            btnLancer.setText("Traduire");
                            btnLancer.setDisable(false);
                        });
                    }
                });
                t.setDaemon(true);
                t.start();
            });

            // ── Boutons Modifier / Supprimer (visible seulement si auteur) ──
            if (moi.getId() == p.getAuteurId()) {
                Separator sepOwner = new Separator();
                sepOwner.setStyle("-fx-background-color:#f3f4f6;");

                Button btnModifier = new Button("✏ Modifier");
                btnModifier.setStyle("-fx-background-color:transparent;-fx-text-fill:#6b7280;" +
                    "-fx-font-size:11px;-fx-cursor:hand;-fx-border-color:#e5e7eb;" +
                    "-fx-border-radius:6px;-fx-background-radius:6px;-fx-padding:4 10;");

                Button btnSupprimerPost = new Button("🗑 Supprimer");
                btnSupprimerPost.setStyle("-fx-background-color:transparent;-fx-text-fill:#ef4444;" +
                    "-fx-font-size:11px;-fx-cursor:hand;-fx-border-color:#fecaca;" +
                    "-fx-border-radius:6px;-fx-background-radius:6px;-fx-padding:4 10;");

                HBox ownerBar = new HBox(8, btnModifier, btnSupprimerPost);
                ownerBar.setAlignment(Pos.CENTER_RIGHT);

                // ── Zone d'édition inline (cachée par défaut) ──
                VBox panelEdit = new VBox(8);
                panelEdit.setVisible(false);
                panelEdit.setManaged(false);
                panelEdit.setStyle("-fx-background-color:#f9fafb;-fx-background-radius:10px;-fx-padding:12;");

                TextField tfEditTitre = new TextField(p.getTitre());
                tfEditTitre.setStyle("-fx-border-color:#e5e7eb;-fx-border-radius:8px;" +
                    "-fx-background-radius:8px;-fx-font-size:13px;-fx-font-weight:700;");

                TextArea taEditContenu = new TextArea(p.getContenu());
                taEditContenu.setWrapText(true);
                taEditContenu.setPrefHeight(80);
                taEditContenu.setStyle("-fx-border-color:#e5e7eb;-fx-border-radius:8px;" +
                    "-fx-background-radius:8px;-fx-font-size:12px;");

                Label lbEditFeedback = new Label("");
                lbEditFeedback.setStyle("-fx-font-size:11px;");

                Button btnSauvegarder = new Button("💾 Sauvegarder");
                btnSauvegarder.setStyle("-fx-background-color:#1f2937;-fx-text-fill:white;" +
                    "-fx-font-size:12px;-fx-font-weight:600;-fx-background-radius:8px;" +
                    "-fx-cursor:hand;-fx-padding:5 14;");

                Button btnAnnulerEdit = new Button("Annuler");
                btnAnnulerEdit.setStyle("-fx-background-color:transparent;-fx-text-fill:#6b7280;" +
                    "-fx-font-size:12px;-fx-cursor:hand;-fx-border-color:#e5e7eb;" +
                    "-fx-border-radius:8px;-fx-background-radius:8px;-fx-padding:5 10;");

                HBox editActions = new HBox(8, btnSauvegarder, btnAnnulerEdit);
                editActions.setAlignment(Pos.CENTER_LEFT);
                panelEdit.getChildren().addAll(tfEditTitre, taEditContenu, lbEditFeedback, editActions);

                // Action Modifier
                btnModifier.setOnAction(e -> {
                    boolean show = !panelEdit.isVisible();
                    panelEdit.setVisible(show);
                    panelEdit.setManaged(show);
                    btnModifier.setText(show ? "✕ Fermer" : "✏ Modifier");
                });

                btnAnnulerEdit.setOnAction(e -> {
                    panelEdit.setVisible(false);
                    panelEdit.setManaged(false);
                    btnModifier.setText("✏ Modifier");
                });

                btnSauvegarder.setOnAction(e -> {
                    String nvTitre   = tfEditTitre.getText().trim();
                    String nvContenu = taEditContenu.getText().trim();
                    if (nvTitre.isEmpty() || nvContenu.isEmpty()) {
                        lbEditFeedback.setStyle("-fx-font-size:11px;-fx-text-fill:#ef4444;");
                        lbEditFeedback.setText("Titre et contenu obligatoires.");
                        return;
                    }
                    try {
                        forumService.modifierPost(p.getId(), nvTitre, nvContenu, moi.getId());
                        lbEditFeedback.setStyle("-fx-font-size:11px;-fx-text-fill:#059669;");
                        lbEditFeedback.setText("Modifié — en attente de re-validation.");
                        panelEdit.setVisible(false);
                        panelEdit.setManaged(false);
                        btnModifier.setText("✏ Modifier");
                        chargerFil();
                        chargerMesPosts();
                    } catch (Exception ex) {
                        lbEditFeedback.setStyle("-fx-font-size:11px;-fx-text-fill:#ef4444;");
                        lbEditFeedback.setText("Erreur : " + ex.getMessage());
                    }
                });

                // Action Supprimer
                btnSupprimerPost.setOnAction(e -> {
                    if (!AlertHelper.confirmer("Supprimer", "Supprimer définitivement ce post ?")) return;
                    try {
                        forumService.supprimer(p.getId());
                        chargerFil();
                        chargerMesPosts();
                    } catch (Exception ex) {
                        AlertHelper.erreur("Erreur", ex.getMessage());
                    }
                });

                card.getChildren().addAll(reactionWrapper, actionBar, sectionComments, panelTrad, sepOwner, ownerBar, panelEdit);
            } else {
                card.getChildren().addAll(reactionWrapper, actionBar, sectionComments, panelTrad);
            }

        } catch (Exception ignored) {
            // Si les tables like/comment n'existent pas encore, on affiche sans erreur
        }

        return card;
    }

    private void chargerCommentaires(VBox section, ForumPost post, Button btnComment) {
        section.getChildren().clear();

        try {
            List<Comment> comments = commentService.getByPost(post.getId());

            // Afficher commentaires existants
            for (Comment c : comments) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.TOP_LEFT);
                row.setPadding(new Insets(6, 0, 6, 0));

                Label initiales = new Label(c.getInitiales());
                initiales.setStyle("-fx-background-color:#e0e7ff;-fx-text-fill:#3730a3;-fx-font-weight:700;" +
                                   "-fx-font-size:11px;-fx-background-radius:50%;-fx-min-width:32px;" +
                                   "-fx-min-height:32px;-fx-pref-width:32px;-fx-pref-height:32px;-fx-alignment:center;");

                VBox bubble = new VBox(2);
                bubble.setStyle("-fx-background-color:#f3f4f6;-fx-background-radius:12px;-fx-padding:8 12;");
                Label nom    = new Label(c.getAuteurNom());
                nom.setStyle("-fx-font-weight:700;-fx-font-size:12px;-fx-text-fill:#1f2937;");
                Label texte  = new Label(c.getContenu());
                texte.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
                texte.setWrapText(true);
                Label date   = new Label(c.getDateFormatee());
                date.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
                bubble.getChildren().addAll(nom, texte, date);
                HBox.setHgrow(bubble, Priority.ALWAYS);

                // ── Comment like button ──
                try {
                    int    nbCLikes = commentLikeService.countLikes(c.getId());
                    boolean cLiked  = commentLikeService.hasLiked(c.getId(), moi.getId());
                    Button btnCLike = new Button((cLiked ? "❤ " : "🤍 ") + nbCLikes);
                    btnCLike.setStyle(
                        "-fx-background-color:transparent;-fx-border-color:transparent;" +
                        "-fx-font-size:11px;-fx-cursor:hand;-fx-text-fill:" +
                        (cLiked ? "#be185d" : "#9ca3af") + ";-fx-padding:2 6;"
                    );
                    final int cid = c.getId();
                    btnCLike.setOnAction(ev -> {
                        try {
                            boolean liked = commentLikeService.toggleLike(cid, moi.getId());
                            int nb = commentLikeService.countLikes(cid);
                            btnCLike.setText((liked ? "❤ " : "🤍 ") + nb);
                            btnCLike.setStyle(
                                "-fx-background-color:transparent;-fx-border-color:transparent;" +
                                "-fx-font-size:11px;-fx-cursor:hand;-fx-text-fill:" +
                                (liked ? "#be185d" : "#9ca3af") + ";-fx-padding:2 6;"
                            );
                        } catch (Exception ex) { AlertHelper.erreur("Like", ex.getMessage()); }
                    });
                    bubble.getChildren().add(btnCLike);
                } catch (Exception ignored) {}

                row.getChildren().addAll(initiales, bubble);
                section.getChildren().add(row);
            }

            if (comments.isEmpty()) {
                Label none = new Label("Soyez le premier à commenter !");
                none.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:12px;-fx-padding:0 0 6 0;");
                section.getChildren().add(none);
            }

            // Zone de saisie nouveau commentaire
            Separator sep2 = new Separator();
            section.getChildren().add(sep2);

            HBox inputRow = new HBox(8);
            inputRow.setAlignment(Pos.CENTER_LEFT);
            Label myInitiales = new Label(moi.getInitiales());
            myInitiales.setStyle("-fx-background-color:" + roleColor(moi.getRole()) + ";-fx-text-fill:white;" +
                                 "-fx-font-weight:700;-fx-font-size:11px;-fx-background-radius:50%;" +
                                 "-fx-min-width:32px;-fx-min-height:32px;-fx-pref-width:32px;-fx-pref-height:32px;-fx-alignment:center;");
            TextField tfComment = new TextField();
            tfComment.setPromptText("Écrire un commentaire...");
            tfComment.setStyle("-fx-background-radius:20px;-fx-border-radius:20px;-fx-border-color:#e5e7eb;-fx-padding:6 12;-fx-font-size:12px;");
            HBox.setHgrow(tfComment, Priority.ALWAYS);

            Button btnEnvoyer = new Button("Envoyer");
            btnEnvoyer.setStyle("-fx-background-color:#7c3aed;-fx-text-fill:white;-fx-font-size:12px;" +
                                "-fx-font-weight:700;-fx-background-radius:20px;-fx-padding:6 14;-fx-cursor:hand;");
            btnEnvoyer.setOnAction(ev -> {
                String txt = tfComment.getText().trim();
                if (txt.isEmpty()) return;
                try {
                    Comment newC = new Comment(post.getId(), moi.getId(), moi.getNomComplet(), txt);
                    commentService.addComment(newC);
                    tfComment.clear();
                    chargerCommentaires(section, post, btnComment);
                    int nb = commentService.countComments(post.getId());
                    btnComment.setText("💬 " + nb + " Commentaire" + (nb > 1 ? "s" : ""));
                } catch (Exception ex) { AlertHelper.erreur("Commentaire", ex.getMessage()); }
            });

            inputRow.getChildren().addAll(myInitiales, tfComment, btnEnvoyer);
            section.getChildren().add(inputRow);

        } catch (Exception e) {
            Label err = new Label("Impossible de charger les commentaires.");
            err.setStyle("-fx-text-fill:#ef4444;-fx-font-size:12px;");
            section.getChildren().add(err);
        }
    }

    private void chargerMesPosts() {
        vbMesPosts.getChildren().clear();
        try {
            List<ForumPost> mes = forumService.getMesPostsPar(moi.getId());
            lbNbMesPosts.setText(mes.size() + " post" + (mes.size() > 1 ? "s" : ""));
            for (ForumPost p : mes) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 12, 8, 12));
                row.setStyle("-fx-background-color:#f9fafb;-fx-background-radius:10px;");
                Label statut = new Label(p.getStatut() == ForumPost.Statut.ACCEPTE ? "✅" :
                                         p.getStatut() == ForumPost.Statut.REFUSE   ? "❌" : "⏳");
                Label titreL = new Label(p.getTitre().length() > 30 ? p.getTitre().substring(0, 30) + "…" : p.getTitre());
                titreL.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
                HBox.setHgrow(titreL, Priority.ALWAYS);
                row.getChildren().addAll(statut, titreL);
                vbMesPosts.getChildren().add(row);
            }
            if (mes.isEmpty()) {
                Label none = new Label("Aucun post soumis.");
                none.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:12px;");
                vbMesPosts.getChildren().add(none);
            }
        } catch (Exception ignored) {}
    }

    @FXML private void ouvrirNotifications() {
        try {
            // Marquer comme lues + cacher badge
            notificationService.marquerToutesLues(moi.getId());
            lbBadge.setVisible(false);
            lbBadge.setManaged(false);

            java.util.List<Notification> notifs = notificationService.getByUser(moi.getId());

            // Construire le contenu de la popup
            VBox content = new VBox(8);
            content.setPadding(new Insets(12, 16, 12, 16));
            content.setPrefWidth(380);

            if (notifs.isEmpty()) {
                Label none = new Label("Aucune notification pour le moment.");
                none.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:13px;");
                content.getChildren().add(none);
            } else {
                for (Notification n : notifs) {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.TOP_LEFT);
                    row.setPadding(new Insets(8, 0, 8, 0));
                    row.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;-fx-border-width:0 0 1 0;");

                    Label icone = new Label(n.getIcone());
                    icone.setStyle("-fx-font-size:18px;");

                    VBox info = new VBox(3);
                    Label msg = new Label(n.getMessage());
                    msg.setWrapText(true);
                    msg.setMaxWidth(300);
                    msg.setStyle("-fx-font-size:12px;-fx-text-fill:#1f2937;");
                    Label date = new Label(n.getDateFormatee());
                    date.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
                    info.getChildren().addAll(msg, date);

                    row.getChildren().addAll(icone, info);
                    content.getChildren().add(row);
                }
            }

            ScrollPane sp = new ScrollPane(content);
            sp.setFitToWidth(true);
            sp.setMaxHeight(400);
            sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");

            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Notifications");
            dialog.setHeaderText(null);
            dialog.getDialogPane().setContent(sp);
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            dialog.getDialogPane().setStyle("-fx-background-color:white;");
            dialog.showAndWait();

        } catch (Exception e) {
            AlertHelper.erreur("Notifications", e.getMessage());
        }
    }

    private void chargerNotifications() {
        try {
            int nonLues = notificationService.compterNonLues(moi.getId());
            lbBadge.setText(String.valueOf(nonLues));
            lbBadge.setVisible(nonLues > 0);
            lbBadge.setManaged(nonLues > 0);
        } catch (Exception e) { /* ignore */ }
    }

    @FXML private void toggleTheme() {
        boolean dark = ThemeManager.toggle(vbFil.getScene());
        majBoutonTheme();
    }

    private void majBoutonTheme() {
        if (btnTheme != null)
            btnTheme.setText(ThemeManager.isDark() ? "☀  Mode clair" : "🌙  Mode sombre");
    }

    private void chargerReputation() {
        try {
            int pts = reputationService.getReputation(moi.getId());
            String badge = ReputationService.getBadge(pts);
            String color = ReputationService.getBadgeColor(pts);
            String prog  = ReputationService.getProgression(pts);
            lbBadgeReputation.setText(badge);
            lbBadgeReputation.setStyle(
                "-fx-background-color:" + color + ";-fx-text-fill:white;" +
                "-fx-font-size:11px;-fx-font-weight:700;" +
                "-fx-background-radius:20px;-fx-padding:3 12;");
            lbPointsReputation.setText(pts + " pts — " + prog);
        } catch (Exception ignored) {}
    }

    @FXML private void seDeconnecter() {
        if (presenceTimeline != null) presenceTimeline.stop();
        chatService.setOffline(moi.getId());
        chatWindows.values().forEach(javafx.stage.Stage::close);
        SessionManager.logout();
        SceneManager.naviguer("Login.fxml", (Stage) vbFil.getScene().getWindow(), "EduNova");
    }

    private void feedback(String msg, boolean ok) {
        lbFeedback.setStyle(ok
            ? "-fx-text-fill:#16a34a;-fx-font-weight:700;-fx-font-size:12px;"
            : "-fx-text-fill:#ef4444;-fx-font-weight:700;-fx-font-size:12px;");
        lbFeedback.setText(msg);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
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

    // ── User profile popup ───────────────────────────────────
    private void afficherProfilUtilisateur(int userId, String nom, String role) {
        int[] stats = userService.getUserStats(userId);
        int totalPosts = stats[0], acceptedPosts = stats[1], reputation = stats[2];
        String badge = services.ReputationService.getBadge(reputation);
        String badgeColor = services.ReputationService.getBadgeColor(reputation);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20, 24, 20, 24));
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(300);

        // Avatar
        Label av = new Label(nom.length() >= 2
            ? (nom.trim().split("\\s+").length >= 2
                ? "" + nom.trim().split("\\s+")[0].charAt(0) + nom.trim().split("\\s+")[1].charAt(0)
                : nom.substring(0, 2)).toUpperCase()
            : "?");
        av.setStyle(
            "-fx-background-color:" + roleColor(role) + ";-fx-text-fill:white;" +
            "-fx-font-weight:700;-fx-font-size:22px;-fx-background-radius:50%;" +
            "-fx-min-width:64px;-fx-min-height:64px;-fx-pref-width:64px;-fx-pref-height:64px;" +
            "-fx-alignment:center;"
        );

        Label lbNom = new Label(nom);
        lbNom.setStyle("-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:#1f2937;");

        Label lbRole = new Label(role.substring(0,1).toUpperCase() + role.substring(1).toLowerCase());
        lbRole.setStyle(
            "-fx-background-color:" + roleColor(role) + ";-fx-text-fill:white;" +
            "-fx-font-size:11px;-fx-font-weight:600;-fx-background-radius:20px;-fx-padding:3 12;"
        );

        Label lbBadge = new Label(badge);
        lbBadge.setStyle(
            "-fx-background-color:" + badgeColor + ";-fx-text-fill:white;" +
            "-fx-font-size:11px;-fx-font-weight:700;-fx-background-radius:20px;-fx-padding:3 12;"
        );

        Separator sep = new Separator();

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);

        VBox colTotal = new VBox(2);
        colTotal.setAlignment(Pos.CENTER);
        Label lbTotal = new Label(String.valueOf(totalPosts));
        lbTotal.setStyle("-fx-font-size:22px;-fx-font-weight:900;-fx-text-fill:#1f2937;");
        Label lbTotalLbl = new Label("Posts");
        lbTotalLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        colTotal.getChildren().addAll(lbTotal, lbTotalLbl);

        VBox colAccepted = new VBox(2);
        colAccepted.setAlignment(Pos.CENTER);
        Label lbAccepted = new Label(String.valueOf(acceptedPosts));
        lbAccepted.setStyle("-fx-font-size:22px;-fx-font-weight:900;-fx-text-fill:#059669;");
        Label lbAcceptedLbl = new Label("Acceptés");
        lbAcceptedLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        colAccepted.getChildren().addAll(lbAccepted, lbAcceptedLbl);

        VBox colRep = new VBox(2);
        colRep.setAlignment(Pos.CENTER);
        Label lbRep = new Label(String.valueOf(reputation));
        lbRep.setStyle("-fx-font-size:22px;-fx-font-weight:900;-fx-text-fill:#7c3aed;");
        Label lbRepLbl = new Label("Points");
        lbRepLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
        colRep.getChildren().addAll(lbRep, lbRepLbl);

        statsRow.getChildren().addAll(colTotal, colAccepted, colRep);
        content.getChildren().addAll(av, lbNom, lbRole, lbBadge, sep, statsRow);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Profil");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color:white;");
        dialog.showAndWait();
    }

    // ── Chat ─────────────────────────────────────────────────

    private void demarrerPollingPresence() {
        presenceTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            chatService.updatePresence(moi.getId());
            chargerContacts();
            majBadgeChat();
        }));
        presenceTimeline.setCycleCount(Timeline.INDEFINITE);
        presenceTimeline.play();
    }

    private void chargerContacts() {
        if (vbContacts == null) return;
        List<ChatService.UserPresence> users = chatService.getUtilisateursAvecPresence(moi.getId());
        javafx.application.Platform.runLater(() -> {
            vbContacts.getChildren().clear();
            for (ChatService.UserPresence u : users) {
                vbContacts.getChildren().add(creerLigneContact(u));
            }
            if (users.isEmpty()) {
                Label none = new Label("Aucun autre utilisateur");
                none.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:11px;-fx-padding:12 14;");
                vbContacts.getChildren().add(none);
            }
        });
    }

    private javafx.scene.Node creerLigneContact(ChatService.UserPresence u) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 14, 8, 14));
        row.setStyle("-fx-cursor:hand;");

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f9fafb;-fx-cursor:hand;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-cursor:hand;"));

        // Avatar with online dot overlay
        javafx.scene.layout.StackPane avatarStack = new javafx.scene.layout.StackPane();
        avatarStack.setMinWidth(38); avatarStack.setMinHeight(38);

        Label avatar = new Label(u.getInitiales());
        avatar.setStyle(
            "-fx-background-color:" + u.getRoleColor() + ";-fx-text-fill:white;" +
            "-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:50%;" +
            "-fx-min-width:36px;-fx-min-height:36px;-fx-pref-width:36px;-fx-pref-height:36px;" +
            "-fx-alignment:center;"
        );

        // Green dot (online) or grey dot (offline)
        Label dot = new Label();
        dot.setStyle(
            "-fx-background-color:" + (u.isOnline ? "#22c55e" : "#d1d5db") + ";" +
            "-fx-background-radius:50%;-fx-min-width:10px;-fx-min-height:10px;" +
            "-fx-pref-width:10px;-fx-pref-height:10px;" +
            "-fx-border-color:white;-fx-border-radius:50%;-fx-border-width:1.5px;"
        );
        javafx.scene.layout.StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);

        avatarStack.getChildren().addAll(avatar, dot);

        // Name + last seen
        VBox info = new VBox(2);
        Label lbNom = new Label(u.nom);
        lbNom.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:#1f2937;");
        lbNom.setMaxWidth(120);
        Label lbStatus = new Label(u.getLastSeenLabel());
        lbStatus.setStyle("-fx-font-size:10px;-fx-text-fill:" +
            (u.isOnline ? "#22c55e" : "#9ca3af") + ";");
        info.getChildren().addAll(lbNom, lbStatus);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Unread badge
        int unread = chatService.compterNonLusDepuis(u.userId, moi.getId());
        if (unread > 0) {
            Label badge = new Label(String.valueOf(unread));
            badge.setStyle(
                "-fx-background-color:#ef4444;-fx-text-fill:white;-fx-font-size:9px;" +
                "-fx-font-weight:700;-fx-background-radius:50%;-fx-min-width:16px;" +
                "-fx-min-height:16px;-fx-pref-width:16px;-fx-pref-height:16px;-fx-alignment:center;"
            );
            row.getChildren().addAll(avatarStack, info, badge);
        } else {
            row.getChildren().addAll(avatarStack, info);
        }

        row.setOnMouseClicked(e -> ouvrirFenetrChat(u));
        return row;
    }

    private void ouvrirFenetrChat(ChatService.UserPresence partner) {
        // Reuse existing window if open
        if (chatWindows.containsKey(partner.userId)) {
            javafx.stage.Stage existing = chatWindows.get(partner.userId);
            existing.toFront();
            return;
        }

        // Mark messages as read
        chatService.marquerLus(partner.userId, moi.getId());
        chargerContacts();
        majBadgeChat();

        // ── Build chat window ──
        javafx.stage.Stage chatStage = new javafx.stage.Stage();
        chatStage.setTitle("Chat — " + partner.nom);
        chatStage.setWidth(340);
        chatStage.setHeight(480);
        chatStage.setResizable(false);
        chatStage.initOwner(vbFil.getScene().getWindow());
        chatStage.initStyle(javafx.stage.StageStyle.DECORATED);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color:#7c3aed;");

        Label hAvatar = new Label(partner.getInitiales());
        hAvatar.setStyle(
            "-fx-background-color:rgba(255,255,255,0.25);-fx-text-fill:white;" +
            "-fx-font-weight:700;-fx-font-size:13px;-fx-background-radius:50%;" +
            "-fx-min-width:36px;-fx-min-height:36px;-fx-pref-width:36px;-fx-pref-height:36px;" +
            "-fx-alignment:center;"
        );
        Label hDot = new Label();
        hDot.setStyle(
            "-fx-background-color:" + (partner.isOnline ? "#22c55e" : "#d1d5db") + ";" +
            "-fx-background-radius:50%;-fx-min-width:9px;-fx-min-height:9px;" +
            "-fx-pref-width:9px;-fx-pref-height:9px;"
        );
        VBox hInfo = new VBox(1);
        Label hNom = new Label(partner.nom);
        hNom.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:white;");
        Label hStatus = new Label(partner.getLastSeenLabel());
        hStatus.setStyle("-fx-font-size:10px;-fx-text-fill:rgba(255,255,255,0.75);");
        hInfo.getChildren().addAll(hNom, hStatus);
        header.getChildren().addAll(hAvatar, hDot, hInfo);

        // Messages area
        VBox vbMessages = new VBox(6);
        vbMessages.setPadding(new Insets(10, 10, 10, 10));
        ScrollPane spMessages = new ScrollPane(vbMessages);
        spMessages.setFitToWidth(true);
        spMessages.setStyle("-fx-background-color:#f0f2f5;-fx-background:#f0f2f5;-fx-border-color:transparent;");
        VBox.setVgrow(spMessages, Priority.ALWAYS);

        // Input area
        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(8, 10, 8, 10));
        inputRow.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb;-fx-border-width:1 0 0 0;");

        // Attachment button
        Button btnAttach = new Button("📎");
        btnAttach.setStyle(
            "-fx-background-color:transparent;-fx-border-color:transparent;" +
            "-fx-font-size:16px;-fx-cursor:hand;-fx-padding:4 6;"
        );
        btnAttach.setTooltip(new Tooltip("Envoyer une image ou un PDF"));

        TextField tfMsg = new TextField();
        tfMsg.setPromptText("Ecrire un message...");
        tfMsg.setStyle(
            "-fx-background-color:#f0f2f5;-fx-border-color:transparent;" +
            "-fx-background-radius:20px;-fx-font-size:12px;-fx-padding:7 12;"
        );
        HBox.setHgrow(tfMsg, Priority.ALWAYS);

        Button btnSend = new Button("Envoyer");
        btnSend.setStyle(
            "-fx-background-color:#7c3aed;-fx-text-fill:white;-fx-font-size:12px;" +
            "-fx-font-weight:700;-fx-background-radius:20px;-fx-cursor:hand;-fx-padding:7 14;"
        );
        inputRow.getChildren().addAll(btnAttach, tfMsg, btnSend);

        // Load existing messages
        Runnable chargerMessages = () -> {
            List<ChatMessage> msgs = chatService.getConversation(moi.getId(), partner.userId);
            vbMessages.getChildren().clear();
            for (ChatMessage msg : msgs) {
                vbMessages.getChildren().add(creerBullMessage(msg, moi.getId()));
            }
            javafx.application.Platform.runLater(() -> spMessages.setVvalue(1.0));
        };
        chargerMessages.run();

        // Attachment picker
        btnAttach.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choisir un fichier");
            fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
            );
            File chosen = fc.showOpenDialog(chatStage);
            if (chosen == null) return;

            String path = chosen.getAbsolutePath();
            String prefix = path.toLowerCase().endsWith(".pdf") ? "[PDF:" : "[IMAGE:";
            String contenu = prefix + path + "]";

            btnSend.setDisable(true);
            Thread t = new Thread(() -> {
                ChatService.SendResult result = chatService.envoyerMessage(moi.getId(), partner.userId, contenu);
                javafx.application.Platform.runLater(() -> {
                    btnSend.setDisable(false);
                    if (result.ok) chargerMessages.run();
                    else {
                        Label blocked = new Label("Fichier bloque : " + result.message);
                        blocked.setStyle("-fx-text-fill:#ef4444;-fx-font-size:10px;" +
                            "-fx-background-color:#fef2f2;-fx-background-radius:8px;-fx-padding:4 8;");
                        vbMessages.getChildren().add(blocked);
                        javafx.application.Platform.runLater(() -> spMessages.setVvalue(1.0));
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        // Send text action
        Runnable envoyerMsg = () -> {
            String txt = tfMsg.getText().trim();
            if (txt.isEmpty()) return;
            tfMsg.clear();
            btnSend.setDisable(true);
            btnSend.setText("...");

            Thread t = new Thread(() -> {
                ChatService.SendResult result = chatService.envoyerMessage(moi.getId(), partner.userId, txt);
                javafx.application.Platform.runLater(() -> {
                    btnSend.setDisable(false);
                    btnSend.setText("Envoyer");
                    if (result.ok) {
                        chargerMessages.run();
                    } else {
                        Label blocked = new Label("Message bloque : " + result.message);
                        blocked.setStyle(
                            "-fx-text-fill:#ef4444;-fx-font-size:10px;" +
                            "-fx-background-color:#fef2f2;-fx-background-radius:8px;-fx-padding:4 8;"
                        );
                        blocked.setWrapText(true);
                        vbMessages.getChildren().add(blocked);
                        javafx.application.Platform.runLater(() -> spMessages.setVvalue(1.0));
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        };

        btnSend.setOnAction(e -> envoyerMsg.run());
        tfMsg.setOnAction(e -> envoyerMsg.run());

        // Auto-refresh messages every 5s while window is open
        Timeline msgTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            chatService.marquerLus(partner.userId, moi.getId());
            chargerMessages.run();
        }));
        msgTimeline.setCycleCount(Timeline.INDEFINITE);
        msgTimeline.play();

        chatStage.setOnCloseRequest(e -> {
            msgTimeline.stop();
            chatWindows.remove(partner.userId);
            chargerContacts();
        });

        VBox root = new VBox(header, spMessages, inputRow);
        VBox.setVgrow(spMessages, Priority.ALWAYS);
        chatStage.setScene(new javafx.scene.Scene(root));
        chatStage.show();
        chatWindows.put(partner.userId, chatStage);
    }

    private javafx.scene.Node creerBullMessage(ChatMessage msg, int myId) {
        boolean isMine = msg.getSenderId() == myId;
        String contenu = msg.getContenu();

        String bubbleBg     = isMine ? "#7c3aed" : "white";
        String bubbleFg     = isMine ? "white"   : "#1f2937";
        String borderRadius = isMine ? "16px 16px 4px 16px" : "16px 16px 16px 4px";
        String border       = isMine ? "" : "-fx-border-color:#e5e7eb;-fx-border-radius:" + borderRadius + ";-fx-border-width:1px;";

        javafx.scene.Node content;

        if (contenu.startsWith("[IMAGE:") && contenu.endsWith("]")) {
            // ── Image bubble ──
            String path = contenu.substring(7, contenu.length() - 1);
            File imgFile = new File(path);
            if (imgFile.exists()) {
                try {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(200);
                    iv.setPreserveRatio(true);
                    iv.setStyle("-fx-background-radius:12px;-fx-cursor:hand;");
                    // Click to open full size
                    iv.setOnMouseClicked(e -> {
                        try {
                            Desktop.getDesktop().open(imgFile);
                        } catch (Exception ignored) {}
                    });
                    VBox imgBox = new VBox(iv);
                    imgBox.setStyle(
                        "-fx-background-color:" + bubbleBg + ";-fx-background-radius:" + borderRadius + ";" +
                        border + "-fx-padding:6;"
                    );
                    content = imgBox;
                } catch (Exception ex) {
                    content = fallbackLabel("[Image: " + imgFile.getName() + "]", bubbleBg, bubbleFg, borderRadius, border);
                }
            } else {
                content = fallbackLabel("[Image introuvable]", bubbleBg, bubbleFg, borderRadius, border);
            }

        } else if (contenu.startsWith("[PDF:") && contenu.endsWith("]")) {
            // ── PDF bubble ──
            String path = contenu.substring(5, contenu.length() - 1);
            File pdfFile = new File(path);
            String fileName = pdfFile.getName();

            HBox pdfBox = new HBox(8);
            pdfBox.setAlignment(Pos.CENTER_LEFT);
            pdfBox.setPadding(new Insets(10, 14, 10, 14));
            pdfBox.setStyle(
                "-fx-background-color:" + bubbleBg + ";-fx-background-radius:" + borderRadius + ";" +
                border + "-fx-cursor:hand;"
            );

            Label icon = new Label("📄");
            icon.setStyle("-fx-font-size:22px;");

            VBox fileInfo = new VBox(2);
            Label lbName = new Label(fileName.length() > 22 ? fileName.substring(0, 22) + "..." : fileName);
            lbName.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:" + bubbleFg + ";");
            Label lbType = new Label("PDF — Cliquer pour ouvrir");
            lbType.setStyle("-fx-font-size:10px;-fx-text-fill:" + (isMine ? "rgba(255,255,255,0.75)" : "#9ca3af") + ";");
            fileInfo.getChildren().addAll(lbName, lbType);

            pdfBox.getChildren().addAll(icon, fileInfo);
            pdfBox.setOnMouseClicked(e -> {
                if (pdfFile.exists()) {
                    try { Desktop.getDesktop().open(pdfFile); }
                    catch (Exception ignored) {}
                }
            });
            // Hover
            pdfBox.setOnMouseEntered(ev -> pdfBox.setStyle(
                "-fx-background-color:" + (isMine ? "#6d28d9" : "#f3f4f6") + ";-fx-background-radius:" + borderRadius + ";" +
                border + "-fx-cursor:hand;"
            ));
            pdfBox.setOnMouseExited(ev -> pdfBox.setStyle(
                "-fx-background-color:" + bubbleBg + ";-fx-background-radius:" + borderRadius + ";" +
                border + "-fx-cursor:hand;"
            ));
            content = pdfBox;

        } else {
            // ── Text bubble ──
            content = fallbackLabel(contenu, bubbleBg, bubbleFg, borderRadius, border);
        }

        Label time = new Label(msg.getHeureFormatee());
        time.setStyle("-fx-font-size:9px;-fx-text-fill:#9ca3af;");

        VBox wrapper = new VBox(2, content, time);
        wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setPadding(new Insets(0, isMine ? 0 : 40, 0, isMine ? 40 : 0));
        return wrapper;
    }

    private Label fallbackLabel(String text, String bg, String fg, String radius, String border) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(220);
        lbl.setStyle(
            "-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";" +
            "-fx-font-size:12px;-fx-padding:8 12;" +
            "-fx-background-radius:" + radius + ";" + border
        );
        return lbl;
    }

    private void majBadgeChat() {
        if (lbChatUnread == null) return;
        int total = chatService.compterNonLus(moi.getId());
        javafx.application.Platform.runLater(() -> {
            lbChatUnread.setText(String.valueOf(total));
            lbChatUnread.setVisible(total > 0);
            lbChatUnread.setManaged(total > 0);
        });
    }

    // Reaction helpers
    private String reactionLabel(String emoji, String[][] reactions) {
        for (String[] r : reactions) if (r[0].equals(emoji)) return r[1];
        return "J'aime";
    }
    private String reactionColor(String emoji, String[][] reactions) {
        for (String[] r : reactions) if (r[0].equals(emoji)) return r[2];
        return "#6b7280";
    }
    private String reactionBg(String emoji, String[][] reactions) {
        for (String[] r : reactions) if (r[0].equals(emoji)) return r[3];
        return "#f9fafb";
    }
}

