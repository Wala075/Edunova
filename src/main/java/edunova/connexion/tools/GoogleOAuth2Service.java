package edunova.connexion.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GoogleOAuth2Service {

    private static final String CLIENT_ID = getClientId();
    private static final String CLIENT_SECRET = getClientSecret();
    private static final String TOKEN_URL = 
        "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = 
        "https://www.googleapis.com/oauth2/v2/userinfo";
    
    private static String getClientId() {
        String envId = System.getProperty("GOOGLE_CLIENT_ID");
        if (envId == null) {
            envId = System.getenv("GOOGLE_CLIENT_ID");
        }
        if (envId != null && !envId.isEmpty()) {
            return envId;
        }
        throw new IllegalStateException("GOOGLE_CLIENT_ID environment variable not set. Please configure .env file.");
    }
    
    private static String getClientSecret() {
        String envSecret = System.getProperty("GOOGLE_CLIENT_SECRET");
        if (envSecret == null) {
            envSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        }
        if (envSecret != null && !envSecret.isEmpty()) {
            return envSecret;
        }
        throw new IllegalStateException("GOOGLE_CLIENT_SECRET environment variable not set. Please configure .env file.");
    }
    
    // Port dynamique pour le callback OAuth2
    private static int callbackPort = 8888;

    public static void setCallbackPort(int port) {
        callbackPort = port;
        System.out.println("GoogleOAuth2Service: Port de callback défini à " + port);
    }

    public static String[] echangerCodePourInfos(String code) {
        try {
            System.out.println("GoogleOAuth2Service: Échange du code...");
            
            // Étape 1 : Échanger le code pour un token
            String token = echangerCodePourToken(code);
            if (token == null) {
                System.err.println("GoogleOAuth2Service: Impossible d'obtenir le token");
                return null;
            }
            
            System.out.println("GoogleOAuth2Service: Token obtenu");
            
            // Étape 2 : Utiliser le token pour obtenir les infos utilisateur
            String[] userInfo = obtenirInfosUtilisateur(token);
            if (userInfo == null) {
                System.err.println("GoogleOAuth2Service: Impossible d'obtenir les infos utilisateur");
                return null;
            }
            
            System.out.println("GoogleOAuth2Service: Infos utilisateur obtenues - Email: " + userInfo[0]);
            return userInfo;
            
        } catch (Exception e) {
            System.err.println("GoogleOAuth2Service: Erreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String echangerCodePourToken(String code) {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Utiliser le port dynamique pour le redirect_uri
            String redirectUri = "http://localhost:" + callbackPort + "/Callback";
            
            String params = 
                "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("GoogleOAuth2Service: Code de réponse token: " + responseCode);

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300 
                                ? conn.getInputStream() 
                                : conn.getErrorStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            String json = response.toString();
            System.out.println("GoogleOAuth2Service: Réponse token: " + json.substring(0, Math.min(100, json.length())));
            
            return extraireValeurJson(json, "access_token");

        } catch (Exception e) {
            System.err.println("GoogleOAuth2Service: Erreur échange code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String[] obtenirInfosUtilisateur(String token) {
        try {
            URL url = new URL(USERINFO_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            System.out.println("GoogleOAuth2Service: Code de réponse userinfo: " + responseCode);

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300 
                                ? conn.getInputStream() 
                                : conn.getErrorStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            String json = response.toString();
            System.out.println("GoogleOAuth2Service: Réponse userinfo: " + json.substring(0, Math.min(150, json.length())));

            String email = extraireValeurJson(json, "email");
            String nom = extraireValeurJson(json, "family_name");
            String prenom = extraireValeurJson(json, "given_name");

            if (email == null || email.isEmpty()) {
                System.err.println("GoogleOAuth2Service: Email non trouvé dans la réponse");
                return null;
            }

            return new String[]{
                email,
                nom != null ? nom : "",
                prenom != null ? prenom : ""
            };

        } catch (Exception e) {
            System.err.println("GoogleOAuth2Service: Erreur obtention infos: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String extraireValeurJson(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int idx = json.indexOf(search);
            if (idx == -1) {
                // Essayer sans guillemets (pour les nombres)
                search = "\"" + key + "\":";
                idx = json.indexOf(search);
                if (idx == -1) return null;
                idx += search.length();
                int end = json.indexOf(",", idx);
                if (end == -1) end = json.indexOf("}", idx);
                return json.substring(idx, end).trim();
            }
            idx += search.length();
            int end = json.indexOf("\"", idx);
            return json.substring(idx, end);
        } catch (Exception e) {
            return null;
        }
    }
}
