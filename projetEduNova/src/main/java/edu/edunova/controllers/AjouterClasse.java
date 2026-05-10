package edu.edunova.controllers;

import edu.edunova.entities.Classe;
import edu.edunova.services.ClasseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AjouterClasse {

    @FXML private ComboBox<Classe> selectionCombo;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private TextField capaciteField;

    @FXML private Label nomError;
    @FXML private Label niveauError;
    @FXML private Label capaciteError;

    private final ClasseService cs = new ClasseService();
    private Classe classeSelectionnee = null;

    private static final String STYLE_VALID = "-fx-background-color: white; -fx-background-radius: 14px; -fx-border-color: #16a34a; -fx-border-radius: 14px; -fx-border-width: 1.5px;";
    private static final String STYLE_INVALID = "-fx-background-color: white; -fx-background-radius: 14px; -fx-border-color: #dc2626; -fx-border-radius: 14px; -fx-border-width: 1.5px;";
    private static final String STYLE_DEFAULT = "";

    @FXML
    public void initialize() {
        ObservableList<String> niveaux = FXCollections.observableArrayList(
                "6ème", "5ème", "4ème", "3ème", "2ème", "1ère"
        );
        niveauCombo.setItems(niveaux);

        rafraichirSelection();

        selectionCombo.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                classeSelectionnee = val;
                nomField.setText(val.getNom());
                niveauCombo.setValue(val.getNiveau());
                capaciteField.setText(String.valueOf(val.getCapacite()));
                clearValidation();
            }
        });

        nomField.textProperty().addListener((obs, old, val) -> validerNom());
        niveauCombo.valueProperty().addListener((obs, old, val) -> validerNiveau());
        capaciteField.textProperty().addListener((obs, old, val) -> validerCapacite());
    }

    private void rafraichirSelection() {
        ObservableList<Classe> classes = FXCollections.observableArrayList(cs.getData());
        selectionCombo.setItems(classes);
    }

    private boolean validerNom() {
        String txt = nomField.getText();
        if (txt == null || txt.trim().isEmpty()) {
            nomField.setStyle(STYLE_INVALID);
            nomError.setText("Le nom est obligatoire");
            return false;
        }
        String nom = txt.trim();
        if (nom.length() < 2) {
            nomField.setStyle(STYLE_INVALID);
            nomError.setText("Min. 2 caracteres");
            return false;
        }
        if (nom.length() > 20) {
            nomField.setStyle(STYLE_INVALID);
            nomError.setText("Max. 20 caracteres");
            return false;
        }
        if (!nom.matches("[A-Za-z0-9 \\-]+")) {
            nomField.setStyle(STYLE_INVALID);
            nomError.setText("Lettres, chiffres, espace ou - uniquement");
            return false;
        }
        for (Classe c : cs.getData()) {
            if (c.getNom().equalsIgnoreCase(nom)
                    && (classeSelectionnee == null || c.getId() != classeSelectionnee.getId())) {
                nomField.setStyle(STYLE_INVALID);
                nomError.setText("Une classe avec ce nom existe deja");
                return false;
            }
        }
        nomField.setStyle(STYLE_VALID);
        nomError.setText("");
        return true;
    }

    private boolean validerNiveau() {
        if (niveauCombo.getValue() == null) {
            niveauCombo.setStyle(STYLE_INVALID);
            niveauError.setText("Selectionnez un niveau");
            return false;
        }
        niveauCombo.setStyle(STYLE_VALID);
        niveauError.setText("");
        return true;
    }

    private boolean validerCapacite() {
        String txt = capaciteField.getText();
        if (txt == null || txt.trim().isEmpty()) {
            capaciteField.setStyle(STYLE_INVALID);
            capaciteError.setText("Capacite obligatoire");
            return false;
        }
        try {
            int cap = Integer.parseInt(txt.trim());
            if (cap <= 0) {
                capaciteField.setStyle(STYLE_INVALID);
                capaciteError.setText("Doit etre > 0");
                return false;
            }
            if (cap > 35) {
                capaciteField.setStyle(STYLE_INVALID);
                capaciteError.setText("Max. 35 places");
                return false;
            }
        } catch (NumberFormatException e) {
            capaciteField.setStyle(STYLE_INVALID);
            capaciteError.setText("Nombre entier requis");
            return false;
        }
        capaciteField.setStyle(STYLE_VALID);
        capaciteError.setText("");
        return true;
    }

    private boolean validerFormulaire() {
        boolean a = validerNom();
        boolean b = validerNiveau();
        boolean c = validerCapacite();
        return a && b && c;
    }

    private void clearValidation() {
        nomField.setStyle(STYLE_DEFAULT);
        niveauCombo.setStyle(STYLE_DEFAULT);
        capaciteField.setStyle(STYLE_DEFAULT);
        nomError.setText("");
        niveauError.setText("");
        capaciteError.setText("");
    }

    @FXML
    private void ajouterClasse() {
        if (!validerFormulaire()) return;
        Classe c = new Classe(
                nomField.getText().trim(),
                niveauCombo.getValue(),
                Integer.parseInt(capaciteField.getText().trim())
        );
        cs.addEntity(c);
        afficher(AlertType.INFORMATION, "Classe ajoutee avec succes !");
        viderForm();
        rafraichirSelection();
    }

    @FXML
    private void modifierClasse() {
        if (classeSelectionnee == null) {
            afficher(AlertType.WARNING, "Veuillez selectionner une classe a modifier dans le menu deroulant.");
            return;
        }
        if (!validerFormulaire()) return;
        Classe c = new Classe(
                nomField.getText().trim(),
                niveauCombo.getValue(),
                Integer.parseInt(capaciteField.getText().trim())
        );
        cs.updateEntity(classeSelectionnee.getId(), c);
        afficher(AlertType.INFORMATION, "Classe modifiee avec succes !");
        viderForm();
        rafraichirSelection();
    }

    @FXML
    private void supprimerClasse() {
        if (classeSelectionnee == null) {
            afficher(AlertType.WARNING, "Veuillez selectionner une classe a supprimer dans le menu deroulant.");
            return;
        }
        String dependances = cs.getReferencesDetails(classeSelectionnee.getId());
        if (!dependances.isEmpty()) {
            afficher(AlertType.ERROR, "Suppression impossible.\nCette classe contient :\n" + dependances);
            return;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer la classe \""
                + classeSelectionnee.getNom() + "\" ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cs.deleteEntity(classeSelectionnee);
                afficher(AlertType.INFORMATION, "Classe supprimee !");
                viderForm();
                rafraichirSelection();
            }
        });
    }

    @FXML
    private void viderForm() {
        nomField.clear();
        niveauCombo.setValue(null);
        capaciteField.clear();
        selectionCombo.setValue(null);
        classeSelectionnee = null;
        clearValidation();
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
