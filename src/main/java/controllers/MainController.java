package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private VBox       sidebar;
    @FXML private VBox       mainContent;
    @FXML private StackPane  pageContainer;

    @FXML private Label lblPageTitre;
    @FXML private Label lblPageSousTitre;
    @FXML private Label lblDateHeure;
    @FXML private Label lblUserNom;
    @FXML private Label lblAvatarInitiales;
    @FXML private TextField txtRechercheGlobal;

    @FXML private Button btnMenuDashboard;

    @FXML private Button btnMenuLive;
    @FXML private Button btnMenuPresences;
    @FXML private Button btnMenuCoursTeacher;
    @FXML private Button btnMenuCoursStudent;
    @FXML private Button btnMenuQuizTeacher;
    @FXML private Button btnMenuQuizStudent;
    @FXML private Button btnToggleTheme;

    private boolean darkMode = true;

    // Styles des boutons du menu (même look que l'équipe)
    private static final String NAV_ACTIVE =
            "-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-background-radius: 8; -fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                    "-fx-cursor: hand; -fx-font-size: 13;";
    private static final String NAV_INACTIVE_DARK =
            "-fx-background-color: transparent; -fx-text-fill: #a8a8c0;" +
                    "-fx-background-radius: 8; -fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                    "-fx-cursor: hand; -fx-font-size: 13;";
    private static final String NAV_INACTIVE_LIGHT =
            "-fx-background-color: transparent; -fx-text-fill: #6b6b85;" +
                    "-fx-background-radius: 8; -fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                    "-fx-cursor: hand; -fx-font-size: 13;";

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblDateHeure.setText(LocalDateTime.now().format(fmt));

        // Charge le tableau de bord par défaut
        Platform.runLater(this::openDashboard);
    }

    /* ============== NAVIGATION ============== */

    @FXML
    public void openDashboard() {
        loadView("/Dashboard.fxml", "Tableau de bord", "Vue d'ensemble", btnMenuDashboard);
    }



    @FXML
    public void openLiveSessions() {
        loadView("/LiveSessions.fxml", "Cours en ligne",
                "Sessions Meet / Zoom", btnMenuLive);
    }

    @FXML
    public void openPresences() {
        loadView("/Presences.fxml", "Présences",
                "Suivi des présences étudiants", btnMenuPresences);
    }

    @FXML
    public void openCoursTeacher() {
        loadView("/CoursTeacher.fxml", "Cours — Espace Enseignant",
                "Gérer les cours et supports pédagogiques", btnMenuCoursTeacher);
    }

    @FXML
    public void openCoursStudent() {
        loadView("/CoursStudent.fxml", "Cours — Espace Étudiant",
                "Consulter et réviser mes cours", btnMenuCoursStudent);
    }

    @FXML
    public void openQuizTeacher() {
        loadView("/QuizTeacher.fxml", "Quiz — Espace Enseignant",
                "Créer et gérer les QCM", btnMenuQuizTeacher);
    }

    @FXML
    public void openQuizStudent() {
        loadView("/QuizStudent.fxml", "Quiz — Espace Étudiant",
                "Passer les quiz disponibles", btnMenuQuizStudent);
    }

    private void loadView(String fxml, String title, String subtitle, Button activeBtn) {
        try {
            URL url = getClass().getResource(fxml);
            if (url == null) {
                System.err.println("FXML introuvable : " + fxml);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Node node = loader.load();
            pageContainer.getChildren().setAll(node);
            lblPageTitre.setText(title);
            lblPageSousTitre.setText(subtitle);
            setActive(activeBtn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button activeBtn) {
        String inactive = darkMode ? NAV_INACTIVE_DARK : NAV_INACTIVE_LIGHT;
        Button[] all = {btnMenuDashboard, btnMenuLive,
                        btnMenuPresences, btnMenuCoursTeacher, btnMenuCoursStudent,
                        btnMenuQuizTeacher, btnMenuQuizStudent};
        for (Button b : all) {
            if (b != null) b.setStyle(inactive);
        }
        if (activeBtn != null) {
            activeBtn.setStyle(NAV_ACTIVE);
        }
    }

    /* ============== TOGGLE DARK / LIGHT ============== */

    @FXML
    public void toggleTheme() {
        darkMode = !darkMode;

        // Swap aussi le CSS file pour les sous-vues (cards, tables, btn-primary, etc.)
        // On garde TOUJOURS pro.css en plus de dark/light
        try {
            String css = darkMode ? "/dark.css" : "/light.css";
            URL cssUrl   = getClass().getResource(css);
            URL proUrl   = getClass().getResource("/pro.css");
            if (cssUrl != null && rootPane.getScene() != null) {
                rootPane.getScene().getStylesheets().clear();
                rootPane.getScene().getStylesheets().add(cssUrl.toExternalForm());
                if (proUrl != null) rootPane.getScene().getStylesheets().add(proUrl.toExternalForm());
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (darkMode) {
            applyDarkTheme();
            btnToggleTheme.setText(" Light");
        } else {
            applyLightTheme();
            btnToggleTheme.setText(" Dark");
        }
        // Re-apply nav state (active button keeps purple, others adapt)
        Button current = findActive();
        setActive(current);
    }

    private Button findActive() {
        Button[] all = {btnMenuDashboard, btnMenuLive,
                        btnMenuPresences, btnMenuCoursTeacher, btnMenuCoursStudent,
                        btnMenuQuizTeacher, btnMenuQuizStudent};
        for (Button b : all) {
            if (b != null && b.getStyle().contains("#7c3aed") && b.getStyle().contains("white")) {
                return b;
            }
        }
        return btnMenuDashboard;
    }

    private void applyDarkTheme() {
        rootPane.setStyle("-fx-background-color: #0f0f1f;");
        sidebar.setStyle("-fx-background-color: #1a1a2e;");
        mainContent.setStyle("-fx-background-color: #0f0f1f;");
        pageContainer.setStyle("-fx-background-color: #0f0f1f; -fx-padding: 25;");
        lblPageTitre.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");
        lblPageSousTitre.setStyle("-fx-font-size: 12; -fx-text-fill: #a8a8c0;");
        lblDateHeure.setStyle("-fx-font-size: 12; -fx-padding: 0 0 0 20; -fx-text-fill: #a8a8c0;");
        if (lblUserNom != null) {
            lblUserNom.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: white;");
        }
        txtRechercheGlobal.setStyle(
                "-fx-background-color: #1a1a2e; -fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #6b6b85; -fx-background-radius: 20;" +
                        "-fx-padding: 8 16; -fx-border-color: #2d2d4e; -fx-border-radius: 20;");
    }

    private void applyLightTheme() {
        rootPane.setStyle("-fx-background-color: #f4f4f9;");
        sidebar.setStyle("-fx-background-color: white;");
        mainContent.setStyle("-fx-background-color: #f4f4f9;");
        pageContainer.setStyle("-fx-background-color: #f4f4f9; -fx-padding: 25;");
        lblPageTitre.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        lblPageSousTitre.setStyle("-fx-font-size: 12; -fx-text-fill: #6b6b85;");
        lblDateHeure.setStyle("-fx-font-size: 12; -fx-padding: 0 0 0 20; -fx-text-fill: #6b6b85;");
        if (lblUserNom != null) {
            lblUserNom.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: #1a1a2e;");
        }
        txtRechercheGlobal.setStyle(
                "-fx-background-color: white; -fx-text-fill: #1a1a2e;" +
                        "-fx-prompt-text-fill: #6b6b85; -fx-background-radius: 20;" +
                        "-fx-padding: 8 16; -fx-border-color: #e0e0ec; -fx-border-radius: 20;");
    }

    @FXML
    public void logout() {
        Stage stage = (Stage) sidebar.getScene().getWindow();
        stage.close();
    }
}
