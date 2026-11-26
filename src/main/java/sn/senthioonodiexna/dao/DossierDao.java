package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.DocumentFourni;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.StatutDossier;
import sn.senthioonodiexna.model.StatutVerification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DossierDao {
    Dossier create(Dossier dossier);
    Optional<Dossier> findById(int id);
    Optional<Dossier> findByNumeroSuivi(String numeroSuivi);
    List<Dossier> findAll();
    List<Dossier> findByUtilisateur(int idUtilisateur);
    List<Dossier> findByService(int idService);
    List<Dossier> search(String query, Integer idService, StatutDossier statut);
    boolean updateStatut(int idDossier, StatutDossier nouveauStatut, String motifRejet, Integer idAgent);
    boolean update(Dossier dossier);
    boolean delete(int id);

    // Document Fourni
    DocumentFourni addDocumentFourni(DocumentFourni document);
    List<DocumentFourni> findDocumentsByDossier(int idDossier);
    boolean updateDocumentVerification(int idDoc, StatutVerification statut, String commentaire);

    // Statistiques
    int countTotal();
    int countByService(int idService);
    Map<StatutDossier, Integer> countByStatut(Integer idService);
}
