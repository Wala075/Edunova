package interfaces;

/**
 * Interface for the moderation service.
 * Inner result types are defined here to break the circular dependency
 * that would arise from importing them from services.ModerationService.
 */
public interface IModerationService {

    /** Result of a pre-submission moderation check. */
    class ResultatModeration {
        public final boolean acceptable;
        public final String  categorie;
        public final String  explication;
        public final int     score;

        public ResultatModeration(boolean ok, String cat, String msg, int score) {
            this.acceptable  = ok;
            this.categorie   = cat;
            this.explication = msg;
            this.score       = score;
        }
        public boolean isOk()     { return acceptable; }
        public String  getIcone() { return acceptable ? "✅" : "🚫"; }
    }

    /** Result of an admin-level analysis. */
    class AnalyseAdmin {
        public final String decision, detail;
        public final int scoreToxicite, scoreGrossMots, scoreInsulte, scoreMenace, scoreDiscrimination;

        public AnalyseAdmin(String d, String det, int t, int g, int i, int m, int disc) {
            decision = d; detail = det;
            scoreToxicite = t; scoreGrossMots = g;
            scoreInsulte = i; scoreMenace = m; scoreDiscrimination = disc;
        }
        public boolean isAccepte() { return "ACCEPTE".equals(decision); }
        public String  getResume() {
            return (isAccepte() ? "✅ ACCEPTE — " : "🚫 REFUSE — ") + detail;
        }
    }

    ResultatModeration moderer(String titre, String contenu)           throws Exception;
    AnalyseAdmin       analyserPourAdmin(String titre, String contenu) throws Exception;
}
