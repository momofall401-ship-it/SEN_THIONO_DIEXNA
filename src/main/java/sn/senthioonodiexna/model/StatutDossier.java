package sn.senthioonodiexna.model;

public enum StatutDossier {
    BROUILLON("Brouillon", "status-brouillon"),
    DEPOSE("Déposé", "status-depose"),
    EN_VERIFICATION("En vérification", "status-en-verification"),
    COMPLEMENT_DEMANDE("Complément demandé", "status-complement-demande"),
    EN_TRAITEMENT("En traitement", "status-en-traitement"),
    ACCEPTE("Accepté", "status-accepte"),
    REJETE("Rejeté", "status-rejete"),
    CLOTURE("Clôturé", "status-cloture");

    private final String libelle;
    private final String cssClass;

    StatutDossier(String libelle, String cssClass) {
        this.libelle = libelle;
        this.cssClass = cssClass;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getCssClass() {
        return cssClass;
    }
}
