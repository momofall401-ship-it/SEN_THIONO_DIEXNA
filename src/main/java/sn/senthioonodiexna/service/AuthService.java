package sn.senthioonodiexna.service;

import sn.senthioonodiexna.config.AppSession;
import sn.senthioonodiexna.dao.UtilisateurDao;
import sn.senthioonodiexna.dao.impl.UtilisateurDaoImpl;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.StatutCompte;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.util.PasswordUtil;
import sn.senthioonodiexna.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class AuthService {

    private final UtilisateurDao utilisateurDao;

    public AuthService() {
        this(new UtilisateurDaoImpl());
    }

    public AuthService(UtilisateurDao utilisateurDao) {
        this.utilisateurDao = utilisateurDao;
    }

    public Utilisateur register(String nom, String prenom, String email, String telephone, String password) {
        if (ValidationUtil.isBlank(nom) || ValidationUtil.isBlank(prenom)) {
            throw new IllegalArgumentException("Le nom et le prénom sont obligatoires.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Adresse email invalide.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        if (!ValidationUtil.isValidPhone(telephone)) {
            throw new IllegalArgumentException("Numéro de téléphone sénégalais invalide (ex: +221 77 123 45 67 ou 771234567).");
        }

        // RG02: L'adresse e-mail d'un compte doit être unique
        if (utilisateurDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Un compte existe déjà avec cette adresse email.");
        }

        String hash = PasswordUtil.hashPassword(password);
        Utilisateur user = new Utilisateur(nom.trim(), prenom.trim(), email.trim().toLowerCase(),
                telephone != null ? telephone.trim() : null, hash, Role.CITOYEN, StatutCompte.ACTIF, null);

        return utilisateurDao.create(user);
    }

    public Utilisateur login(String email, String password) {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("Veuillez saisir votre email et votre mot de passe.");
        }

        Optional<Utilisateur> optUser = utilisateurDao.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            throw new IllegalArgumentException("Identifiants incorrects (email ou mot de passe invalide).");
        }

        Utilisateur user = optUser.get();

        // RG11: Un compte désactivé ne peut pas se connecter
        if (user.getStatutCompte() == StatutCompte.DESACTIVE) {
            throw new IllegalStateException("Ce compte a été désactivé par l'administration. Veuillez contacter le support.");
        }

        if (!PasswordUtil.checkPassword(password, user.getMotDePasseHash())) {
            throw new IllegalArgumentException("Identifiants incorrects (email ou mot de passe invalide).");
        }

        // Enregistrement dans la session active
        AppSession.getInstance().setCurrentUser(user);
        return user;
    }

    public void logout() {
        AppSession.getInstance().clear();
    }

    public boolean updateProfile(int userId, String nom, String prenom, String telephone) {
        Optional<Utilisateur> optUser = utilisateurDao.findById(userId);
        if (optUser.isEmpty()) return false;

        Utilisateur user = optUser.get();
        user.setNom(nom.trim());
        user.setPrenom(prenom.trim());
        user.setTelephone(telephone != null ? telephone.trim() : null);

        boolean updated = utilisateurDao.update(user);
        if (updated && AppSession.getInstance().getCurrentUser() != null && AppSession.getInstance().getCurrentUser().getId() == userId) {
            AppSession.getInstance().setCurrentUser(user);
        }
        return updated;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        Optional<Utilisateur> optUser = utilisateurDao.findById(userId);
        if (optUser.isEmpty()) return false;

        Utilisateur user = optUser.get();
        if (!PasswordUtil.checkPassword(oldPassword, user.getMotDePasseHash())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }

        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return utilisateurDao.updateMotDePasse(userId, newHash);
    }

    // Gestion administrative
    public List<Utilisateur> getAllUsers() {
        return utilisateurDao.findAll();
    }

    public boolean toggleAccountStatus(int userId) {
        Optional<Utilisateur> optUser = utilisateurDao.findById(userId);
        if (optUser.isEmpty()) return false;

        Utilisateur user = optUser.get();
        StatutCompte newStatus = (user.getStatutCompte() == StatutCompte.ACTIF) ? StatutCompte.DESACTIVE : StatutCompte.ACTIF;
        return utilisateurDao.updateStatutCompte(userId, newStatus.name());
    }

    public boolean updateUserRoleAndService(int userId, Role newRole, Integer newServiceId) {
        Optional<Utilisateur> optUser = utilisateurDao.findById(userId);
        if (optUser.isEmpty()) return false;

        Utilisateur user = optUser.get();
        user.setRole(newRole);
        user.setIdService(newServiceId);
        return utilisateurDao.update(user);
    }
}
