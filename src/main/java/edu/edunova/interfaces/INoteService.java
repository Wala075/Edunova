package edu.edunova.interfaces;




import edu.edunova.entities.Note;
import java.util.List;

public interface INoteService extends IService<Note> {
    // Méthodes spécifiques aux notes
    List<Note> getNotesByStudent(int studentId);
    double calculerMoyenne(int studentId);

    double calculerMoyenneClasse(int matiereId, String annee);
}