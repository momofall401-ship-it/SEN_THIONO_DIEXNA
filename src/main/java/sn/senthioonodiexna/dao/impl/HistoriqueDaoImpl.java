package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.HistoriqueDao;
import sn.senthioonodiexna.model.Historique;
import sn.senthioonodiexna.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueDaoImpl implements HistoriqueDao {

    private static final Logger logger = LoggerFactory.getLogger(HistoriqueDaoImpl.class);

    @Override
    public Historique create(Historique h) {
        String sql = "INSERT INTO historique (ancien_statut, nouveau_statut, commentaire, date_action, id_dossier, id_utilisateur) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getAncienStatut());
            ps.setString(2, h.getNouveauStatut());
            ps.setString(3, h.getCommentaire());
            ps.setTimestamp(4, Timestamp.valueOf(h.getDateAction() != null ? h.getDateAction() : LocalDateTime.now()));
            ps.setInt(5, h.getIdDossier());
            ps.setInt(6, h.getIdUtilisateur());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setId(rs.getInt(1));
                }
            }
            return h;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'historique : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Historique> findByDossier(int idDossier) {
        List<Historique> list = new ArrayList<>();
        String sql = "SELECT h.*, u.nom, u.prenom, u.role " +
                     "FROM historique h " +
                     "JOIN utilisateur u ON h.id_utilisateur = u.id " +
                     "WHERE h.id_dossier = ? ORDER BY h.date_action ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDossier);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Historique h = new Historique();
                    h.setId(rs.getInt("id"));
                    h.setAncienStatut(rs.getString("ancien_statut"));
                    h.setNouveauStatut(rs.getString("nouveau_statut"));
                    h.setCommentaire(rs.getString("commentaire"));
                    if (rs.getTimestamp("date_action") != null) {
                        h.setDateAction(rs.getTimestamp("date_action").toLocalDateTime());
                    }
                    h.setIdDossier(rs.getInt("id_dossier"));
                    h.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    h.setNomUtilisateur((rs.getString("prenom") != null ? rs.getString("prenom") : "") + " " +
                                        (rs.getString("nom") != null ? rs.getString("nom") : ""));
                    try {
                        h.setRoleUtilisateur(Role.valueOf(rs.getString("role")));
                    } catch (Exception ignored) {}
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByDossier historique : {}", e.getMessage());
        }
        return list;
    }
}
