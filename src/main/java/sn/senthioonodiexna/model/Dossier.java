package sn.senthioonodiexna.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Dossier {
    private int id;
    private String numeroSuivi;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDepot;
    private StatutDossier statut = StatutDossier.BROUILLON;
    private String motifRejet;
    private String notes;
    private int idUtilisateur;
    private int idDemarche;
    private Integer idAgent;

    // Joined metadata
    private String nomCitoyen;
    private String prenomCitoyen;
    private String emailCitoyen;
    private String telephoneCitoyen;

    private String titreDemarche;
    private int idService;
    private String nomService;
    private String nomAdministration;

    private String nomAgent;

    // Associated lists
    private List<DocumentFourni> documents = new ArrayList<>();
    private List<Historique> historiques = new ArrayList<>();

    public Dossier() {}

    public Dossier(int id, String numeroSuivi, LocalDateTime dateCreation, LocalDateTime dateDepot,
                   StatutDossier statut, String motifRejet, String notes,
                   int idUtilisateur, int idDemarche, Integer idAgent) {
        this.id = id;
        this.numeroSuivi = numeroSuivi;
        this.dateCreation = dateCreation;
        this.dateDepot = dateDepot;
        this.statut = statut != null ? statut : StatutDossier.BROUILLON;
        this.motifRejet = motifRejet;
        this.notes = notes;
        this.idUtilisateur = idUtilisateur;
        this.idDemarche = idDemarche;
        this.idAgent = idAgent;
    }

    public Dossier(String numeroSuivi, int idUtilisateur, int idDemarche) {
        this.numeroSuivi = numeroSuivi;
        this.idUtilisateur = idUtilisateur;
        this.idDemarche = idDemarche;
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutDossier.BROUILLON;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroSuivi() { return numeroSuivi; }
    public void setNumeroSuivi(String numeroSuivi) { this.numeroSuivi = numeroSuivi; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }

    public StatutDossier getStatut() { return statut; }
    public void setStatut(StatutDossier statut) { this.statut = statut; }

    public String getMotifRejet() { return motifRejet; }
    public void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getIdDemarche() { return idDemarche; }
    public void setIdDemarche(int idDemarche) { this.idDemarche = idDemarche; }

    public Integer getIdAgent() { return idAgent; }
    public void setIdAgent(Integer idAgent) { this.idAgent = idAgent; }

    public String getNomCitoyen() { return nomCitoyen; }
    public void setNomCitoyen(String nomCitoyen) { this.nomCitoyen = nomCitoyen; }

    public String getPrenomCitoyen() { return prenomCitoyen; }
    public void setPrenomCitoyen(String prenomCitoyen) { this.prenomCitoyen = prenomCitoyen; }

    public String getNomCompletCitoyen() {
        return (prenomCitoyen != null ? prenomCitoyen : "") + " " + (nomCitoyen != null ? nomCitoyen : "");
    }

    public String getEmailCitoyen() { return emailCitoyen; }
    public void setEmailCitoyen(String emailCitoyen) { this.emailCitoyen = emailCitoyen; }

    public String getTelephoneCitoyen() { return telephoneCitoyen; }
    public void setTelephoneCitoyen(String telephoneCitoyen) { this.telephoneCitoyen = telephoneCitoyen; }

    public String getTitreDemarche() { return titreDemarche; }
    public void setTitreDemarche(String titreDemarche) { this.titreDemarche = titreDemarche; }

    public int getIdService() { return idService; }
    public void setIdService(int idService) { this.idService = idService; }

    public String getNomService() { return nomService; }
    public void setNomService(String nomService) { this.nomService = nomService; }

    public String getNomAdministration() { return nomAdministration; }
    public void setNomAdministration(String nomAdministration) { this.nomAdministration = nomAdministration; }

    public String getNomAgent() { return nomAgent; }
    public void setNomAgent(String nomAgent) { this.nomAgent = nomAgent; }

    public List<DocumentFourni> getDocuments() { return documents; }
    public void setDocuments(List<DocumentFourni> documents) { this.documents = documents; }

    public List<Historique> getHistoriques() { return historiques; }
    public void setHistoriques(List<Historique> historiques) { this.historiques = historiques; }

    @Override
    public String toString() {
        return "Dossier " + numeroSuivi + " (" + (statut != null ? statut.getLibelle() : "") + ")";
    }
}
