package controllers;

import edu.edunova.entities.Matiere;
import edu.edunova.services.AlerteService;
import edu.edunova.services.MatiereService;
import edu.edunova.services.NoteService;
import edu.edunova.services.StudentService;
import edu.edunova.utils.MyConnection;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Dashboard {

    // ===== Sidebar =====
    @FXML private BorderPane rootPane;
    @FXML private Label lbAvatarInitiales;
    @FXML private Label lbHelloName;
    @FXML private Label lbProfileName;
    @FXML private Label lbAlerteBadge;

    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuAjouter;
    @FXML private Button btnMenuListe;
    @FXML private Button btnMenuBulletin;
    @FXML private Button btnMenuAlertes;
    @FXML private Button btnMenuCalendrier;
    @FXML private Button btnMenuParents;
    @FXML private Button btnToggleTheme;

    // ===== Navbar =====
    @FXML private Label lblPageTitre;
    @FXML private Label lblPageSousTitre;
    @FXML private Label lblDateHeure;

    // ===== Stats cards =====
    @FXML private Label lbStudentsCount;
    @FXML private Label lbActivityCount;
    @FXML private Label lbAlertesCount;
    @FXML private Label lbMoyenneEcole;

    // ===== Mes matières =====
    @FXML private Label lbCurrentAnnee;
    @FXML private VBox m1Card, m2Card, m3Card, m4Card;
    @FXML private Label m1Tag, m2Tag, m3Tag, m4Tag;
    @FXML private Label m1Title, m2Title, m3Title, m4Title;
    @FXML private Label m1Students, m2Students, m3Students, m4Students;
    @FXML private Label m1Rating, m2Rating, m3Rating, m4Rating;

    // ===== Répartition élèves =====
    @FXML private Label lbExcellence;
    @FXML private Label lbReussite;
    @FXML private Label lbDifficulte;

    private String currentAnnee;
    private boolean darkMode = true;
    private Timeline clock;
    private final String[] matiereNames = new String[4]; // pour clic carte

    @FXML
    public void initialize() {
        currentAnnee = currentAnneeScolaire();
        if (lbCurrentAnnee != null) lbCurrentAnnee.setText("Année " + currentAnnee);
        if (lbAvatarInitiales != null) {
            lbAvatarInitiales.setText(initialesFrom(safeText(lbHelloName)));
        }

        startClock();
        loadAll();
    }

    /** Recharge toutes les données du dashboard. */
    public void loadAll() {
        try {
            loadMatieres();
            loadStudentsCount();
            loadActivity();
            loadAlerteBadge();
            loadAlertesCount();
            loadMoyenneEcole();
            loadRepartitionEleves();
        } catch (Exception e) {
            System.err.println("Erreur chargement dashboard : " + e.getMessage());
        }
    }

    // ============================================================
    // Chargements
    // ============================================================

    private void loadMatieres() {
        MatiereService ms = new MatiereService();
        NoteService ns = new NoteService();
        List<Matiere> matieres = ms.getData();

        VBox[]  cards    = {m1Card, m2Card, m3Card, m4Card};
        Label[] tags     = {m1Tag, m2Tag, m3Tag, m4Tag};
        Label[] titles   = {m1Title, m2Title, m3Title, m4Title};
        Label[] students = {m1Students, m2Students, m3Students, m4Students};
        Label[] ratings  = {m1Rating, m2Rating, m3Rating, m4Rating};

        for (int i = 0; i < 4; i++) {
            if (cards[i] == null) continue;
            if (i < matieres.size()) {
                Matiere m = matieres.get(i);
                cards[i].setVisible(true);
                cards[i].setManaged(true);
                tags[i].setText(m.getNom_m());

                int nb = ns.countStudentsByMatiere(m.getId_m(), currentAnnee);
                students[i].setText(nb + (nb <= 1 ? " élève" : " élèves"));

                double moy = ns.calculerMoyenneClasse(m.getId_m(), currentAnnee);
                ratings[i].setText(moy > 0 ? String.format("%.1f", moy) : "—");

                titles[i].setText("Moyenne classe");
                matiereNames[i] = m.getNom_m();
            } else {
                cards[i].setVisible(false);
                cards[i].setManaged(false);
                matiereNames[i] = null;
            }
        }
    }

    private void loadStudentsCount() {
        if (lbStudentsCount == null) return;
        int total = new StudentService().countAll();
        lbStudentsCount.setText(String.valueOf(total));
    }

    private void loadActivity() {
        if (lbActivityCount == null) return;
        int total = new NoteService().countTotalByAnnee(currentAnnee);
        lbActivityCount.setText(String.valueOf(total));
    }

    private void loadAlerteBadge() {
        if (lbAlerteBadge == null) return;
        try {
            int n = new AlerteService().countNouvelles();
            if (n > 0) {
                lbAlerteBadge.setText(n > 99 ? "99+" : String.valueOf(n));
                lbAlerteBadge.setVisible(true);
            } else {
                lbAlerteBadge.setVisible(false);
            }
        } catch (Exception e) {
            lbAlerteBadge.setVisible(false);
        }
    }

    private void loadAlertesCount() {
        if (lbAlertesCount == null) return;
        try {
            int n = new AlerteService().countNouvelles();
            lbAlertesCount.setText(String.valueOf(n));
        } catch (Exception e) {
            lbAlertesCount.setText("0");
        }
    }

    /** Moyenne globale de l'école = moyenne de toutes les notes pondérées. */
    private void loadMoyenneEcole() {
        if (lbMoyenneEcole == null) return;
        String q = "SELECT SUM(valeur*coefficient)/SUM(coefficient) FROM note WHERE annee_scolaire = ?";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(q)) {
            pst.setString(1, currentAnnee);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble(1);
                    if (rs.wasNull()) {
                        lbMoyenneEcole.setText("—");
                    } else {
                        lbMoyenneEcole.setText(String.format("%.2f", v));
                    }
                }
            }
        } catch (Exception e) {
            lbMoyenneEcole.setText("—");
        }
    }

    /**
     * Compte combien d'élèves sont en :
     *  - Excellence (moyenne >= 16)
     *  - Réussite   (moyenne >= 10)
     *  - Difficulté (moyenne < 10)
     */
    private void loadRepartitionEleves() {
        if (lbExcellence == null) return;
        int exc = 0, reu = 0, diff = 0;

        // Calcul: pour chaque élève, moyenne des moyennes par matière
        String q = "SELECT student_id, AVG(moy) AS moy FROM ( " +
                "  SELECT student_id, matiere_id, " +
                "         SUM(valeur*coefficient)/SUM(coefficient) AS moy " +
                "  FROM note WHERE annee_scolaire = ? " +
                "  GROUP BY student_id, matiere_id" +
                ") sub GROUP BY student_id";
        try (PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(q)) {
            pst.setString(1, currentAnnee);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    double moy = rs.getDouble("moy");
                    if (moy >= 16) exc++;
                    else if (moy >= 10) reu++;
                    else diff++;
                }
            }
        } catch (Exception e) {
            // silently ignore
        }
        lbExcellence.setText(String.valueOf(exc));
        lbReussite.setText(String.valueOf(reu));
        lbDifficulte.setText(String.valueOf(diff));
    }

    // ============================================================
    // Horloge navbar
    // ============================================================
    private void startClock() {
        if (lblDateHeure == null) return;
        if (clock != null) clock.stop();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy · HH:mm:ss");
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            lblDateHeure.setText(LocalDateTime.now().format(f));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        // Tick initial
        lblDateHeure.setText(LocalDateTime.now().format(f));
    }

    // ============================================================
    // Menu sidebar
    // ============================================================
    @FXML public void handleMenuDashboard(ActionEvent e) {
        // déjà sur dashboard → juste reload
        markActive(btnMenuDashboard);
        loadAll();
    }
    @FXML public void handleMenuAjouter(ActionEvent e) {
        markActive(btnMenuAjouter);
        openWindow("/AjouterNote.fxml", "EduNova - Ajouter une note");
    }
    @FXML public void handleMenuListe(ActionEvent e) {
        markActive(btnMenuListe);
        openWindow("/ListeNotes.fxml", "EduNova - Liste des notes");
    }
    @FXML public void handleMenuBulletin(ActionEvent e) {
        markActive(btnMenuBulletin);
        openWindow("/Bulletin.fxml", "EduNova - Bulletin scolaire");
    }
    @FXML public void handleMenuAlertes(ActionEvent e) {
        markActive(btnMenuAlertes);
        openWindow("/Alertes.fxml", "EduNova - Alertes intelligentes");
    }
    @FXML public void handleMenuCalendrier(ActionEvent e) {
        markActive(btnMenuCalendrier);
        showInfo("Calendrier", "Module Calendrier à venir.");
    }
    @FXML public void handleMenuParents(ActionEvent e) {
        markActive(btnMenuParents);
        openWindow("/GestionParents.fxml", "EduNova - Coordonnées parents");
    }

    private void markActive(Button active) {
        Button[] all = {btnMenuDashboard, btnMenuAjouter, btnMenuListe,
                btnMenuBulletin, btnMenuAlertes, btnMenuCalendrier, btnMenuParents};
        for (Button b : all) {
            if (b == null) continue;
            b.getStyleClass().removeAll("pro-menu-active", "pro-menu");
            if (b == active) b.getStyleClass().add("pro-menu-active");
            else b.getStyleClass().add("pro-menu");
        }
    }

    // ============================================================
    // Click carte matière
    // ============================================================
    @FXML public void openMatiere1(MouseEvent e) { openMatiereByIndex(0); }
    @FXML public void openMatiere2(MouseEvent e) { openMatiereByIndex(1); }
    @FXML public void openMatiere3(MouseEvent e) { openMatiereByIndex(2); }
    @FXML public void openMatiere4(MouseEvent e) { openMatiereByIndex(3); }

    private void openMatiereByIndex(int idx) {
        String name = (idx < matiereNames.length) ? matiereNames[idx] : null;
        if (name == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeNotes.fxml"));
            Parent root = loader.load();
            ListeNotes ctrl = loader.getController();
            ctrl.setMatiereFilter(name);

            Stage stage = new Stage();
            stage.setTitle("EduNova - Notes - " + name);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(rootPane.getScene().getWindow());
            stage.showAndWait();
            loadAll();
        } catch (Exception ex) {
            showError("Erreur", "Impossible d'ouvrir : " + ex.getMessage());
        }
    }

    // ============================================================
    // Theme toggle
    // ============================================================
    @FXML
    public void handleToggleTheme(ActionEvent e) {
        darkMode = !darkMode;
        if (rootPane != null) {
            rootPane.getStyleClass().removeAll("root-dark", "root-light");
            rootPane.getStyleClass().add(darkMode ? "root-dark" : "root-light");
        }
        if (btnToggleTheme != null) {
            btnToggleTheme.setText(darkMode ? "Light" : "Dark");
        }
    }

    @FXML
    public void handleDeconnexion(ActionEvent e) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment quitter EduNova ?",
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);
        conf.setHeaderText(null);
        conf.showAndWait().ifPresent(bt -> {
            if (bt == javafx.scene.control.ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    // ============================================================
    // Anciennes méthodes (compat sidebar icônes si existantes)
    // ============================================================
    @FXML public void openAjouterNote(MouseEvent e) { handleMenuAjouter(null); }
    @FXML public void openListeNotes(MouseEvent e)  { handleMenuListe(null); }
    @FXML public void openBulletin(MouseEvent e)    { handleMenuBulletin(null); }
    @FXML public void openHome(MouseEvent e)        { handleMenuDashboard(null); }
    @FXML public void openCalendrier(MouseEvent e)  { handleMenuCalendrier(null); }
    @FXML public void openSettings(MouseEvent e)    { showInfo("Paramètres", "Module à venir."); }
    @FXML public void openNotifications(MouseEvent e){ showInfo("Notifications", "Aucune."); }
    @FXML public void openAlertes(MouseEvent e)     { handleMenuAlertes(null); }

    // ============================================================
    // Helpers
    // ============================================================

    private String currentAnneeScolaire() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        if (month >= 9) return year + "-" + (year + 1);
        return (year - 1) + "-" + year;
    }

    private String initialesFrom(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.length() == 0 ? "U" : sb.toString();
    }

    private String safeText(Label l) { return (l == null || l.getText() == null) ? "" : l.getText(); }

    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            if (rootPane != null && rootPane.getScene() != null) {
                stage.initOwner(rootPane.getScene().getWindow());
            }
            stage.showAndWait();
            loadAll();
        } catch (Exception ex) {
            showError("Erreur", "Impossible d'ouvrir : " + ex.getMessage());
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.show();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.show();
    }
}
