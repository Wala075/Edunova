package edu.edunova.ai;

import edu.edunova.entities.Bulletin;
import edu.edunova.entities.Bulletin.BulletinLigne;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Construit les prompts envoyés à Gemini.
 * Garde la logique de prompt centralisée pour pouvoir l'ajuster facilement.
 */
public class PromptBuilder {

    private PromptBuilder() {}

    /**
     * Prompt PRO pour appréciation de bulletin scolaire.
     *
     * Construit une instruction détaillée qui force Gemini à produire
     * une appréciation digne d'un professeur principal d'établissement
     * réputé : analyse pédagogique, vocabulaire précis, ton soutenu,
     * recommandation concrète.
     */
    public static String buildAppreciation(Bulletin b) {
        StringBuilder sb = new StringBuilder(1500);

        // ============ RÔLE ============
        sb.append("Tu es Madame Laurent, professeure principale depuis 20 ans dans un établissement ")
          .append("réputé. Tu rédiges les appréciations des bulletins trimestriels avec ")
          .append("professionnalisme, finesse pédagogique et bienveillance. Tes appréciations ")
          .append("sont reconnues pour leur justesse, leur structure et leur ton humain.\n\n");

        // ============ PROFIL ÉLÈVE (analyse pré-mâchée pour Gemini) ============
        Profil profil = analyser(b);
        sb.append("PROFIL DÉTECTÉ : ").append(profil.label).append("\n");
        sb.append(profil.guidance).append("\n\n");

        // ============ DONNÉES BRUTES ============
        sb.append("INFORMATIONS DE L'ÉLÈVE :\n");
        if (b.getStudent() != null) {
            String prenom = nullSafe(b.getStudent().getPrenom_s());
            String nom = nullSafe(b.getStudent().getNom_s());
            sb.append("- Nom : ").append(nom).append("\n");
            sb.append("- Prénom : ").append(prenom).append("\n");
        }
        sb.append("- Trimestre : ").append(b.getTrimestre()).append("\n");
        sb.append("- Année scolaire : ").append(nullSafe(b.getAnnee())).append("\n");
        sb.append("- Moyenne générale : ").append(String.format("%.2f", b.getMoyenneGenerale())).append("/20\n");
        sb.append("- Mention obtenue : ").append(nullSafe(b.getMention())).append("\n");
        sb.append("- Décision conseil de classe : ").append(nullSafe(b.getDecision())).append("\n\n");

        // ============ ANALYSE PAR MATIÈRE ============
        sb.append("RÉSULTATS DÉTAILLÉS PAR MATIÈRE :\n");
        if (b.getLignes() != null && !b.getLignes().isEmpty()) {
            // Trier de la plus haute à la plus basse pour faciliter l'analyse
            List<BulletinLigne> tri = new ArrayList<>(b.getLignes());
            tri.sort(Comparator.comparingDouble(BulletinLigne::getMoyenne).reversed());
            for (BulletinLigne l : tri) {
                sb.append("• ").append(l.getMatiere())
                  .append(" : ").append(String.format("%.2f", l.getMoyenne())).append("/20")
                  .append("  (").append(l.getNbNotes()).append(" évaluation(s))")
                  .append("  → ").append(qualifier(l.getMoyenne()))
                  .append("\n");
            }
            // Synthèse points forts / faibles
            sb.append("\nMatière(s) forte(s) : ").append(profil.matieresFortes).append("\n");
            sb.append("Matière(s) à consolider : ").append(profil.matieresFragiles).append("\n");
        } else {
            sb.append("(aucune note saisie pour ce trimestre)\n");
        }

        // ============ STRUCTURE ATTENDUE ============
        sb.append("\nCONSIGNES DE RÉDACTION (à respecter strictement) :\n");
        sb.append("1. LONGUEUR : 4 à 6 phrases (80 à 130 mots), ni plus, ni moins.\n");
        sb.append("2. STRUCTURE :\n");
        sb.append("   • Phrase 1 : synthèse globale du trimestre (niveau, attitude implicite).\n");
        sb.append("   • Phrase 2-3 : éléments positifs concrets (matières/compétences précises).\n");
        sb.append("   • Phrase 3-4 : axes de progrès identifiés et conseil pédagogique.\n");
        sb.append("   • Phrase finale : encouragement personnalisé et message au-delà des notes.\n");
        sb.append("3. TON : soutenu mais chaleureux ; jamais infantilisant, jamais condescendant.\n");
        sb.append("4. VOCABULAIRE PÉDAGOGIQUE : utiliser un lexique professionnel (rigueur, ");
        sb.append("investissement, curiosité, méthode, autonomie, persévérance, consolidation, ");
        sb.append("approfondissement, etc.) sans tomber dans les clichés.\n");
        sb.append("5. PERSONNALISATION : utiliser le prénom une seule fois (au début ou en finale).\n");
        sb.append("6. INTERDITS :\n");
        sb.append("   • PAS de markdown, ni listes à puces, ni titres.\n");
        sb.append("   • PAS de phrases creuses (\"continue comme ça\", \"bon trimestre\").\n");
        sb.append("   • PAS de chiffres bruts (la moyenne est déjà sur le bulletin).\n");
        sb.append("   • PAS de formules figées comme \"Ensemble satisfaisant\" ou \"Doit travailler\".\n");
        sb.append("   • PAS de guillemets autour de la réponse.\n");
        sb.append("7. SIGNATURE : NE PAS signer, NE PAS dater.\n\n");

        sb.append("Réponds UNIQUEMENT avec le texte brut de l'appréciation, en un seul paragraphe fluide.");
        return sb.toString();
    }

