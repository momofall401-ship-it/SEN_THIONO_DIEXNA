package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.Historique;

import java.util.List;

public interface HistoriqueDao {
    Historique create(Historique historique);
    List<Historique> findByDossier(int idDossier);
}
