package sn.senthioonodiexna.model;

public class DemarchePiece {
    private int idDemarche;
    private int idPiece;
    private boolean obligatoire = true;
    private String instructions;

    // Joined fields for display
    private String nomPiece;
    private String descriptionPiece;
    private String formatsAcceptes;

    public DemarchePiece() {}

    public DemarchePiece(int idDemarche, int idPiece, boolean obligatoire, String instructions) {
        this.idDemarche = idDemarche;
        this.idPiece = idPiece;
        this.obligatoire = obligatoire;
        this.instructions = instructions;
    }

    public int getIdDemarche() { return idDemarche; }
    public void setIdDemarche(int idDemarche) { this.idDemarche = idDemarche; }

    public int getIdPiece() { return idPiece; }
    public void setIdPiece(int idPiece) { this.idPiece = idPiece; }

    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getNomPiece() { return nomPiece; }
    public void setNomPiece(String nomPiece) { this.nomPiece = nomPiece; }

    public String getDescriptionPiece() { return descriptionPiece; }
    public void setDescriptionPiece(String descriptionPiece) { this.descriptionPiece = descriptionPiece; }

    public String getFormatsAcceptes() { return formatsAcceptes; }
    public void setFormatsAcceptes(String formatsAcceptes) { this.formatsAcceptes = formatsAcceptes; }

    @Override
    public String toString() {
        return (nomPiece != null ? nomPiece : "Pièce #" + idPiece) + (obligatoire ? " (Obligatoire)" : " (Facultatif)");
    }
}
