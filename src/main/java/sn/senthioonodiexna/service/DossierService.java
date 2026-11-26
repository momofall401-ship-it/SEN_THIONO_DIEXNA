package sn.senthioonodiexna.service;

import sn.senthioonodiexna.dao.DemarcheDao;
import sn.senthioonodiexna.dao.DossierDao;
import sn.senthioonodiexna.dao.HistoriqueDao;
import sn.senthioonodiexna.dao.impl.DemarcheDaoImpl;
import sn.senthioonodiexna.dao.impl.DossierDaoImpl;
import sn.senthioonodiexna.dao.impl.HistoriqueDaoImpl;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.DemarchePiece;
import sn.senthioonodiexna.model.DocumentFourni;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.Historique;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.StatutDossier;
import sn.senthioonodiexna.model.StatutVerification;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.util.ValidationUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

public class DossierService {

    private final DossierDao dossierDao;
    private final DemarcheDao demarcheDao;
    private final HistoriqueDao historiqueDao;
    private final NotificationService notificationService;

    public DossierService() {
        this(new DossierDaoImpl(), new DemarcheDaoImpl(), new HistoriqueDaoImpl(), new NotificationService());
    }

    public DossierService(DossierDao dossierDao, DemarcheDao demarcheDao, HistoriqueDao historiqueDao, NotificationService notificationService) {
        this.dossierDao = dossierDao;
        this.demarcheDao = demarcheDao;
        this.historiqueDao = historiqueDao;
        this.notificationService = notificationService;
    }

    // RG06: Chaque dossier possède un numéro de suivi unique
    public synchronized String generateUniqueNumeroSuivi() {
        SecureRandom random = new SecureRandom();
        String candidate;
        int attempts = 0;
        int currentYear = Year.now().getValue();

        do {
            int randomNum = 10000 + random.nextInt(90000);
            candidate = String.format("STD-%d-%05d", currentYear, randomNum);
            attempts++;
            if (attempts > 50) {
                candidate = "STD-" + currentYear + "-" + System.currentTimeMillis() % 100000;
                break;
            }
        } while (dossierDao.findByNumeroSuivi(candidate).isPresent());

        return candidate;
    }

    // 6.4 : Création d'un dossier par le citoyen
    public Dossier createDossier(int idCitoyen, int idDemarche, List<Integer> preparedPieceIds, boolean submitImmediately) {
        Optional<Demarche> optDemarche = demarcheDao.findById(idDemarche);
        if (optDemarche.isEmpty()) {
            throw new IllegalArgumentException("Démarche introuvable (ID: " + idDemarche + ").");
        }

        String numeroSuivi = generateUniqueNumeroSuivi();
        Dossier dossier = new Dossier(numeroSuivi, idCitoyen, idDemarche);

        if (submitImmediately) {
            dossier.setStatut(StatutDossier.DEPOSE);
            dossier.setDateDepot(LocalDateTime.now());
        } else {
            dossier.setStatut(StatutDossier.BROUILLON);
        }

        Dossier created = dossierDao.create(dossier);

        // Association des pièces de la démarche au dossier (documents fournis)
        List<DemarchePiece> pieces = demarcheDao.findPiecesByDemarche(idDemarche);
        for (DemarchePiece dp : pieces) {
            boolean isChecked = preparedPieceIds != null && preparedPieceIds.contains(dp.getIdPiece());
            DocumentFourni doc = new DocumentFourni();
            doc.setIdDossier(created.getId());
            doc.setIdPiece(dp.getIdPiece());
            doc.setDateDepot(LocalDateTime.now());
            doc.setStatutVerification(isChecked ? StatutVerification.EN_ATTENTE : StatutVerification.A_COMPLETER);
            doc.setCommentaire(isChecked ? "Préparé par l'usager" : "En attente de fourniture");
            doc.setCheminFichier(isChecked ? "piece_" + dp.getIdPiece() + ".pdf" : null);
            dossierDao.addDocumentFourni(doc);
        }

        // RG07: Historique
        String commentaireInit = submitImmediately ? "Dossier déposé par l'usager." : "Dossier créé en brouillon.";
        historiqueDao.create(new Historique(null, created.getStatut().name(), commentaireInit, created.getId(), idCitoyen));

        // Notification citoyen
        if (submitImmediately) {
            notificationService.notifyUser(idCitoyen, "Dossier déposé",
                    "Votre dossier n° " + created.getNumeroSuivi() + " pour la démarche \"" + optDemarche.get().getTitre() + "\" a été enregistré avec succès.",
                    created.getId());
        }

        return getDossierById(created.getId()).orElse(created);
    }

