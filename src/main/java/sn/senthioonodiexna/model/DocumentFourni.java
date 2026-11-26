package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class DocumentFourni {
    private int id;
    private String cheminFichier;
    private LocalDateTime dateDepot;
    private StatutVerification statutVerification = StatutVerification.EN_ATTENTE;
    private String commentaire;
    private int idDossier;
    private int idPiece;

    // Joined metadata for display
    private String nomPiece;
    private String descriptionPiece;
    private boolean obligatoire = true;

    public DocumentFourni() {}

    public DocumentFourni(int id, String cheminFichier, LocalDateTime dateDepot,
                          StatutVerification statutVerification, String commentaire,
                          int idDossier, int idPiece) {
        this.id = id;
        this.cheminFichier = cheminFichier;
        this.dateDepot = dateDepot;
        this.statutVerification = statutVerification != null ? statutVerification : StatutVerification.EN_ATTENTE;
        this.commentaire = commentaire;
        this.idDossier = idDossier;
        this.idPiece = idPiece;
    }

    public DocumentFourni(int idDossier, int idPiece, String cheminFichier) {
        this.idDossier = idDossier;
        this.idPiece = idPiece;
        this.cheminFichier = cheminFichier;
        this.statutVerification = StatutVerification.EN_ATTENTE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }

    public StatutVerification getStatutVerification() { return statutVerification; }
    public void setStatutVerification(StatutVerification statutVerification) { this.statutVerification = statutVerification; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getIdDossier() { return idDossier; }
    public void setIdDossier(int idDossier) { this.idDossier = idDossier; }

    public int getIdPiece() { return idPiece; }
    public void setIdPiece(int idPiece) { this.idPiece = idPiece; }

    public String getNomPiece() { return nomPiece; }
    public void setNomPiece(String nomPiece) { this.nomPiece = nomPiece; }

    public String getDescriptionPiece() { return descriptionPiece; }
    public void setDescriptionPiece(String descriptionPiece) { this.descriptionPiece = descriptionPiece; }

    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }
}
