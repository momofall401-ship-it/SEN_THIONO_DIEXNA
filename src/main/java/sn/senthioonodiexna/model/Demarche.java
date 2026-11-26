package sn.senthioonodiexna.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Demarche {
    private int id;
    private String titre;
    private String description;
    private String conditions;
    private String delaiEstime;
    private String fraisIndicatifs;
    private int idService;
    private StatutPublication statutPublication = StatutPublication.PUBLIEE;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;

    // Joined metadata
    private String nomService;
    private String nomAdministration;
    private String telephoneService;
    private String emailService;
    private String adresseAdministration;

    // Sub-lists
    private List<Etape> etapes = new ArrayList<>();
    private List<DemarchePiece> pieces = new ArrayList<>();

    public Demarche() {}

    public Demarche(int id, String titre, String description, String conditions,
                    String delaiEstime, String fraisIndicatifs, int idService,
                    StatutPublication statutPublication) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.conditions = conditions;
        this.delaiEstime = delaiEstime;
        this.fraisIndicatifs = fraisIndicatifs;
        this.idService = idService;
        this.statutPublication = statutPublication != null ? statutPublication : StatutPublication.PUBLIEE;
    }

    public Demarche(String titre, String description, String conditions,
                    String delaiEstime, String fraisIndicatifs, int idService,
                    StatutPublication statutPublication) {
        this.titre = titre;
        this.description = description;
        this.conditions = conditions;
        this.delaiEstime = delaiEstime;
        this.fraisIndicatifs = fraisIndicatifs;
        this.idService = idService;
        this.statutPublication = statutPublication != null ? statutPublication : StatutPublication.PUBLIEE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public String getDelaiEstime() { return delaiEstime; }
    public void setDelaiEstime(String delaiEstime) { this.delaiEstime = delaiEstime; }

    public String getFraisIndicatifs() { return fraisIndicatifs; }
    public void setFraisIndicatifs(String fraisIndicatifs) { this.fraisIndicatifs = fraisIndicatifs; }

    public int getIdService() { return idService; }
    public void setIdService(int idService) { this.idService = idService; }

    public StatutPublication getStatutPublication() { return statutPublication; }
    public void setStatutPublication(StatutPublication statutPublication) { this.statutPublication = statutPublication; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }

    public String getNomService() { return nomService; }
    public void setNomService(String nomService) { this.nomService = nomService; }

    public String getNomAdministration() { return nomAdministration; }
    public void setNomAdministration(String nomAdministration) { this.nomAdministration = nomAdministration; }

    public String getTelephoneService() { return telephoneService; }
    public void setTelephoneService(String telephoneService) { this.telephoneService = telephoneService; }

    public String getEmailService() { return emailService; }
    public void setEmailService(String emailService) { this.emailService = emailService; }

    public String getAdresseAdministration() { return adresseAdministration; }
    public void setAdresseAdministration(String adresseAdministration) { this.adresseAdministration = adresseAdministration; }

    public List<Etape> getEtapes() { return etapes; }
    public void setEtapes(List<Etape> etapes) { this.etapes = etapes; }

    public List<DemarchePiece> getPieces() { return pieces; }
    public void setPieces(List<DemarchePiece> pieces) { this.pieces = pieces; }

    public boolean isPubliee() {
        return statutPublication == StatutPublication.PUBLIEE;
    }

    @Override
    public String toString() {
        return titre;
    }
}
