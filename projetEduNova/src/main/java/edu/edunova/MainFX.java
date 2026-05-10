package edu.edunova;

import edu.edunova.services.MatiereService;
import edu.edunova.services.TeacherService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Auto-init: si matiere/teacher vides → insère données test
        new MatiereService().insertDataTestSiVide();
        new TeacherService().insertDataTestSiVide();

        Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("EduNova — Plateforme de gestion scolaire");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
