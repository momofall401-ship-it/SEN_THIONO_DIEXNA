package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String titre;
    private String message;
    private LocalDateTime dateCreation;
    private boolean estLue = false;
    private int idUtilisateur;
    private Integer idDossier;

    // Joined metadata
    private String numeroSuiviDossier;

    public Notification() {}

    public Notification(int id, String titre, String message, LocalDateTime dateCreation,
                        boolean estLue, int idUtilisateur, Integer idDossier) {
        this.id = id;
        this.titre = titre;
        this.message = message;
        this.dateCreation = dateCreation;
        this.estLue = estLue;
        this.idUtilisateur = idUtilisateur;
        this.idDossier = idDossier;
    }

    public Notification(String titre, String message, int idUtilisateur, Integer idDossier) {
        this.titre = titre;
        this.message = message;
        this.idUtilisateur = idUtilisateur;
        this.idDossier = idDossier;
        this.dateCreation = LocalDateTime.now();
        this.estLue = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean isEstLue() { return estLue; }
    public void setEstLue(boolean estLue) { this.estLue = estLue; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public Integer getIdDossier() { return idDossier; }
    public void setIdDossier(Integer idDossier) { this.idDossier = idDossier; }

    public String getNumeroSuiviDossier() { return numeroSuiviDossier; }
    public void setNumeroSuiviDossier(String numeroSuiviDossier) { this.numeroSuiviDossier = numeroSuiviDossier; }
}
