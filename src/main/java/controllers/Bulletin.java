package controllers;

import edu.edunova.ai.DeepLService;
import edu.edunova.ai.GeminiService;
import edu.edunova.entities.Bulletin.BulletinLigne;
import edu.edunova.entities.Student;
import edu.edunova.utils.AnneeScolaire;
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
    @FXML private Button btnReformuler;
    @FXML private Button btnGenererIA;
    @FXML private Button btnRegenerer;
    @FXML private ProgressIndicator aiSpinner;

    // ===== Email parents (Feature 3) =====
    @FXML private Button btnEnvoyerParents;

    // ===== PDF =====
    @FXML private Button btnImprimerPdf;

    // ===== Version Arabe (DeepL) =====
    @FXML private javafx.scene.layout.VBox boxArabe;
    @FXML private TextArea taAppreciationAr;
    @FXML private Button btnTraduireAr;
    @FXML private Button btnRetraduireAr;
    @FXML private ProgressIndicator arSpinner;

    private edu.edunova.entities.Bulletin currentBulletin;
    private final Map<String, Integer> elevesMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Trimestres
        cbTrimestre.getItems().addAll("1", "2", "3");
        cbTrimestre.setValue("1");

        // Année scolaire automatique (verrouillée)
        tfAnnee.setText(AnneeScolaire.current());
        tfAnnee.setEditable(false);
        tfAnnee.setFocusTraversable(false);
        tfAnnee.setStyle(tfAnnee.getStyle() + " -fx-opacity: 0.85;");
        tfAnnee.setTooltip(new Tooltip("Année scolaire courante (calculée automatiquement)"));

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

            // Affiche la card Version arabe (DeepL) - prête à traduire
            if (boxArabe != null) {
                boxArabe.setVisible(true);
                boxArabe.setManaged(true);
            }
            // Reset éventuelle ancienne traduction
            if (taAppreciationAr != null) taAppreciationAr.clear();
            if (btnTraduireAr != null) {
                btnTraduireAr.setVisible(true);
                btnTraduireAr.setManaged(true);
            }
            if (btnRetraduireAr != null) {
                btnRetraduireAr.setVisible(false);
                btnRetraduireAr.setManaged(false);
            }
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
    // FEATURE 2 BIS - REFORMULER UN TEXTE UTILISATEUR (Gemini)
    // ============================================================
    @FXML
    public void reformulerAppreciation(ActionEvent event) {
        if (currentBulletin == null || currentBulletin.getStudent() == null) {
            showAlert(Alert.AlertType.WARNING, "Bulletin manquant",
                    "Génère d'abord un bulletin avant de reformuler.");
            return;
        }
        String texteUtilisateur = taAppreciationIA == null ? "" : taAppreciationIA.getText();
        if (texteUtilisateur == null || texteUtilisateur.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Texte vide",
                    "Tape d'abord ton appréciation dans la zone de texte, " +
                    "puis clique sur 'Améliorer mon texte'.");
            return;
        }

        // UI feedback
        aiSpinner.setVisible(true);
        if (btnReformuler != null) btnReformuler.setDisable(true);
        if (btnGenererIA != null)  btnGenererIA.setDisable(true);
        if (btnRegenerer != null)  btnRegenerer.setDisable(true);

        final edu.edunova.entities.Bulletin snapshot = currentBulletin;
        final String original = texteUtilisateur;

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return new GeminiService().reformulerAppreciation(snapshot, original);
            }
        };

        task.setOnSucceeded(e -> {
            String improved = task.getValue();
            taAppreciationIA.setText(improved);
            aiSpinner.setVisible(false);
            if (btnReformuler != null) btnReformuler.setDisable(false);
            if (btnGenererIA != null)  btnGenererIA.setDisable(false);
            if (btnRegenerer != null)  btnRegenerer.setDisable(false);

            // Affiche la card Version arabe (peut traduire le texte amélioré)
            if (boxArabe != null) {
                boxArabe.setVisible(true);
                boxArabe.setManaged(true);
            }
            // Reset traduction arabe (texte FR a changé)
            if (taAppreciationAr != null) taAppreciationAr.clear();
            if (btnTraduireAr != null) {
                btnTraduireAr.setVisible(true);
                btnTraduireAr.setManaged(true);
            }
            if (btnRetraduireAr != null) {
                btnRetraduireAr.setVisible(false);
                btnRetraduireAr.setManaged(false);
            }
        });

        task.setOnFailed(e -> {
            aiSpinner.setVisible(false);
            if (btnReformuler != null) btnReformuler.setDisable(false);
            if (btnGenererIA != null)  btnGenererIA.setDisable(false);
            if (btnRegenerer != null)  btnRegenerer.setDisable(false);
            Throwable ex = task.getException();
            String msg = ex == null ? "Erreur inconnue" : ex.getMessage();
            showAlert(Alert.AlertType.ERROR, "Erreur reformulation",
                    "Impossible de reformuler :\n" + msg);
        });

        Thread t = new Thread(task, "Gemini-Reformulate");
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
        final String appreciationFr = (taAppreciationIA == null) ? null : taAppreciationIA.getText();
        final String appreciationAr = (taAppreciationAr == null) ? null : taAppreciationAr.getText();

        Task<java.io.File> task = new Task<>() {
            @Override
            protected java.io.File call() throws Exception {
                return new PdfService().genererBulletinPdf(snapshot, appreciationFr,
                        appreciationAr, dest);
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

        // 1. Récupérer email parent (depuis Student) - AUTO, sans dialog
        Student student = currentBulletin.getStudent();
        StudentService studentService = new StudentService();
        Student fresh = studentService.findById(student.getId_s());
        String emailParent = (fresh != null) ? fresh.getEmail_parent() : null;

        // Si pas d'email pour cet élève -> erreur + redirige vers Coordonnées parents
        if (emailParent == null || emailParent.isBlank()) {
            showAlert(Alert.AlertType.ERROR,
                    "📭  Email parent manquant",
                    "Aucun email parent enregistré pour " + student.getNomComplet().trim() +
                    ".\n\nVa dans le menu « 👨‍👩‍👧 Coordonnées parents » du Dashboard " +
                    "pour configurer l'email avant d'envoyer le bulletin.");
            return;
        }
        // ENVOI AUTOMATIQUE - pas de confirmation

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

    // ============================================================
    // TRADUCTION ARABE (DeepL)
    // ============================================================
    @FXML
    public void traduireEnArabe(ActionEvent event) {
        if (taAppreciationIA == null
                || taAppreciationIA.getText() == null
                || taAppreciationIA.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Appréciation manquante",
                    "Génère d'abord une appréciation IA avant de la traduire.");
            return;
        }

        // UI feedback
        if (arSpinner != null) arSpinner.setVisible(true);
        if (btnTraduireAr != null) btnTraduireAr.setDisable(true);
        if (btnRetraduireAr != null) btnRetraduireAr.setDisable(true);
        if (taAppreciationAr != null) taAppreciationAr.setText("جاري الترجمة...");

        final String texteFr = taAppreciationIA.getText();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return new DeepLService().traduireEnArabe(texteFr);
            }
        };

        task.setOnSucceeded(e -> {
            String txt = task.getValue();
            if (taAppreciationAr != null) taAppreciationAr.setText(txt);
            if (arSpinner != null) arSpinner.setVisible(false);
            if (btnTraduireAr != null) btnTraduireAr.setDisable(false);
            if (btnRetraduireAr != null) btnRetraduireAr.setDisable(false);
            // Bascule vers "Retraduire"
            if (btnTraduireAr != null) {
                btnTraduireAr.setVisible(false);
                btnTraduireAr.setManaged(false);
            }
            if (btnRetraduireAr != null) {
                btnRetraduireAr.setVisible(true);
                btnRetraduireAr.setManaged(true);
            }
        });

        task.setOnFailed(e -> {
            if (arSpinner != null) arSpinner.setVisible(false);
            if (btnTraduireAr != null) btnTraduireAr.setDisable(false);
            if (btnRetraduireAr != null) btnRetraduireAr.setDisable(false);
            if (taAppreciationAr != null) taAppreciationAr.clear();
            Throwable ex = task.getException();
            String msg = ex == null ? "Erreur inconnue" : ex.getMessage();
            showAlert(Alert.AlertType.ERROR, "Erreur DeepL",
                    "Impossible de traduire :\n" + msg);
        });

        Thread t = new Thread(task, "DeepL-Translate");
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
