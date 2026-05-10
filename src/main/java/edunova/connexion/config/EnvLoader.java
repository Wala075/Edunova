package edunova.connexion.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Charge les variables d'environnement depuis le fichier .env
 * Doit être appelé au démarrage de l'application
 */
public class EnvLoader {
    
    private static boolean loaded = false;
    
    /**
     * Charge les variables d'environnement depuis .env
     * Appeler cette méthode au démarrage de l'application
     */
    public static void load() {
        if (loaded) {
            return; // Déjà chargé
        }
        
        try {
            // Charger le fichier .env
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            // Charger les variables dans System.getenv()
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            
            loaded = true;
            System.out.println("✅ Variables d'environnement chargées depuis .env");
            
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors du chargement de .env: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
