package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String motDePasseHash;
    private Role role = Role.CITOYEN;
    private StatutCompte statutCompte = StatutCompte.ACTIF;
    private Integer idService;
    private String nomService;
    private LocalDateTime dateCreation;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom, String email, String telephone,
                       String motDePasseHash, Role role, StatutCompte statutCompte, Integer idService) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.motDePasseHash = motDePasseHash;
        this.role = role != null ? role : Role.CITOYEN;
        this.statutCompte = statutCompte != null ? statutCompte : StatutCompte.ACTIF;
        this.idService = idService;
    }

    public Utilisateur(String nom, String prenom, String email, String telephone,
                       String motDePasseHash, Role role, StatutCompte statutCompte, Integer idService) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.motDePasseHash = motDePasseHash;
        this.role = role != null ? role : Role.CITOYEN;
        this.statutCompte = statutCompte != null ? statutCompte : StatutCompte.ACTIF;
        this.idService = idService;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public StatutCompte getStatutCompte() { return statutCompte; }
    public void setStatutCompte(StatutCompte statutCompte) { this.statutCompte = statutCompte; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public String getNomService() { return nomService; }
    public void setNomService(String nomService) { this.nomService = nomService; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean isActif() {
        return statutCompte == StatutCompte.ACTIF;
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + role.getLibelle() + ")";
    }
}
