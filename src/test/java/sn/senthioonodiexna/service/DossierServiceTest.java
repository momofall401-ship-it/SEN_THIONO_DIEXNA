package sn.senthioonodiexna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.senthioonodiexna.dao.DemarcheDao;
import sn.senthioonodiexna.dao.DossierDao;
import sn.senthioonodiexna.dao.HistoriqueDao;
import sn.senthioonodiexna.model.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DossierServiceTest {

    private DossierService dossierService;
    private MockDossierDao mockDossierDao;
    private MockDemarcheDao mockDemarcheDao;

    @BeforeEach
    void setUp() {
        mockDossierDao = new MockDossierDao();
        mockDemarcheDao = new MockDemarcheDao();
        MockHistoriqueDao mockHistoriqueDao = new MockHistoriqueDao();
        NotificationService notifService = new NotificationService(new MockNotificationDao());

        dossierService = new DossierService(mockDossierDao, mockDemarcheDao, mockHistoriqueDao, notifService);

        // Démarche Test
        Demarche demarche = new Demarche(1, "Extrait de Casier Judiciaire", "Description", "Conditions", "24h", "500 FCFA", 1, StatutPublication.PUBLIEE);
        mockDemarcheDao.demarches.add(demarche);

        // Dossier Test
        Dossier dossier = new Dossier(1, "STD-2026-00001", LocalDateTime.now(), LocalDateTime.now(),
                StatutDossier.EN_VERIFICATION, null, null, 10, 1, null);
        dossier.setIdService(1); // Service Justice
        mockDossierDao.dossiers.add(dossier);
    }

    @Test
    @DisplayName("RG06: Génération d'un numéro de suivi au format sénégalais valide")
    void testGenerateNumeroSuiviFormat() {
        String num = dossierService.generateUniqueNumeroSuivi();
        assertNotNull(num);
        assertTrue(num.startsWith("STD-"));
        assertTrue(num.length() >= 12);
    }

    @Test
    @DisplayName("RG08: Rejet d'un dossier sans motif lance une exception")
    void testRejectWithoutMotifThrowsException() {
        Utilisateur agent = new Utilisateur(5, "Fall", "Moussa", "agent@justice.sn", null, "hash", Role.AGENT, StatutCompte.ACTIF, 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            dossierService.rejectDossier(1, agent, "   ");
        });
        assertTrue(ex.getMessage().contains("RG08"));
    }

    @Test
    @DisplayName("RG09: Demande de complément sans détails lance une exception")
    void testComplementWithoutDetailsThrowsException() {
        Utilisateur agent = new Utilisateur(5, "Fall", "Moussa", "agent@justice.sn", null, "hash", Role.AGENT, StatutCompte.ACTIF, 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            dossierService.requestComplement(1, agent, "");
        });
        assertTrue(ex.getMessage().contains("RG09"));
    }

    @Test
    @DisplayName("RG04: Un agent ne peut pas modifier un dossier d'un autre service")
    void testAgentDifferentServiceBlocked() {
        // Agent du service 2 (DPAF) qui tente de traiter un dossier du service 1 (Justice)
        Utilisateur agentDpaf = new Utilisateur(6, "Ndiaye", "Fatou", "agent@dpaf.sn", null, "hash", Role.AGENT, StatutCompte.ACTIF, 2);

        SecurityException ex = assertThrows(SecurityException.class, () -> {
            dossierService.rejectDossier(1, agentDpaf, "Motif quelconque");
        });
        assertTrue(ex.getMessage().contains("RG04"));
    }

    @Test
    @DisplayName("RG04: Un administrateur peut superviser les dossiers de tous les services")
    void testAdminCanAccessAnyServiceDossier() {
        Utilisateur admin = new Utilisateur(1, "Diop", "Amadou", "admin@senthioono.sn", null, "hash", Role.ADMIN, StatutCompte.ACTIF, null);

        assertDoesNotThrow(() -> {
            dossierService.checkAgentServiceAuthorization(mockDossierDao.dossiers.get(0), admin);
        });
    }

    // Mocks DAO isolés
    static class MockDossierDao implements DossierDao {
        List<Dossier> dossiers = new ArrayList<>();
        @Override public Dossier create(Dossier d) { d.setId(dossiers.size() + 1); dossiers.add(d); return d; }
        @Override public Optional<Dossier> findById(int id) { return dossiers.stream().filter(d -> d.getId() == id).findFirst(); }
        @Override public Optional<Dossier> findByNumeroSuivi(String n) { return dossiers.stream().filter(d -> d.getNumeroSuivi().equalsIgnoreCase(n)).findFirst(); }
        @Override public List<Dossier> findAll() { return dossiers; }
        @Override public List<Dossier> findByUtilisateur(int id) { return List.of(); }
        @Override public List<Dossier> findByService(int id) { return List.of(); }
        @Override public List<Dossier> search(String q, Integer s, StatutDossier st) { return dossiers; }
        @Override public boolean updateStatut(int id, StatutDossier st, String motif, Integer agent) { return true; }
        @Override public boolean update(Dossier d) { return true; }
        @Override public boolean delete(int id) { return true; }
        @Override public DocumentFourni addDocumentFourni(DocumentFourni d) { return d; }
        @Override public List<DocumentFourni> findDocumentsByDossier(int id) { return List.of(); }
        @Override public boolean updateDocumentVerification(int id, StatutVerification st, String c) { return true; }
        @Override public int countTotal() { return dossiers.size(); }
        @Override public int countByService(int id) { return 0; }
        @Override public Map<StatutDossier, Integer> countByStatut(Integer s) { return Map.of(); }
    }

    static class MockDemarcheDao implements DemarcheDao {
        List<Demarche> demarches = new ArrayList<>();
        @Override public Demarche create(Demarche d) { return d; }
        @Override public Optional<Demarche> findById(int id) { return demarches.stream().filter(d -> d.getId() == id).findFirst(); }
        @Override public List<Demarche> findAll() { return demarches; }
        @Override public List<Demarche> findPublished() { return demarches; }
        @Override public List<Demarche> findByService(int id) { return List.of(); }
        @Override public List<Demarche> searchPublished(String k, Integer s) { return demarches; }
        @Override public boolean update(Demarche d) { return true; }
        @Override public boolean delete(int id) { return true; }
        @Override public List<Etape> findEtapesByDemarche(int id) { return List.of(); }
        @Override public void addEtape(Etape e) {}
        @Override public void deleteEtapesByDemarche(int id) {}
        @Override public List<DemarchePiece> findPiecesByDemarche(int id) { return List.of(); }
        @Override public void addDemarchePiece(DemarchePiece dp) {}
        @Override public void deleteDemarchePieces(int id) {}
        @Override public List<Piece> findAllPieces() { return List.of(); }
        @Override public Piece createPiece(Piece p) { return p; }
    }

    static class MockHistoriqueDao implements HistoriqueDao {
        @Override public Historique create(Historique h) { return h; }
        @Override public List<Historique> findByDossier(int id) { return List.of(); }
    }

    static class MockNotificationDao implements sn.senthioonodiexna.dao.NotificationDao {
        @Override public Notification create(Notification n) { return n; }
        @Override public List<Notification> findByUtilisateur(int id) { return List.of(); }
        @Override public int countUnreadByUtilisateur(int id) { return 0; }
        @Override public boolean markAsRead(int id) { return true; }
        @Override public boolean markAllAsRead(int id) { return true; }
        @Override public boolean delete(int id) { return true; }
    }
}
