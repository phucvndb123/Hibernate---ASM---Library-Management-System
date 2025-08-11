package com.libsys.repo;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID extends Serializable> {
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);

    void delete(T entity);
}
