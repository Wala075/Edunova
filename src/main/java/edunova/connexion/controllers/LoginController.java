package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.dao.RiskDAO;
import edunova.connexion.models.User;
import edunova.connexion.models.RiskData;
import edunova.connexion.tools.*;

import com.google.api.services.oauth2.model.Userinfo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class LoginController {

    // ── Connexion ─────────────────────────────────────────────────
    @FXML private VBox          sidebarLogin;
    @FXML private VBox          panneauConnexion;
    @FXML private TextField     txtEmailO;
    @FXML private PasswordField txtPasswordO;
    @FXML private Label         errLoginEmail;
    @FXML private Label         errLoginPassword;

    // ── Inscription ───────────────────────────────────────────────
    @FXML private VBox             panneauInscription;
    @FXML private TextField        txtRegNom;
    @FXML private TextField        txtRegPrenom;
    @FXML private TextField        txtRegEmail;
    @FXML private TextField        txtRegTel;
    @FXML private PasswordField    txtRegPassword;
    @FXML private TextField        txtRegPasswordVisible;
    @FXML private PasswordField    txtRegConfirm;
    @FXML private CheckBox         chkCgu;
    @FXML private Label            errRegNom;
    @FXML private Label            errRegPrenom;
    @FXML private Label            errRegEmail;
    @FXML private Label            errRegTel;
    @FXML private Label            errRegPassword;
    @FXML private Label            errRegConfirm;
    @FXML private Label            errCgu;
    @FXML private Button           btnGoogleLogin;

    // ── Captcha Mathématique ─────────────────────────────────────
    @FXML private CheckBox chkCaptcha;
    @FXML private VBox vboxCaptchaQuestion;
    @FXML private Label lblCaptchaQuestion;
    @FXML private TextField txtCaptchaReponse;
    @FXML private Button btnVerifierCaptcha;
    @FXML private Label errCaptcha;
    private boolean captchaValide = false;
    private int reponseCorrecteCaptcha = 0;

    // ── Dropdown téléphone ────────────────────────────────────────
    @FXML private Button    btnPaysReg;
    @FXML private VBox      dropdownPaysReg;
    @FXML private TextField txtRecherchePaysReg;
    @FXML private VBox      listePaysReg;
    private boolean dropdownRegVisible = false;
    private String  codePaysCourantReg = "+216";

    private final UserDAO dao = new UserDAO();

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Apply background image to sidebar
        applySidebarBackground();
        
        // Validations temps réel connexion
        txtEmailO.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerLoginEmail();
            else errLoginEmail.setText("");
        });
        txtPasswordO.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerLoginPassword();
            else errLoginPassword.setText("");
        });

        // Validations temps réel inscription
        txtRegNom.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegNom(); });
        txtRegPrenom.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegPrenom(); });
        txtRegEmail.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegEmail(); });
        txtRegPassword.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegPassword(); });
        txtRegConfirm.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegConfirm(); });

        // Recherche pays en temps réel
        txtRecherchePaysReg.textProperty()
                .addListener((obs, old, n) ->
                        filtrerPays(n, listePaysReg,
                                true, btnPaysReg));
    }

    /**
     * Applies a background image to the sidebar
     */
    private void applySidebarBackground() {
        if (sidebarLogin != null) {
            try {
                // Load the background image
                String imagePath = getClass().getResource("/images/login_sidebar_bg.jpg").toExternalForm();
                
                // Create background image with larger dimensions (450x660)
                javafx.scene.image.Image bgImage = new javafx.scene.image.Image(imagePath, 450, 660, false, true);
                
                // Create background fill with the image - simple approach
                javafx.scene.layout.BackgroundImage backgroundImage = new javafx.scene.layout.BackgroundImage(
                    bgImage,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundPosition.DEFAULT,
                    new javafx.scene.layout.BackgroundSize(
                        javafx.scene.layout.BackgroundSize.AUTO,
                        javafx.scene.layout.BackgroundSize.AUTO,
                        true, true, false, false
                    )
                );
                
                // Apply background to sidebar
                sidebarLogin.setBackground(new javafx.scene.layout.Background(backgroundImage));
                
                System.out.println("✅ Background image applied to sidebar");
                
            } catch (Exception e) {
                System.err.println("⚠️ Could not load background image, using gradient fallback: " + e.getMessage());
                // Fallback to gradient if image fails to load
                applySidebarGradient();
            }
        }
    }
    
    /**
     * Applies a gradient background to the sidebar (fallback)
     */
    private void applySidebarGradient() {
        if (sidebarLogin != null) {
            // Create a gradient background using JavaFX
            javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                0, 0,           // Start X, Y
                0, 1,           // End X, Y (1 = 100% of height)
                true,           // proportional
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#0f1e3c")),      // Dark blue
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#1e508c"))       // Light blue
            );
            
            sidebarLogin.setStyle(
                "-fx-padding: 40 28;"
            );
            sidebarLogin.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                    gradient,
                    new javafx.scene.layout.CornerRadii(0),
                    new javafx.geometry.Insets(0)
                )
            ));
        }
    }

    // ── Captcha Mathématique ─────────────────────────────────────
    @FXML
    private void handleCaptchaCheckbox() {
        if (chkCaptcha.isSelected()) {
            // Générer une nouvelle question
            genererQuestionCaptcha();
            vboxCaptchaQuestion.setVisible(true);
            vboxCaptchaQuestion.setManaged(true);
            txtCaptchaReponse.clear();
            txtCaptchaReponse.requestFocus();
        } else {
            vboxCaptchaQuestion.setVisible(false);
            vboxCaptchaQuestion.setManaged(false);
            captchaValide = false;
            errCaptcha.setText("");
        }
    }

    private void genererQuestionCaptcha() {
        int num1 = (int) (Math.random() * 10) + 1;
        int num2 = (int) (Math.random() * 10) + 1;
        reponseCorrecteCaptcha = num1 + num2;
        lblCaptchaQuestion.setText(num1 + " + " + num2 + " = ?");
        errCaptcha.setText("");
    }

    @FXML
    private void handleVerifierCaptcha() {
        String reponse = txtCaptchaReponse.getText().trim();
        
        if (reponse.isEmpty()) {
            errCaptcha.setText("⚠ Veuillez entrer votre réponse.");
            return;
        }
        
        try {
            int reponseUtilisateur = Integer.parseInt(reponse);
            
            if (reponseUtilisateur == reponseCorrecteCaptcha) {
                captchaValide = true;
                errCaptcha.setText("✅ Correct!");
                errCaptcha.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                btnVerifierCaptcha.setDisable(true);
                txtCaptchaReponse.setDisable(true);
            } else {
                captchaValide = false;
                errCaptcha.setText("❌ Incorrect, réessayez.");
                errCaptcha.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                txtCaptchaReponse.clear();
                txtCaptchaReponse.requestFocus();
            }
        } catch (NumberFormatException e) {
            errCaptcha.setText("⚠ Veuillez entrer un nombre.");
        }
    }

    private void resetCaptchaLogin() {
        captchaValide = false;
        chkCaptcha.setSelected(false);
        vboxCaptchaQuestion.setVisible(false);
        vboxCaptchaQuestion.setManaged(false);
        txtCaptchaReponse.clear();
        txtCaptchaReponse.setDisable(false);
        btnVerifierCaptcha.setDisable(false);
        errCaptcha.setText("");
        errCaptcha.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11;");
    }
    // ══════════════════════════════════════════════════════════════
    //  DROPDOWN TÉLÉPHONE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleToggleDropdownReg() {
        dropdownRegVisible = !dropdownRegVisible;
        dropdownPaysReg.setVisible(dropdownRegVisible);
        dropdownPaysReg.setManaged(dropdownRegVisible);
        if (dropdownRegVisible) {
            remplirListePays(listePaysReg, true, btnPaysReg);
            txtRecherchePaysReg.clear();
            txtRecherchePaysReg.requestFocus();
        }
    }

    private void remplirListePays(VBox liste,
                                  boolean light, Button btnPays) {
        liste.getChildren().clear();
        PhonePickerController.PAYS.forEach((pays, code) ->
                liste.getChildren().add(
                        PhonePickerController.creerItem(
                                pays, code, !light,
                                () -> selectionnerPays(
                                        pays, code, btnPays))));
    }

    private void filtrerPays(String keyword, VBox liste,
                             boolean light, Button btnPays) {
        liste.getChildren().clear();
        PhonePickerController.PAYS.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase()
                        .contains(keyword.toLowerCase()) ||
                        e.getValue().contains(keyword))
                .forEach(e -> liste.getChildren().add(
                        PhonePickerController.creerItem(
                                e.getKey(), e.getValue(), !light,
                                () -> selectionnerPays(
                                        e.getKey(), e.getValue(), btnPays))));
    }

    private void selectionnerPays(String pays, String code,
                                  Button btnPays) {
        // Bouton : emoji + code
        String emoji = PhonePickerController.getEmoji(pays);
        btnPays.setText(emoji + "  " + code);
        codePaysCourantReg = code;

        // Nettoyer le numéro existant
        String numero = txtRegTel.getText()
                .replaceAll("^\\+\\d+\\s*", "").trim();
        txtRegTel.setText(numero);

        // Fermer dropdown
        dropdownRegVisible = false;
        dropdownPaysReg.setVisible(false);
        dropdownPaysReg.setManaged(false);
        txtRegTel.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════
    //  CONNEXION
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        boolean e = validerLoginEmail();
        boolean p = validerLoginPassword();

        // Vérifier captcha
        if (!captchaValide) {
            errCaptcha.setText(
                    "⚠ Veuillez cocher 'Je ne suis pas un robot' et résoudre la vérification.");
            return;
        }
        errCaptcha.setText("");

        if (!e || !p) return;
        effectuerConnexion();
    }

    private void effectuerConnexion() {
        String email    = txtEmailO.getText().trim();
        String password = txtPasswordO.getText();

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT u.id_u, u.password_u, " +
                            "       u.nom_u, u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND u.actif_u = 1";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id_u");
                
                if (PasswordUtils.verify(password,
                        rs.getString("password_u"))) {

                    // 🔍 Analyser le risque de connexion
                    RiskDAO riskDAO = new RiskDAO();
                    java.util.Map<String, Object> userHistory = riskDAO.getUserConnectionHistory(userId);
                    
                    RiskData riskData = 
                        RiskAnalyzerIA.analyzeRisk(
                            userId,
                            "127.0.0.1", // IP locale pour test
                            "Tunisia",   // Pays
                            "Windows",   // Device
                            0,           // Pas de tentatives échouées
                            50.0,        // Vitesse de saisie normale
                            userHistory
                        );
                    
                    // Enregistrer l'analyse de risque
                    riskDAO.insertRiskData(riskData);
                    
                    // Vérifier si la connexion est bloquée
                    if (riskData.isBlocked()) {
                        showAlert("❌ Connexion Bloquée\n\n" +
                                "Score de risque trop élevé: " + riskData.getRiskScore() + "/100\n" +
                                "Niveau: " + riskData.getRiskLevel() + "\n\n" +
                                "Veuillez contacter l'administrateur.");
                        resetCaptchaLogin();
                        // Rafraîchir les statistiques du dashboard
                        DashboardController.rafraichirStatistiquesGlobales();
                        return;
                    }

                    String role = rs.getString("nom_r");
                    SessionManager s =
                            SessionManager.getInstance();
                    s.setUserId(userId);
                    s.setEmail(email);
                    s.setRole(role);
                    
                    // Ajouter le score de risque à la session
                    s.setRiskScore(riskData.getRiskScore());

                    enregistrerHistorique(conn,
                            userId, true);
                    ouvrirDashboard();
                    resetCaptchaLogin();

                } else {
                    enregistrerHistorique(conn,
                            rs.getInt("id_u"), false);
                    setErreur(txtPasswordO,
                            errLoginPassword,
                            "Mot de passe incorrect.");
                    resetCaptchaLogin();
                    // Rafraîchir les statistiques du dashboard
                    DashboardController.rafraichirStatistiquesGlobales();
                }
            } else {
                setErreur(txtEmailO, errLoginEmail,
                        "Aucun compte actif trouvé.");
            }

        } catch (SQLException ex) {
            showAlert("Erreur BD : " + ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur : " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  GOOGLE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleGoogleLogin() {
        try {
            // Charger la fenêtre Google OAuth2
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/google_oauth2_window.fxml"));
            VBox root = loader.load();
            
            GoogleOAuth2WindowController controller = loader.getController();
            
            // Créer la fenêtre avec une taille augmentée
            Stage googleWindow = new Stage();
            googleWindow.setTitle("Connexion Google");
            googleWindow.setScene(new Scene(root, 500, 600));
            googleWindow.initModality(Modality.APPLICATION_MODAL);
            googleWindow.setResizable(false);
            
            controller.setStage(googleWindow);
            
            // Callback de succès
            controller.setOnSuccessCallback(() -> {
                String code = controller.getAuthorizationCode();
                if (code != null && !code.isEmpty()) {
                    // Traiter le code d'autorisation
                    traiterCodeGoogleOAuth2(code);
                }
            });
            
            // Afficher la fenêtre
            googleWindow.showAndWait();
            
        } catch (Exception ex) {
            showAlert("❌ Erreur: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void traiterCodeGoogleOAuth2(String code) {
        new Thread(() -> {
            try {
                System.out.println("LoginController: Traitement du code Google OAuth2");
                
                // Échanger le code pour les infos utilisateur
                String[] userInfo = GoogleOAuth2Service.echangerCodePourInfos(code);
                
                if (userInfo != null && userInfo.length >= 1) {
                    String email = userInfo[0];
                    String nom = userInfo.length > 1 ? userInfo[1] : "";
                    String prenom = userInfo.length > 2 ? userInfo[2] : "";
                    
                    Platform.runLater(() -> {
                        traiterConnexionGoogle(email, nom, prenom);
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("❌ Impossible de récupérer les informations Google");
                    });
                }
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showAlert("❌ Erreur lors du traitement: " + ex.getMessage());
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void traiterConnexionGoogle(
            String email, String nom, String prenom) {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT u.id_u, u.nom_u, " +
                            "       u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND u.actif_u = 1";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id_u");
                String role = rs.getString("nom_r");
                
                // Enregistrer les données de connexion dans la BD
                enregistrerDonneesConnexion(conn, userId);
                
                SessionManager s =
                        SessionManager.getInstance();
                s.setUserId(userId);
                s.setEmail(email);
                s.setRole(role);
                enregistrerHistorique(conn, userId, true);
                showAlert("✅ Connexion Google réussie !\n" +
                        "Bienvenue " +
                        rs.getString("prenom_u") + " " +
                        rs.getString("nom_u"));
                ouvrirDashboard();
                // Rafraîchir les statistiques du dashboard
                DashboardController.rafraichirStatistiquesGlobales();
            } else {
                showAlert("📋 Compte Google non trouvé.\n\n" +
                        "Veuillez compléter votre inscription.");
                panneauConnexion.setVisible(false);
                panneauConnexion.setManaged(false);
                panneauInscription.setVisible(true);
                panneauInscription.setManaged(true);
                txtRegNom.setText(nom);
                txtRegPrenom.setText(prenom);
                txtRegEmail.setText(email);
                String mdpTemp = UUID.randomUUID()
                        .toString().substring(0, 8);
                txtRegPassword.setText(mdpTemp);
                txtRegConfirm.setText(mdpTemp);
                showAlert("🔑 Mot de passe temporaire :\n" +
                        mdpTemp);
            }
        } catch (SQLException ex) {
            showAlert("Erreur BD : " + ex.getMessage());
        }
    }

    // ── Ouvrir Dashboard ──────────────────────────────────────────
    private void ouvrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/views/dashboard.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EduNova — Dashboard");
            stage.setScene(new Scene(
                    loader.load(), 1100, 700));
            stage.show();
            ((Stage) txtEmailO.getScene()
                    .getWindow()).close();
        } catch (Exception ex) {
            showAlert("Erreur : " + ex.getMessage());
        }
    }

    // ── Historique ────────────────────────────────────────────────
    private void enregistrerHistorique(
            Connection conn, int userId, boolean succes) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO login_history " +
                            "(user_id, adresse_ip_lh, succes_lh) " +
                            "VALUES (?, ?, ?)");
            stmt.setInt(1, userId);
            stmt.setString(2, "127.0.0.1");
            stmt.setBoolean(3, succes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(
                    "Erreur historique : " + e.getMessage());
        }
    }

    // ── Enregistrer Données de Connexion ──────────────────────────
    private void enregistrerDonneesConnexion(
            Connection conn, int userId) {
        try {
            RiskDAO riskDAO = new RiskDAO();
            
            // Créer un objet RiskData avec les informations de connexion
            RiskData riskData = new RiskData();
            riskData.setUserId(userId);
            riskData.setIpAddress("127.0.0.1");
            riskData.setCountry("Local");
            riskData.setDevice("Desktop");
            riskData.setLoginTime(java.time.LocalDateTime.now());
            riskData.setFailedAttempts(0);
            riskData.setTypingSpeed(0.0);
            
            // Insérer les données de risque
            riskDAO.insertRiskData(riskData);
            
            System.out.println("LoginController: Données de connexion enregistrées pour l'utilisateur " + userId);
            
        } catch (Exception e) {
            System.out.println(
                    "Erreur enregistrement données connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Mot de passe oublié ───────────────────────────────────────
    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/views/forgot_password.fxml"));
            Stage stage = new Stage();
            stage.setTitle(
                    "EduNova - Mot de passe oublié");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(
                    txtEmailO.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  INSCRIPTION
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleShowInscription() {
        panneauConnexion.setVisible(false);
        panneauConnexion.setManaged(false);
        panneauInscription.setVisible(true);
        panneauInscription.setManaged(true);
        effacerErreursInscription();
    }

    @FXML
    private void handleShowConnexion() {
        panneauInscription.setVisible(false);
        panneauInscription.setManaged(false);
        panneauConnexion.setVisible(true);
        panneauConnexion.setManaged(true);
    }

    @FXML
    private void handleInscription() {
        boolean n  = validerRegNom();
        boolean p  = validerRegPrenom();
        boolean e  = validerRegEmail();
        boolean pw = validerRegPassword();
        boolean cf = validerRegConfirm();
        boolean cg = validerCgu();
        if (!n || !p || !e || !pw || !cf || !cg)
            return;

        User u = new User();
        u.setNom(txtRegNom.getText().trim());
        u.setPrenom(txtRegPrenom.getText().trim());
        u.setEmail(txtRegEmail.getText().trim());
        String numero = txtRegTel.getText().trim();
        if (!numero.isEmpty() && !numero.startsWith("+"))
            numero = codePaysCourantReg + " " + numero;
        u.setTelephone(numero);
        u.setPassword(txtRegPassword.getText());
        u.setActif(true);

        if (dao.insert(u)) {
            showAlert("✅ Compte créé avec succès !\n\n" +
                    "Bienvenue " + u.getPrenom() +
                    " " + u.getNom() + " !");
            handleShowConnexion();
        } else {
            setErreurLabel(errRegEmail,
                    "Cet email est déjà utilisé.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  VALIDATIONS
    // ══════════════════════════════════════════════════════════════

    private boolean validerLoginEmail() {
        String v = txtEmailO.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtEmailO, errLoginEmail,
                    "L'email est obligatoire.");
            return false;
        }
        if (!v.matches(
                "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setErreur(txtEmailO, errLoginEmail,
                    "Format invalide.");
            return false;
        }
        setOk(txtEmailO, errLoginEmail);
        return true;
    }

    private boolean validerLoginPassword() {
        String v = txtPasswordO.getText();
        if (v.isEmpty()) {
            setErreur(txtPasswordO, errLoginPassword,
                    "Le mot de passe est obligatoire.");
            return false;
        }
        setOk(txtPasswordO, errLoginPassword);
        return true;
    }

    private boolean validerRegNom() {
        String v = txtRegNom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegNom, errRegNom, "Obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtRegNom, errRegNom,
                    "Lettres uniquement (2-50 car.)");
            return false;
        }
        setOk(txtRegNom, errRegNom);
        return true;
    }

    private boolean validerRegPrenom() {
        String v = txtRegPrenom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegPrenom, errRegPrenom,
                    "Obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtRegPrenom, errRegPrenom,
                    "Lettres uniquement (2-50 car.)");
            return false;
        }
        setOk(txtRegPrenom, errRegPrenom);
        return true;
    }

    private boolean validerRegEmail() {
        String v = txtRegEmail.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegEmail, errRegEmail,
                    "Obligatoire.");
            return false;
        }
        if (!v.matches(
                "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setErreur(txtRegEmail, errRegEmail,
                    "Format invalide.");
            return false;
        }
        setOk(txtRegEmail, errRegEmail);
        return true;
    }

    private boolean validerRegPassword() {
        String v = txtRegPassword.getText();
        if (v.isEmpty()) {
            setErreur(txtRegPassword, errRegPassword,
                    "Obligatoire.");
            return false;
        }
        if (v.length() < 6) {
            setErreur(txtRegPassword, errRegPassword,
                    "Minimum 6 caractères.");
            return false;
        }
        setOk(txtRegPassword, errRegPassword);
        return true;
    }

    private boolean validerRegConfirm() {
        String v1 = txtRegPassword.getText();
        String v2 = txtRegConfirm.getText();
        if (v2.isEmpty()) {
            setErreur(txtRegConfirm, errRegConfirm,
                    "Obligatoire.");
            return false;
        }
        if (!v1.equals(v2)) {
            setErreur(txtRegConfirm, errRegConfirm,
                    "Ne correspondent pas.");
            return false;
        }
        setOk(txtRegConfirm, errRegConfirm);
        return true;
    }

    private boolean validerCgu() {
        if (!chkCgu.isSelected()) {
            errCgu.setText(
                    "⚠ Vous devez accepter les conditions.");
            return false;
        }
        errCgu.setText("");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private static final String STYLE_BASE =
            "-fx-background-color: #f8fafc;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-prompt-text-fill: #94a3b8;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 11 14;" +
                    "-fx-font-size: 13;";

    private void setErreur(Control champ,
                           Label errLabel, String msg) {
        errLabel.setText("⚠ " + msg);
        champ.setStyle(STYLE_BASE +
                "-fx-border-color: #ef4444;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;");
    }

    private void setOk(Control champ, Label errLabel) {
        errLabel.setText("");
        champ.setStyle(STYLE_BASE +
                "-fx-border-color: #22c55e;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;");
    }

    private void setErreurLabel(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
    }

    private void effacerErreursInscription() {
        txtRegNom.clear();
        txtRegPrenom.clear();
        txtRegEmail.clear();
        txtRegTel.clear();
        txtRegPassword.clear();
        txtRegConfirm.clear();
        chkCgu.setSelected(false);
        errRegNom.setText("");
        errRegPrenom.setText("");
        errRegEmail.setText("");
        errRegTel.setText("");
        errRegPassword.setText("");
        errRegConfirm.setText("");
        errCgu.setText("");
        dropdownRegVisible = false;
        dropdownPaysReg.setVisible(false);
        dropdownPaysReg.setManaged(false);
        btnPaysReg.setText("🇹🇳 +216");
        codePaysCourantReg = "+216";
    }

    private int getRoleId(String roleNom) {
        List<String[]> roles = dao.findAllRoles();
        for (String[] role : roles)
            if (role[1].equals(roleNom))
                return Integer.parseInt(role[0]);
        return 1;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("EduNova");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}