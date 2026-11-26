package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.FaqDao;
import sn.senthioonodiexna.model.Faq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FaqDaoImpl implements FaqDao {

    private static final Logger logger = LoggerFactory.getLogger(FaqDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT f.*, s.nom AS nom_service FROM faq f " +
            "LEFT JOIN service_administratif s ON f.id_service = s.id ";

    @Override
    public Faq create(Faq faq) {
        String sql = "INSERT INTO faq (question, reponse, publiee, id_service, ordre) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, faq.getQuestion());
            ps.setString(2, faq.getReponse());
            ps.setInt(3, faq.isPubliee() ? 1 : 0);
            if (faq.getIdService() != null && faq.getIdService() > 0) {
                ps.setInt(4, faq.getIdService());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, faq.getOrdre());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    faq.setId(rs.getInt(1));
                }
            }
            return faq;
        } catch (SQLException e) {
            logger.error("Erreur create faq : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Faq> findById(int id) {
        String sql = SELECT_BASE + "WHERE f.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById faq : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Faq> findAll() {
        List<Faq> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY f.ordre ASC, f.id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll faq : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Faq> findPublished() {
        List<Faq> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE f.publiee = 1 ORDER BY f.ordre ASC, f.id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur findPublished faq : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Faq> search(String keyword, Integer idService) {
        List<Faq> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE f.publiee = 1 ");

        List<Object> params = new ArrayList<>();
        if (idService != null && idService > 0) {
            sql.append("AND (f.id_service = ? OR f.id_service IS NULL) ");
            params.add(idService);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(f.question) LIKE ? OR LOWER(f.reponse) LIKE ?) ");
            String term = "%" + keyword.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
        }
        sql.append("ORDER BY f.ordre ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur search faq : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean update(Faq faq) {
        String sql = "UPDATE faq SET question = ?, reponse = ?, publiee = ?, id_service = ?, ordre = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, faq.getQuestion());
            ps.setString(2, faq.getReponse());
            ps.setInt(3, faq.isPubliee() ? 1 : 0);
            if (faq.getIdService() != null && faq.getIdService() > 0) {
                ps.setInt(4, faq.getIdService());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, faq.getOrdre());
            ps.setInt(6, faq.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur update faq : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM faq WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete faq : {}", e.getMessage());
            return false;
        }
    }

    private Faq mapResultSet(ResultSet rs) throws SQLException {
        Faq f = new Faq();
        f.setId(rs.getInt("id"));
        f.setQuestion(rs.getString("question"));
        f.setReponse(rs.getString("reponse"));
        f.setPubliee(rs.getInt("publiee") == 1);
        int idService = rs.getInt("id_service");
        if (!rs.wasNull()) {
            f.setIdService(idService);
        }
        f.setOrdre(rs.getInt("ordre"));
        if (rs.getTimestamp("date_creation") != null) {
            f.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        }
        f.setNomService(rs.getString("nom_service"));
        return f;
    }
}
