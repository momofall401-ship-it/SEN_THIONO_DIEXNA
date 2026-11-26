package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.DemarchePiece;
import sn.senthioonodiexna.model.Etape;
import sn.senthioonodiexna.model.Piece;

import java.util.List;
import java.util.Optional;

public interface DemarcheDao {
    Demarche create(Demarche demarche);
    Optional<Demarche> findById(int id);
    List<Demarche> findAll();
    List<Demarche> findPublished();
    List<Demarche> findByService(int idService);
    List<Demarche> searchPublished(String keyword, Integer idService);
    boolean update(Demarche demarche);
    boolean delete(int id);

    // Associated Pieces and Steps
    List<Etape> findEtapesByDemarche(int idDemarche);
    void addEtape(Etape etape);
    void deleteEtapesByDemarche(int idDemarche);

    List<DemarchePiece> findPiecesByDemarche(int idDemarche);
    void addDemarchePiece(DemarchePiece demarchePiece);
    void deleteDemarchePieces(int idDemarche);

    List<Piece> findAllPieces();
    Piece createPiece(Piece piece);
}
