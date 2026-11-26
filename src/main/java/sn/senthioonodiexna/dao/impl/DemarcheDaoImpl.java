package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.DemarcheDao;
import sn.senthioonodiexna.model.Demarche;
import sn.senthioonodiexna.model.DemarchePiece;
import sn.senthioonodiexna.model.Etape;
import sn.senthioonodiexna.model.Piece;
import sn.senthioonodiexna.model.StatutPublication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemarcheDaoImpl implements DemarcheDao {

    private static final Logger logger = LoggerFactory.getLogger(DemarcheDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT d.id, d.titre, d.description, d.conditions, d.delai_estime, d.frais_indicatifs, " +
            "d.id_service, d.statut_publication, d.date_creation, d.date_mise_a_jour, " +
            "s.nom AS nom_service, s.telephone AS tel_service, s.email AS email_service, " +
            "a.nom AS nom_administration, a.adresse AS adresse_admin " +
            "FROM demarche d " +
            "JOIN service_administratif s ON d.id_service = s.id " +
            "JOIN administration a ON s.id_administration = a.id ";

    @Override
    public Demarche create(Demarche d) {
        String sql = "INSERT INTO demarche (titre, description, conditions, delai_estime, frais_indicatifs, id_service, statut_publication) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getConditions());
            ps.setString(4, d.getDelaiEstime());
            ps.setString(5, d.getFraisIndicatifs());
            ps.setInt(6, d.getIdService());
            ps.setString(7, d.getStatutPublication() != null ? d.getStatutPublication().name() : StatutPublication.PUBLIEE.name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        d.setId(rs.getInt(1));
                    }
                }
            }
            return d;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de démarche : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Demarche> findById(int id) {
        String sql = SELECT_BASE + "WHERE d.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Demarche d = mapResultSet(rs);
                    d.setEtapes(findEtapesByDemarche(d.getId()));
                    d.setPieces(findPiecesByDemarche(d.getId()));
                    return Optional.of(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById démarche : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Demarche> findAll() {
        List<Demarche> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY d.titre";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Demarche d = mapResultSet(rs);
                d.setEtapes(findEtapesByDemarche(d.getId()));
                d.setPieces(findPiecesByDemarche(d.getId()));
                list.add(d);
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll démarches : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Demarche> findPublished() {
        List<Demarche> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE d.statut_publication = 'PUBLIEE' ORDER BY d.titre";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Demarche d = mapResultSet(rs);
                d.setEtapes(findEtapesByDemarche(d.getId()));
                d.setPieces(findPiecesByDemarche(d.getId()));
                list.add(d);
            }
        } catch (SQLException e) {
            logger.error("Erreur findPublished démarches : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Demarche> findByService(int idService) {
        List<Demarche> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE d.id_service = ? ORDER BY d.titre";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idService);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Demarche d = mapResultSet(rs);
                    d.setEtapes(findEtapesByDemarche(d.getId()));
                    d.setPieces(findPiecesByDemarche(d.getId()));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByService démarches : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Demarche> searchPublished(String keyword, Integer idService) {
        List<Demarche> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE d.statut_publication = 'PUBLIEE' ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(d.titre) LIKE ? OR LOWER(d.description) LIKE ? OR LOWER(a.nom) LIKE ? OR LOWER(s.nom) LIKE ?) ");
            String term = "%" + keyword.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        if (idService != null && idService > 0) {
            sql.append("AND d.id_service = ? ");
            params.add(idService);
        }
        sql.append("ORDER BY d.titre");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Demarche d = mapResultSet(rs);
                    d.setEtapes(findEtapesByDemarche(d.getId()));
                    d.setPieces(findPiecesByDemarche(d.getId()));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur searchPublished démarches : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean update(Demarche d) {
        String sql = "UPDATE demarche SET titre = ?, description = ?, conditions = ?, delai_estime = ?, frais_indicatifs = ?, " +
                     "id_service = ?, statut_publication = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getTitre());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getConditions());
            ps.setString(4, d.getDelaiEstime());
            ps.setString(5, d.getFraisIndicatifs());
            ps.setInt(6, d.getIdService());
            ps.setString(7, d.getStatutPublication().name());
            ps.setInt(8, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur update démarche : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM demarche WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete démarche : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<Etape> findEtapesByDemarche(int idDemarche) {
        List<Etape> list = new ArrayList<>();
        String sql = "SELECT * FROM etape WHERE id_demarche = ? ORDER BY ordre ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDemarche);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Etape e = new Etape();
                    e.setId(rs.getInt("id"));
                    e.setTitre(rs.getString("titre"));
                    e.setDescription(rs.getString("description"));
                    e.setOrdre(rs.getInt("ordre"));
                    e.setIdDemarche(rs.getInt("id_demarche"));
                    list.add(e);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findEtapesByDemarche : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public void addEtape(Etape etape) {
        String sql = "INSERT INTO etape (titre, description, ordre, id_demarche) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, etape.getTitre());
            ps.setString(2, etape.getDescription());
            ps.setInt(3, etape.getOrdre());
            ps.setInt(4, etape.getIdDemarche());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur addEtape : {}", e.getMessage());
        }
    }

    @Override
    public void deleteEtapesByDemarche(int idDemarche) {
        String sql = "DELETE FROM etape WHERE id_demarche = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDemarche);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur deleteEtapesByDemarche : {}", e.getMessage());
        }
    }

    @Override
    public List<DemarchePiece> findPiecesByDemarche(int idDemarche) {
        List<DemarchePiece> list = new ArrayList<>();
        String sql = "SELECT dp.id_demarche, dp.id_piece, dp.obligatoire, dp.instructions, " +
                     "p.nom AS nom_piece, p.description AS desc_piece, p.formats_acceptes " +
                     "FROM demarche_piece dp " +
                     "JOIN piece p ON dp.id_piece = p.id " +
                     "WHERE dp.id_demarche = ? ORDER BY dp.obligatoire DESC, p.nom ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDemarche);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DemarchePiece dp = new DemarchePiece();
                    dp.setIdDemarche(rs.getInt("id_demarche"));
                    dp.setIdPiece(rs.getInt("id_piece"));
                    dp.setObligatoire(rs.getInt("obligatoire") == 1);
                    dp.setInstructions(rs.getString("instructions"));
                    dp.setNomPiece(rs.getString("nom_piece"));
                    dp.setDescriptionPiece(rs.getString("desc_piece"));
                    dp.setFormatsAcceptes(rs.getString("formats_acceptes"));
                    list.add(dp);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findPiecesByDemarche : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public void addDemarchePiece(DemarchePiece dp) {
        String sql = "INSERT INTO demarche_piece (id_demarche, id_piece, obligatoire, instructions) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE obligatoire = VALUES(obligatoire), instructions = VALUES(instructions)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dp.getIdDemarche());
            ps.setInt(2, dp.getIdPiece());
            ps.setInt(3, dp.isObligatoire() ? 1 : 0);
            ps.setString(4, dp.getInstructions());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur addDemarchePiece : {}", e.getMessage());
        }
    }

    @Override
    public void deleteDemarchePieces(int idDemarche) {
        String sql = "DELETE FROM demarche_piece WHERE id_demarche = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDemarche);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur deleteDemarchePieces : {}", e.getMessage());
        }
    }

    @Override
    public List<Piece> findAllPieces() {
        List<Piece> list = new ArrayList<>();
        String sql = "SELECT * FROM piece ORDER BY nom";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Piece p = new Piece();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setDescription(rs.getString("description"));
                p.setFormatsAcceptes(rs.getString("formats_acceptes"));
                list.add(p);
            }
        } catch (SQLException e) {
            logger.error("Erreur findAllPieces : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public Piece createPiece(Piece p) {
        String sql = "INSERT INTO piece (nom, description, formats_acceptes) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getFormatsAcceptes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
            return p;
        } catch (SQLException e) {
            logger.error("Erreur createPiece : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Demarche mapResultSet(ResultSet rs) throws SQLException {
        Demarche d = new Demarche();
        d.setId(rs.getInt("id"));
        d.setTitre(rs.getString("titre"));
        d.setDescription(rs.getString("description"));
        d.setConditions(rs.getString("conditions"));
        d.setDelaiEstime(rs.getString("delai_estime"));
        d.setFraisIndicatifs(rs.getString("frais_indicatifs"));
        d.setIdService(rs.getInt("id_service"));
        try {
            d.setStatutPublication(StatutPublication.valueOf(rs.getString("statut_publication")));
        } catch (Exception e) {
            d.setStatutPublication(StatutPublication.PUBLIEE);
        }
        if (rs.getTimestamp("date_creation") != null) {
            d.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        }
        if (rs.getTimestamp("date_mise_a_jour") != null) {
            d.setDateMiseAJour(rs.getTimestamp("date_mise_a_jour").toLocalDateTime());
        }

        d.setNomService(rs.getString("nom_service"));
        d.setTelephoneService(rs.getString("tel_service"));
        d.setEmailService(rs.getString("email_service"));
        d.setNomAdministration(rs.getString("nom_administration"));
        d.setAdresseAdministration(rs.getString("adresse_admin"));

        return d;
    }
}
