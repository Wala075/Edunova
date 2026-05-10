package edu.edunova_a.tests;

import edu.edunova_a.entities.Seance;
import edu.edunova_a.services.LiveSessionService;
import edu.edunova_a.services.PresenceService;
import edu.edunova_a.services.SeanceService;

public class MainClass {

    public static void main(String[] args) {
        try {
            SeanceService ss = new SeanceService();
            System.out.println("--- Séances en base ---");
            ss.afficher().forEach(System.out::println);

            LiveSessionService ls = new LiveSessionService();
            System.out.println("--- Live sessions: " + ls.afficher().size() + " ---");

            PresenceService ps = new PresenceService();
            System.out.println("--- Présences: " + ps.afficher().size() + " ---");

            System.out.println("OK : services et connexion fonctionnent.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
