package services;

/**
 * PostValidator — Couche métier : règles de validation des posts.
 * Séparée du service de données (ForumService) pour respecter la
 * séparation des responsabilités (SRP).
 */
public class PostValidator {

    private static final int TITRE_MIN   = 5;
    private static final int TITRE_MAX   = 200;
    private static final int CONTENU_MIN = 10;
    private static final int CONTENU_MAX = 5000;

    private static final String[] MOTS_INTERDITS = {
        "insulte", "violence", "spam", "publicité"
    };

    public static class ResultatValidation {
        public final boolean valide;
        public final String  message;
        ResultatValidation(boolean valide, String message) {
            this.valide = valide; this.message = message;
        }
    }

    /** Valide titre + contenu avant soumission. */
    public ResultatValidation valider(String titre, String contenu) {
        if (titre == null || titre.isBlank())
            return new ResultatValidation(false, "Le titre est obligatoire.");
        if (titre.length() < TITRE_MIN)
            return new ResultatValidation(false, "Titre trop court (min " + TITRE_MIN + " caractères).");
        if (titre.length() > TITRE_MAX)
            return new ResultatValidation(false, "Titre trop long (max " + TITRE_MAX + " caractères).");
        if (contenu == null || contenu.isBlank())
            return new ResultatValidation(false, "Le contenu est obligatoire.");
        if (contenu.length() < CONTENU_MIN)
            return new ResultatValidation(false, "Contenu trop court (min " + CONTENU_MIN + " caractères).");
        if (contenu.length() > CONTENU_MAX)
            return new ResultatValidation(false, "Contenu trop long (max " + CONTENU_MAX + " caractères).");

        String combined = (titre + " " + contenu).toLowerCase();
        for (String mot : MOTS_INTERDITS) {
            if (combined.contains(mot))
                return new ResultatValidation(false, "Contenu inapproprié détecté : \"" + mot + "\".");
        }
        return new ResultatValidation(true, "Post valide.");
    }
}
