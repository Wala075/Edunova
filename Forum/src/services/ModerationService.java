package services;

import interfaces.IModerationService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * ModerationService — vector.profanity.dev (gratuit, sans cle).
 * Detecte gros mots et contenu inapproprie automatiquement.
 *
 * Result types (ResultatModeration, AnalyseAdmin) are defined in
 * IModerationService to avoid a circular package dependency.
 */
public class ModerationService implements IModerationService {

    // Convenience aliases so existing call-sites (ForumAdminController,
    // ForumMembreController) that use ModerationService.ResultatModeration
    // and ModerationService.AnalyseAdmin continue to compile unchanged.
    public static class ResultatModeration extends IModerationService.ResultatModeration {
        public ResultatModeration(boolean ok, String cat, String msg, int score) {
            super(ok, cat, msg, score);
        }
    }

    public static class AnalyseAdmin extends IModerationService.AnalyseAdmin {
        public AnalyseAdmin(String d, String det, int t, int g, int i, int m, int disc) {
            super(d, det, t, g, i, m, disc);
        }
    }

    private static final String API_URL = "https://vector.profanity.dev";

    @Override
    public ResultatModeration moderer(String titre, String contenu) throws Exception {
        String texte = (titre + " " + contenu).trim();
        if (texte.length() > 1000) texte = texte.substring(0, 1000);

        String body = "{\"message\":\"" + escape(texte) + "\"}";

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(12)).build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200)
            throw new Exception("Profanity API erreur " + res.statusCode());

        return parseReponse(res.body());
    }

    @Override
    public AnalyseAdmin analyserPourAdmin(String titre, String contenu) throws Exception {
        ResultatModeration mod = moderer(titre, contenu);
        String detail = mod.getIcone() + " Score: " + mod.score + "% — " + mod.explication;
        return new AnalyseAdmin(
            mod.isOk() ? "ACCEPTE" : "REFUSE", detail,
            mod.score, mod.score, mod.score / 2, mod.score / 3, mod.score / 4
        );
    }

    // {"isProfanity":true,"score":0.99,"flaggedFor":"..."}
    private ResultatModeration parseReponse(String json) {
        try {
            boolean flag  = json.contains("\"isProfanity\":true");
            int     score = extraireScore(json);
            String  raison = extraireString(json, "flaggedFor");

            if (flag) {
                String msg = "Contenu inapproprié détecté";
                if (raison != null && !raison.isEmpty()) msg += " (" + raison + ")";
                msg += ". Score : " + score + "%.";
                return new ResultatModeration(false, "INAPPROPRIE", msg, score);
            }
            return new ResultatModeration(true, "OK",
                "Contenu approprié. Score : " + score + "%.", score);
        } catch (Exception e) {
            return new ResultatModeration(true, "OK", "Analyse indisponible.", 0);
        }
    }

    private int extraireScore(String json) {
        try {
            String marker = "\"score\":";
            int idx = json.indexOf(marker);
            if (idx == -1) return 0;
            int s = idx + marker.length();
            int e = json.indexOf(",", s);
            if (e == -1) e = json.indexOf("}", s);
            return (int) (Double.parseDouble(json.substring(s, e).trim()) * 100);
        } catch (Exception e) { return 0; }
    }

    private String extraireString(String json, String key) {
        try {
            String marker = "\"" + key + "\":\"";
            int idx = json.indexOf(marker);
            if (idx == -1) return "";
            int s = idx + marker.length();
            int e = json.indexOf("\"", s);
            return json.substring(s, e);
        } catch (Exception e) { return ""; }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", "");
    }
}
