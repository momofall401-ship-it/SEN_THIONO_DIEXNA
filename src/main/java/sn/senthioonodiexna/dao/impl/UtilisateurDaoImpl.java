package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.UtilisateurDao;
import sn.senthioonodiexna.model.Role;
import sn.senthioonodiexna.model.StatutCompte;
import sn.senthioonodiexna.model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilisateurDaoImpl implements UtilisateurDao {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, u.mot_de_passe_hash, u.role, u.statut_compte, " +
            "u.id_service, u.date_creation, s.nom AS nom_service " +
            "FROM utilisateur u " +
            "LEFT JOIN service_administratif s ON u.id_service = s.id ";

    @Override
    public Utilisateur create(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, telephone, mot_de_passe_hash, role, statut_compte, id_service) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail().toLowerCase().trim());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getMotDePasseHash());
            ps.setString(6, u.getRole() != null ? u.getRole().name() : Role.CITOYEN.name());
            ps.setString(7, u.getStatutCompte() != null ? u.getStatutCompte().name() : StatutCompte.ACTIF.name());
            if (u.getIdService() != null && u.getIdService() > 0) {
                ps.setInt(8, u.getIdService());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        u.setId(rs.getInt(1));
                    }
                }
            }
            return u;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'utilisateur : {}", e.getMessage());
            throw new RuntimeException("Impossible de créer l'utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Utilisateur> findById(int id) {
        String sql = SELECT_BASE + "WHERE u.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par ID : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE LOWER(u.email) = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par email : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY u.nom, u.prenom";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des utilisateurs : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        List<Utilisateur> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE u.role = ? ORDER BY u.nom, u.prenom";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par rôle : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Utilisateur> findByService(int idService) {
        List<Utilisateur> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE u.id_service = ? ORDER BY u.nom, u.prenom";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idService);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par service : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean update(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, telephone = ?, role = ?, statut_compte = ?, id_service = ? " +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getTelephone());
            ps.setString(4, u.getRole().name());
            ps.setString(5, u.getStatutCompte().name());
            if (u.getIdService() != null && u.getIdService() > 0) {
                ps.setInt(6, u.getIdService());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateMotDePasse(int id, String nouveauMotDePasseHash) {
        String sql = "UPDATE utilisateur SET mot_de_passe_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauMotDePasseHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors du changement de mot de passe : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatutCompte(int id, String nouveauStatut) {
        String sql = "UPDATE utilisateur SET statut_compte = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors du changement de statut de compte : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'utilisateur : {}", e.getMessage());
            return false;
        }
    }

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        try {
            u.setRole(Role.valueOf(rs.getString("role")));
        } catch (Exception e) {
            u.setRole(Role.CITOYEN);
        }
        try {
            u.setStatutCompte(StatutCompte.valueOf(rs.getString("statut_compte")));
        } catch (Exception e) {
            u.setStatutCompte(StatutCompte.ACTIF);
        }
        int idService = rs.getInt("id_service");
        if (!rs.wasNull()) {
            u.setIdService(idService);
        }
        u.setNomService(rs.getString("nom_service"));
        if (rs.getTimestamp("date_creation") != null) {
            u.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        }
        return u;
    }
}
