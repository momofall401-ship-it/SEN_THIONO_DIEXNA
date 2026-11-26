package sn.senthioonodiexna.service;

import sn.senthioonodiexna.dao.DossierDao;
import sn.senthioonodiexna.dao.impl.DossierDaoImpl;
import sn.senthioonodiexna.model.StatutDossier;

import java.util.Map;

public class DashboardService {

    private final DossierDao dossierDao;

    public DashboardService() {
        this(new DossierDaoImpl());
    }

    public DashboardService(DossierDao dossierDao) {
        this.dossierDao = dossierDao;
    }

    public Map<StatutDossier, Integer> getStatusCounts(Integer idService) {
        return dossierDao.countByStatut(idService);
    }

    public int getTotalDossiers(Integer idService) {
        if (idService != null && idService > 0) {
            return dossierDao.countByService(idService);
        }
        return dossierDao.countTotal();
    }

    public int getEnCoursDossiers(Integer idService) {
        Map<StatutDossier, Integer> map = getStatusCounts(idService);
        return map.getOrDefault(StatutDossier.DEPOSE, 0) +
               map.getOrDefault(StatutDossier.EN_VERIFICATION, 0) +
               map.getOrDefault(StatutDossier.COMPLEMENT_DEMANDE, 0) +
               map.getOrDefault(StatutDossier.EN_TRAITEMENT, 0);
    }

    public int getAcceptesDossiers(Integer idService) {
        return getStatusCounts(idService).getOrDefault(StatutDossier.ACCEPTE, 0);
    }

    public int getRejetesDossiers(Integer idService) {
        return getStatusCounts(idService).getOrDefault(StatutDossier.REJETE, 0);
    }
}
