package edu.edunova.interfaces;

import edu.edunova.entities.Parent;

import java.util.List;

/**
 * Specialisation of {@link IService} for the {@link Parent} aggregate. Adds
 * a class-based lookup used by the bulk-mailing flow to resolve recipients
 * from a single class identifier.
 */
public interface IParentService extends IService<Parent> {

    /**
     * @param classeId  primary key of the class
     * @return every parent whose child belongs to the given class
     */
    List<Parent> getByClasseId(int classeId);
}
