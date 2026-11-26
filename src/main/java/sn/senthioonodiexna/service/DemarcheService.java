package sn.senthioonodiexna.service;

import sn.senthioonodiexna.dao.DemarcheDao;
import sn.senthioonodiexna.dao.impl.DemarcheDaoImpl;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.DemarchePiece;
import sn.senthioonodiexna.model.Etape;
import sn.senthioonodiexna.model.Piece;
import sn.senthioonodiexna.model.StatutPublication;

import java.util.List;
import java.util.Optional;

public class DemarcheService {

    private final DemarcheDao demarcheDao;

    public DemarcheService() {
        this(new DemarcheDaoImpl());
    }

    public DemarcheService(DemarcheDao demarcheDao) {
        this.demarcheDao = demarcheDao;
    }

    // RG05: Seules les démarches publiées et actives sont visibles par les citoyens
    public List<Demarche> getPublishedDemarches() {
        return demarcheDao.findPublished();
    }

    public List<Demarche> searchDemarches(String keyword, Integer idService) {
        return demarcheDao.searchPublished(keyword, idService);
    }

    public Optional<Demarche> getDemarcheById(int id) {
        return demarcheDao.findById(id);
    }

    public List<Demarche> getAllDemarchesForAdmin() {
        return demarcheDao.findAll();
    }

    public List<Demarche> getDemarchesByService(int idService) {
        return demarcheDao.findByService(idService);
    }

    public Demarche createDemarche(Demarche d, List<Etape> etapes, List<DemarchePiece> pieces) {
        if (d.getTitre() == null || d.getTitre().isBlank()) {
            throw new IllegalArgumentException("Le titre de la démarche est obligatoire.");
        }
        if (d.getIdService() <= 0) {
            throw new IllegalArgumentException("Le service associé est obligatoire.");
        }

        Demarche created = demarcheDao.create(d);

        if (etapes != null) {
            for (Etape e : etapes) {
                e.setIdDemarche(created.getId());
                demarcheDao.addEtape(e);
            }
        }

        if (pieces != null) {
            for (DemarchePiece p : pieces) {
                p.setIdDemarche(created.getId());
                demarcheDao.addDemarchePiece(p);
            }
        }

        return getDemarcheById(created.getId()).orElse(created);
    }

    public boolean updateDemarche(Demarche d, List<Etape> etapes, List<DemarchePiece> pieces) {
        boolean updated = demarcheDao.update(d);
        if (updated) {
            if (etapes != null) {
                demarcheDao.deleteEtapesByDemarche(d.getId());
                for (Etape e : etapes) {
                    e.setIdDemarche(d.getId());
                    demarcheDao.addEtape(e);
                }
            }
            if (pieces != null) {
                demarcheDao.deleteDemarchePieces(d.getId());
                for (DemarchePiece p : pieces) {
                    p.setIdDemarche(d.getId());
                    demarcheDao.addDemarchePiece(p);
                }
            }
        }
        return updated;
    }

    public boolean setPublicationStatus(int idDemarche, StatutPublication status) {
        Optional<Demarche> opt = demarcheDao.findById(idDemarche);
        if (opt.isEmpty()) return false;
        Demarche d = opt.get();
        d.setStatutPublication(status);
        return demarcheDao.update(d);
    }

    public boolean deleteDemarche(int id) {
        return demarcheDao.delete(id);
    }

    public List<Piece> getAllPieces() {
        return demarcheDao.findAllPieces();
    }

    public Piece createPiece(String nom, String description, String formats) {
        Piece p = new Piece(nom, description, formats);
        return demarcheDao.createPiece(p);
    }
}
