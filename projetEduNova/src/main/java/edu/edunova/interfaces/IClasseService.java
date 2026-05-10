package edu.edunova.interfaces;

import edu.edunova.entities.Classe;

/**
 * Specialisation of {@link IService} for the {@link Classe} aggregate. Adds
 * read-by-id and referential-integrity helpers that are specific to school
 * classes (e.g. preventing deletion of a class that still contains students).
 */
public interface IClasseService extends IService<Classe> {

    /**
     * Returns a human-readable summary of the rows that still reference the
     * given class. Used by the deletion flow to enforce referential integrity
     * before issuing a {@code DELETE} statement.
     *
     * @param classeId  identifier of the class to inspect
     * @return multi-line description of dependent rows; empty string if none
     */
    String getReferencesDetails(int classeId);

    /**
     * Looks up a single class by its primary key.
     *
     * @param id  primary key
     * @return the matching class, or {@code null} when not found
     */
    Classe getById(int id);
}
