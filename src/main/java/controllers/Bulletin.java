package controllers;

import edu.edunova.ai.GeminiService;
import edu.edunova.entities.Bulletin.BulletinLigne;
import edu.edunova.entities.Student;
import edu.edunova.notifications.BrevoService;
import edu.edunova.notifications.BrevoService.SendResult;
import edu.edunova.notifications.EmailLogService;
import edu.edunova.notifications.EmailTemplate;
import edu.edunova.pdf.PdfService;
import edu.edunova.services.BulletinService;
import edu.edunova.services.StudentService;
import javafx.stage.FileChooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Bulletin {

    @FXML private ComboBox<String> cbEleve;
    @FXML private ComboBox<String> cbTrimestre;
    @FXML private TextField tfAnnee;

    @FXML private Label lbEleve;
    @FXML private Label lbInfos;
    @FXML private Label lbMoyenne;
    @FXML private Label lbMention;
    @FXML private Label lbDecision;

    @FXML private TableView<BulletinLigne> tvBulletin;
    @FXML private TableColumn<BulletinLigne, String> colMatiere;
    @FXML private TableColumn<BulletinLigne, Integer> colNbNotes;
    @FXML private TableColumn<BulletinLigne, Double> colMoyenne;
    @FXML private TableColumn<BulletinLigne, String> colAppreciation;

    // ===== Appréciation IA (Feature 2) =====
    @FXML private TextArea taAppreciationIA;
    @FXML private Button btnGenererIA;
    @FXML private Button btnRegenerer;
    @FXML private ProgressIndicator aiSpinner;

    // ===== Email parents (Feature 3) =====
    @FXML private Button btnEnvoyerParents;

    // ===== PDF =====
    @FXML private Button btnImprimerPdf;

    private edu.edunova.entities.Bulletin currentBulletin;
    private final Map<String, Integer> elevesMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Trimestres
        cbTrimestre.getItems().addAll("1", "2", "3");
        cbTrimestre.setValue("1");

        // Charger élèves
        StudentService ss = new StudentService();
        for (Student s : ss.getData()) {
            String label = s.getNomComplet().trim();
            cbEleve.getItems().add(label);
            elevesMap.put(label, s.getId_s());
        }

        // Configurer colonnes
        colMatiere.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        colNbNotes.setCellValueFactory(new PropertyValueFactory<>("nbNotes"));
        colMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenne"));
        colAppreciation.setCellValueFactory(new PropertyValueFactory<>("appreciation"));

        // Format moyenne /20
        colMoyenne.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : String.format("%.2f / 20", v));
            }
        });
    }

    @FXML
    public void genererBulletin(ActionEvent event) {
        if (cbEleve.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Élève manquant", "Veuillez sélectionner un élève.");
            return;
        }
        if (cbTrimestre.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Trimestre manquant", "Veuillez choisir un trimestre.");
            return;
        }
        if (tfAnnee.getText() == null || tfAnnee.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Année manquante", "Veuillez saisir l'année scolaire.");
            return;
        }

        int studentId = elevesMap.get(cbEleve.getValue());
        int trim = Integer.parseInt(cbTrimestre.getValue());
        String annee = tfAnnee.getText().trim();

        edu.edunova.entities.Bulletin b = new BulletinService()
                .genererBulletin(studentId, trim, annee);

        if (b.getStudent() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Élève introuvable.");
            return;
        }

        // Garde la référence pour l'appel IA
        this.currentBulletin = b;
        // Reset zone IA quand on régénère un bulletin
        if (taAppreciationIA != null) taAppreciationIA.clear();
        if (btnRegenerer != null) {
            btnRegenerer.setVisible(false);
            btnRegenerer.setManaged(false);
        }
        if (btnGenererIA != null) {
            btnGenererIA.setVisible(true);
            btnGenererIA.setManaged(true);
        }

        lbEleve.setText(b.getStudent().getNomComplet().trim());
        lbInfos.setText("Trimestre " + b.getTrimestre() + "  •  Année " + b.getAnnee()
                + "  •  " + b.getLignes().size() + " matière(s)");

        if (b.getLignes().isEmpty()) {
            lbMoyenne.setText("—");
            lbMention.setText("Aucune note");
            lbDecision.setText("");
        } else {
            lbMoyenne.setText(String.format("%.2f", b.getMoyenneGenerale()));
            lbMention.setText(b.getMention());
            lbDecision.setText("Décision : " + b.getDecision());
        }

        ObservableList<BulletinLigne> lignes = FXCollections.observableArrayList(b.getLignes());
        tvBulletin.setItems(lignes);
    }

    @FXML
    public void retour(ActionEvent event) {
        Stage stage = (Stage) tvBulletin.getScene().getWindow();
        stage.close();
    }

    // ============================================================
    // FEATURE 2 - APPRÉCIATION IA (Gemini)
    // ============================================================
    @FXML
    public void genererAppreciationIA(ActionEvent event) {
        if (currentBulletin == null || currentBulletin.getStudent() == null) {
            showAlert(Alert.AlertType.WARNING, "Bulletin manquant",
                    "Génère d'abord un bulletin avant de demander une appréciation IA.");
            return;
        }
        if (currentBulletin.getLignes() == null || currentBulletin.getLignes().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune note",
                    "Impossible de générer une appréciation : aucune note pour cet élève.");
            return;
        }

        // UI feedback : spinner ON, button disabled
        aiSpinner.setVisible(true);
        btnGenererIA.setDisable(true);
        btnRegenerer.setDisable(true);
        taAppreciationIA.setText("Génération en cours...");

        final edu.edunova.entities.Bulletin snapshot = currentBulletin;

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return new GeminiService().genererAppreciation(snapshot);
            }
        };

        task.setOnSucceeded(e -> {
            String txt = task.getValue();
            taAppreciationIA.setText(txt);
            aiSpinner.setVisible(false);
            btnGenererIA.setDisable(false);
            btnRegenerer.setDisable(false);
            // Après première génération, on bascule sur le bouton "Régénérer"
            btnGenererIA.setVisible(false);
            btnGenererIA.setManaged(false);
            btnRegenerer.setVisible(true);
            btnRegenerer.setManaged(true);
        });

        task.setOnFailed(e -> {
            aiSpinner.setVisible(false);
            btnGenererIA.setDisable(false);
            btnRegenerer.setDisable(false);
            taAppreciationIA.clear();
            Throwable ex = task.getException();
            String msg = ex == null ? "Erreur inconnue" : ex.getMessage();
            showAlert(Alert.AlertType.ERROR, "Erreur Gemini",
                    "Impossible de générer l'appréciation :\n" + msg);
        });

        Thread t = new Thread(task, "Gemini-AI");
        t.setDaemon(true);
        t.start();
    }

    // ============================================================
    // IMPRESSION PDF (OpenHTMLtoPDF)
    // ============================================================
    @FXML
    public void imprimerPdf(ActionEvent event) {
        if (currentBulletin == null || currentBulletin.getStudent() == null) {
            showAlert(Alert.AlertType.WARNING, "Bulletin manquant",
                    "Génère d'abord un bulletin avant d'imprimer.");
            return;
        }
        if (currentBulletin.getLignes() == null || currentBulletin.getLignes().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune note",
                    "Impossible d'imprimer un bulletin vide.");
            return;
        }

        // FileChooser pour choisir l'emplacement de sauvegarde
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le bulletin PDF");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF (*.pdf)", "*.pdf"));
        fc.setInitialFileName(PdfService.filenameFor(currentBulletin));

        java.io.File destination = fc.showSaveDialog(btnImprimerPdf.getScene().getWindow());
        if (destination == null) return; // utilisateur a annulé

        // S'assurer qu'on termine bien par .pdf
        if (!destination.getName().toLowerCase().endsWith(".pdf")) {
            destination = new java.io.File(destination.getAbsolutePath() + ".pdf");
        }

        // Génération async (le rendu PDF peut prendre 1-2s sur des gros documents)
        btnImprimerPdf.setDisable(true);
        btnImprimerPdf.setText("⏳  Génération...");

        final java.io.File dest = destination;
        final edu.edunova.entities.Bulletin snapshot = currentBulletin;
        final String appreciation = (taAppreciationIA == null) ? null : taAppreciationIA.getText();

        Task<java.io.File> task = new Task<>() {
            @Override
            protected java.io.File call() throws Exception {
                return new PdfService().genererBulletinPdf(snapshot, appreciation, dest);
            }
        };

        task.setOnSucceeded(e -> {
            btnImprimerPdf.setDisable(false);
            btnImprimerPdf.setText("📄  Imprimer PDF");

            java.io.File pdf = task.getValue();
            // Proposer d'ouvrir le PDF
            Alert ok = new Alert(Alert.AlertType.CONFIRMATION,
                    "Bulletin sauvegardé :\n" + pdf.getAbsolutePath() + "\n\nL'ouvrir maintenant ?",
                    ButtonType.YES, ButtonType.NO);
            ok.setTitle("PDF généré ✅");
            ok.setHeaderText("Bulletin créé avec succès");
            ok.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(pdf);
                        }
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur d'ouverture",
                                "Impossible d'ouvrir le PDF : " + ex.getMessage());
                    }
                }
            });
        });

        task.setOnFailed(e -> {
            btnImprimerPdf.setDisable(false);
            btnImprimerPdf.setText("📄  Imprimer PDF");
            Throwable ex = task.getException();
            showAlert(Alert.AlertType.ERROR, "Erreur PDF",
                    "Impossible de générer le PDF :\n" +
                            (ex == null ? "erreur inconnue" : ex.getMessage()));
        });

        Thread t = new Thread(task, "PDF-Generator");
        t.setDaemon(true);
        t.start();
    }

    // ============================================================
    // FEATURE 3 - ENVOI EMAIL AUX PARENTS (Brevo)
    // ============================================================
    @FXML
    public void envoyerAuxParents(ActionEvent event) {
        if (currentBulletin == null || currentBulletin.getStudent() == null) {
            showAlert(Alert.AlertType.WARNING, "Bulletin manquant",
                    "Génère d'abord un bulletin avant de l'envoyer.");
            return;
        }
        if (currentBulletin.getLignes() == null || currentBulletin.getLignes().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune note",
                    "Impossible d'envoyer un bulletin vide.");
            return;
        }

        // 1. Récupérer email parent (depuis Student ou demander à l'utilisateur)
        Student student = currentBulletin.getStudent();
        StudentService studentService = new StudentService();
        Student fresh = studentService.findById(student.getId_s());
        String emailParent = (fresh != null) ? fresh.getEmail_parent() : null;

        if (emailParent == null || emailParent.isBlank()) {
            // Pas d'email enregistré pour cet élève -> proposer 2 options
            Alert noMail = new Alert(Alert.AlertType.WARNING,
                    "Aucun email parent enregistré pour " + student.getNomComplet().trim() +
                    ".\n\nUtilise la page « Coordonnées parents » (sidebar) " +
                    "pour configurer les emails de tous les élèves.\n\n" +
                    "Veux-tu saisir l'email maintenant pour cet envoi uniquement ?",
                    ButtonType.OK, ButtonType.CANCEL);
            noMail.setTitle("Email parent manquant");
            noMail.setHeaderText("📭  Pas d'email pour cet élève");
            if (noMail.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Email du parent");
            dialog.setHeaderText("Email parent de " + student.getNomComplet().trim());
            dialog.setContentText("Email :");
            java.util.Optional<String> result = dialog.showAndWait();
            if (!result.isPresent() || result.get().isBlank()) return;
            emailParent = result.get().trim();

            // Sauvegarder pour les prochaines fois
            studentService.updateEmailParent(student.getId_s(), emailParent);
        }

        // 2. Confirmation
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Le bulletin va être envoyé à :\n\n📧  " + emailParent +
                "\n\n(parent de " + student.getNomComplet().trim() + ")",
                ButtonType.OK, ButtonType.CANCEL);
        conf.setTitle("Envoyer aux parents");
        conf.setHeaderText("Bulletin trimestriel - T" + currentBulletin.getTrimestre());
        java.util.Optional<ButtonType> ok = conf.showAndWait();
        if (!ok.isPresent() || ok.get() != ButtonType.OK) return;

        // 3. Envoi async
        btnEnvoyerParents.setDisable(true);
        btnEnvoyerParents.setText("⏳  Envoi en cours...");

        final String to = emailParent;
        final String parentName = "Parent de " + student.getNomComplet().trim();
        final String subject = "Bulletin scolaire - Trimestre " + currentBulletin.getTrimestre()
                + " - " + student.getNomComplet().trim();
        final String html = EmailTemplate.bulletinHtml(currentBulletin,
                taAppreciationIA == null ? null : taAppreciationIA.getText());
        final int sid = student.getId_s();

        Task<SendResult> task = new Task<>() {
            @Override
            protected SendResult call() {
                SendResult r = new BrevoService().sendEmail(to, parentName, subject, html);
                new EmailLogService().log(to, subject, r.success,
                        r.success ? null : r.errorMessage, null, sid);
                return r;
            }
        };

        task.setOnSucceeded(e -> {
            btnEnvoyerParents.setDisable(false);
            btnEnvoyerParents.setText("📧  Envoyer aux parents");
            SendResult r = task.getValue();
            if (r.success) {
                showAlert(Alert.AlertType.INFORMATION, "Email envoyé ✅",
                        "Le bulletin a été envoyé à " + to + ".\n" +
                        "Message ID : " + r.messageId);
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de l'envoi",
                        "Impossible d'envoyer le bulletin :\n" + r.errorMessage);
            }
        });
        task.setOnFailed(e -> {
            btnEnvoyerParents.setDisable(false);
            btnEnvoyerParents.setText("📧  Envoyer aux parents");
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    task.getException() == null ? "Erreur inconnue"
                            : task.getException().getMessage());
        });

        Thread t = new Thread(task, "Brevo-Email");
        t.setDaemon(true);
        t.start();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}
