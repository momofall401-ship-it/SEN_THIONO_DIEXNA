package sn.senthioonodiexna.model;

public enum StatutPublication {
    BROUILLON("Brouillon"),
    PUBLIEE("Publiée"),
    ARCHIVEE("Archivée");

    private final String libelle;

    StatutPublication(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
