package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.Faq;

import java.util.List;
import java.util.Optional;

public interface FaqDao {
    Faq create(Faq faq);
    Optional<Faq> findById(int id);
    List<Faq> findAll();
    List<Faq> findPublished();
    List<Faq> search(String keyword, Integer idService);
    boolean update(Faq faq);
    boolean delete(int id);
}