    // Dépôt d'un dossier existant en brouillon par le citoyen
    public boolean submitDraft(int idDossier, int idCitoyen) {
        Dossier d = getAuthorizedDossierForCitizen(idDossier, idCitoyen);
        if (d.getStatut() != StatutDossier.BROUILLON) {
            throw new IllegalStateException("Le dossier n'est plus en brouillon.");
        }

        boolean updated = dossierDao.updateStatut(idDossier, StatutDossier.DEPOSE, null, null);
        if (updated) {
            historiqueDao.create(new Historique(StatutDossier.BROUILLON.name(), StatutDossier.DEPOSE.name(),
                    "Dépôt officiel du dossier par l'usager.", idDossier, idCitoyen));
            notificationService.notifyUser(idCitoyen, "Dossier déposé",
                    "Votre dossier n° " + d.getNumeroSuivi() + " est désormais enregistré comme déposé.", idDossier);
        }
        return updated;
    }

    // RG04: Vérification des droits d'un agent sur le dossier
    public void checkAgentServiceAuthorization(Dossier dossier, Utilisateur agent) {
        if (agent == null) {
            throw new SecurityException("Authentification requise pour cette opération.");
        }
        if (agent.getRole() == Role.ADMIN) {
            return; // L'administrateur a tous les droits de supervision
        }
        if (agent.getRole() == Role.AGENT || agent.getRole() == Role.RESPONSABLE) {
            if (agent.getIdService() == null || agent.getIdService() != dossier.getIdService()) {
                throw new SecurityException("RG04: Un agent ne peut traiter que les dossiers de son propre service administratif.");
            }
            return;
        }
        throw new SecurityException("Vous n'avez pas les autorisations nécessaires pour modifier ce dossier.");
    }

