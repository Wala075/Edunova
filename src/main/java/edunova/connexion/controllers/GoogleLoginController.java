package edunova.connexion.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class GoogleLoginController {

    @FXML private WebView webView;
    
    private Runnable onSuccessCallback;
    private String authorizationCode;
    private boolean traite = false;
    private HttpServer httpServer;

    public void setOnTokenCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @FXML
    public void initialize() {
        WebEngine engine = webView.getEngine();
        
        // Démarrer le serveur HTTP local
        demarrerServeurLocal();
        
        // Configuration Google OAuth2 - Load from environment
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID environment variable not set");
        }
        
        String redirectUri = "http://localhost:8888/Callback";
        String scope = "openid email profile";
        
        // URL de connexion Google
        String googleAuthUrl = 
            "https://accounts.google.com/o/oauth2/v2/auth?" +
            "client_id=" + clientId +
            "&redirect_uri=" + redirectUri +
            "&response_type=code" +
            "&scope=" + scope +
            "&access_type=offline";
        
        System.out.println("GoogleLoginController: Démarrage du serveur local sur le port 8888");
        System.out.println("GoogleLoginController: Chargement URL: " + googleAuthUrl);
        
        // Charger la page de connexion Google
        engine.load(googleAuthUrl);
    }

    private void demarrerServeurLocal() {
        new Thread(() -> {
            try {
                httpServer = HttpServer.create(new InetSocketAddress(8888), 0);
                httpServer.createContext("/Callback", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        System.out.println("GoogleLoginController: Requête reçue: " + exchange.getRequestURI());
                        
                        String query = exchange.getRequestURI().getQuery();
                        if (query != null && query.contains("code=")) {
                            if (!traite) {
                                traite = true;
                                
                                // Extraire le code
                                String code = extraireCodeDeURL(query);
                                if (code != null && !code.isEmpty()) {
                                    System.out.println("GoogleLoginController: Code d'autorisation reçu");
                                    authorizationCode = code;
                                    
                                    // Envoyer une réponse HTML
                                    String response = 
                                        "<html><body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                                        "<h1>✅ Connexion réussie!</h1>" +
                                        "<p>Veuillez patienter...</p>" +
                                        "</body></html>";
                                    
                                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                                    exchange.sendResponseHeaders(200, response.getBytes().length);
                                    OutputStream os = exchange.getResponseBody();
                                    os.write(response.getBytes());
                                    os.close();
                                    
                                    // Appeler le callback
                                    if (onSuccessCallback != null) {
                                        Platform.runLater(() -> {
                                            onSuccessCallback.run();
                                            fermer();
                                        });
                                    }
                                }
                            }
                        } else {
                            // Erreur
                            String response = 
                                "<html><body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                                "<h1>❌ Erreur d'authentification</h1>" +
                                "<p>Impossible de récupérer le code d'autorisation.</p>" +
                                "</body></html>";
                            
                            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                            exchange.sendResponseHeaders(400, response.getBytes().length);
                            OutputStream os = exchange.getResponseBody();
                            os.write(response.getBytes());
                            os.close();
                        }
                    }
                });
                
                httpServer.setExecutor(null);
                httpServer.start();
                System.out.println("GoogleLoginController: Serveur HTTP démarré sur le port 8888");
                
            } catch (IOException e) {
                System.err.println("GoogleLoginController: Erreur démarrage serveur: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private String extraireCodeDeURL(String query) {
        try {
            if (query.contains("code=")) {
                String[] parts = query.split("code=");
                if (parts.length > 1) {
                    String code = parts[1];
                    // Supprimer les paramètres supplémentaires
                    if (code.contains("&")) {
                        code = code.split("&")[0];
                    }
                    // Décoder l'URL
                    String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
                    System.out.println("GoogleLoginController: Code extrait: " + decodedCode.substring(0, Math.min(20, decodedCode.length())) + "...");
                    return decodedCode;
                }
            }
        } catch (Exception e) {
            System.err.println("GoogleLoginController: Erreur extraction code: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    private void fermer() {
        // Arrêter le serveur
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("GoogleLoginController: Serveur HTTP arrêté");
        }
        
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
    }
}
