package sn.senthioonodiexna.service;

import sn.senthioonodiexna.dao.FaqDao;
import sn.senthioonodiexna.dao.impl.FaqDaoImpl;
import sn.senthioonodiexna.model.Faq;

import java.util.List;
import java.util.Optional;

public class FaqService {

    private final FaqDao faqDao;

    public FaqService() {
        this(new FaqDaoImpl());
    }

    public FaqService(FaqDao faqDao) {
        this.faqDao = faqDao;
    }

    public List<Faq> getPublishedFaq() {
        return faqDao.findPublished();
    }

    public List<Faq> getAllFaqForAdmin() {
        return faqDao.findAll();
    }

    public List<Faq> searchFaq(String keyword, Integer idService) {
        return faqDao.search(keyword, idService);
    }

    public Optional<Faq> getById(int id) {
        return faqDao.findById(id);
    }

    public Faq createFaq(String question, String reponse, boolean publiee, Integer idService, int ordre) {
        if (question == null || question.isBlank() || reponse == null || reponse.isBlank()) {
            throw new IllegalArgumentException("La question et la réponse sont obligatoires.");
        }
        Faq faq = new Faq(question.trim(), reponse.trim(), publiee, idService, ordre);
        return faqDao.create(faq);
    }

    public boolean updateFaq(Faq faq) {
        return faqDao.update(faq);
    }

    public boolean deleteFaq(int id) {
        return faqDao.delete(id);
    }
}
