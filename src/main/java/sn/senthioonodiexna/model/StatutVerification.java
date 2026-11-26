package sn.senthioonodiexna.model;

public enum StatutVerification {
    EN_ATTENTE("En attente"),
    CONFORME("Conforme"),
    NON_CONFORME("Non conforme"),
    A_COMPLETER("À compléter");

    private final String libelle;

    StatutVerification(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
