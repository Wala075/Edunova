package edunova.connexion.tools;

import edunova.connexion.models.RiskData;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'analyse de risque avec IA
 * Analyse 6 facteurs de risque pour calculer un score 0-100
 */
public class RiskAnalyzerIA {

    // Facteurs de risque
    private static final int FACTOR_IP_LOCATION = 25;
    private static final int FACTOR_NEW_DEVICE = 20;
    private static final int FACTOR_UNUSUAL_TIME = 15;
    private static final int FACTOR_FAILED_ATTEMPTS = 20;
    private static final int FACTOR_COUNTRY_CHANGE = 15;
    private static final int FACTOR_TYPING_SPEED = 5;

    // Seuils de risque
    private static final int RISK_LOW = 30;
    private static final int RISK_MEDIUM = 60;
    private static final int RISK_HIGH = 85;

    /**
     * Calcule le score de risque basé sur 6 facteurs
     */
    public static RiskData analyzeRisk(int userId, String ipAddress, String country,
                                       String device, int failedAttempts, double typingSpeed,
                                       Map<String, Object> userHistory) {
        
        System.out.println("🔍 Analyse de risque pour l'utilisateur: " + userId);
        
        RiskData riskData = new RiskData(userId, ipAddress, country, device,
                LocalDateTime.now(), failedAttempts, typingSpeed);

        // Calculer les scores partiels pour chaque facteur
        int ipLocationScore = analyzeIPLocation(ipAddress, userHistory);
        int newDeviceScore = analyzeNewDevice(device, userHistory);
        int unusualTimeScore = analyzeUnusualTime(LocalDateTime.now(), userHistory);
        int failedAttemptsScore = analyzeFailedAttempts(failedAttempts);
        int countryChangeScore = analyzeCountryChange(country, userHistory);
        int typingSpeedScore = analyzeTypingSpeed(typingSpeed);

        // Calculer le score total
        int totalScore = (ipLocationScore + newDeviceScore + unusualTimeScore +
                         failedAttemptsScore + countryChangeScore + typingSpeedScore) / 6;

        // Déterminer le niveau de risque
        String riskLevel = determineRiskLevel(totalScore);
        boolean blocked = totalScore >= RISK_HIGH;

        riskData.setRiskScore(totalScore);
        riskData.setRiskLevel(riskLevel);
        riskData.setBlocked(blocked);

        // Afficher les détails
        System.out.println("📊 Facteurs de Risque:");
        System.out.println("  📍 IP Location: " + ipLocationScore + "/100");
        System.out.println("  🖥️  New Device: " + newDeviceScore + "/100");
        System.out.println("  ⏱️  Unusual Time: " + unusualTimeScore + "/100");
        System.out.println("  🔁 Failed Attempts: " + failedAttemptsScore + "/100");
        System.out.println("  🌍 Country Change: " + countryChangeScore + "/100");
        System.out.println("  ⚡ Typing Speed: " + typingSpeedScore + "/100");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📈 Score Total: " + totalScore + "/100");
        System.out.println("🎯 Niveau: " + riskLevel);
        System.out.println("🚫 Bloqué: " + (blocked ? "OUI" : "NON"));

        return riskData;
    }

    /**
     * Analyse la localisation de l'IP
     * Vérifie si l'IP est inhabituelle par rapport à l'historique
     */
    private static int analyzeIPLocation(String currentIP, Map<String, Object> userHistory) {
        if (userHistory == null || userHistory.isEmpty()) {
            return 10; // Première connexion, risque faible
        }

        @SuppressWarnings("unchecked")
        java.util.List<String> previousIPs = (java.util.List<String>) userHistory.get("previousIPs");
        
        if (previousIPs != null && previousIPs.contains(currentIP)) {
            return 5; // IP connue, très faible risque
        }

        return 40; // IP nouvelle, risque élevé
    }

    /**
     * Analyse si c'est un nouvel appareil
     */
    private static int analyzeNewDevice(String currentDevice, Map<String, Object> userHistory) {
        if (userHistory == null || userHistory.isEmpty()) {
            return 15; // Première connexion
        }

        @SuppressWarnings("unchecked")
        java.util.List<String> previousDevices = (java.util.List<String>) userHistory.get("previousDevices");
        
        if (previousDevices != null && previousDevices.contains(currentDevice)) {
            return 5; // Appareil connu
        }

        return 50; // Nouvel appareil, risque élevé
    }

