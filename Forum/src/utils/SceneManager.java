package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    public static FXMLLoader naviguer(String fxml, Stage stage, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/"+fxml));
            Parent root = loader.load();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            return loader;
        } catch (IOException e) {
            AlertHelper.erreur("Navigation", e.getMessage()); return null;
        }
    }
    public static FXMLLoader ouvrirModal(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/"+fxml));
            Parent root = loader.load();
            Stage s = new Stage();
            s.setTitle(titre); s.initModality(Modality.APPLICATION_MODAL);
            s.setScene(new Scene(root)); s.showAndWait();
            return loader;
        } catch (IOException e) {
            AlertHelper.erreur("Modal", e.getMessage()); return null;
        }
    }
    public static Stage getStage(javafx.scene.Node n) { return (Stage)n.getScene().getWindow(); }
}
