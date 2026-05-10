package edunova.connexion.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
import java.util.concurrent.CountDownLatch;

/**
 * Contrôleur pour la fenêtre de connexion Google OAuth2 avec WebView intégrée
 * Affiche la page de connexion Google directement dans l'application
 */
public class GoogleOAuth2WindowController {

    @FXML private VBox mainContainer;
    @FXML private Label lblTitle;
    @FXML private WebView webView;
    @FXML private Button btnCancel;

    private Runnable onSuccessCallback;
    private String authorizationCode;
    private boolean traite = false;
    private HttpServer httpServer;
    private Stage stage;
    private int serverPort = -1;
    private CountDownLatch serverStarted = new CountDownLatch(1);

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        System.out.println("GoogleOAuth2WindowController: Initialisation");
        
        // Démarrer le serveur HTTP local pour le callback
        demarrerServeurLocal();
        
        // Configurer le bouton Annuler
        btnCancel.setOnAction(e -> annuler());
        
        // Charger la page de connexion Google après que le serveur soit prêt
        new Thread(() -> {
            try {
                serverStarted.await(); // Attendre que le serveur soit prêt
                // Passer le port au service OAuth2
                edunova.connexion.tools.GoogleOAuth2Service.setCallbackPort(serverPort);
                Platform.runLater(this::chargerPageConnexionGoogle);
            } catch (InterruptedException e) {
                System.err.println("GoogleOAuth2WindowController: Erreur attente serveur: " + e.getMessage());
            }
        }).start();
    }

    private void chargerPageConnexionGoogle() {
        try {
            WebEngine engine = webView.getEngine();
            
            // Configuration Google OAuth2 - Load from environment
            String clientId = System.getProperty("GOOGLE_CLIENT_ID");
            if (clientId == null) {
                clientId = System.getenv("GOOGLE_CLIENT_ID");
            }
            if (clientId == null || clientId.isEmpty()) {
                throw new IllegalStateException("GOOGLE_CLIENT_ID environment variable not set");
            }
            
            String redirectUri = "http://localhost:" + serverPort + "/Callback";
            String scope = "openid email profile";
            
            // URL de connexion Google avec encodage correct
            String googleAuthUrl = 
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri, "UTF-8") +
                "&response_type=code" +
                "&scope=" + java.net.URLEncoder.encode(scope, "UTF-8") +
                "&access_type=offline";
            
            System.out.println("GoogleOAuth2WindowController: Chargement URL: " + googleAuthUrl);
            
            // Charger la page de connexion Google dans le WebView
            engine.load(googleAuthUrl);
            
        } catch (Exception ex) {
            System.err.println("GoogleOAuth2WindowController: Erreur chargement page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void demarrerServeurLocal() {
        new Thread(() -> {
            try {
                // Essayer de démarrer le serveur sur le port 8888
                int port = 8888;
                HttpServer server = null;
                
                // Essayer plusieurs fois avec des délais
                for (int tentative = 0; tentative < 5; tentative++) {
                    try {
                        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
                        System.out.println("GoogleOAuth2WindowController: Serveur HTTP créé sur le port " + port);
                        break;
                    } catch (IOException e) {
                        if (tentative < 4) {
                            System.out.println("GoogleOAuth2WindowController: Port " + port + " occupé, tentative " + (tentative + 1));
                            port++;
                            Thread.sleep(500);
                        } else {
                            // Utiliser un port dynamique
                            System.out.println("GoogleOAuth2WindowController: Tous les ports occupés, utilisation d'un port dynamique");
                            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                            port = server.getAddress().getPort();
                            System.out.println("GoogleOAuth2WindowController: Port dynamique assigné: " + port);
                        }
                    }
                }
                
                httpServer = server;
                serverPort = port;
                final int finalPort = port;
                
                httpServer.createContext("/Callback", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        System.out.println("GoogleOAuth2WindowController: Callback reçu sur le port " + finalPort);
                        
                        String query = exchange.getRequestURI().getQuery();
                        if (query != null && query.contains("code=")) {
                            if (!traite) {
                                traite = true;
                                
                                // Extraire le code
                                String code = extraireCodeDeURL(query);
                                if (code != null && !code.isEmpty()) {
                                    System.out.println("GoogleOAuth2WindowController: Code d'autorisation reçu");
                                    authorizationCode = code;
                                    
                                    // Envoyer une réponse HTML
                                    String response = 
                                        "<html><body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                                        "<h1>✅ Connexion réussie!</h1>" +
                                        "<p>Redirection vers l'application...</p>" +
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
                System.out.println("GoogleOAuth2WindowController: Serveur HTTP démarré sur le port " + finalPort);
                
                // Signal que le serveur est prêt
                serverStarted.countDown();
                
            } catch (IOException | InterruptedException e) {
                System.err.println("GoogleOAuth2WindowController: Erreur démarrage serveur: " + e.getMessage());
                e.printStackTrace();
                serverStarted.countDown();
            }
        }).start();
    }

    private String extraireCodeDeURL(String query) {
        try {
            if (query.contains("code=")) {
                String[] parts = query.split("code=");
                if (parts.length > 1) {
                    String code = parts[1];
                    if (code.contains("&")) {
                        code = code.split("&")[0];
                    }
                    String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
                    System.out.println("GoogleOAuth2WindowController: Code extrait");
                    return decodedCode;
                }
            }
        } catch (Exception e) {
            System.err.println("GoogleOAuth2WindowController: Erreur extraction code: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        // Arrêter le serveur
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("GoogleOAuth2WindowController: Serveur HTTP arrêté");
        }
        
        if (stage != null) {
            stage.close();
        }
    }
}

