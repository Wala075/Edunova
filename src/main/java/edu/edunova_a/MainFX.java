package edu.edunova_a;

import edu.edunova_a.config.AppConfig;
import edu.edunova_a.http.AttendanceHttpServer;
import edu.edunova_a.utils.NgrokService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 1. Démarre le serveur HTTP embarqué
        AttendanceHttpServer.getInstance().start();
        int port = AttendanceHttpServer.getInstance().getActualPort();

        // 2. Démarre ngrok automatiquement si pas d'URL publique configurée
        String publicUrl = AppConfig.get("attendance.public.url", "").trim();
        if (publicUrl.isEmpty()) {
            System.out.println("[MainFX] Démarrage du tunnel ngrok sur port " + port + "...");
            String ngrokUrl = NgrokService.start(port);
            if (ngrokUrl != null) {
                System.out.println("[MainFX] URL publique ngrok : " + ngrokUrl);
                // Injecte l'URL dans AppConfig pour que PointageController l'utilise
                AppConfig.setPublicUrl(ngrokUrl);
            } else {
                System.out.println("[MainFX] ngrok non disponible — utilisation IP locale");
            }
        }

        // 3. Charge l'interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1366, 720);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/dark.css")).toExternalForm());
        // Thème pro unifié — surcharge dark.css avec une palette violet/indigo
        // et stylise tous les composants (boutons, tables, inputs, scrollbars, tabs...)
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/pro.css")).toExternalForm());

        primaryStage.setTitle("EduNova - Application de gestion scolaire");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(640);
        primaryStage.show();

        // 4. Arrêt propre à la fermeture
        primaryStage.setOnCloseRequest(e -> {
            AttendanceHttpServer.getInstance().stop();
            NgrokService.stop();
        });
    }
}