    /**
     * Analyse si l'heure de connexion est inhabituelle
     */
    private static int analyzeUnusualTime(LocalDateTime loginTime, Map<String, Object> userHistory) {
        LocalTime currentTime = loginTime.toLocalTime();
        
        // Heures suspectes: 0h-6h
        if (currentTime.getHour() >= 0 && currentTime.getHour() < 6) {
            return 60; // Très suspect
        }
        
        // Heures normales: 8h-22h
        if (currentTime.getHour() >= 8 && currentTime.getHour() < 22) {
            return 10; // Faible risque
        }
        
        // Heures intermédiaires
        return 30; // Risque moyen
    }

    /**
     * Analyse le nombre de tentatives échouées
     */
    private static int analyzeFailedAttempts(int failedAttempts) {
        if (failedAttempts == 0) {
            return 0; // Aucune tentative échouée
        }
        
        if (failedAttempts == 1) {
            return 20; // Une tentative échouée
        }
        
        if (failedAttempts <= 3) {
            return 50; // 2-3 tentatives échouées
        }
        
        return 90; // Plus de 3 tentatives échouées, très suspect
    }

    /**
     * Analyse les changements de pays
     */
    private static int analyzeCountryChange(String currentCountry, Map<String, Object> userHistory) {
        if (userHistory == null || userHistory.isEmpty()) {
            return 10; // Première connexion
        }

        String previousCountry = (String) userHistory.get("lastCountry");
        
        if (previousCountry == null || previousCountry.equals(currentCountry)) {
            return 5; // Même pays
        }

        // Changement de pays détecté
        return 70; // Très suspect
    }

    /**
     * Analyse la vitesse de saisie (détection de bot)
     * Vitesse normale: 40-80 caractères/seconde
     * Bot: > 100 caractères/seconde
     */
    private static int analyzeTypingSpeed(double typingSpeed) {
        if (typingSpeed < 0) {
            return 0; // Pas de données
        }
        
        if (typingSpeed > 100) {
            return 80; // Probablement un bot
        }
        
        if (typingSpeed > 80) {
            return 40; // Vitesse suspecte
        }
        
        if (typingSpeed < 20) {
            return 30; // Très lent, peut être un humain
        }
        
        return 10; // Vitesse normale
    }

    /**
     * Détermine le niveau de risque basé sur le score
     */
    private static String determineRiskLevel(int score) {
        if (score < RISK_LOW) {
            return "✅ FAIBLE";
        } else if (score < RISK_MEDIUM) {
            return "⚠️  MOYEN";
        } else if (score < RISK_HIGH) {
            return "🔴 ÉLEVÉ";
        } else {
            return "🚫 CRITIQUE";
        }
    }

    /**
     * Obtient la couleur pour l'affichage du score
     */
    public static String getScoreColor(int score) {
        if (score < RISK_LOW) {
            return "#16a34a"; // Vert
        } else if (score < RISK_MEDIUM) {
            return "#f59e0b"; // Orange
        } else if (score < RISK_HIGH) {
            return "#ef4444"; // Rouge
        } else {
            return "#7f1d1d"; // Rouge foncé
        }
    }

    /**
     * Obtient l'emoji pour le score
     */
    public static String getScoreEmoji(int score) {
        if (score < RISK_LOW) {
            return "✅";
        } else if (score < RISK_MEDIUM) {
            return "⚠️";
        } else if (score < RISK_HIGH) {
            return "🔴";
        } else {
            return "🚫";
        }
    }

    /**
     * Crée un historique utilisateur pour les tests
     */
    public static Map<String, Object> createUserHistory(String lastIP, String lastDevice,
                                                         String lastCountry) {
        Map<String, Object> history = new HashMap<>();
        
        java.util.List<String> previousIPs = new java.util.ArrayList<>();
        previousIPs.add(lastIP);
        history.put("previousIPs", previousIPs);
        
        java.util.List<String> previousDevices = new java.util.ArrayList<>();
        previousDevices.add(lastDevice);
        history.put("previousDevices", previousDevices);
        
        history.put("lastCountry", lastCountry);
        
        return history;
    }
}
