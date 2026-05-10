import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DatabaseConnection;

/**
 * Application entry point.
 * NOTE: intentionally left without a package declaration so that
 * the Maven source root (src/) resolves it as the default package,
 * matching the pom.xml <mainClass>MainApp</mainClass> setting.
 * All other classes use explicit packages (controllers, services, etc.)
 * and are referenced by their fully-qualified names from FXML files.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("EduNova — Forum");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(560);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
