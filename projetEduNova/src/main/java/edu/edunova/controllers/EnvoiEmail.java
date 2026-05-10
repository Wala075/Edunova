package edu.edunova.controllers;

import edu.edunova.entities.Classe;
import edu.edunova.entities.Parent;
import edu.edunova.entities.Seance;
import edu.edunova.services.ClasseService;
import edu.edunova.services.EmailService;
import edu.edunova.services.ParentService;
import edu.edunova.services.SeanceService;
import edu.edunova.utils.EmploiDuTempsPdfGenerator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue read-only pour envoyer l'emploi du temps aux parents.
 * Les parents sont gérés ailleurs (par l'administrateur via la table parent).
 */
public class EnvoiEmail {

    // Recherche & filtre
    @FXML private TextField searchField;
    @FXML private ComboBox<Classe> filtreClasseCombo;
    @FXML private Label resultatLabel;

    // Tableau
    @FXML private TableView<Parent> parentsTable;
    @FXML private TableColumn<Parent, String> colNom;
    @FXML private TableColumn<Parent, String> colEmail;
    @FXML private TableColumn<Parent, String> colClasse;

    private final ParentService ps = new ParentService();
    private final ClasseService cs = new ClasseService();
    private final SeanceService ss = new SeanceService();

    private ObservableList<Parent> tousLesParents;
    private FilteredList<Parent> parentsFiltres;

    @FXML
    public void initialize() {
        // Filtre classe (avec option "Toutes")
        ObservableList<Classe> classes = FXCollections.observableArrayList(cs.getData());
        ObservableList<Classe> filtreClasses = FXCollections.observableArrayList();
        filtreClasses.add(null); // pour "Toutes"
        filtreClasses.addAll(classes);
        filtreClasseCombo.setItems(filtreClasses);

        // Configuration colonnes
        colNom.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNomComplet()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colClasse.setCellValueFactory(cellData -> {
            String nom = cellData.getValue().getClasseNom();
            return new SimpleStringProperty(nom != null ? nom : "—");
        });

        rafraichirListe();

        // Recherche & filtre en temps réel
        searchField.textProperty().addListener((obs, o, v) -> appliquerFiltres());
        filtreClasseCombo.valueProperty().addListener((obs, o, v) -> appliquerFiltres());
    }

    // ── Envoi email ───────────────────────────────────────────────────

    @FXML
    private void envoyerEmail() {
        Parent selected = parentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficher(AlertType.WARNING, "Veuillez sélectionner un parent dans le tableau.");
            return;
        }
        envoyerEmailA(selected);
    }

    @FXML
    private void envoyerATous() {
        Classe classeFiltre = filtreClasseCombo.getValue();
        if (classeFiltre == null) {
            afficher(AlertType.WARNING,
                    "Sélectionnez une classe dans le filtre pour envoyer à tous ses parents.");
            return;
        }

        List<Parent> parents = ps.getByClasseId(classeFiltre.getId());
        if (parents.isEmpty()) {
            afficher(AlertType.WARNING,
                    "Aucun parent enregistré pour la classe " + classeFiltre.getNom());
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Envoyer l'emploi du temps à " + parents.size()
                + " parent(s) de la classe " + classeFiltre.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                File pdfFile = genererPdfPourClasse(classeFiltre);
                if (pdfFile == null) return;

                int succes = 0, echecs = 0;
                StringBuilder erreurs = new StringBuilder();
                for (Parent p : parents) {
                    try {
                        envoyerMailAvecPdf(p, classeFiltre, pdfFile);
                        succes++;
                    } catch (Exception e) {
                        echecs++;
                        erreurs.append("• ").append(p.getEmail())
                                .append(": ").append(e.getMessage()).append("\n");
                    }
                }
                pdfFile.delete();

                if (echecs == 0) {
                    afficher(AlertType.INFORMATION,
                            "✓ " + succes + " email(s) envoyé(s) avec succès !");
                } else {
                    afficher(AlertType.WARNING,
                            succes + " réussi(s), " + echecs + " échec(s):\n" + erreurs);
                }
            }
        });
    }

    private void envoyerEmailA(Parent parent) {
        if (parent.getClasseId() == null) {
            afficher(AlertType.ERROR,
                    "Ce parent n'a pas de classe associée.\nL'administrateur doit lui en attribuer une.");
            return;
        }
        Classe classe = trouverClasseParId(parent.getClasseId());
        if (classe == null) {
            afficher(AlertType.ERROR, "Classe introuvable pour ce parent.");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Envoyer l'emploi du temps de la classe " + classe.getNom()
                + " à :\n" + parent.getNomComplet() + " <" + parent.getEmail() + "> ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                File pdfFile = genererPdfPourClasse(classe);
                if (pdfFile == null) return;

                try {
                    envoyerMailAvecPdf(parent, classe, pdfFile);
                    afficher(AlertType.INFORMATION,
                            "✓ Email envoyé à " + parent.getEmail() + " !");
                } catch (Exception e) {
                    afficher(AlertType.ERROR, "Échec d'envoi: " + e.getMessage());
                } finally {
                    pdfFile.delete();
                }
            }
        });
    }

    private File genererPdfPourClasse(Classe classe) {
        try {
            List<Seance> toutes = ss.getData();
            List<Seance> seancesClasse = new ArrayList<>();
            for (Seance s : toutes) {
                if (s.getClasseId() == classe.getId()) seancesClasse.add(s);
            }

            if (seancesClasse.isEmpty()) {
                afficher(AlertType.WARNING,
                        "Aucune séance pour la classe " + classe.getNom() + ".");
                return null;
            }

            File pdfFile = File.createTempFile("emploi_" + classe.getNom() + "_", ".pdf");
            EmploiDuTempsPdfGenerator.genererPourClasse(classe, seancesClasse, pdfFile);
            return pdfFile;
        } catch (Exception e) {
            afficher(AlertType.ERROR, "Erreur génération PDF: " + e.getMessage());
            return null;
        }
    }

    private void envoyerMailAvecPdf(Parent parent, Classe classe, File pdfFile) throws Exception {
        EmailService emailService = new EmailService();
        String sujet = "Emploi du temps — " + classe.getNom() + " (" + classe.getNiveau() + ")";
        String corps = "<html><body style='font-family: Arial, sans-serif; color: #1f2937;'>"
                + "<h2 style='color: #0f1419;'>Bonjour " + parent.getNomComplet() + ",</h2>"
                + "<p>Veuillez trouver ci-joint l'emploi du temps de votre enfant "
                + "pour la classe <b>" + classe.getNom() + "</b> (" + classe.getNiveau() + ").</p>"
                + "<p>Pour toute question, n'hésitez pas à nous contacter.</p>"
                + "<br/>"
                + "<p style='color: #6b7280; font-size: 13px;'>"
                + "Cordialement,<br/>"
                + "<b>L'équipe pédagogique EduNova</b></p>"
                + "</body></html>";

        emailService.envoyerAvecPDF(parent.getEmail(), sujet, corps, pdfFile);
    }

    // ── Filtres ───────────────────────────────────────────────────────

    private void appliquerFiltres() {
        if (parentsFiltres == null) return;

        String searchText = searchField.getText() == null ? ""
                : searchField.getText().trim().toLowerCase();
        Classe classeChoisie = filtreClasseCombo.getValue();

        parentsFiltres.setPredicate(parent -> {
            // Recherche multi-champ (nom, prénom, email)
            if (!searchText.isEmpty()) {
                String tout = ((parent.getNom() != null ? parent.getNom() : "") + " "
                        + (parent.getPrenom() != null ? parent.getPrenom() : "") + " "
                        + (parent.getEmail() != null ? parent.getEmail() : ""))
                        .toLowerCase();
                if (!tout.contains(searchText)) return false;
            }
            // Filtre classe
            if (classeChoisie != null) {
                if (parent.getClasseId() == null) return false;
                if (parent.getClasseId() != classeChoisie.getId()) return false;
            }
            return true;
        });

        majResultatLabel();
    }

    @FXML
    private void reinitialiserFiltre() {
        searchField.clear();
        filtreClasseCombo.setValue(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void rafraichirListe() {
        tousLesParents = FXCollections.observableArrayList(ps.getData());
        parentsFiltres = new FilteredList<>(tousLesParents, p -> true);
        parentsTable.setItems(parentsFiltres);
        majResultatLabel();
    }

    private void majResultatLabel() {
        if (resultatLabel != null) {
            int n = parentsFiltres != null ? parentsFiltres.size() : 0;
            int total = tousLesParents != null ? tousLesParents.size() : 0;
            resultatLabel.setText("(" + n + " sur " + total + " parent" + (total > 1 ? "s" : "") + ")");
        }
    }

    private Classe trouverClasseParId(int id) {
        for (Classe c : cs.getData()) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    private void afficher(AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(type == AlertType.ERROR ? "Erreur"
                : type == AlertType.WARNING ? "Attention" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
