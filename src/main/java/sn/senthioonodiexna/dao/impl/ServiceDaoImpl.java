package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.ServiceDao;
import sn.senthioonodiexna.model.ServiceAdministratif;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceDaoImpl implements ServiceDao {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT s.id, s.nom, s.description, s.telephone, s.email, s.id_administration, s.date_creation, " +
            "a.nom AS nom_administration " +
            "FROM service_administratif s " +
            "JOIN administration a ON s.id_administration = a.id ";

    @Override
    public ServiceAdministratif create(ServiceAdministratif s) {
        String sql = "INSERT INTO service_administratif (nom, description, telephone, email, id_administration) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getNom());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getTelephone());
            ps.setString(4, s.getEmail());
            ps.setInt(5, s.getIdAdministration());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setId(rs.getInt(1));
                    }
                }
            }
            return s;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création du service : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ServiceAdministratif> findById(int id) {
        String sql = SELECT_BASE + "WHERE s.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById service : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<ServiceAdministratif> findAll() {
        List<ServiceAdministratif> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.nom, s.nom";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll services : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<ServiceAdministratif> findByAdministration(int idAdministration) {
        List<ServiceAdministratif> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE s.id_administration = ? ORDER BY s.nom";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAdministration);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByAdministration : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean update(ServiceAdministratif s) {
        String sql = "UPDATE service_administratif SET nom = ?, description = ?, telephone = ?, email = ?, id_administration = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getNom());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getTelephone());
            ps.setString(4, s.getEmail());
            ps.setInt(5, s.getIdAdministration());
            ps.setInt(6, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur update service : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM service_administratif WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete service : {}", e.getMessage());
            return false;
        }
    }

    private ServiceAdministratif mapResultSet(ResultSet rs) throws SQLException {
        ServiceAdministratif s = new ServiceAdministratif();
        s.setId(rs.getInt("id"));
        s.setNom(rs.getString("nom"));
        s.setDescription(rs.getString("description"));
        s.setTelephone(rs.getString("telephone"));
        s.setEmail(rs.getString("email"));
        s.setIdAdministration(rs.getInt("id_administration"));
        s.setNomAdministration(rs.getString("nom_administration"));
        if (rs.getTimestamp("date_creation") != null) {
            s.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        }
        return s;
    }
}