    // ============================================================
    // Analyse interne du bulletin pour aider Gemini
    // ============================================================

    private static class Profil {
        final String label;
        final String guidance;
        final String matieresFortes;
        final String matieresFragiles;

        Profil(String label, String guidance, String mf, String mfr) {
            this.label = label;
            this.guidance = guidance;
            this.matieresFortes = mf;
            this.matieresFragiles = mfr;
        }
    }

    /** Détermine le profil pédagogique pour orienter le ton de l'appréciation. */
    private static Profil analyser(Bulletin b) {
        double moy = b.getMoyenneGenerale();

        // Trouver matières fortes (>= moy + 1.5) et fragiles (<= moy - 1.5)
        List<String> fortes = new ArrayList<>();
        List<String> fragiles = new ArrayList<>();
        if (b.getLignes() != null) {
            for (BulletinLigne l : b.getLignes()) {
                if (l.getMoyenne() >= moy + 1.5 || l.getMoyenne() >= 14) fortes.add(l.getMatiere());
                else if (l.getMoyenne() <= moy - 1.5 || l.getMoyenne() < 8) fragiles.add(l.getMatiere());
            }
        }
        String fortesStr = fortes.isEmpty() ? "aucune particulièrement saillante" : String.join(", ", fortes);
        String fragilesStr = fragiles.isEmpty() ? "aucune particulièrement faible" : String.join(", ", fragiles);

        if (moy >= 16) {
            return new Profil(
                    "ÉLÈVE EXCELLENT",
                    "L'élève fait preuve d'une rigueur exceptionnelle. " +
                    "Souligner la maîtrise, l'investissement et l'autonomie. " +
                    "Encourager l'approfondissement et la curiosité au-delà du programme. " +
                    "Éviter la flatterie : rester mesuré et nuancé.",
                    fortesStr, fragilesStr);
        }
        if (moy >= 14) {
            return new Profil(
                    "TRÈS BON ÉLÈVE",
                    "Excellents résultats globaux. Mentionner la solidité des acquis, " +
                    "valoriser la régularité et identifier 1 ou 2 axes d'approfondissement " +
                    "(ouverture culturelle, prise de parole, méthodologie de la dissertation, etc.).",
                    fortesStr, fragilesStr);
        }
        if (moy >= 12) {
            return new Profil(
                    "BON ÉLÈVE EN PROGRESSION",
                    "Niveau satisfaisant, marge de progrès claire. Relever les efforts visibles " +
                    "et inviter à plus de prise d'initiative ou de méthode. " +
                    "Conseil concret bienvenu (révisions actives, fiches de synthèse, etc.).",
                    fortesStr, fragilesStr);
        }
        if (moy >= 10) {
            return new Profil(
                    "ÉLÈVE MOYEN AVEC POTENTIEL",
                    "Trimestre correct mais hétérogène. Insister sur la régularité et la rigueur " +
                    "à acquérir. Identifier précisément les matières où concentrer l'effort. " +
                    "Le conseil doit être actionnable et la conclusion encourageante.",
                    fortesStr, fragilesStr);
        }
        if (moy >= 8) {
            return new Profil(
                    "ÉLÈVE EN DIFFICULTÉ",
                    "Trimestre fragile. Rester bienveillant : valoriser les points forts existants " +
                    "puis évoquer franchement les difficultés. Recommander un soutien (tutorat, " +
                    "rendez-vous avec le professeur principal). Conclusion humaine et porteuse d'espoir.",
                    fortesStr, fragilesStr);
        }
        return new Profil(
                "ÉLÈVE EN GRANDE DIFFICULTÉ",
                "Situation préoccupante. Aborder le sujet avec gravité mais sans démoraliser. " +
                "Souligner toute lueur positive (présence, effort isolé). " +
                "Recommander une concertation famille/équipe pédagogique. " +
                "Finir sur une note constructive : remobiliser, reconstruire.",
                fortesStr, fragilesStr);
    }

    private static String qualifier(double m) {
        if (m >= 16) return "excellent";
        if (m >= 14) return "très bon niveau";
        if (m >= 12) return "bon niveau";
        if (m >= 10) return "niveau correct";
        if (m >= 8)  return "fragile";
        return "préoccupant";
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
