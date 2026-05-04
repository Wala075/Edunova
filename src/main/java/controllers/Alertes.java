package controllers;

import edu.edunova.entities.Alerte;
import edu.edunova.entities.Alerte.Severite;
import edu.edunova.entities.Alerte.Statut;
import edu.edunova.notifications.AlerteNotifier;
import edu.edunova.notifications.AlerteNotifier.NotifyReport;
import edu.edunova.services.AlerteEngine;
import edu.edunova.services.AlerteEngine.ScanReport;
import edu.edunova.services.AlerteService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Alertes {

    @FXML private Button btnScan;

    @FXML private Label lbCritiques;
    @FXML private Label lbMoyennes;
    @FXML private Label lbPositives;
    @FXML private Label lbInfos;
    @FXML private Label lbNouvelles;

    @FXML private ComboBox<String> cbSeverite;
    @FXML private ComboBox<String> cbStatut;

    @FXML private TableView<Alerte> tvAlertes;
    @FXML private TableColumn<Alerte, String> colSeverite;
    @FXML private TableColumn<Alerte, String> colType;
    @FXML private TableColumn<Alerte, String> colTitre;
    @FXML private TableColumn<Alerte, String> colEleve;
    @FXML private TableColumn<Alerte, String> colMatiere;
    @FXML private TableColumn<Alerte, String> colDate;
    @FXML private TableColumn<Alerte, String> colStatut;

    @FXML private Label lbMessage;

    private final AlerteService alerteService = new AlerteService();
    private final AlerteEngine engine = new AlerteEngine();
    private final ObservableList<Alerte> data = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Combos filtres
        cbSeverite.getItems().addAll("Toutes", "CRITIQUE", "MOYENNE", "POSITIVE", "INFO");
        cbSeverite.setValue("Toutes");
        cbStatut.getItems().addAll("Tous", "NOUVELLE", "LUE", "TRAITEE", "IGNOREE");
        cbStatut.setValue("Tous");

        // Colonnes
        colSeverite.setCellValueFactory(c ->
                new SimpleStringProperty(formatSeverite(c.getValue().getSeverite())));
        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTypeAlerte() == null
                        ? "" : c.getValue().getTypeAlerte().name()));
        colTitre.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTitre()));
        colEleve.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStudentNom() == null
                        ? "—" : c.getValue().getStudentNom()));
        colMatiere.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getMatiereNom() == null
                        ? "—" : c.getValue().getMatiereNom()));
        colDate.setCellValueFactory(c -> {
            LocalDateTime d = c.getValue().getDateCreation();
            return new SimpleStringProperty(d == null ? "" : d.format(DATE_FMT));
        });
        colStatut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatut() == null
                        ? "" : c.getValue().getStatut().name()));

        // Coloration ligne entière selon sévérité
        tvAlertes.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Alerte a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null || a.getSeverite() == null) {
                    setStyle("");
                    return;
                }
                String bg;
                switch (a.getSeverite()) {
                    case CRITIQUE: bg = "#fee2e2"; break;
                    case MOYENNE:  bg = "#fef3c7"; break;
                    case POSITIVE: bg = "#d1fae5"; break;
                    case INFO:     bg = "#dbeafe"; break;
                    default:       bg = "transparent";
                }
                // Ligne plus claire si déjà traitée / ignorée
                String opacity = (a.getStatut() == Statut.TRAITEE
                        || a.getStatut() == Statut.IGNOREE) ? "0.55" : "1";
                setStyle("-fx-background-color:" + bg + "; -fx-opacity:" + opacity + ";");
            }
        });

        // Sélection => afficher message détaillé
        tvAlertes.getSelectionModel().selectedItemProperty().addListener((obs, oldA, newA) -> {
            if (newA != null) {
                lbMessage.setText(newA.getMessage() == null ? "(aucun message)" : newA.getMessage());
                // Auto-marquer comme LUE si NOUVELLE
                if (newA.getStatut() == Statut.NOUVELLE) {
                    alerteService.marquerStatut(newA.getId_a(), Statut.LUE);
                    newA.setStatut(Statut.LUE);
                    tvAlertes.refresh();
                    refreshStats();
                }
            } else {
                lbMessage.setText("Sélectionner une alerte pour voir les détails.");
            }
        });

        tvAlertes.setItems(data);

        chargerToutes();
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    @FXML
    public void lancerScan(ActionEvent e) {
        btnScan.setDisable(true);
        btnScan.setText("⏳  Scan en cours...");
        String annee = currentAnneeScolaire();

        // On encapsule le scan + l'envoi d'emails dans une seule Task
        class ScanAndNotify {
            ScanReport scan;
            NotifyReport notify;
        }

        Task<ScanAndNotify> task = new Task<>() {
            @Override
            protected ScanAndNotify call() {
                ScanAndNotify out = new ScanAndNotify();
                out.scan = engine.scanComplet(annee);
                // Envoie les emails pour CRITIQUE + EXCELLENCE
                out.notify = new AlerteNotifier().notifierBatch(out.scan.alertes);
                return out;
            }
        };

        task.setOnSucceeded(ev -> {
            ScanAndNotify out = task.getValue();
            ScanReport r = out.scan;
            NotifyReport n = out.notify;
            chargerToutes();
            btnScan.setDisable(false);
            btnScan.setText("✨  Scan intelligent");

            String emailLine = "";
            if (n != null) {
                if (n.sentEmails > 0)
                    emailLine = "\n📧 Emails envoyés : " + n.sentEmails;
                if (n.failures > 0)
                    emailLine += "\n⚠ Échecs envoi : " + n.failures;
                if (n.skippedNoMail > 0)
                    emailLine += "\n📭 Sans email parent : " + n.skippedNoMail;
            }

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Scan terminé");
            info.setHeaderText("Analyse de " + r.eleves + " élèves");
            info.setContentText(
                    "✅ " + r.inserees + " nouvelles alertes\n" +
                    "↻ " + r.doublonsIgnores + " déjà existantes\n\n" +
                    "🔴 Critiques : " + r.critiques + "\n" +
                    "🟠 Moyennes : " + r.moyennes + "\n" +
                    "🟢 Positives : " + r.positives + "\n" +
                    "🔵 Infos : " + r.infos +
                    emailLine);
            info.showAndWait();
        });

        task.setOnFailed(ev -> {
            btnScan.setDisable(false);
            btnScan.setText("✨  Scan intelligent");
            showError("Scan échoué", task.getException() == null
                    ? "Erreur inconnue" : task.getException().getMessage());
        });

        Thread t = new Thread(task, "AlerteScan");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void filtrer(ActionEvent e) {
        String sev = cbSeverite.getValue();
        String stat = cbStatut.getValue();

        List<Alerte> list = alerteService.getAll();
        list.removeIf(a -> {
            if (!"Toutes".equals(sev) && a.getSeverite() != null
                    && !a.getSeverite().name().equals(sev)) return true;
            if (!"Tous".equals(stat) && a.getStatut() != null
                    && !a.getStatut().name().equals(stat)) return true;
            return false;
        });

        data.setAll(list);
        refreshStats();
    }

    @FXML
    public void reset(ActionEvent e) {
        cbSeverite.setValue("Toutes");
        cbStatut.setValue("Tous");
        chargerToutes();
    }

    @FXML
    public void marquerLue(ActionEvent e)     { changerStatut(Statut.LUE); }
    @FXML
    public void marquerTraitee(ActionEvent e) { changerStatut(Statut.TRAITEE); }
    @FXML
    public void ignorer(ActionEvent e)        { changerStatut(Statut.IGNOREE); }

    @FXML
    public void supprimer(ActionEvent e) {
        Alerte sel = tvAlertes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showInfo("Aucune sélection", "Sélectionner une alerte à supprimer.");
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette alerte ?", ButtonType.OK, ButtonType.CANCEL);
        conf.setHeaderText(null);
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                alerteService.delete(sel.getId_a());
                chargerToutes();
            }
        });
    }

    @FXML
    public void retour(ActionEvent e) {
        Stage stage = (Stage) tvAlertes.getScene().getWindow();
        stage.close();
    }

    // ============================================================
    // INTERNAL
    // ============================================================

    private void chargerToutes() {
        List<Alerte> all = alerteService.getAll();
        data.setAll(all);
        refreshStats();
    }

    private void refreshStats() {
        int crit = 0, moy = 0, pos = 0, inf = 0, nouv = 0;
        for (Alerte a : data) {
            if (a.getSeverite() == Severite.CRITIQUE) crit++;
            else if (a.getSeverite() == Severite.MOYENNE) moy++;
            else if (a.getSeverite() == Severite.POSITIVE) pos++;
            else if (a.getSeverite() == Severite.INFO) inf++;
            if (a.getStatut() == Statut.NOUVELLE) nouv++;
        }
        lbCritiques.setText(String.valueOf(crit));
        lbMoyennes.setText(String.valueOf(moy));
        lbPositives.setText(String.valueOf(pos));
        lbInfos.setText(String.valueOf(inf));
        lbNouvelles.setText(String.valueOf(nouv));
    }

    private void changerStatut(Statut nouveau) {
        Alerte sel = tvAlertes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showInfo("Aucune sélection", "Sélectionner une alerte.");
            return;
        }
        alerteService.marquerStatut(sel.getId_a(), nouveau);
        sel.setStatut(nouveau);
        tvAlertes.refresh();
        refreshStats();
    }

    private String formatSeverite(Severite s) {
        if (s == null) return "";
        switch (s) {
            case CRITIQUE: return "🔴 CRITIQUE";
            case MOYENNE:  return "🟠 MOYENNE";
            case POSITIVE: return "🟢 POSITIVE";
            case INFO:     return "🔵 INFO";
            default:       return s.name();
        }
    }

    private String currentAnneeScolaire() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        if (month >= 9) return year + "-" + (year + 1);
        return (year - 1) + "-" + year;
    }

    private void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}
