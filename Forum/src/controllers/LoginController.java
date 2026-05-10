package controllers;

import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UserService;
import utils.AlertHelper;
import utils.SceneManager;
import utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField     tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lbErreur;
    @FXML private Button        btnAdmin;
    @FXML private Button        btnEtudiant;
    @FXML private Button        btnParent;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lbErreur.setText("");

        // Quick-fill buttons for development/demo — remove before production deployment
        btnAdmin.setOnAction(e -> {
            tfUsername.setText("admin@edunova.com");
            pfPassword.setText("admin123");
        });
        btnEtudiant.setOnAction(e -> {
            tfUsername.setText("etudiant@edunova.com");
            pfPassword.setText("etudiant123");
        });
        btnParent.setOnAction(e -> {
            tfUsername.setText("parent@edunova.com");
            pfPassword.setText("parent123");
        });
    }

    @FXML private void seConnecter() {
        String email = tfUsername.getText().trim();
        String pass  = pfPassword.getText().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        try {
            User u = userService.login(email, pass);

            if (u == null) {
                afficherErreur("Email ou mot de passe incorrect.");
                return;
            }

            SessionManager.setCurrentUser(u);
            Stage stage = (Stage) tfUsername.getScene().getWindow();

            // Routage selon le role
            if (u.isAdmin()) {
                SceneManager.naviguer("ForumAdmin.fxml", stage, "EduNova — Forum Admin");
            } else {
                // student, parent, teacher -> meme interface membre
                SceneManager.naviguer("ForumMembre.fxml", stage, "EduNova — Forum");
            }

        } catch (Exception e) {
            afficherErreur("Erreur de connexion : " + e.getMessage());
        }
    }

    private void afficherErreur(String msg) {
        lbErreur.setStyle("-fx-text-fill:#ef4444;-fx-font-size:12px;-fx-font-weight:600;");
        lbErreur.setText(msg);
    }
}