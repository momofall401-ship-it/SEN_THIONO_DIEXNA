package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.ServiceAdministratif;

import java.util.List;
import java.util.Optional;

public interface ServiceDao {
    ServiceAdministratif create(ServiceAdministratif service);
    Optional<ServiceAdministratif> findById(int id);
    List<ServiceAdministratif> findAll();
    List<ServiceAdministratif> findByAdministration(int idAdministration);
    boolean update(ServiceAdministratif service);
    boolean delete(int id);
}
