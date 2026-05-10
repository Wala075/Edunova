package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * TranslationService — Google Translate (gratuit, sans cle).
 */
public class TranslationService implements interfaces.ITranslationService {

    private static final String BASE =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&dt=t&tl=";

    public enum Langue {
        FRANCAIS("fr", "Francais"),
        ARABE("ar",    "Arabe"),
        ANGLAIS("en",  "Anglais");
        public final String code, label;
        Langue(String c, String l) { code=c; label=l; }
    }

    public static class ResultatTraduction {
        public final String texteTraduit;
        public ResultatTraduction(String t) { this.texteTraduit = t; }
    }

    public ResultatTraduction traduire(String texte, Langue cible) throws Exception {
        if (texte == null || texte.isBlank()) throw new Exception("Texte vide.");
        texte = texte.replace("\n", " ").trim();
        if (texte.length() > 800) texte = texte.substring(0, 800);

        String url = BASE + cible.code + "&q="
                   + URLEncoder.encode(texte, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .GET().timeout(Duration.ofSeconds(15)).build();

        HttpResponse<String> res = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build()
            .send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200)
            throw new Exception("Erreur HTTP " + res.statusCode());

        return new ResultatTraduction(extraireTraduction(res.body()));
    }

    /**
     * Format Google Translate :
     * [[["seg1_traduit","seg1_orig",...],["seg2_traduit","seg2_orig",...],...],null,"fr"]
     *
     * On isole la zone entre [[[ et ]],  pour ne pas capturer d'autres champs JSON.
     * Dans cette zone, chaque segment commence par [" — on prend le 1er token de chaque.
     */
    private String extraireTraduction(String json) throws Exception {
        // Localiser la zone des traductions
        int debut = json.indexOf("[[[");
        if (debut < 0) throw new Exception("Format inattendu.");
        debut += 2; // pointe sur le '[' du premier segment : [["traduit","orig",...]

        int fin = json.indexOf("]],");
        if (fin < 0) fin = json.length();

        String zone = json.substring(debut, fin); // ex: ["Bonjour","Hello",...],["monde","world",...]

        StringBuilder sb = new StringBuilder();
        int pos = 0;

        while (pos < zone.length()) {
            // Chaque segment commence par ["
            int segStart = zone.indexOf("[\"", pos);
            if (segStart < 0) break;

            int textStart = segStart + 2;
            // Lire le premier token (texte traduit) jusqu'au " fermant
            int textEnd = textStart;
            while (textEnd < zone.length()) {
                char c = zone.charAt(textEnd);
                if (c == '\\') { textEnd += 2; continue; } // caractere echappe
                if (c == '"')  { break; }
                textEnd++;
            }

            String segment = zone.substring(textStart, textEnd);
            sb.append(segment);

            // Sauter jusqu'a la fin de ce sous-tableau ']'
            int closeBracket = zone.indexOf(']', textEnd);
            if (closeBracket < 0) break;
            pos = closeBracket + 1;
        }

        if (sb.length() == 0) throw new Exception("Traduction vide.");
        return decodeUnicode(sb.toString());
    }

    private String decodeUnicode(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            // 92=backslash 117='u'
            if (s.charAt(i) == 92 && i + 5 < s.length() && s.charAt(i+1) == 117) {
                try {
                    sb.append((char) Integer.parseInt(s.substring(i+2, i+6), 16));
                    i += 6; continue;
                } catch (NumberFormatException ignored) {}
            }
            sb.append(s.charAt(i));
            i++;
        }
        return sb.toString();
    }
}
