package edunova.connexion.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HCaptchaService {

    private static final String SECRET_KEY = getSecretKey();

    private static String getSecretKey() {
        // Load from environment variable
        String envKey = System.getenv("HCAPTCHA_SECRET_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        
        // If not set, throw exception to alert developer
        throw new IllegalStateException("HCAPTCHA_SECRET_KEY environment variable not set. Please configure .env file.");
    }

    private static final String VERIFY_URL =
            "https://hcaptcha.com/siteverify";

    public static boolean verifier(String token) {
        if (token == null || token.isEmpty()) {
            System.out.println(
                    "hCaptcha: token vide");
            return false;
        }
        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            String params =
                    "secret=" + URLEncoder.encode(
                            SECRET_KEY,
                            StandardCharsets.UTF_8) +
                            "&response=" + URLEncoder.encode(
                            token,
                            StandardCharsets.UTF_8);

            System.out.println(
                    "hCaptcha: envoi vérification...");

            try (OutputStream os =
                         conn.getOutputStream()) {
                os.write(params.getBytes(
                        StandardCharsets.UTF_8));
            }

            StringBuilder response =
                    new StringBuilder();
            try (BufferedReader br =
                         new BufferedReader(
                                 new InputStreamReader(
                                         conn.getInputStream(),
                                         StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null)
                    response.append(line);
            }

            String resp = response.toString();
            System.out.println(
                    "hCaptcha response: " + resp);
            return resp.contains("\"success\":true");

        } catch (Exception e) {
            System.out.println(
                    "hCaptcha erreur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Ouvrir le navigateur système ──────────────────────────────
    public static void ouvrirNavigateur(String url) {
        try {
            String os = System.getProperty("os.name")
                    .toLowerCase();
            Runtime rt = Runtime.getRuntime();
            if (os.contains("win")) {
                rt.exec(new String[]{
                        "rundll32", "url.dll,FileProtocolHandler",
                        url});
            } else if (os.contains("mac")) {
                rt.exec(new String[]{"open", url});
            } else {
                rt.exec(new String[]{"xdg-open", url});
            }
        } catch (Exception e) {
            System.out.println(
                    "Erreur ouverture navigateur: " +
                            e.getMessage());
        }
    }
}