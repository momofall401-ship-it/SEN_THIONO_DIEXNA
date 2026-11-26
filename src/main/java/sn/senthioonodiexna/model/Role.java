package sn.senthioonodiexna.model;

public enum Role {
    CITOYEN("Citoyen / Usager"),
    AGENT("Agent Administratif"),
    RESPONSABLE("Responsable de Service"),
    ADMIN("Administrateur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
