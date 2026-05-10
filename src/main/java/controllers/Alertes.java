package controllers;

import edu.edunova.entities.Alerte;
import edu.edunova.entities.Alerte.Severite;
import edu.edunova.entities.Alerte.Statut;
import edu.edunova.notifications.AlerteNotifier;
import edu.edunova.notifications.AlerteNotifier.NotifyReport;
import edu.edunova.services.AlerteEngine;
import edu.edunova.services.AlerteEngine.ScanReport;
import edu.edunova.services.AlerteService;
import edu.edunova.utils.AnneeScolaire;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    @FXML private ListView<Alerte> lvAlertes;
    @FXML private Label lbMessage;

    private final AlerteService alerteService = new AlerteService();
    private final AlerteEngine engine = new AlerteEngine();
    private final ObservableList<Alerte> data = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        cbSeverite.getItems().addAll("Toutes", "CRITIQUE", "MOYENNE", "POSITIVE", "INFO");
        cbSeverite.setValue("Toutes");
        cbStatut.getItems().addAll("Tous", "NOUVELLE", "LUE", "TRAITEE", "IGNOREE");
        cbStatut.setValue("Tous");

        // Cards
        lvAlertes.setItems(data);
        lvAlertes.setCellFactory(lv -> new AlerteCardCell());

        // Sélection => afficher message détaillé
        lvAlertes.getSelectionModel().selectedItemProperty().addListener((obs, oldA, newA) -> {
            if (newA != null) {
                lbMessage.setText(newA.getMessage() == null ? "(aucun message)" : newA.getMessage());
                if (newA.getStatut() == Statut.NOUVELLE) {
                    alerteService.marquerStatut(newA.getId_a(), Statut.LUE);
                    newA.setStatut(Statut.LUE);
                    lvAlertes.refresh();
                    refreshStats();
                }
            } else {
                lbMessage.setText("Sélectionner une alerte pour voir les détails.");
            }
        });

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

        class ScanAndNotify {
            ScanReport scan;
            NotifyReport notify;
        }

        Task<ScanAndNotify> task = new Task<>() {
            @Override
            protected ScanAndNotify call() {
                ScanAndNotify out = new ScanAndNotify();
                out.scan = engine.scanComplet(annee);
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

    @FXML public void marquerLue(ActionEvent e)     { changerStatut(Statut.LUE); }
    @FXML public void marquerTraitee(ActionEvent e) { changerStatut(Statut.TRAITEE); }
    @FXML public void ignorer(ActionEvent e)        { changerStatut(Statut.IGNOREE); }

    @FXML
    public void supprimer(ActionEvent e) {
        Alerte sel = lvAlertes.getSelectionModel().getSelectedItem();
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
        Stage stage = (Stage) lvAlertes.getScene().getWindow();
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
        Alerte sel = lvAlertes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showInfo("Aucune sélection", "Sélectionner une alerte.");
            return;
        }
        alerteService.marquerStatut(sel.getId_a(), nouveau);
        sel.setStatut(nouveau);
        lvAlertes.refresh();
        refreshStats();
    }

    private String currentAnneeScolaire() {
        return AnneeScolaire.current();
    }

    private void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    // ============================================================
    //  CARTE ALERTE - cellule personnalisée
    // ============================================================
    private static class AlerteCardCell extends ListCell<Alerte> {
        @Override
        protected void updateItem(Alerte a, boolean empty) {
            super.updateItem(a, empty);
            if (empty || a == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            String color = colorFor(a.getSeverite());
            String icon  = iconFor(a.getSeverite());

            // ---- Icone gauche ----
            Label iconLb = new Label(icon);
            iconLb.setStyle("-fx-font-size: 26px;");
            StackPane iconPane = new StackPane(iconLb);
            iconPane.setStyle("-fx-background-color: " + color + "33; "
                    + "-fx-background-radius: 12; -fx-min-width: 56; -fx-min-height: 56; "
                    + "-fx-max-width: 56; -fx-max-height: 56;");

            // ---- Header (sévérité + type) ----
            Label sevLb = new Label(a.getSeverite() == null ? "" : a.getSeverite().name());
            sevLb.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                    + "-fx-font-size: 10px; -fx-font-weight: 700; -fx-padding: 3 10; "
                    + "-fx-background-radius: 8;");

            Label typeLb = new Label(a.getTypeAlerte() == null ? "" : a.getTypeAlerte().name());
            typeLb.setStyle("-fx-background-color: #1e1e3a; -fx-text-fill: #94a3b8; "
                    + "-fx-font-size: 10px; -fx-font-weight: 600; -fx-padding: 3 10; "
                    + "-fx-background-radius: 8;");

            HBox topRow = new HBox(8, sevLb, typeLb);
            topRow.setAlignment(Pos.CENTER_LEFT);

            // ---- Title + élève + matière ----
            Label titre = new Label(safe(a.getTitre()));
            titre.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #e2e8f0;");
            titre.setWrapText(true);

            String sousTexte = safe(a.getStudentNom());
            if (a.getMatiereNom() != null && !a.getMatiereNom().isBlank()) {
                sousTexte += "  ·  " + a.getMatiereNom();
            }
            if (a.getDateCreation() != null) {
                sousTexte += "  ·  " + a.getDateCreation().format(DATE_FMT);
            }
            Label sub = new Label(sousTexte);
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

            VBox infos = new VBox(6, topRow, titre, sub);
            HBox.setHgrow(infos, Priority.ALWAYS);

            // ---- Statut badge à droite ----
            Statut st = a.getStatut();
            String stColor = "#94a3b8", stBg = "#1e1e3a";
            if (st == Statut.NOUVELLE) { stColor = "#fbbf24"; stBg = "#3d2e0a"; }
            else if (st == Statut.LUE) { stColor = "#a78bfa"; stBg = "#2d1b69"; }
            else if (st == Statut.TRAITEE) { stColor = "#34d399"; stBg = "#052e1a"; }
            else if (st == Statut.IGNOREE) { stColor = "#94a3b8"; stBg = "#1e1e3a"; }

            Label statutBadge = new Label(st == null ? "" : st.name());
            statutBadge.setStyle("-fx-background-color: " + stBg + "; -fx-text-fill: " + stColor + "; "
                    + "-fx-font-size: 10px; -fx-font-weight: 700; -fx-padding: 5 12; "
                    + "-fx-background-radius: 10;");

            VBox right = new VBox(statutBadge);
            right.setAlignment(Pos.CENTER_RIGHT);

            // ---- Card final ----
            HBox card = new HBox(16, iconPane, infos, right);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("pro-alerte-card");
            String opacity = (st == Statut.TRAITEE || st == Statut.IGNOREE) ? "0.6" : "1";
            card.setStyle("-fx-background-color: #16162e; "
                    + "-fx-background-radius: 12; -fx-padding: 18 22; "
                    + "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 4; "
                    + "-fx-border-radius: 12; -fx-opacity: " + opacity + ";");

            setGraphic(card);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
        }

        private String colorFor(Severite s) {
            if (s == null) return "#94a3b8";
            switch (s) {
                case CRITIQUE: return "#ef4444";
                case MOYENNE:  return "#f59e0b";
                case POSITIVE: return "#10b981";
                case INFO:     return "#0ea5e9";
                default:       return "#94a3b8";
            }
        }

        private String iconFor(Severite s) {
            if (s == null) return "•";
            switch (s) {
                case CRITIQUE: return "🔴";
                case MOYENNE:  return "🟠";
                case POSITIVE: return "🟢";
                case INFO:     return "🔵";
                default:       return "•";
            }
        }

        private String safe(String s) { return s == null ? "—" : s; }
    }
}
