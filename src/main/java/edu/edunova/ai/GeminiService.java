package edu.edunova.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.edunova.entities.Bulletin;
import edu.edunova.utils.ConfigLoader;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Appelle l'API Gemini (Google AI) pour générer du texte.
 *
 * Utilise le modèle gemini-1.5-flash (gratuit dans la limite quota,
 * rapide ~1-2s, parfait pour des appréciations).
 *
 * Configuration :
 *   .env -> GEMINI_API_KEY=AIzaSy...
 *
 * Usage :
 *   String txt = new GeminiService().genererAppreciation(bulletin);
 */
public class GeminiService {

    /**
     * Modèle Gemini utilisé. Modifiable via .env :
     *   GEMINI_MODEL=gemini-2.5-flash
     * Modèles dispo (mai 2025) :
     *   - gemini-2.5-flash       (recommandé : rapide, gratuit, FR ok)
     *   - gemini-2.5-pro         (plus puissant, plus lent)
     *   - gemini-2.0-flash       (stable)
     *   - gemini-flash-latest    (alias dynamique)
     */
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";

    private static String endpoint() {
        String model = ConfigLoader.get("GEMINI_MODEL", DEFAULT_MODEL);
        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent";
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    // ============================================================
    // API publique
    // ============================================================

    /** Génère une appréciation pour un bulletin. Synchrone (à appeler dans un Task). */
    public String genererAppreciation(Bulletin b) throws IOException {
        String prompt = PromptBuilder.buildAppreciation(b);
        return generate(prompt);
    }

    /** Reformule/corrige un texte brut écrit par le professeur. */
    public String reformulerAppreciation(Bulletin b, String texteUtilisateur) throws IOException {
        if (texteUtilisateur == null || texteUtilisateur.isBlank()) {
            throw new IOException("Texte vide à reformuler.");
        }
        String prompt = PromptBuilder.buildReformulation(b, texteUtilisateur);
        return generate(prompt);
    }

    /** Appel générique : envoie un prompt brut, renvoie la réponse texte. */
    public String generate(String prompt) throws IOException {
        String apiKey = ConfigLoader.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("GEMINI_API_KEY non configurée. " +
                    "Crée un fichier .env à la racine du projet (voir .env.example).");
        }

        // ---- Construction du body JSON ----
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject genConfig = new JsonObject();
        // Temperature basse = sortie cohérente, vocabulaire choisi (style "professeur principal")
        genConfig.addProperty("temperature", 0.55);
        genConfig.addProperty("topP", 0.9);
        // ⚠ IMPORTANT pour Gemini 2.5 : maxOutputTokens inclut les tokens de "thinking"
        // qui mangent le quota visible. On met large + on désactive le thinking ci-dessous.
        genConfig.addProperty("maxOutputTokens", 2048);

        // Désactiver le mode "thinking" de Gemini 2.5 :
        // sinon les jetons de raisonnement mangent maxOutputTokens
        // et la réponse visible est tronquée.
        JsonObject thinking = new JsonObject();
        thinking.addProperty("thinkingBudget", 0);
        genConfig.add("thinkingConfig", thinking);

        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", genConfig);

        // ---- Requête HTTP ----
        Request req = new Request.Builder()
                .url(endpoint() + "?key=" + apiKey)
                .post(RequestBody.create(gson.toJson(body), JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            ResponseBody rb = resp.body();
            String raw = rb == null ? "" : rb.string();

            if (!resp.isSuccessful()) {
                throw new IOException("Gemini HTTP " + resp.code() + " : " + extractErrorMessage(raw));
            }

            return extractText(raw);
        }
    }

    // ============================================================
    // Parsing réponse
    // ============================================================

    /** Extrait candidates[0].content.parts[0].text et concatène toutes les parts. */
    private String extractText(String raw) throws IOException {
        try {
            JsonElement root = JsonParser.parseString(raw);
            JsonObject obj = root.getAsJsonObject();

            JsonArray candidates = obj.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                // Si Gemini bloque le contenu (safety), il met promptFeedback
                if (obj.has("promptFeedback")) {
                    throw new IOException("Gemini a bloqué la requête : "
                            + obj.get("promptFeedback").toString());
                }
                throw new IOException("Aucun candidat dans la réponse Gemini : " + raw);
            }

            JsonObject candidate = candidates.get(0).getAsJsonObject();

            // Détecter une réponse tronquée par MAX_TOKENS
            String finishReason = candidate.has("finishReason")
                    ? candidate.get("finishReason").getAsString() : "";

            JsonObject content = candidate.getAsJsonObject("content");
            if (content == null) {
                if ("MAX_TOKENS".equals(finishReason)) {
                    throw new IOException("Limite de tokens atteinte avant que Gemini ait pu répondre. " +
                            "Augmente maxOutputTokens ou désactive thinkingConfig.");
                }
                throw new IOException("Pas de 'content' dans la réponse Gemini (finishReason="
                        + finishReason + ")");
            }

            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.size() == 0) {
                throw new IOException("Pas de 'parts' dans la réponse Gemini (finishReason="
                        + finishReason + ")");
            }

            // Concaténer toutes les parts (utile pour les longues réponses)
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                JsonObject p = parts.get(i).getAsJsonObject();
                if (p.has("text")) {
                    out.append(p.get("text").getAsString());
                }
            }

            String text = out.toString();
            // Petit nettoyage : retirer guillemets et "Voici l'appréciation :" etc.
            text = text.trim()
                    .replaceAll("^[\"«»\\s]+", "")
                    .replaceAll("[\"«»\\s]+$", "");

            // Avertissement console si tronqué (mais on retourne quand même ce qu'on a)
            if ("MAX_TOKENS".equals(finishReason)) {
                System.err.println("[Gemini] WARNING: réponse tronquée (MAX_TOKENS). " +
                        "Considère augmenter maxOutputTokens.");
            }

            if (text.isBlank()) {
                throw new IOException("Réponse Gemini vide. finishReason=" + finishReason);
            }
            return text;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Réponse Gemini malformée : " + e.getMessage()
                    + " | raw=" + raw, e);
        }
    }

    /** Tente d'extraire un message d'erreur lisible depuis le body d'erreur. */
    private String extractErrorMessage(String raw) {
        try {
            JsonObject err = JsonParser.parseString(raw).getAsJsonObject().getAsJsonObject("error");
            if (err != null && err.has("message")) return err.get("message").getAsString();
        } catch (Exception ignore) {}
        return raw;
    }
}
