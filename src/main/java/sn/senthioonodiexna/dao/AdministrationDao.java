package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.Administration;

import java.util.List;
import java.util.Optional;

public interface AdministrationDao {
    Administration create(Administration administration);
    Optional<Administration> findById(int id);
    List<Administration> findAll();
    boolean update(Administration administration);
    boolean delete(int id);
}
