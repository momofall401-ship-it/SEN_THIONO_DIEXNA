package sn.senthioonodiexna.model;

public class Piece {
    private int id;
    private String nom;
    private String description;
    private String formatsAcceptes = "PDF, JPG, PNG";

    public Piece() {}

    public Piece(int id, String nom, String description, String formatsAcceptes) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.formatsAcceptes = formatsAcceptes;
    }

    public Piece(String nom, String description, String formatsAcceptes) {
        this.nom = nom;
        this.description = description;
        this.formatsAcceptes = formatsAcceptes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFormatsAcceptes() { return formatsAcceptes; }
    public void setFormatsAcceptes(String formatsAcceptes) { this.formatsAcceptes = formatsAcceptes; }

    @Override
    public String toString() {
        return nom;
    }
}
