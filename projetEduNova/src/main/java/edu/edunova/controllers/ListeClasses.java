package edu.edunova.controllers;

import edu.edunova.entities.Classe;
import edu.edunova.services.ClasseService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ListeClasses {

    @FXML private TextField searchField;
    @FXML private Label resultatLabel;

    @FXML private TableView<Classe> classesTable;
    @FXML private TableColumn<Classe, String> colNom;
    @FXML private TableColumn<Classe, String> colNiveau;
    @FXML private TableColumn<Classe, Integer> colCapacite;
    @FXML private TableColumn<Classe, String> colStatut;

    private final ClasseService cs = new ClasseService();
    private ObservableList<Classe> toutesLesClasses;
    private FilteredList<Classe> classesFiltrees;

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));

        colCapacite.setCellFactory(column -> new TableCell<Classe, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item + " places");
            }
        });

        colStatut.setCellValueFactory(cellData -> {
            int cap = cellData.getValue().getCapacite();
            if (cap >= 30) return new SimpleStringProperty("Eleve");
            else if (cap >= 15) return new SimpleStringProperty("Moyen");
            else return new SimpleStringProperty("Faible");
        });

        rafraichir();

        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltre());
    }

    private void rafraichir() {
        toutesLesClasses = FXCollections.observableArrayList(cs.getData());
        classesFiltrees = new FilteredList<>(toutesLesClasses, p -> true);
        classesTable.setItems(classesFiltrees);
        majResultat();
    }

    private void appliquerFiltre() {
        if (classesFiltrees == null) return;
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        classesFiltrees.setPredicate(c -> {
            if (q.isEmpty()) return true;
            return c.getNom().toLowerCase().contains(q)
                    || c.getNiveau().toLowerCase().contains(q);
        });
        majResultat();
    }

    private void majResultat() {
        if (resultatLabel == null) return;
        int n = classesFiltrees != null ? classesFiltrees.size() : 0;
        int total = toutesLesClasses != null ? toutesLesClasses.size() : 0;
        resultatLabel.setText("(" + n + " sur " + total + " classe" + (total > 1 ? "s" : "") + ")");
    }
}
