package edu.edunova.interfaces;

import java.util.List;


public interface IService<T> {

    /**
     * Persists a new entity.
     *
     * @param t  entity to insert; identifier is assigned by the datastore
     */
    void addEntity(T t);

    /**
     * Removes an existing entity from the datastore.
     *
     * @param t  entity whose identifier is used as the deletion key
     */
    void deleteEntity(T t);

    /**
     * Updates the row identified by {@code id} with the provided values.
     *
     * @param id  primary key of the row to update
     * @param t   payload carrying the new field values
     */
    void updateEntity(int id, T t);

    /**
     * @return all rows from the underlying table; never {@code null}
     */
    List<T> getData();

}
