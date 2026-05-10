package edu.edunova.tests;

import edu.edunova.entities.Classe;
import edu.edunova.entities.Matiere;
import edu.edunova.entities.Seance;
import edu.edunova.entities.Teacher;
import edu.edunova.services.ClasseService;
import edu.edunova.services.MatiereService;
import edu.edunova.services.SeanceService;
import edu.edunova.services.TeacherService;

import java.sql.Time;

public class MainClass {

    public static void main(String[] args) {

        // ===== Initialisation des services =====
        ClasseService cs = new ClasseService();
        MatiereService ms = new MatiereService();
        TeacherService ts = new TeacherService();
        SeanceService ss = new SeanceService();

        // ===== 1. Insertion données test si tables vides =====
        System.out.println("=== INITIALISATION DONNÉES TEST ===");
        ms.insertDataTestSiVide();
        ts.insertDataTestSiVide();

        // Vérifier qu'on a au moins 1 classe
        if (cs.getData().isEmpty()) {
            cs.addEntity(new Classe("6A", "6ème", 30));
            cs.addEntity(new Classe("5B", "5ème", 28));
            System.out.println("✅ 2 classes de test ajoutées.");
        }

        // ===== 2. Lister tout =====
        System.out.println("\n=== CLASSES ===");
        for (Classe c : cs.getData()) {
            System.out.println("  " + c);
        }

        System.out.println("\n=== MATIÈRES ===");
        for (Matiere m : ms.getData()) {
            System.out.println("  " + m);
        }

        System.out.println("\n=== PROFS ===");
        for (Teacher t : ts.getData()) {
            System.out.println("  " + t);
        }

        // ===== 3. Test ajout séance =====
        System.out.println("\n=== TEST AJOUT SÉANCE ===");
        if (!cs.getData().isEmpty() && !ms.getData().isEmpty() && !ts.getData().isEmpty()) {
            Classe c = cs.getData().get(0);
            Matiere m = ms.getData().get(0);
            Teacher t = ts.getData().get(0);

            Seance seance = new Seance(
                    "LUNDI",
                    Time.valueOf("08:00:00"),
                    Time.valueOf("09:00:00"),
                    "S101",
                    "PRESENTIEL",
                    "2024-2025",
                    c.getId(),
                    m.getId(),
                    t.getId()
            );
            ss.addEntity(seance);
        }

        // ===== 4. Test détection conflit =====
        System.out.println("\n=== TEST DÉTECTION CONFLIT ===");
        if (!cs.getData().isEmpty() && !ms.getData().isEmpty() && !ts.getData().isEmpty()) {
            Classe c = cs.getData().get(0);
            Matiere m = ms.getData().get(0);
            Teacher t = ts.getData().get(0);

            // Tentative: même prof, même créneau (DOIT BLOQUER!)
            Seance conflit = new Seance(
                    "LUNDI",
                    Time.valueOf("08:30:00"),
                    Time.valueOf("09:30:00"),
                    "S102",
                    "PRESENTIEL",
                    "2024-2025",
                    c.getId(),
                    m.getId(),
                    t.getId()
            );
            ss.addEntity(conflit);  // → Doit être bloqué
        }

        // ===== 5. Lister emploi du temps =====
        System.out.println("\n=== EMPLOI DU TEMPS COMPLET ===");
        for (Seance s : ss.getData()) {
            System.out.println("  " + s);
        }
    }
}
