package edu.edunova.utils;

import java.time.LocalDate;
import java.util.Date;

/**
 * Calcul automatique de l'année scolaire.
 *
 * Convention Tunisie/France :
 *   - Une année scolaire commence en SEPTEMBRE et se termine en JUIN/JUILLET.
 *   - De septembre à décembre :  YYYY-YYYY+1
 *   - De janvier à août       :  YYYY-1-YYYY
 *
 * Exemples :
 *   2024-10-15  →  "2024-2025"
 *   2025-03-20  →  "2024-2025"
 *   2025-09-01  →  "2025-2026"
 *
 * Utilisation :
 *   String a = AnneeScolaire.current();             // basé sur aujourd'hui
 *   String a = AnneeScolaire.fromDate(localDate);   // basé sur une date donnée
 */
public final class AnneeScolaire {

    /** Mois de bascule : septembre (= 9). Avant ce mois, on est encore dans l'année précédente. */
    public static final int MOIS_RENTREE = 9;

    private AnneeScolaire() {}

    /** Année scolaire courante (basée sur la date système d'aujourd'hui). */
    public static String current() {
        return fromDate(LocalDate.now());
    }

    /** Année scolaire pour une date donnée. */
    public static String fromDate(LocalDate date) {
        if (date == null) return current();
        int year = date.getYear();
        int month = date.getMonthValue();
        if (month >= MOIS_RENTREE) {
            return year + "-" + (year + 1);
        }
        return (year - 1) + "-" + year;
    }

    /** Variante pour java.util.Date (utilisé dans Note.date_saisie). */
    public static String fromDate(Date date) {
        if (date == null) return current();
        LocalDate ld = date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        return fromDate(ld);
    }

    /** Vérifie si une chaîne ressemble bien à "YYYY-YYYY". */
    public static boolean estValide(String annee) {
        if (annee == null) return false;
        return annee.matches("\\d{4}-\\d{4}");
    }
}
