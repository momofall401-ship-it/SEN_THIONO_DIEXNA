package sn.senthioonodiexna.model;

public enum StatutCompte {
    ACTIF("Actif"),
    DESACTIVE("Désactivé");

    private final String libelle;

    StatutCompte(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
