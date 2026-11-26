package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class Administration {
    private int id;
    private String nom;
    private String description;
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    private LocalDateTime dateCreation;

    public Administration() {}

    public Administration(int id, String nom, String description, String adresse, String telephone, String email, String siteWeb) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siteWeb = siteWeb;
    }

    public Administration(String nom, String description, String adresse, String telephone, String email, String siteWeb) {
        this.nom = nom;
        this.description = description;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.siteWeb = siteWeb;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return nom;
    }
}
