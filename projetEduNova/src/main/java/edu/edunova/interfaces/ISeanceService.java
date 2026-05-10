package edu.edunova.interfaces;

import edu.edunova.entities.Seance;

import java.util.List;

/**
 * Specialisation of {@link IService} for the {@link Seance} aggregate. Adds
 * conflict detection and convenience lookups by class or teacher used to
 * project the timetable from different angles.
 */
public interface ISeanceService extends IService<Seance> {

    /**
     * Detects scheduling conflicts that would arise if {@code seance} were
     * added or updated. A conflict is reported when the candidate session
     * overlaps an existing one on the same day for the same teacher,
     * the same class or the same room.
     *
     * @param seance  candidate session
     * @return multi-line description of every detected conflict;
     *         empty string when the session is valid
     */
    String detecterConflits(Seance seance);

    /**
     * @param classeId  primary key of the class
     * @return all sessions taught to the given class, ordered by day and time
     */
    List<Seance> getByClasse(int classeId);

    /**
     * @param teacherId  primary key of the teacher
     * @return all sessions taught by the given teacher, ordered by day and time
     */
    List<Seance> getByTeacher(int teacherId);

    /**
     * @param id  primary key
     * @return the matching session, or {@code null} when not found
     */
    Seance getById(int id);
}