    // Enregistrement de dépôt physique par l'agent au guichet
    public boolean registerPhysicalDeposit(int idDossier, Utilisateur agent, String observation) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.DEPOSE, null, agent.getId());
        if (ok) {
            String comment = "Réception physique enregistrée au guichet par l'agent." +
                             (observation != null && !observation.isBlank() ? " (" + observation.trim() + ")" : "");
            historiqueDao.create(new Historique(ancien, StatutDossier.DEPOSE.name(), comment, idDossier, agent.getId()));
            notificationService.notifyUser(d.getIdUtilisateur(), "Dépôt physique enregistré",
                    "Votre dossier n° " + d.getNumeroSuivi() + " a été réceptionné physiquement au guichet.", idDossier);
        }
        return ok;
    }

    // Passage en vérification
    public boolean startVerification(int idDossier, Utilisateur agent) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.EN_VERIFICATION, null, agent.getId());
        if (ok) {
            historiqueDao.create(new Historique(ancien, StatutDossier.EN_VERIFICATION.name(),
                    "Instruction et vérification des pièces démarrées par l'agent.", idDossier, agent.getId()));
            notificationService.notifyUser(d.getIdUtilisateur(), "Dossier en cours de vérification",
                    "Votre dossier n° " + d.getNumeroSuivi() + " est actuellement en cours d'instruction par nos services.", idDossier);
        }
        return ok;
    }

    // Vérification unitaire d'un document fourni
    public boolean verifyDocument(int idDoc, StatutVerification statut, String commentaire, Utilisateur agent, int idDossier) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        return dossierDao.updateDocumentVerification(idDoc, statut, commentaire);
    }

    // RG09: Demande de complément (précise les pièces ou informations attendues)
    public boolean requestComplement(int idDossier, Utilisateur agent, String detailsAttente) {
        if (ValidationUtil.isBlank(detailsAttente)) {
            throw new IllegalArgumentException("RG09: Une demande de complément doit obligatoirement préciser les pièces ou informations attendues.");
        }

        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.COMPLEMENT_DEMANDE, null, agent.getId());
        if (ok) {
            String comment = "Complément requis : " + detailsAttente.trim();
            historiqueDao.create(new Historique(ancien, StatutDossier.COMPLEMENT_DEMANDE.name(), comment, idDossier, agent.getId()));
            notificationService.notifyUser(d.getIdUtilisateur(), "Complément de dossier demandé",
                    "Des pièces complémentaires sont nécessaires pour votre dossier n° " + d.getNumeroSuivi() + " : " + detailsAttente.trim(), idDossier);
        }
        return ok;
    }

    // Mise en traitement
    public boolean setEnTraitement(int idDossier, Utilisateur agent, String commentaire) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.EN_TRAITEMENT, null, agent.getId());
        if (ok) {
            String c = (commentaire != null && !commentaire.isBlank()) ? commentaire : "Dossier complet, en cours de traitement.";
            historiqueDao.create(new Historique(ancien, StatutDossier.EN_TRAITEMENT.name(), c, idDossier, agent.getId()));
            notificationService.notifyUser(d.getIdUtilisateur(), "Dossier en traitement",
                    "Votre dossier n° " + d.getNumeroSuivi() + " est complet et suit son traitement administratif.", idDossier);
        }
        return ok;
    }

    // Acceptation du dossier
    public boolean acceptDossier(int idDossier, Utilisateur agent, String observation) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.ACCEPTE, null, agent.getId());
        if (ok) {
            String comment = "Décision favorable. " + (observation != null && !observation.isBlank() ? observation.trim() : "Dossier accepté.");
            historiqueDao.create(new Historique(ancien, StatutDossier.ACCEPTE.name(), comment, idDossier, agent.getId()));
            notificationService.notifyUser(d.getIdUtilisateur(), "Dossier validé et accepté !",
                    "Félicitations, votre demande n° " + d.getNumeroSuivi() + " a reçu une décision favorable. Vous pouvez vous présenter pour le retrait.", idDossier);
        }
        return ok;
    }

    // RG08: Un rejet doit obligatoirement être accompagné d'un motif
    public boolean rejectDossier(int idDossier, Utilisateur agent, String motifRejet) {
        if (ValidationUtil.isBlank(motifRejet)) {
            throw new IllegalArgumentException("RG08: Un rejet doit obligatoirement être accompagné d'un motif explicite.");
        }

        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        checkAgentServiceAuthorization(d, agent);

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.REJETE, motifRejet.trim(), agent.getId());
        if (ok) {
            historiqueDao.create(new Historique(ancien, StatutDossier.REJETE.name(), "Motif du rejet : " + motifRejet.trim(), idDossier, agent.getId()));
            notificationService.notifyUser(d.getIdUtilisateur(), "Dossier rejeté",
                    "Votre dossier n° " + d.getNumeroSuivi() + " n'a pas pu aboutir. Motif : " + motifRejet.trim(), idDossier);
        }
        return ok;
    }

    // Clôture du dossier
    public boolean closeDossier(int idDossier, Utilisateur user, String motif) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));

        String ancien = d.getStatut().name();
        boolean ok = dossierDao.updateStatut(idDossier, StatutDossier.CLOTURE, null, user.getId());
        if (ok) {
            String c = "Dossier clôturé. " + (motif != null ? motif.trim() : "");
            historiqueDao.create(new Historique(ancien, StatutDossier.CLOTURE.name(), c, idDossier, user.getId()));
        }
        return ok;
    }

    // RG03: Un citoyen ne consulte que ses propres dossiers
    public Dossier getAuthorizedDossierForCitizen(int idDossier, int idCitoyen) {
        Dossier d = getDossierById(idDossier).orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        if (d.getIdUtilisateur() != idCitoyen) {
            throw new SecurityException("RG03: Vous n'êtes pas autorisé à consulter le dossier d'un autre citoyen.");
        }
        return d;
    }

    public List<Dossier> getDossiersForCitizen(int idCitoyen) {
        return dossierDao.findByUtilisateur(idCitoyen);
    }

    public List<Dossier> getDossiersForService(int idService) {
        return dossierDao.findByService(idService);
    }

    public List<Dossier> searchDossiers(String query, Integer idService, StatutDossier statut) {
        return dossierDao.search(query, idService, statut);
    }

    public Optional<Dossier> getDossierById(int id) {
        Optional<Dossier> opt = dossierDao.findById(id);
        opt.ifPresent(d -> d.setHistoriques(historiqueDao.findByDossier(d.getId())));
        return opt;
    }

    public Optional<Dossier> getDossierByNumeroSuivi(String numeroSuivi) {
        Optional<Dossier> opt = dossierDao.findByNumeroSuivi(numeroSuivi);
        opt.ifPresent(d -> d.setHistoriques(historiqueDao.findByDossier(d.getId())));
        return opt;
    }
}
