package sn.senthioonodiexna.dao;

import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.Utilisateur;

import java.util.List;
import java.util.Optional;

public interface UtilisateurDao {
    Utilisateur create(Utilisateur utilisateur);
    Optional<Utilisateur> findById(int id);
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findAll();
    List<Utilisateur> findByRole(Role role);
    List<Utilisateur> findByService(int idService);
    boolean update(Utilisateur utilisateur);
    boolean updateMotDePasse(int id, String nouveauMotDePasseHash);
    boolean updateStatutCompte(int id, String nouveauStatut);
    boolean delete(int id);
}
