package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.NotificationDao;
import sn.senthioonodiexna.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDaoImpl implements NotificationDao {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDaoImpl.class);

    @Override
    public Notification create(Notification n) {
        String sql = "INSERT INTO notification (titre, message, date_creation, est_lue, id_utilisateur, id_dossier) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitre());
            ps.setString(2, n.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(n.getDateCreation() != null ? n.getDateCreation() : LocalDateTime.now()));
            ps.setInt(4, n.isEstLue() ? 1 : 0);
            ps.setInt(5, n.getIdUtilisateur());
            if (n.getIdDossier() != null && n.getIdDossier() > 0) {
                ps.setInt(6, n.getIdDossier());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    n.setId(rs.getInt(1));
                }
            }
            return n;
        } catch (SQLException e) {
            logger.error("Erreur create notification : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Notification> findByUtilisateur(int idUtilisateur) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, d.numero_suivi FROM notification n " +
                     "LEFT JOIN dossier d ON n.id_dossier = d.id " +
                     "WHERE n.id_utilisateur = ? ORDER BY n.date_creation DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setTitre(rs.getString("titre"));
                    n.setMessage(rs.getString("message"));
                    if (rs.getTimestamp("date_creation") != null) {
                        n.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                    }
                    n.setEstLue(rs.getInt("est_lue") == 1);
                    n.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    int idDossier = rs.getInt("id_dossier");
                    if (!rs.wasNull()) {
                        n.setIdDossier(idDossier);
                    }
                    n.setNumeroSuiviDossier(rs.getString("numero_suivi"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByUtilisateur notifications : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public int countUnreadByUtilisateur(int idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM notification WHERE id_utilisateur = ? AND est_lue = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur countUnread notifications : {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean markAsRead(int idNotification) {
        String sql = "UPDATE notification SET est_lue = 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotification);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur markAsRead notification : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(int idUtilisateur) {
        String sql = "UPDATE notification SET est_lue = 1 WHERE id_utilisateur = ? AND est_lue = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Erreur markAllAsRead : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int idNotification) {
        String sql = "DELETE FROM notification WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotification);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete notification : {}", e.getMessage());
            return false;
        }
    }
}
