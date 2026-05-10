package edu.edunova.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class MainController {

    private static final String LIGHT_CLASS = "light";
    private static final String ACTIVE_CLASS = "menu-btn-active";
    private static final String DEFAULT_CLASS = "menu-btn";

    private static boolean lightMode = false;

    @FXML private HBox rootPane;
    @FXML private StackPane contentArea;
    @FXML private Button themeToggleBtn;

    @FXML private Button btnClasses;
    @FXML private Button btnSeances;
    @FXML private Button btnEnvoiEmail;
    @FXML private Button btnEnseignants;
    @FXML private Button btnListeClasses;

    @FXML
    public void initialize() {
        appliquerTheme();
        chargerClasses();
    }

    @FXML
    private void chargerClasses() {
        setActive(btnClasses);
        chargerVue("/AjouterClasse.fxml");
    }

    @FXML
    private void chargerSeances() {
        setActive(btnSeances);
        chargerVue("/AjouterSeance.fxml");
    }

    @FXML
    private void chargerEnvoiEmail() {
        setActive(btnEnvoiEmail);
        chargerVue("/EnvoiEmail.fxml");
    }

    @FXML
    private void chargerEmploisEnseignants() {
        setActive(btnEnseignants);
        chargerVue("/EmploisEnseignants.fxml");
    }

    @FXML
    private void chargerListeClasses() {
        setActive(btnListeClasses);
        chargerVue("/ListeClasses.fxml");
    }

    /**
     * Move the active style to the supplied button. Every other menu button is
     * reset to its default style so only one is highlighted at a time.
     */
    private void setActive(Button current) {
        Button[] all = {btnClasses, btnSeances, btnEnvoiEmail, btnEnseignants, btnListeClasses};
        for (Button b : all) {
            if (b == null) continue;
            b.getStyleClass().removeAll(ACTIVE_CLASS, DEFAULT_CLASS);
            if (b == current) {
                b.getStyleClass().add(ACTIVE_CLASS);
            } else {
                b.getStyleClass().add(DEFAULT_CLASS);
            }
        }
    }

    @FXML
    private void toggleTheme() {
        lightMode = !lightMode;
        appliquerTheme();
    }

    private void appliquerTheme() {
        if (rootPane == null) return;
        if (lightMode) {
            if (!rootPane.getStyleClass().contains(LIGHT_CLASS)) {
                rootPane.getStyleClass().add(LIGHT_CLASS);
            }
            if (themeToggleBtn != null) themeToggleBtn.setText("🌙   Mode sombre");
        } else {
            rootPane.getStyleClass().remove(LIGHT_CLASS);
            if (themeToggleBtn != null) themeToggleBtn.setText("☀   Mode clair");
        }
    }

    private void chargerVue(String fxmlPath) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(vue);
        } catch (Exception e) {
            System.out.println("Erreur chargement vue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
