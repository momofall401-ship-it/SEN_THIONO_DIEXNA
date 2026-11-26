package sn.senthioonodiexna.model;

public class Etape {
    private int id;
    private String titre;
    private String description;
    private int ordre;
    private int idDemarche;

    public Etape() {}

    public Etape(int id, String titre, String description, int ordre, int idDemarche) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.ordre = ordre;
        this.idDemarche = idDemarche;
    }

    public Etape(String titre, String description, int ordre, int idDemarche) {
        this.titre = titre;
        this.description = description;
        this.ordre = ordre;
        this.idDemarche = idDemarche;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public int getIdDemarche() { return idDemarche; }
    public void setIdDemarche(int idDemarche) { this.idDemarche = idDemarche; }

    @Override
    public String toString() {
        return ordre + ". " + titre;
    }
}
