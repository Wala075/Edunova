package edu.edunova.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.edunova.utils.ConfigLoader;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service de traduction multilingue.
 *
 * Utilise l'API MyMemory (https://mymemory.translated.net/) :
 *   - Gratuit, sans compte, sans carte bancaire
 *   - 50 000 caractères/jour gratuits avec un email "de" anonymous
 *   - Endpoint GET, simple à appeler
 *
 * Configuration .env (optionnelle) :
 *   MYMEMORY_EMAIL=ton-email@gmail.com   (augmente le quota gratuit)
 *
 * Le nom de la classe est resté "DeepLService" pour la compatibilité
 * avec le code existant (Bulletin.java). On a juste swappé le backend.
 */
public class DeepLService {

    private static final String ENDPOINT = "https://api.mymemory.translated.net/get";

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static final String LANG_AR = "ar";
    public static final String LANG_FR = "fr";
    public static final String LANG_EN = "en";

    /** Traduit FR → AR. Synchrone (à appeler dans un Task). */
    public String traduireEnArabe(String texteFrancais) throws IOException {
        return translate(texteFrancais, LANG_FR, LANG_AR);
    }

    /**
     * Traduction générique source → target.
     */
    public String translate(String text, String sourceLang, String targetLang) throws IOException {
        if (text == null || text.isBlank()) return "";

        // MyMemory limite chaque appel à 500 caractères -> on découpe et on concatène
        StringBuilder out = new StringBuilder();
        for (String chunk : splitForTranslation(text, 480)) {
            out.append(translateChunk(chunk, sourceLang, targetLang));
            out.append(' ');
        }
        return out.toString().trim();
    }

    private String translateChunk(String chunk, String src, String tgt) throws IOException {
        String email = ConfigLoader.get("MYMEMORY_EMAIL");

        HttpUrl.Builder url = HttpUrl.parse(ENDPOINT).newBuilder()
                .addQueryParameter("q", chunk)
                .addQueryParameter("langpair", src + "|" + tgt);
        if (email != null && !email.isBlank()) {
            url.addQueryParameter("de", email.trim());
        }

        Request req = new Request.Builder()
                .url(url.build())
                .header("User-Agent", "EduNova/1.0")
                .get()
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String raw = resp.body() == null ? "" : resp.body().string();

            // Log debug
            System.out.println("[MyMemory] HTTP " + resp.code() + " | "
                    + src + "->" + tgt + " | chunk=" + chunk.length() + " chars");

            if (!resp.isSuccessful()) {
                throw new IOException("Translate HTTP " + resp.code() + " : " + raw);
            }

            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            int status = obj.has("responseStatus")
                    ? obj.get("responseStatus").getAsInt() : 200;
            if (status >= 400) {
                String details = obj.has("responseDetails")
                        ? obj.get("responseDetails").getAsString() : "erreur inconnue";
                throw new IOException("Translate API status " + status + " : " + details);
            }

            JsonObject responseData = obj.getAsJsonObject("responseData");
            if (responseData == null || !responseData.has("translatedText")) {
                throw new IOException("Réponse traduction vide : " + raw);
            }
            return responseData.get("translatedText").getAsString();
        }
    }

    /**
     * Découpe un texte en chunks <= maxLen caractères en respectant
     * les frontières de phrases (point, point-virgule, virgule).
     */
    private static java.util.List<String> splitForTranslation(String text, int maxLen) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (text.length() <= maxLen) {
            out.add(text);
            return out;
        }
        // Découper sur la ponctuation (en gardant le séparateur)
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder cur = new StringBuilder();
        for (String s : sentences) {
            if (cur.length() + s.length() + 1 > maxLen) {
                if (cur.length() > 0) {
                    out.add(cur.toString().trim());
                    cur.setLength(0);
                }
                // Si une seule phrase > maxLen, découpe brutale
                if (s.length() > maxLen) {
                    int start = 0;
                    while (start < s.length()) {
                        int end = Math.min(start + maxLen, s.length());
                        out.add(s.substring(start, end));
                        start = end;
                    }
                    continue;
                }
            }
            if (cur.length() > 0) cur.append(' ');
            cur.append(s);
        }
        if (cur.length() > 0) out.add(cur.toString().trim());
        return out;
    }
}
