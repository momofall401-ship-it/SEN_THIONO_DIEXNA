package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class ServiceAdministratif {
    private int id;
    private String nom;
    private String description;
    private String telephone;
    private String email;
    private int idAdministration;
    private String nomAdministration; // Join convenience
    private LocalDateTime dateCreation;

    public ServiceAdministratif() {}

    public ServiceAdministratif(int id, String nom, String description, String telephone, String email, int idAdministration) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.telephone = telephone;
        this.email = email;
        this.idAdministration = idAdministration;
    }

    public ServiceAdministratif(String nom, String description, String telephone, String email, int idAdministration) {
        this.nom = nom;
        this.description = description;
        this.telephone = telephone;
        this.email = email;
        this.idAdministration = idAdministration;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getIdAdministration() { return idAdministration; }
    public void setIdAdministration(int idAdministration) { this.idAdministration = idAdministration; }

    public String getNomAdministration() { return nomAdministration; }
    public void setNomAdministration(String nomAdministration) { this.nomAdministration = nomAdministration; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return nom;
    }
}
