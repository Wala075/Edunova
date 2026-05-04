package edunova.connexion.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HCaptchaService {

    private static final String SECRET_KEY =
            "ES_804bcf09368b42b7b2a91e658e7c09e6";

    private static final String VERIFY_URL =
            "https://hcaptcha.com/siteverify";

    public static boolean verifier(String token) {
        if (token == null || token.isEmpty())
            return false;
        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String params =
                    "secret=" + URLEncoder.encode(
                            SECRET_KEY,
                            StandardCharsets.UTF_8) +
                            "&response=" + URLEncoder.encode(
                            token,
                            StandardCharsets.UTF_8);

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
                                         conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null)
                    response.append(line);
            }

            System.out.println(
                    "hCaptcha response: " + response);
            return response.toString()
                    .contains("\"success\":true");

        } catch (Exception e) {
            System.out.println(
                    "Erreur hCaptcha : " + e.getMessage());
            return false;
        }
    }
}
