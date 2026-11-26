package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.AdministrationDao;
import sn.senthioonodiexna.model.Administration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdministrationDaoImpl implements AdministrationDao {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationDaoImpl.class);

    @Override
    public Administration create(Administration a) {
        String sql = "INSERT INTO administration (nom, description, adresse, telephone, email, site_web) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getAdresse());
            ps.setString(4, a.getTelephone());
            ps.setString(5, a.getEmail());
            ps.setString(6, a.getSiteWeb());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        a.setId(rs.getInt(1));
                    }
                }
            }
            return a;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création d'administration : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Administration> findById(int id) {
        String sql = "SELECT * FROM administration WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById administration : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Administration> findAll() {
        List<Administration> list = new ArrayList<>();
        String sql = "SELECT * FROM administration ORDER BY nom";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll administrations : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean update(Administration a) {
        String sql = "UPDATE administration SET nom = ?, description = ?, adresse = ?, telephone = ?, email = ?, site_web = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getAdresse());
            ps.setString(4, a.getTelephone());
            ps.setString(5, a.getEmail());
            ps.setString(6, a.getSiteWeb());
            ps.setInt(7, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur update administration : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM administration WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete administration : {}", e.getMessage());
            return false;
        }
    }

    private Administration mapResultSet(ResultSet rs) throws SQLException {
        Administration a = new Administration();
        a.setId(rs.getInt("id"));
        a.setNom(rs.getString("nom"));
        a.setDescription(rs.getString("description"));
        a.setAdresse(rs.getString("adresse"));
        a.setTelephone(rs.getString("telephone"));
        a.setEmail(rs.getString("email"));
        a.setSiteWeb(rs.getString("site_web"));
        if (rs.getTimestamp("date_creation") != null) {
            a.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        }
        return a;
    }
}
