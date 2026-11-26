package sn.senthioonodiexna.model;

import java.time.LocalDateTime;

public class Faq {
    private int id;
    private String question;
    private String reponse;
    private boolean publiee = true;
    private Integer idService;
    private int ordre = 1;
    private LocalDateTime dateCreation;

    // Joined metadata
    private String nomService;

    public Faq() {}

    public Faq(int id, String question, String reponse, boolean publiee, Integer idService, int ordre) {
        this.id = id;
        this.question = question;
        this.reponse = reponse;
        this.publiee = publiee;
        this.idService = idService;
        this.ordre = ordre;
    }

    public Faq(String question, String reponse, boolean publiee, Integer idService, int ordre) {
        this.question = question;
        this.reponse = reponse;
        this.publiee = publiee;
        this.idService = idService;
        this.ordre = ordre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public boolean isPubliee() { return publiee; }
    public void setPubliee(boolean publiee) { this.publiee = publiee; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getNomService() { return nomService; }
    public void setNomService(String nomService) { this.nomService = nomService; }

    @Override
    public String toString() {
        return question;
    }
}
