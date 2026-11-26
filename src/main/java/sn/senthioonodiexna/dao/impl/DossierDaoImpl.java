package sn.senthioonodiexna.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.senthioonodiexna.config.DatabaseConfig;
import sn.senthioonodiexna.dao.DossierDao;
import sn.senthioonodiexna.model.DocumentFourni;
import sn.senthioonodiexna.model.Dossier;
import sn.senthioonodiexna.model.StatutDossier;
import sn.senthioonodiexna.model.StatutVerification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DossierDaoImpl implements DossierDao {

    private static final Logger logger = LoggerFactory.getLogger(DossierDaoImpl.class);

    private static final String SELECT_BASE =
            "SELECT d.id, d.numero_suivi, d.date_creation, d.date_depot, d.statut, d.motif_rejet, d.notes, " +
            "d.id_utilisateur, d.id_demarche, d.id_agent, " +
            "u.nom AS nom_citoyen, u.prenom AS prenom_citoyen, u.email AS email_citoyen, u.telephone AS tel_citoyen, " +
            "dm.titre AS titre_demarche, dm.id_service, " +
            "s.nom AS nom_service, a.nom AS nom_administration, " +
            "ag.nom AS nom_agent, ag.prenom AS prenom_agent " +
            "FROM dossier d " +
            "JOIN utilisateur u ON d.id_utilisateur = u.id " +
            "JOIN demarche dm ON d.id_demarche = dm.id " +
            "JOIN service_administratif s ON dm.id_service = s.id " +
            "JOIN administration a ON s.id_administration = a.id " +
            "LEFT JOIN utilisateur ag ON d.id_agent = ag.id ";

    @Override
    public Dossier create(Dossier d) {
        String sql = "INSERT INTO dossier (numero_suivi, date_creation, date_depot, statut, motif_rejet, notes, id_utilisateur, id_demarche, id_agent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNumeroSuivi());
            ps.setTimestamp(2, Timestamp.valueOf(d.getDateCreation() != null ? d.getDateCreation() : LocalDateTime.now()));
            if (d.getDateDepot() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(d.getDateDepot()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setString(4, d.getStatut() != null ? d.getStatut().name() : StatutDossier.BROUILLON.name());
            ps.setString(5, d.getMotifRejet());
            ps.setString(6, d.getNotes());
            ps.setInt(7, d.getIdUtilisateur());
            ps.setInt(8, d.getIdDemarche());
            if (d.getIdAgent() != null && d.getIdAgent() > 0) {
                ps.setInt(9, d.getIdAgent());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

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
            logger.error("Erreur lors de la création de dossier : {}", e.getMessage());
            throw new RuntimeException("Erreur création dossier: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Dossier> findById(int id) {
        String sql = SELECT_BASE + "WHERE d.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dossier d = mapResultSet(rs);
                    d.setDocuments(findDocumentsByDossier(d.getId()));
                    return Optional.of(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findById dossier : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Dossier> findByNumeroSuivi(String numeroSuivi) {
        if (numeroSuivi == null) return Optional.empty();
        String sql = SELECT_BASE + "WHERE UPPER(d.numero_suivi) = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroSuivi.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dossier d = mapResultSet(rs);
                    d.setDocuments(findDocumentsByDossier(d.getId()));
                    return Optional.of(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByNumeroSuivi : {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Dossier> findAll() {
        List<Dossier> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY d.date_creation DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Dossier d = mapResultSet(rs);
                d.setDocuments(findDocumentsByDossier(d.getId()));
                list.add(d);
            }
        } catch (SQLException e) {
            logger.error("Erreur findAll dossiers : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Dossier> findByUtilisateur(int idUtilisateur) {
        List<Dossier> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE d.id_utilisateur = ? ORDER BY d.date_creation DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dossier d = mapResultSet(rs);
                    d.setDocuments(findDocumentsByDossier(d.getId()));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByUtilisateur dossiers : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Dossier> findByService(int idService) {
        List<Dossier> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE dm.id_service = ? ORDER BY d.date_creation DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idService);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dossier d = mapResultSet(rs);
                    d.setDocuments(findDocumentsByDossier(d.getId()));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findByService dossiers : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public List<Dossier> search(String query, Integer idService, StatutDossier statut) {
        List<Dossier> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (idService != null && idService > 0) {
            sql.append("AND dm.id_service = ? ");
            params.add(idService);
        }
        if (statut != null) {
            sql.append("AND d.statut = ? ");
            params.add(statut.name());
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(d.numero_suivi) LIKE ? OR LOWER(u.nom) LIKE ? OR LOWER(u.prenom) LIKE ? OR LOWER(dm.titre) LIKE ?) ");
            String term = "%" + query.toLowerCase().trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        sql.append("ORDER BY d.date_creation DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dossier d = mapResultSet(rs);
                    d.setDocuments(findDocumentsByDossier(d.getId()));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur search dossiers : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateStatut(int idDossier, StatutDossier nouveauStatut, String motifRejet, Integer idAgent) {
        String sql = "UPDATE dossier SET statut = ?, motif_rejet = ?, id_agent = ? " +
                     (nouveauStatut == StatutDossier.DEPOSE ? ", date_depot = CURRENT_TIMESTAMP " : " ") +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut.name());
            ps.setString(2, motifRejet);
            if (idAgent != null && idAgent > 0) {
                ps.setInt(3, idAgent);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, idDossier);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur updateStatut dossier : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Dossier d) {
        String sql = "UPDATE dossier SET statut = ?, motif_rejet = ?, notes = ?, id_agent = ?, date_depot = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getStatut().name());
            ps.setString(2, d.getMotifRejet());
            ps.setString(3, d.getNotes());
            if (d.getIdAgent() != null && d.getIdAgent() > 0) {
                ps.setInt(4, d.getIdAgent());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (d.getDateDepot() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(d.getDateDepot()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setInt(6, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur update dossier : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM dossier WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur delete dossier : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public DocumentFourni addDocumentFourni(DocumentFourni doc) {
        String sql = "INSERT INTO document_fourni (chemin_fichier, date_depot, statut_verification, commentaire, id_dossier, id_piece) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doc.getCheminFichier());
            ps.setTimestamp(2, Timestamp.valueOf(doc.getDateDepot() != null ? doc.getDateDepot() : LocalDateTime.now()));
            ps.setString(3, doc.getStatutVerification() != null ? doc.getStatutVerification().name() : StatutVerification.EN_ATTENTE.name());
            ps.setString(4, doc.getCommentaire());
            ps.setInt(5, doc.getIdDossier());
            ps.setInt(6, doc.getIdPiece());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    doc.setId(rs.getInt(1));
                }
            }
            return doc;
        } catch (SQLException e) {
            logger.error("Erreur addDocumentFourni : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DocumentFourni> findDocumentsByDossier(int idDossier) {
        List<DocumentFourni> list = new ArrayList<>();
        String sql = "SELECT df.*, p.nom AS nom_piece, p.description AS desc_piece, dp.obligatoire " +
                     "FROM document_fourni df " +
                     "JOIN piece p ON df.id_piece = p.id " +
                     "JOIN dossier d ON df.id_dossier = d.id " +
                     "LEFT JOIN demarche_piece dp ON d.id_demarche = dp.id_demarche AND df.id_piece = dp.id_piece " +
                     "WHERE df.id_dossier = ? ORDER BY dp.obligatoire DESC, p.nom ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDossier);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentFourni doc = new DocumentFourni();
                    doc.setId(rs.getInt("id"));
                    doc.setCheminFichier(rs.getString("chemin_fichier"));
                    if (rs.getTimestamp("date_depot") != null) {
                        doc.setDateDepot(rs.getTimestamp("date_depot").toLocalDateTime());
                    }
                    try {
                        doc.setStatutVerification(StatutVerification.valueOf(rs.getString("statut_verification")));
                    } catch (Exception e) {
                        doc.setStatutVerification(StatutVerification.EN_ATTENTE);
                    }
                    doc.setCommentaire(rs.getString("commentaire"));
                    doc.setIdDossier(rs.getInt("id_dossier"));
                    doc.setIdPiece(rs.getInt("id_piece"));
                    doc.setNomPiece(rs.getString("nom_piece"));
                    doc.setDescriptionPiece(rs.getString("desc_piece"));
                    doc.setObligatoire(rs.getInt("obligatoire") == 1);
                    list.add(doc);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur findDocumentsByDossier : {}", e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateDocumentVerification(int idDoc, StatutVerification statut, String commentaire) {
        String sql = "UPDATE document_fourni SET statut_verification = ?, commentaire = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setString(2, commentaire);
            ps.setInt(3, idDoc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur updateDocumentVerification : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM dossier";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Erreur countTotal : {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public int countByService(int idService) {
        String sql = "SELECT COUNT(*) FROM dossier d JOIN demarche dm ON d.id_demarche = dm.id WHERE dm.id_service = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idService);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur countByService : {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public Map<StatutDossier, Integer> countByStatut(Integer idService) {
        Map<StatutDossier, Integer> map = new EnumMap<>(StatutDossier.class);
        for (StatutDossier s : StatutDossier.values()) {
            map.put(s, 0);
        }

        StringBuilder sql = new StringBuilder("SELECT d.statut, COUNT(*) FROM dossier d ");
        if (idService != null && idService > 0) {
            sql.append("JOIN demarche dm ON d.id_demarche = dm.id WHERE dm.id_service = ? ");
        }
        sql.append("GROUP BY d.statut");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (idService != null && idService > 0) {
                ps.setInt(1, idService);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        StatutDossier st = StatutDossier.valueOf(rs.getString(1));
                        map.put(st, rs.getInt(2));
                    } catch (Exception ignored) {}
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur countByStatut : {}", e.getMessage());
        }
        return map;
    }

    private Dossier mapResultSet(ResultSet rs) throws SQLException {
        Dossier d = new Dossier();
        d.setId(rs.getInt("id"));
        d.setNumeroSuivi(rs.getString("numero_suivi"));
        if (rs.getTimestamp("date_creation") != null) {
            d.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        }
        if (rs.getTimestamp("date_depot") != null) {
            d.setDateDepot(rs.getTimestamp("date_depot").toLocalDateTime());
        }
        try {
            d.setStatut(StatutDossier.valueOf(rs.getString("statut")));
        } catch (Exception e) {
            d.setStatut(StatutDossier.BROUILLON);
        }
        d.setMotifRejet(rs.getString("motif_rejet"));
        d.setNotes(rs.getString("notes"));
        d.setIdUtilisateur(rs.getInt("id_utilisateur"));
        d.setIdDemarche(rs.getInt("id_demarche"));

        int idAgent = rs.getInt("id_agent");
        if (!rs.wasNull()) {
            d.setIdAgent(idAgent);
        }

        d.setNomCitoyen(rs.getString("nom_citoyen"));
        d.setPrenomCitoyen(rs.getString("prenom_citoyen"));
        d.setEmailCitoyen(rs.getString("email_citoyen"));
        d.setTelephoneCitoyen(rs.getString("tel_citoyen"));

        d.setTitreDemarche(rs.getString("titre_demarche"));
        d.setIdService(rs.getInt("id_service"));
        d.setNomService(rs.getString("nom_service"));
        d.setNomAdministration(rs.getString("nom_administration"));

        String nomAgent = rs.getString("nom_agent");
        String prenomAgent = rs.getString("prenom_agent");
        if (nomAgent != null || prenomAgent != null) {
            d.setNomAgent((prenomAgent != null ? prenomAgent : "") + " " + (nomAgent != null ? nomAgent : ""));
        }

        return d;
    }
}
