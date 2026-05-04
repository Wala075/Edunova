package edu.edunova.tests;



import edu.edunova.entities.Note;
import edu.edunova.services.NoteService;
import java.sql.Date;
import java.util.List;

public class MainClass {
    public static void main(String[] args) {
        NoteService ns = new NoteService();

        // 1. Création d'un objet Note pour le test
        // Attention : student_id 1 et matiere_id 1 doivent exister dans ta base !
        Date dateActuelle = new Date(System.currentTimeMillis());
        Note nouvelleNote = new Note(16.5, 2, "Controle", 1, dateActuelle, 1, 1, "2025-2026");

        // 2. Test de l'ajout
        System.out.println("--- Test Ajout ---");
        ns.addEntity(nouvelleNote);

        // 3. Test de la récupération
        System.out.println("--- Liste des notes en base ---");
        List<Note> liste = ns.getData();
        for (Note n : liste) {
            System.out.println(n);
        }
    }
}