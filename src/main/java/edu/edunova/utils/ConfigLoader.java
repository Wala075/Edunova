package edu.edunova.utils;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Charge la configuration depuis un fichier .env à la racine du projet.
 * Permet de cacher les API keys (Gemini, Brevo, Twilio) hors du code source.
 *
 * Utilisation :
 *   String key = ConfigLoader.get("GEMINI_API_KEY");
 */
public class ConfigLoader {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()       // pas d'erreur si .env absent
            .ignoreIfMalformed()
            .load();

    private ConfigLoader() {}

    /** Renvoie la valeur de la clé, ou null si non définie. */
    public static String get(String key) {
        // priorité : variable d'environnement système, puis .env
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;
        return dotenv.get(key);
    }

    /** Renvoie la valeur, ou la valeur par défaut si absente. */
    public static String get(String key, String defaultValue) {
        String v = get(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    /** Vérifie si une clé est configurée. */
    public static boolean has(String key) {
        String v = get(key);
        return v != null && !v.isBlank();
    }
}
