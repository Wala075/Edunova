package edunova.connexion.controllers;

import edunova.connexion.tools.HCaptchaService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CaptchaController {

    @FXML private WebView webView;

    private Runnable onSuccessCallback;
    private boolean  traite = false;

    public void setOnSuccessCallback(Runnable r) {
        this.onSuccessCallback = r;
    }

    @FXML
    public void initialize() {
        WebEngine engine = webView.getEngine();

        // Charger la page hCaptcha
        String url = getClass()
                .getResource("/views/captcha.html")
                .toExternalForm();
        engine.load(url);

        // Intercepter captcha://success?token=...
        engine.locationProperty()
                .addListener((obs, oldLoc, newLoc) -> {
                    if (newLoc != null &&
                            newLoc.startsWith(
                                    "captcha://success") &&
                            !traite) {

                        traite = true;

                        // Extraire le token
                        String token = "";
                        try {
                            if (newLoc.contains("token=")) {
                                String raw = newLoc
                                        .split("token=")[1];
                                token = URLDecoder.decode(
                                        raw,
                                        StandardCharsets.UTF_8);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        final String finalToken = token;

                        // Vérifier avec l'API hCaptcha
                        new Thread(() -> {
                            boolean ok =
                                    HCaptchaService.verifier(
                                            finalToken);

                            Platform.runLater(() -> {
                                if (ok) {
                                    if (onSuccessCallback
                                            != null)
                                        onSuccessCallback
                                                .run();
                                    fermer();
                                } else {
                                    // Échec → recharger
                                    traite = false;
                                    engine.load(url);
                                }
                            });
                        }).start();
                    }
                });
    }

    private void fermer() {
        Stage stage = (Stage)
                webView.getScene().getWindow();
        stage.close();
    }
}
