package sn.senthioonodiexna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.senthioonodiexna.dao.UtilisateurDao;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.StatutCompte;
import sn.senthioonodiexna.model.Utilisateur;
import sn.senthioonodiexna.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private MockUtilisateurDao mockDao;

    @BeforeEach
    void setUp() {
        mockDao = new MockUtilisateurDao();
        authService = new AuthService(mockDao);

        // Utilisateur de test actif
        Utilisateur userActif = new Utilisateur(1, "Diop", "Amadou", "amadou@test.sn", "+221 77 111 22 33",
                PasswordUtil.hashPassword("Secret@123"), Role.CITOYEN, StatutCompte.ACTIF, null);
        mockDao.users.add(userActif);

        // Utilisateur désactivé (RG11)
        Utilisateur userDesactive = new Utilisateur(2, "Sow", "Fatou", "fatou@test.sn", "+221 77 222 33 44",
                PasswordUtil.hashPassword("Secret@123"), Role.CITOYEN, StatutCompte.DESACTIVE, null);
        mockDao.users.add(userDesactive);
    }

    @Test
    @DisplayName("Connexion réussie avec identifiants valides")
    void testLoginSuccess() {
        Utilisateur logged = authService.login("amadou@test.sn", "Secret@123");
        assertNotNull(logged);
        assertEquals("amadou@test.sn", logged.getEmail());
    }

    @Test
    @DisplayName("Échec connexion avec mauvais mot de passe")
    void testLoginWrongPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login("amadou@test.sn", "MauvaisMotDePasse");
        });
    }

    @Test
    @DisplayName("RG11: Blocage de connexion pour un compte désactivé")
    void testLoginDeactivatedAccount() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            authService.login("fatou@test.sn", "Secret@123");
        });
        assertTrue(ex.getMessage().contains("désactivé"));
    }

    @Test
    @DisplayName("RG02: Refus d'inscription avec email déjà existant")
    void testRegisterDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("Diop", "Moussa", "amadou@test.sn", "+221 77 333 44 55", "Secret@123");
        });
    }

    // Mock DAO pour les tests unitaires isolés
    static class MockUtilisateurDao implements UtilisateurDao {
        List<Utilisateur> users = new ArrayList<>();

        @Override
        public Utilisateur create(Utilisateur u) {
            u.setId(users.size() + 1);
            users.add(u);
            return u;
        }

        @Override
        public Optional<Utilisateur> findById(int id) {
            return users.stream().filter(u -> u.getId() == id).findFirst();
        }

        @Override
        public Optional<Utilisateur> findByEmail(String email) {
            return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
        }

        @Override public List<Utilisateur> findAll() { return users; }
        @Override public List<Utilisateur> findByRole(Role role) { return List.of(); }
        @Override public List<Utilisateur> findByService(int idService) { return List.of(); }
        @Override public boolean update(Utilisateur utilisateur) { return true; }
        @Override public boolean updateMotDePasse(int id, String nouveauMotDePasseHash) { return true; }
        @Override public boolean updateStatutCompte(int id, String nouveauStatut) { return true; }
        @Override public boolean delete(int id) { return true; }
    }
}
