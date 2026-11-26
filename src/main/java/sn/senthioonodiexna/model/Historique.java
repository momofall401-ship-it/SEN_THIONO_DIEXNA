package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class Historique {
    private int id;
    private String ancienStatut;
    private String nouveauStatut;
    private String commentaire;
    private LocalDateTime dateAction;
    private int idDossier;
    private int idUtilisateur;

    // Joined metadata for display
    private String nomUtilisateur;
    private Role roleUtilisateur;

    public Historique() {}

    public Historique(int id, String ancienStatut, String nouveauStatut, String commentaire,
                      LocalDateTime dateAction, int idDossier, int idUtilisateur) {
        this.id = id;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
        this.dateAction = dateAction;
        this.idDossier = idDossier;
        this.idUtilisateur = idUtilisateur;
    }

    public Historique(String ancienStatut, String nouveauStatut, String commentaire,
                      int idDossier, int idUtilisateur) {
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
        this.idDossier = idDossier;
        this.idUtilisateur = idUtilisateur;
        this.dateAction = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(String ancienStatut) { this.ancienStatut = ancienStatut; }

    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public int getIdDossier() { return idDossier; }
    public void setIdDossier(int idDossier) { this.idDossier = idDossier; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public Role getRoleUtilisateur() { return roleUtilisateur; }
    public void setRoleUtilisateur(Role roleUtilisateur) { this.roleUtilisateur = roleUtilisateur; }
}
