package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.dao.RiskDAO;
import edunova.connexion.models.User;
import edunova.connexion.tools.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {

    // ROOT
    @FXML private BorderPane rootPane;

    // SIDEBAR
    @FXML private VBox     sidebar;
    @FXML private VBox     logoBox;
    @FXML private VBox     userBox;
    @FXML private VBox     menuBox;
    @FXML private VBox     bottomBox;
    @FXML private Region   sepLogo;
    @FXML private Region   sepUser;
    @FXML private Label    lblLogo;
    @FXML private Label    lblLogoSub;
    @FXML private Label    lblAvatarInitiales;
    @FXML private Label    lblUserNom;
    @FXML private Label    lblUserRole;
    @FXML private StackPane avatarPane;
    @FXML private Label    lblTheme;
    @FXML private Button   btnToggleTheme;
    @FXML private Button   btnDeconnexion;

    // MENU BUTTONS
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuUsers;
    @FXML private Button btnMenuEtudiants;
    @FXML private Button btnMenuEnseignants;
    @FXML private Button btnMenuParent;
    @FXML private Button btnMenuClasses;
    @FXML private Button btnMenuEvenement;
    @FXML private Button btnMenuForum;

    // NAVBAR
    @FXML private HBox      navbar;
    @FXML private VBox      mainContent;
    @FXML private Label     lblPageTitre;
    @FXML private Label     lblPageSousTitre;
    @FXML private Label     lblDateHeure;
    @FXML private TextField txtRechercheGlobal;
    @FXML private Region    sepNavbar;

    // PAGES
    @FXML private ScrollPane pageDashboard;
    @FXML private VBox       pageUsers;
    @FXML private VBox       pageEtudiants;
    @FXML private VBox       pageEnseignants;
    @FXML private VBox       pageParent;
    @FXML private VBox       pageEvenement;
    @FXML private VBox       pageForum;
    @FXML private VBox       dashContent;

    //  STATS LABELS
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalAdmins;
    @FXML private Label lblTotalEnseignants;
    @FXML private Label lblTotalEtudiants;
    @FXML private Label lblEvolutionUsers;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;
    @FXML private Label lblVueEnsemble;
    @FXML private Label lblVueSous;
    @FXML private Label lblDerniersTitle;
    @FXML private Label lblDerniersSub;
    @FXML private Label lblActifsTitle;
    @FXML private Label lblActifsLbl;
    @FXML private Label lblInactifsLbl;
    @FXML private Label lblActionsTitle;
    @FXML private Label lblStatUsers;
    @FXML private Label lblStatAdmins;
    @FXML private Label lblStatEnseignants;
    @FXML private Label lblStatEtudiants;

    // CARDS DASHBOARD
    @FXML private VBox     cardUsers;
    @FXML private VBox     cardAdmins;
    @FXML private VBox     cardEnseignants;
    @FXML private VBox     cardEtudiants;
    @FXML private VBox     cardDerniers;
    @FXML private VBox     cardActifs;
    @FXML private VBox     cardActions;
    @FXML private Button   btnGererUsers;
    @FXML private FlowPane flowDerniers;

    // PAGE USERS
    @FXML private FlowPane  flowCartes;
    @FXML private Label     lblCompteurUsers;
    @FXML private TextField txtRecherche;
    @FXML private Label     lblUsersGrand;
    @FXML private Label     lblUsersSous;
    @FXML private ScrollPane scrollUsers;
    
    // RISK STATISTICS PANEL
    @FXML private VBox      riskStatsPanel;
    @FXML private HBox      riskStatsContainer;
    @FXML private Label     lblRiskStatsTitle;
    @FXML private Label     lblRiskStatsSub;

    // PAGE ETUDIANTS
    @FXML private VBox      etudientsContent;
    @FXML private FlowPane  flowEtudiants;
    @FXML private Label     lblCompteurEtudiants;
    @FXML private TextField txtRechercheEtudiants;
    @FXML private Label     lblEtudiantsGrand;
    @FXML private Label     lblEtudiantsSous;
    @FXML private HBox      statsEtudiantsPanel;

    // PAGE ENSEIGNANTS
    @FXML private FlowPane  flowEnseignants;
    @FXML private Label     lblCompteurEnseignants;
    @FXML private TextField txtRechercheEnseignants;
    @FXML private Label     lblEnseignantsGrand;
    @FXML private Label     lblEnseignantsSous;
    @FXML private HBox      statsEnseignantsPanel;

    // PAGE EVENEMENT
    @FXML private Label     lblEvenementGrand;
    @FXML private Label     lblEvenementSous;

    // PAGE PARENT
    @FXML private Label     lblParentGrand;
    @FXML private Label     lblParentSous;
    @FXML private FlowPane  flowParent;
    @FXML private Label     lblCompteurParent;
    @FXML private TextField txtRechercheParent;
    @FXML private HBox      statsParentPanel;

    // PAGE FORUM
    @FXML private Label     lblForumGrand;
    @FXML private Label     lblForumSous;

    private final UserDAO dao    = new UserDAO();
    private final RiskDAO riskDAO = new RiskDAO();
    private       boolean isDark = false;
    
    // Instance statique pour accès global
    private static DashboardController instance;


    //  COULEURS THÈMES


    // DARK
    private static final String D_BG_MAIN    = "#0f0f1a";
    private static final String D_BG_SIDEBAR = "#1a1a2e";
    private static final String D_BG_CARD    = "#1a1a2e";
    private static final String D_BG_NAVBAR  = "#1a1a2e";
    private static final String D_BORDER     = "#2d2d4e";
    private static final String D_TEXT_MAIN  = "#e2e8f0";
    private static final String D_TEXT_SUB   = "#64748b";
    private static final String D_TEXT_MENU  = "#94a3b8";
    private static final String D_EMOJI_BG_1 = "#2d1b69"; // violet foncé
    private static final String D_EMOJI_BG_2 = "#0c2340"; // bleu foncé
    private static final String D_EMOJI_BG_3 = "#052e1a"; // vert foncé

    // LIGHT
    private static final String L_BG_MAIN    = "#f1f5f9";
    private static final String L_BG_SIDEBAR = "#ffffff";
    private static final String L_BG_CARD    = "#ffffff";
    private static final String L_BG_NAVBAR  = "#ffffff";
    private static final String L_BORDER     = "#e2e8f0";
    private static final String L_TEXT_MAIN  = "#1e293b";
    private static final String L_TEXT_SUB   = "#64748b";
    private static final String L_TEXT_MENU  = "#475569";
    private static final String L_EMOJI_BG_1 = "#ede9fe"; // violet clair
    private static final String L_EMOJI_BG_2 = "#e0f2fe"; // bleu clair
    private static final String L_EMOJI_BG_3 = "#dcfce7"; // vert clair

    //  INITIALISATION
    @FXML
    public void initialize() {
        instance = this;
        configurerSession();
        configurerDate();
        appliquerTheme();
        chargerStatistiques();
        chargerTousUsers();
    }

    //  Session
    private void configurerSession() {
        SessionManager s = SessionManager.getInstance();
        lblUserNom.setText(s.getEmail());
        lblUserRole.setText(s.getRole());
        lblAvatarInitiales.setText(
                s.getEmail().substring(0, 1).toUpperCase());
    }

    //  Date
    private void configurerDate() {
        lblDateHeure.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }


    //  TOGGLE THÈME


    @FXML
    private void handleToggleTheme() {
        isDark = !isDark;
        appliquerTheme();
    }

    private void appliquerTheme() {
        String bgMain    = isDark ? D_BG_MAIN    : L_BG_MAIN;
        String bgSidebar = isDark ? D_BG_SIDEBAR : L_BG_SIDEBAR;
        String bgCard    = isDark ? D_BG_CARD    : L_BG_CARD;
        String bgNavbar  = isDark ? D_BG_NAVBAR  : L_BG_NAVBAR;
        String border    = isDark ? D_BORDER     : L_BORDER;
        String textMain  = isDark ? D_TEXT_MAIN  : L_TEXT_MAIN;
        String textSub   = isDark ? D_TEXT_SUB   : L_TEXT_SUB;
        String textMenu  = isDark ? D_TEXT_MENU  : L_TEXT_MENU;
        String emojiBg1  = isDark ? D_EMOJI_BG_1 : L_EMOJI_BG_1;
        String emojiBg2  = isDark ? D_EMOJI_BG_2 : L_EMOJI_BG_2;
        String emojiBg3  = isDark ? D_EMOJI_BG_3 : L_EMOJI_BG_3;

        //  Root
        rootPane.setStyle(
                "-fx-background-color: " + bgMain + ";");

        //  Sidebar
        sidebar.setStyle(
                "-fx-background-color: " + bgSidebar + ";");
        sepLogo.setStyle(
                "-fx-background-color: " + border + "; -fx-pref-height: 1;");
        sepUser.setStyle(
                "-fx-background-color: " + border + "; -fx-pref-height: 1;");

        lblLogo.setStyle(
                "-fx-font-size: 36; -fx-font-weight: bold;" +
                        "-fx-text-fill: #7c3aed;");
        lblLogoSub.setStyle(
                "-fx-font-size: 10;" +
                        "-fx-text-fill: " + (isDark ? "#a78bfa" : "#7c3aed") + ";" +
                        "-fx-wrap-text: true;");

        lblUserNom.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + "; -fx-padding: 8 0 2 0;");

        lblTheme.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textMenu + ";");
        btnToggleTheme.setText(isDark ? "☀ Light" : "🌙 Dark");
        btnToggleTheme.setStyle(
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                        "-fx-background-radius: 20; -fx-padding: 5 16;" +
                        "-fx-font-size: 12; -fx-cursor: hand;");

        btnDeconnexion.setStyle(
                "-fx-background-color: " + (isDark ? "#2d1b1b" : "#fff0f0") + ";" +
                        "-fx-text-fill: #f87171; -fx-font-size: 13;" +
                        "-fx-background-radius: 8; -fx-padding: 11 15;" +
                        "-fx-alignment: CENTER_LEFT; -fx-cursor: hand;");

        //  Boutons menu
        String styleNormal =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + textMenu + ";" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;";
        String styleActif =
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;";

        for (Button b : new Button[]{
                btnMenuUsers, btnMenuEtudiants,
                btnMenuEnseignants, btnMenuClasses}) {
            b.setStyle(styleNormal);
        }
        btnMenuDashboard.setStyle(
                pageUsers.isVisible() ? styleNormal : styleActif);
        btnMenuUsers.setStyle(
                pageUsers.isVisible() ? styleActif : styleNormal);

        //  Navbar
        navbar.setStyle(
                "-fx-background-color: " + bgNavbar + "; -fx-padding: 15 25;");
        sepNavbar.setStyle(
                "-fx-background-color: " + border + "; -fx-pref-height: 1;");

        lblPageTitre.setStyle(
                "-fx-font-size: 26; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");

        // Masquer le sous-titre
        lblPageSousTitre.setVisible(false);
        lblPageSousTitre.setManaged(false);

        lblDateHeure.setStyle(
                "-fx-text-fill: " + textSub + ";" +
                        "-fx-font-size: 12; -fx-padding: 0 0 0 20;");

        txtRechercheGlobal.setStyle(
                "-fx-background-color: " + bgMain + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 20; -fx-padding: 8 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 20;");

        //  Main content
        mainContent.setStyle(
                "-fx-background-color: " + bgMain + ";");

        // Dashboard
        pageDashboard.setStyle(
                "-fx-background: " + bgMain + ";" +
                        "-fx-background-color: " + bgMain + ";");
        dashContent.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");

        lblVueEnsemble.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblVueSous.setStyle(
                "-fx-font-size: 13; -fx-text-fill: " + textSub + ";");

        // Labels stats sous les chiffres
        lblStatUsers.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblStatAdmins.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblStatEnseignants.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblStatEtudiants.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblTotalUsers.setStyle(
                "-fx-font-size: 28; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");

        // Logos emoji — fond adapté au thème ────────────
        // Carte Users (emoji 👥)
        cardUsers.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 12; -fx-border-width: 1;");

        // On récupère le HBox dans cardUsers pour mettre à jour
        // le fond de l'emoji directement via les enfants
        appliquerFondEmoji(cardUsers, 0, emojiBg1);
        appliquerFondEmoji(cardAdmins, 0, emojiBg1);
        appliquerFondEmoji(cardEnseignants, 0, emojiBg2);
        appliquerFondEmoji(cardEtudiants, 0, emojiBg3);

        // Cartes dashboard
        String styleCard =
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 12; -fx-border-width: 1;";

        cardAdmins.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-border-color: #7c3aed;" +
                        "-fx-border-radius: 12; -fx-border-width: 1;");
        cardEnseignants.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-border-color: #0ea5e9;" +
                        "-fx-border-radius: 12; -fx-border-width: 1;");
        cardEtudiants.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-border-color: #10b981;" +
                        "-fx-border-radius: 12; -fx-border-width: 1;");
        cardDerniers.setStyle(styleCard);
        cardActifs.setStyle(styleCard);
        cardActions.setStyle(styleCard);

        lblDerniersTitle.setStyle(
                "-fx-font-size: 16; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblDerniersSub.setStyle(
                "-fx-font-size: 11; -fx-text-fill: " + textSub + ";");
        lblActifsTitle.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + "; -fx-padding: 0 0 15 0;");
        lblActifsLbl.setStyle(
                "-fx-text-fill: " + textSub + ";");
        lblInactifsLbl.setStyle(
                "-fx-text-fill: " + textSub + ";");
        lblActionsTitle.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + "; -fx-padding: 0 0 15 0;");

        btnGererUsers.setStyle(
                "-fx-background-color: " + bgMain + ";" +
                        "-fx-text-fill: " + textMenu + ";" +
                        "-fx-background-radius: 8; -fx-padding: 10;" +
                        "-fx-font-size: 13; -fx-border-color: " + border + ";" +
                        "-fx-border-radius: 8; -fx-cursor: hand;");

        //  Page users
        pageUsers.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");

        // ScrollPane cartes
        if (scrollUsers != null) {
            scrollUsers.setStyle(
                    "-fx-background: " + bgMain + ";" +
                            "-fx-background-color: " + bgMain + ";" +
                            "-fx-border-color: transparent;");
        }

        flowCartes.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 5;");

        lblUsersGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblUsersSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
        lblCompteurUsers.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");

        txtRecherche.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 8; -fx-padding: 9 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 8;");

        // Rafraîchir les cartes
        chargerTousUsers();
        applyThemeToRiskStats();
        afficherCartesDerniers(dao.findAll().stream().limit(5).toList());

        // PAGE ETUDIANTS
        pageEtudiants.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");
        lblEtudiantsGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblEtudiantsSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
        lblCompteurEtudiants.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        txtRechercheEtudiants.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 8; -fx-padding: 9 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        flowEtudiants.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 5;");
        applyThemeToEtudiantsStats();

        // PAGE ENSEIGNANTS
        pageEnseignants.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");
        lblEnseignantsGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblEnseignantsSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
        lblCompteurEnseignants.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        txtRechercheEnseignants.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 8; -fx-padding: 9 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        flowEnseignants.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 5;");
        applyThemeToEnseignantsStats();

        // PAGE PARENT
        pageParent.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");
        lblParentGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblParentSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
        lblCompteurParent.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        txtRechercheParent.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 8; -fx-padding: 9 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 8;");
        flowParent.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 5;");
        applyThemeToParentStats();

        // PAGE EVENEMENT
        pageEvenement.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");
        lblEvenementGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblEvenementSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");

        // PAGE FORUM
        pageForum.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");
        lblForumGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblForumSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
    }

    // Mettre à jour le fond des emojis
    private void appliquerFondEmoji(VBox card, int hboxIndex, String bgEmoji) {
        try {
            // Structure : VBox > HBox > Label(emoji)
            HBox hbox = (HBox) card.getChildren().get(hboxIndex);
            Label lblEmoji = (Label) hbox.getChildren().get(0);
            lblEmoji.setStyle(
                    "-fx-font-size: 24;" +
                            "-fx-background-color: " + bgEmoji + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8;");
        } catch (Exception ignored) {}
    }

    //  Statistiques
    private void chargerStatistiques() {
        List<User> tous = dao.findAll();

        long total       = tous.size();
        long admins      = tous.stream()
                .filter(u -> "Administrateur".equals(u.getRoleNom())).count();
        long enseignants = tous.stream()
                .filter(u -> "Enseignant".equals(u.getRoleNom())).count();
        long etudiants   = tous.stream()
                .filter(u -> "Etudiant".equals(u.getRoleNom())).count();
        long actifs      = tous.stream().filter(User::isActif).count();
        long inactifs    = total - actifs;

        lblTotalUsers.setText(String.valueOf(total));
        lblTotalAdmins.setText(String.valueOf(admins));
        lblTotalEnseignants.setText(String.valueOf(enseignants));
        lblTotalEtudiants.setText(String.valueOf(etudiants));
        lblEvolutionUsers.setText(actifs + " comptes actifs");
        lblActifs.setText(String.valueOf(actifs));
        lblInactifs.setText(String.valueOf(inactifs));

        afficherCartesDerniers(tous.stream().limit(5).toList());
    }

    //  Cartes mini dashboard
    private void afficherCartesDerniers(List<User> users) {
        flowDerniers.getChildren().clear();

        String bgMini   = isDark ? "#0f0f1a" : "#f8fafc";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";

        for (User u : users) {
            String couleur, emoji;
            switch (u.getRoleNom() != null ? u.getRoleNom() : "") {
                case "Administrateur" -> { couleur = "#7c3aed"; emoji = "🛡️"; }
                case "Enseignant"     -> { couleur = "#0ea5e9"; emoji = "👨‍🏫"; }
                case "Etudiant"       -> { couleur = "#10b981"; emoji = "🎓"; }
                default               -> { couleur = "#64748b"; emoji = "👤"; }
            }

            HBox carte = new HBox(12);
            carte.setAlignment(Pos.CENTER_LEFT);
            carte.setPrefWidth(280);
            carte.setStyle(styleMiniNormal(couleur, bgMini));

            String initiales = "";
            if (u.getPrenom() != null && !u.getPrenom().isEmpty())
                initiales += u.getPrenom().substring(0, 1).toUpperCase();
            if (u.getNom() != null && !u.getNom().isEmpty())
                initiales += u.getNom().substring(0, 1).toUpperCase();

            StackPane avatar = new StackPane();
            avatar.setMinSize(40, 40);
            avatar.setMaxSize(40, 40);
            avatar.setStyle(
                    "-fx-background-color: " + couleur + ";" +
                            "-fx-background-radius: 50;");
            Label lblInit = new Label(initiales);
            lblInit.setStyle(
                    "-fx-font-size: 14; -fx-font-weight: bold;" +
                            "-fx-text-fill: white;");
            avatar.getChildren().add(lblInit);

            VBox infos = new VBox(3);
            infos.setAlignment(Pos.CENTER_LEFT);

            Label lblNom = new Label(u.getPrenom() + " " + u.getNom());
            lblNom.setStyle(
                    "-fx-font-size: 13; -fx-font-weight: bold;" +
                            "-fx-text-fill: " + textMain + ";");

            HBox ligne2 = new HBox(8);
            ligne2.setAlignment(Pos.CENTER_LEFT);

            Label lblEmoji = new Label(emoji);
            lblEmoji.setStyle("-fx-font-size: 12;");

            Label lblRole = new Label(u.getRoleNom());
            lblRole.setStyle(
                    "-fx-font-size: 11; -fx-font-weight: bold;" +
                            "-fx-text-fill: " + couleur + ";");

            Label lblSep = new Label("•");
            lblSep.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

            Label lblStatut = new Label(u.isActif() ? "● Actif" : "● Inactif");
            lblStatut.setStyle(
                    "-fx-font-size: 11; -fx-font-weight: bold;" +
                            "-fx-text-fill: " +
                            (u.isActif() ? "#22c55e" : "#f87171") + ";");

            ligne2.getChildren().addAll(lblEmoji, lblRole, lblSep, lblStatut);
            infos.getChildren().addAll(lblNom, ligne2);

            final String c  = couleur;
            final String bg = bgMini;
            carte.setOnMouseEntered(e -> carte.setStyle(styleMiniHover(c)));
            carte.setOnMouseExited(e  -> carte.setStyle(styleMiniNormal(c, bg)));
            carte.setOnMouseClicked(e -> afficherPage("users"));

            carte.getChildren().addAll(avatar, infos);
            flowDerniers.getChildren().add(carte);
        }
    }

    private String styleMiniNormal(String c, String bg) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 10; -fx-padding: 12 15;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 10; -fx-border-width: 1;" +
                "-fx-cursor: hand;";
    }

    private String styleMiniHover(String c) {
        String bgHover = isDark ? "#1a1a2e" : "#f0f4ff";
        return "-fx-background-color: " + bgHover + ";" +
                "-fx-background-radius: 10; -fx-padding: 12 15;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 10; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian," + c + "66,12,0,0,0);" +
                "-fx-cursor: hand;";
    }

    //  Cartes utilisateurs
    private void chargerTousUsers() {
        afficherCartes(dao.findAll());
        afficherStatistiquesRisque();
    }

    //  Cartes étudiants
    private void chargerTousEtudiants() {
        afficherCartesEtudiants(dao.findStudents());
        afficherStatistiquesEtudiants();
    }

    private void afficherCartesEtudiants(List<User> etudiants) {
        flowEtudiants.getChildren().clear();
        if (lblCompteurEtudiants != null)
            lblCompteurEtudiants.setText(etudiants.size() + " étudiant(s)");
        for (User e : etudiants)
            flowEtudiants.getChildren().add(creerCarte(e));
    }

    private void afficherStatistiquesEtudiants() {
        try {
            int totalEtudiants = dao.countStudents();
            int actifs = dao.countActiveStudents();
            int inactifs = dao.countInactiveStudents();
            
            statsEtudiantsPanel.getChildren().clear();
            
            String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
            String textMain = isDark ? "#e2e8f0" : "#1e293b";
            String textSub  = isDark ? "#64748b"  : "#94a3b8";
            
            // Stat 1: Total Etudiants
            VBox stat1 = creerStatCard(
                "🎓",
                "Total Etudiants",
                String.valueOf(totalEtudiants),
                "#10b981",
                bgCard, textMain, textSub
            );
            
            // Stat 2: Actifs
            VBox stat2 = creerStatCard(
                "✅",
                "Etudiants Actifs",
                String.valueOf(actifs),
                "#22c55e",
                bgCard, textMain, textSub
            );
            
            // Stat 3: Inactifs
            VBox stat3 = creerStatCard(
                "❌",
                "Etudiants Inactifs",
                String.valueOf(inactifs),
                "#f87171",
                bgCard, textMain, textSub
            );
            
            statsEtudiantsPanel.getChildren().addAll(stat1, stat2, stat3);
            applyThemeToEtudiantsStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyThemeToEtudiantsStats() {
        String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
        String border   = isDark ? "#2d2d4e" : "#e2e8f0";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";
        
        for (javafx.scene.Node node : statsEtudiantsPanel.getChildren()) {
            if (node instanceof VBox vbox) {
                vbox.setStyle("-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");
            }
        }
    }

    //  Cartes enseignants
    private void chargerTousEnseignants() {
        afficherCartesEnseignants(dao.findTeachers());
        afficherStatistiquesEnseignants();
    }

    private void afficherCartesEnseignants(List<User> enseignants) {
        flowEnseignants.getChildren().clear();
        if (lblCompteurEnseignants != null)
            lblCompteurEnseignants.setText(enseignants.size() + " enseignant(s)");
        for (User e : enseignants)
            flowEnseignants.getChildren().add(creerCarte(e));
    }

    private void afficherStatistiquesEnseignants() {
        try {
            int totalEnseignants = dao.countTeachers();
            int actifs = dao.countActiveTeachers();
            int inactifs = dao.countInactiveTeachers();
            
            statsEnseignantsPanel.getChildren().clear();
            
            String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
            String textMain = isDark ? "#e2e8f0" : "#1e293b";
            String textSub  = isDark ? "#64748b"  : "#94a3b8";
            
            // Stat 1: Total Enseignants
            VBox stat1 = creerStatCard(
                "👨‍🏫",
                "Total Enseignants",
                String.valueOf(totalEnseignants),
                "#0ea5e9",
                bgCard, textMain, textSub
            );
            
            // Stat 2: Actifs
            VBox stat2 = creerStatCard(
                "✅",
                "Enseignants Actifs",
                String.valueOf(actifs),
                "#22c55e",
                bgCard, textMain, textSub
            );
            
            // Stat 3: Inactifs
            VBox stat3 = creerStatCard(
                "❌",
                "Enseignants Inactifs",
                String.valueOf(inactifs),
                "#f87171",
                bgCard, textMain, textSub
            );
            
            statsEnseignantsPanel.getChildren().addAll(stat1, stat2, stat3);
            applyThemeToEnseignantsStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyThemeToEnseignantsStats() {
        String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
        String border   = isDark ? "#2d2d4e" : "#e2e8f0";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";
        
        for (javafx.scene.Node node : statsEnseignantsPanel.getChildren()) {
            if (node instanceof VBox vbox) {
                vbox.setStyle("-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");
            }
        }
    }

    //  Cartes parents
    private void chargerTousParents() {
        afficherCartesParent(dao.findParents());
        afficherStatistiquesParent();
    }

    private void afficherCartesParent(List<User> parents) {
        flowParent.getChildren().clear();
        if (lblCompteurParent != null)
            lblCompteurParent.setText(parents.size() + " parent(s)");
        for (User p : parents)
            flowParent.getChildren().add(creerCarte(p));
    }

    private void afficherStatistiquesParent() {
        try {
            int totalParents = dao.countParents();
            int actifs = dao.countActiveParents();
            int inactifs = dao.countInactiveParents();
            
            statsParentPanel.getChildren().clear();
            
            String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
            String textMain = isDark ? "#e2e8f0" : "#1e293b";
            String textSub  = isDark ? "#64748b"  : "#94a3b8";
            
            // Stat 1: Total Parents
            VBox stat1 = creerStatCard(
                "👨‍👩‍👧",
                "Total Parents",
                String.valueOf(totalParents),
                "#f59e0b",
                bgCard, textMain, textSub
            );
            
            // Stat 2: Actifs
            VBox stat2 = creerStatCard(
                "✅",
                "Parents Actifs",
                String.valueOf(actifs),
                "#22c55e",
                bgCard, textMain, textSub
            );
            
            // Stat 3: Inactifs
            VBox stat3 = creerStatCard(
                "❌",
                "Parents Inactifs",
                String.valueOf(inactifs),
                "#f87171",
                bgCard, textMain, textSub
            );
            
            statsParentPanel.getChildren().addAll(stat1, stat2, stat3);
            applyThemeToParentStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyThemeToParentStats() {
        String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
        String border   = isDark ? "#2d2d4e" : "#e2e8f0";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";
        
        for (javafx.scene.Node node : statsParentPanel.getChildren()) {
            if (node instanceof VBox vbox) {
                vbox.setStyle("-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");
            }
        }
    }

    private void afficherCartes(List<User> users) {
        flowCartes.getChildren().clear();
        if (lblCompteurUsers != null)
            lblCompteurUsers.setText(users.size() + " utilisateur(s)");
        for (User u : users)
            flowCartes.getChildren().add(creerCarte(u));
    }

    /**
     * Affiche les statistiques de risque globales dans le panneau des utilisateurs
     */
    private void afficherStatistiquesRisque() {
        try {
            Map<String, Object> stats = riskDAO.getGlobalRiskStatistics();
            
            riskStatsContainer.getChildren().clear();
            
            String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
            String textMain = isDark ? "#e2e8f0" : "#1e293b";
            String textSub  = isDark ? "#64748b"  : "#94a3b8";
            
            // Stat 1: Total Connexions
            VBox stat1 = creerStatCard(
                "👥",
                "Total Connexions",
                String.valueOf(stats.getOrDefault("totalLogins", 0)),
                "#7c3aed",
                bgCard, textMain, textSub
            );
            stat1.setOnMouseClicked(e -> afficherRapportStatistique("Total Connexions", stats));
            
            // Stat 2: Connexions Bloquées
            VBox stat2 = creerStatCard(
                "🚫",
                "Bloquées",
                String.valueOf(stats.getOrDefault("blockedLogins", 0)),
                "#ef4444",
                bgCard, textMain, textSub
            );
            stat2.setOnMouseClicked(e -> afficherRapportStatistique("Connexions Bloquées", stats));
            
            // Stat 3: Score de la Tentative
            double avgScore = (double) stats.getOrDefault("avgRiskScore", 0.0);
            String avgScoreStr = String.format("%.1f", avgScore);
            VBox stat3 = creerStatCard(
                "📊",
                "Score Tentative",
                avgScoreStr + "/100",
                "#f59e0b",
                bgCard, textMain, textSub
            );
            stat3.setOnMouseClicked(e -> afficherRapportStatistique("Score Tentative", stats));
            
            // Stat 4: Tentatives de Connexion Échouées
            VBox stat4 = creerStatCard(
                "❌",
                "Tentatives Échouées",
                String.valueOf(stats.getOrDefault("failedAttempts", 0)),
                "#10b981",
                bgCard, textMain, textSub
            );
            stat4.setOnMouseClicked(e -> afficherRapportStatistique("Tentatives Échouées", stats));
            
            // Stat 5: Connexions à Risque Élevé
            VBox stat5 = creerStatCard(
                "⚠️",
                "Risque Élevé",
                String.valueOf(stats.getOrDefault("highRiskCount", 0)),
                "#f87171",
                bgCard, textMain, textSub
            );
            stat5.setOnMouseClicked(e -> afficherRapportStatistique("Risque Élevé", stats));
            
            // Stat 6: Dernière Connexion
            String lastConnection = (String) stats.getOrDefault("lastConnectionTime", "N/A");
            VBox stat6 = creerStatCard(
                "🕐",
                "Dernière",
                lastConnection,
                "#0ea5e9",
                bgCard, textMain, textSub
            );
            stat6.setOnMouseClicked(e -> afficherRapportStatistique("Dernière Connexion", stats));
            
            riskStatsContainer.getChildren().addAll(stat1, stat2, stat3, stat4, stat5, stat6);
            
            // Appliquer le thème
            applyThemeToRiskStats();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques de risque: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Rafraîchit les statistiques de risque en temps réel
     */
    public void rafraichirStatistiquesRisque() {
        afficherStatistiquesRisque();
    }
    
    /**
     * Méthode statique pour rafraîchir les statistiques depuis n'importe où
     */
    public static void rafraichirStatistiquesGlobales() {
        if (instance != null) {
            instance.rafraichirStatistiquesRisque();
        }
    }
    
    /**
     * Affiche un rapport détaillé pour une statistique cliquée
     */
    private void afficherRapportStatistique(String nomStat, Map<String, Object> stats) {
        try {
            Stage reportStage = new Stage();
            reportStage.setTitle("Rapport - " + nomStat);
            reportStage.initModality(Modality.APPLICATION_MODAL);
            
            VBox reportContent = new VBox(15);
            reportContent.setPadding(new javafx.geometry.Insets(20));
            reportContent.setStyle(
                "-fx-background-color: " + (isDark ? "#0f0f1a" : "#f1f5f9") + ";"
            );
            
            // Titre du rapport
            Label lblTitre = new Label("📊 " + nomStat);
            lblTitre.setStyle(
                "-fx-font-size: 20; -fx-font-weight: bold;" +
                "-fx-text-fill: " + (isDark ? "#e2e8f0" : "#1e293b") + ";"
            );
            
            // Contenu du rapport selon le type de statistique
            VBox contenuRapport = new VBox(10);
            contenuRapport.setStyle(
                "-fx-background-color: " + (isDark ? "#1a1a2e" : "#ffffff") + ";" +
                "-fx-background-radius: 10; -fx-padding: 15;" +
                "-fx-border-color: " + (isDark ? "#2d2d4e" : "#e2e8f0") + ";" +
                "-fx-border-radius: 10; -fx-border-width: 1;"
            );
            
            String textMain = isDark ? "#e2e8f0" : "#1e293b";
            String textSub = isDark ? "#64748b" : "#94a3b8";
            
            switch (nomStat) {
                case "Total Connexions" -> {
                    int total = (int) stats.getOrDefault("totalLogins", 0);
                    int blocked = (int) stats.getOrDefault("blockedLogins", 0);
                    int successful = total - blocked;
                    
                    ajouterLigneRapport(contenuRapport, "Total des connexions", String.valueOf(total), textMain, textSub);
                    ajouterLigneRapport(contenuRapport, "Connexions réussies", String.valueOf(successful), "#10b981", textSub);
                    ajouterLigneRapport(contenuRapport, "Connexions bloquées", String.valueOf(blocked), "#ef4444", textSub);
                    
                    double pourcentageReussi = total > 0 ? (successful * 100.0 / total) : 0;
                    ajouterLigneRapport(contenuRapport, "Taux de réussite", String.format("%.1f%%", pourcentageReussi), "#7c3aed", textSub);
                }
                case "Connexions Bloquées" -> {
                    int blocked = (int) stats.getOrDefault("blockedLogins", 0);
                    int highRisk = (int) stats.getOrDefault("highRiskCount", 0);
                    
                    ajouterLigneRapport(contenuRapport, "Connexions bloquées", String.valueOf(blocked), "#ef4444", textSub);
                    ajouterLigneRapport(contenuRapport, "Connexions à risque élevé", String.valueOf(highRisk), "#f87171", textSub);
                    ajouterLigneRapport(contenuRapport, "Raison", "Score de risque trop élevé", textSub, textSub);
                }
                case "Score Tentative" -> {
                    double avgScore = (double) stats.getOrDefault("avgRiskScore", 0.0);
                    String scoreStr = String.format("%.1f", avgScore);
                    
                    ajouterLigneRapport(contenuRapport, "Score moyen", scoreStr + "/100", "#f59e0b", textSub);
                    
                    String niveau;
                    String couleur;
                    if (avgScore < 30) {
                        niveau = "Faible";
                        couleur = "#10b981";
                    } else if (avgScore < 60) {
                        niveau = "Moyen";
                        couleur = "#f59e0b";
                    } else {
                        niveau = "Élevé";
                        couleur = "#ef4444";
                    }
                    ajouterLigneRapport(contenuRapport, "Niveau de risque", niveau, couleur, textSub);
                }
                case "Tentatives Échouées" -> {
                    int failed = (int) stats.getOrDefault("failedAttempts", 0);
                    int total = (int) stats.getOrDefault("totalLogins", 0);
                    
                    ajouterLigneRapport(contenuRapport, "Tentatives échouées", String.valueOf(failed), "#ef4444", textSub);
                    ajouterLigneRapport(contenuRapport, "Total des tentatives", String.valueOf(total), textMain, textSub);
                    
                    double pourcentageEchec = total > 0 ? (failed * 100.0 / total) : 0;
                    ajouterLigneRapport(contenuRapport, "Taux d'échec", String.format("%.1f%%", pourcentageEchec), "#ef4444", textSub);
                }
                case "Risque Élevé" -> {
                    int highRisk = (int) stats.getOrDefault("highRiskCount", 0);
                    int total = (int) stats.getOrDefault("totalLogins", 0);
                    
                    ajouterLigneRapport(contenuRapport, "Connexions à risque élevé", String.valueOf(highRisk), "#f87171", textSub);
                    ajouterLigneRapport(contenuRapport, "Total des connexions", String.valueOf(total), textMain, textSub);
                    
                    double pourcentageRisque = total > 0 ? (highRisk * 100.0 / total) : 0;
                    ajouterLigneRapport(contenuRapport, "Pourcentage", String.format("%.1f%%", pourcentageRisque), "#f87171", textSub);
                }
                case "Dernière Connexion" -> {
                    String lastConnection = (String) stats.getOrDefault("lastConnectionTime", "N/A");
                    ajouterLigneRapport(contenuRapport, "Dernière connexion", lastConnection, textMain, textSub);
                    ajouterLigneRapport(contenuRapport, "Statut", "Mise à jour en temps réel", "#10b981", textSub);
                }
            }
            
            ScrollPane scrollRapport = new ScrollPane(contenuRapport);
            scrollRapport.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
            scrollRapport.setFitToWidth(true);
            
            // Bouton fermer
            Button btnFermer = new Button("Fermer");
            btnFermer.setStyle(
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                "-fx-font-size: 13; -fx-padding: 10 20;" +
                "-fx-background-radius: 8; -fx-cursor: hand;"
            );
            btnFermer.setOnAction(e -> reportStage.close());
            
            HBox hboxBouton = new HBox();
            hboxBouton.setAlignment(Pos.CENTER_RIGHT);
            hboxBouton.getChildren().add(btnFermer);
            
            reportContent.getChildren().addAll(lblTitre, scrollRapport, hboxBouton);
            
            Scene scene = new Scene(reportContent, 500, 400);
            reportStage.setScene(scene);
            reportStage.show();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage du rapport: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ajoute une ligne au rapport détaillé
     */
    private void ajouterLigneRapport(VBox parent, String label, String valeur, String couleurValeur, String textSub) {
        HBox ligne = new HBox(15);
        ligne.setAlignment(Pos.CENTER_LEFT);
        
        Label lblLabel = new Label(label + ":");
        lblLabel.setStyle(
            "-fx-font-size: 12; -fx-text-fill: " + textSub + ";"
        );
        lblLabel.setPrefWidth(150);
        
        Label lblValeur = new Label(valeur);
        lblValeur.setStyle(
            "-fx-font-size: 14; -fx-font-weight: bold;" +
            "-fx-text-fill: " + couleurValeur + ";"
        );
        
        ligne.getChildren().addAll(lblLabel, lblValeur);
        parent.getChildren().add(ligne);
    }

    /**
     * Crée une carte de statistique
     */
    private VBox creerStatCard(String emoji, String titre, String valeur, 
                               String couleur, String bgCard, String textMain, String textSub) {
        VBox card = new VBox(8);
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setStyle(
            "-fx-background-color: " + bgCard + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 15;" +
            "-fx-border-color: " + couleur + ";" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        );
        card.setAlignment(Pos.TOP_LEFT);
        
        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 24;");
        
        Label lblTitre = new Label(titre);
        lblTitre.setStyle(
            "-fx-font-size: 11;" +
            "-fx-text-fill: " + textSub + ";"
        );
        
        Label lblValeur = new Label(valeur);
        lblValeur.setStyle(
            "-fx-font-size: 18;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + couleur + ";"
        );
        lblValeur.setWrapText(true);
        
        card.getChildren().addAll(lblEmoji, lblTitre, lblValeur);
        return card;
    }

    /**
     * Applique le thème aux statistiques de risque
     */
    private void applyThemeToRiskStats() {
        String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";
        String textSub  = isDark ? "#64748b"  : "#94a3b8";
        String borderColor = isDark ? "#2d2d4e" : "#e2e8f0";
        
        riskStatsPanel.setStyle(
            "-fx-background-color: " + bgCard + ";" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 20;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        
        lblRiskStatsTitle.setStyle(
            "-fx-font-size: 16;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + textMain + ";"
        );
        
        lblRiskStatsSub.setStyle(
            "-fx-font-size: 11;" +
            "-fx-text-fill: " + textSub + ";"
        );
    }

    private VBox creerCarte(User u) {
        String couleur, emoji;
        switch (u.getRoleNom() != null ? u.getRoleNom() : "") {
            case "Administrateur" -> { couleur = "#7c3aed"; emoji = "🛡️"; }
            case "Enseignant"     -> { couleur = "#0ea5e9"; emoji = "👨‍🏫"; }
            case "Etudiant"       -> { couleur = "#10b981"; emoji = "🎓"; }
            default               -> { couleur = "#64748b"; emoji = "👤"; }
        }

        String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";
        String textSub  = isDark ? "#64748b"  : "#94a3b8";
        String sepColor = isDark ? "#2d2d4e"  : "#e2e8f0";

        VBox carte = new VBox(12);
        carte.setPrefWidth(220);
        carte.setMaxWidth(220);
        carte.setStyle(styleCarteNormal(couleur, bgCard));

        String initiales = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty())
            initiales += u.getPrenom().substring(0, 1).toUpperCase();
        if (u.getNom() != null && !u.getNom().isEmpty())
            initiales += u.getNom().substring(0, 1).toUpperCase();

        StackPane avatar = new StackPane();
        avatar.setMinSize(56, 56);
        avatar.setMaxSize(56, 56);
        avatar.setStyle(
                "-fx-background-color: " + couleur + ";" +
                        "-fx-background-radius: 50;");
        Label lblInit = new Label(initiales);
        lblInit.setStyle(
                "-fx-font-size: 20; -fx-font-weight: bold;" +
                        "-fx-text-fill: white;");
        avatar.getChildren().add(lblInit);

        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 20;");

        HBox header = new HBox(10, avatar, lblEmoji);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblNom = new Label(u.getPrenom() + " " + u.getNom());
        lblNom.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblNom.setWrapText(true);

        Label lblEmail = new Label(u.getEmail());
        lblEmail.setStyle(
                "-fx-font-size: 11; -fx-text-fill: " + textSub + ";");
        lblEmail.setWrapText(true);

        String tel = (u.getTelephone() != null && !u.getTelephone().isEmpty())
                ? u.getTelephone() : "Pas de téléphone";
        Label lblTel = new Label("📞 " + tel);
        lblTel.setStyle(
                "-fx-font-size: 11; -fx-text-fill: " + textSub + ";");

        Label lblRole = new Label(u.getRoleNom());
        lblRole.setStyle(
                "-fx-background-color: " + couleur + "22;" +
                        "-fx-text-fill: " + couleur + ";" +
                        "-fx-background-radius: 20; -fx-padding: 3 12;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;");

        Label lblStatut = new Label(u.isActif() ? "● Actif" : "● Inactif");
        lblStatut.setStyle(
                "-fx-text-fill: " + (u.isActif() ? "#22c55e" : "#f87171") + ";" +
                        "-fx-font-size: 11; -fx-font-weight: bold;");

        HBox badges = new HBox(8, lblRole, lblStatut);
        badges.setAlignment(Pos.CENTER_LEFT);

        Region sep = new Region();
        sep.setStyle("-fx-background-color: " + sepColor + ";");
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);

        Button btnMod = new Button("✏ Modifier");
        btnMod.setMaxWidth(Double.MAX_VALUE);
        btnMod.setStyle(
                "-fx-background-color: #2d1b69; -fx-text-fill: #a78bfa;" +
                        "-fx-background-radius: 7; -fx-padding: 7 0;" +
                        "-fx-font-size: 12; -fx-cursor: hand;");

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.setMaxWidth(Double.MAX_VALUE);
        btnDel.setStyle(
                "-fx-background-color: #2d1b1b; -fx-text-fill: #f87171;" +
                        "-fx-background-radius: 7; -fx-padding: 7 0;" +
                        "-fx-font-size: 12; -fx-cursor: hand;");

        HBox btns = new HBox(8, btnMod, btnDel);
        HBox.setHgrow(btnMod, Priority.ALWAYS);
        HBox.setHgrow(btnDel, Priority.ALWAYS);

        btnMod.setOnAction(e -> ouvrirFormulaire(u));
        btnDel.setOnAction(e -> supprimerUser(u));

        final String c  = couleur;
        final String bg = bgCard;
        carte.setOnMouseEntered(e -> carte.setStyle(styleCarteHover(c)));
        carte.setOnMouseExited(e  -> carte.setStyle(styleCarteNormal(c, bg)));

        carte.getChildren().addAll(
                header, lblNom, lblEmail, lblTel, badges, sep, btns);
        return carte;
    }

    private String styleCarteNormal(String c, String bg) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 14; -fx-padding: 20;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 14; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),10,0,0,4);";
    }

    private String styleCarteHover(String c) {
        String bgHover = isDark ? "#1e1e38" : "#f0f4ff";
        return "-fx-background-color: " + bgHover + ";" +
                "-fx-background-radius: 14; -fx-padding: 20;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 14; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian," + c + "66,20,0,0,0);";
    }

    //  Navigation
    @FXML private void handleMenuDashboard()   { afficherPage("dashboard"); }
    @FXML private void handleMenuUsers()       { afficherPage("users"); }
    @FXML private void handleMenuEtudiants()   { afficherPage("etudiants"); }
    @FXML private void handleMenuEnseignants() { afficherPage("enseignants"); }
    @FXML private void handleMenuParent()      { afficherPage("parent"); }
    @FXML private void handleMenuClasses()     {
        showInfo("Module Classes — bientôt disponible !"); }
    @FXML private void handleMenuEvenement()   { afficherPage("evenement"); }
    @FXML private void handleMenuForum()       { afficherPage("forum"); }

    private void afficherPage(String page) {
        pageDashboard.setVisible(false); pageDashboard.setManaged(false);
        pageUsers.setVisible(false);     pageUsers.setManaged(false);
        pageEtudiants.setVisible(false); pageEtudiants.setManaged(false);
        pageEnseignants.setVisible(false); pageEnseignants.setManaged(false);
        pageParent.setVisible(false); pageParent.setManaged(false);
        pageEvenement.setVisible(false); pageEvenement.setManaged(false);
        pageForum.setVisible(false); pageForum.setManaged(false);

        String textMenu  = isDark ? D_TEXT_MENU : L_TEXT_MENU;
        String styleNormal =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + textMenu + ";" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;";
        String styleActif =
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;";

        for (Button b : new Button[]{
                btnMenuDashboard, btnMenuUsers, btnMenuEtudiants,
                btnMenuEnseignants, btnMenuParent, btnMenuClasses, btnMenuEvenement, btnMenuForum}) {
            b.setStyle(styleNormal);
        }

        switch (page) {
            case "dashboard" -> {
                pageDashboard.setVisible(true);
                pageDashboard.setManaged(true);
                lblPageTitre.setText("Tableau de bord");
                btnMenuDashboard.setStyle(styleActif);
                chargerStatistiques();
            }
            case "users" -> {
                pageUsers.setVisible(true);
                pageUsers.setManaged(true);
                lblPageTitre.setText("Utilisateurs");
                btnMenuUsers.setStyle(styleActif);
                chargerTousUsers();
            }
            case "etudiants" -> {
                pageEtudiants.setVisible(true);
                pageEtudiants.setManaged(true);
                lblPageTitre.setText("Etudiants");
                btnMenuEtudiants.setStyle(styleActif);
                chargerTousEtudiants();
            }
            case "enseignants" -> {
                pageEnseignants.setVisible(true);
                pageEnseignants.setManaged(true);
                lblPageTitre.setText("Enseignants");
                btnMenuEnseignants.setStyle(styleActif);
                chargerTousEnseignants();
            }
            case "parent" -> {
                pageParent.setVisible(true);
                pageParent.setManaged(true);
                lblPageTitre.setText("Parents");
                btnMenuParent.setStyle(styleActif);
                chargerTousParents();
            }
            case "evenement" -> {
                pageEvenement.setVisible(true);
                pageEvenement.setManaged(true);
                lblPageTitre.setText("Evenements");
                btnMenuEvenement.setStyle(styleActif);
            }
            case "forum" -> {
                pageForum.setVisible(true);
                pageForum.setManaged(true);
                lblPageTitre.setText("Forum");
                btnMenuForum.setStyle(styleActif);
            }
        }
    }

    //  Formulaire
    @FXML
    private void handleAjouterUser() { ouvrirFormulaire(null); }

    private void ouvrirFormulaire(User userAModifier) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/user_form.fxml"));
            Stage stage = new Stage();
            stage.setTitle(userAModifier == null
                    ? "Ajouter un utilisateur"
                    : "Modifier l'utilisateur");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            UserFormController ctrl = loader.getController();
            if (userAModifier == null) ctrl.configurerAjout();
            else                       ctrl.configurerModification(userAModifier);

            ctrl.setOnSauvegardeCallback(() -> {
                chargerTousUsers();
                chargerStatistiques();
            });
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    //  Supprimer
    private void supprimerUser(User u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Supprimer " + u.getPrenom() + " " + u.getNom() + " ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (dao.delete(u.getId())) {
                    chargerTousUsers();
                    chargerStatistiques();
                } else {
                    showInfo("Échec de la suppression.");
                }
            }
        });
    }

    //  Recherche
    @FXML
    private void handleRecherche() {
        String kw = txtRecherche.getText().trim();
        if (kw.isEmpty()) { afficherCartes(dao.findAll()); return; }
        afficherCartes(dao.search(kw));
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerTousUsers();
    }

    //  Déconnexion
    @FXML
    private void handleDeconnexion() {
        SessionManager.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/login.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EduNova - Connexion");
            stage.setScene(new Scene(loader.load()));
            stage.show();
            ((Stage) lblPageTitre.getScene().getWindow()).close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}