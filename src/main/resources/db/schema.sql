-- =====================================================================
-- APPLICATION : SEN THIOONO DIEXNA
-- BASE DE DONNÉES : sen_thioono_diexna
-- SCRIPT DE CRÉATION DU SCHÉMA RELATIONNEL (MySQL 8 / 9)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `sen_thioono_diexna`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `sen_thioono_diexna`;

-- 1. Table ADMINISTRATION
CREATE TABLE IF NOT EXISTS `administration` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nom` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `adresse` VARCHAR(255),
    `telephone` VARCHAR(30),
    `email` VARCHAR(100),
    `site_web` VARCHAR(150),
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table SERVICE_ADMINISTRATIF
CREATE TABLE IF NOT EXISTS `service_administratif` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nom` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `telephone` VARCHAR(30),
    `email` VARCHAR(100),
    `id_administration` INT NOT NULL,
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_service_admin` FOREIGN KEY (`id_administration`)
        REFERENCES `administration` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table UTILISATEUR
CREATE TABLE IF NOT EXISTS `utilisateur` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nom` VARCHAR(100) NOT NULL,
    `prenom` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `telephone` VARCHAR(30),
    `mot_de_passe_hash` VARCHAR(255) NOT NULL,
    `role` ENUM('CITOYEN', 'AGENT', 'RESPONSABLE', 'ADMIN') NOT NULL DEFAULT 'CITOYEN',
    `statut_compte` ENUM('ACTIF', 'DESACTIVE') NOT NULL DEFAULT 'ACTIF',
    `id_service` INT NULL,
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_service` FOREIGN KEY (`id_service`)
        REFERENCES `service_administratif` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table DEMARCHE
CREATE TABLE IF NOT EXISTS `demarche` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `titre` VARCHAR(200) NOT NULL,
    `description` TEXT NOT NULL,
    `conditions` TEXT,
    `delai_estime` VARCHAR(100),
    `frais_indicatifs` VARCHAR(100),
    `id_service` INT NOT NULL,
    `statut_publication` ENUM('BROUILLON', 'PUBLIEE', 'ARCHIVEE') NOT NULL DEFAULT 'PUBLIEE',
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `date_mise_a_jour` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_demarche_service` FOREIGN KEY (`id_service`)
        REFERENCES `service_administratif` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Table ETAPE
CREATE TABLE IF NOT EXISTS `etape` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `titre` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `ordre` INT NOT NULL DEFAULT 1,
    `id_demarche` INT NOT NULL,
    CONSTRAINT `fk_etape_demarche` FOREIGN KEY (`id_demarche`)
        REFERENCES `demarche` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Table PIECE
CREATE TABLE IF NOT EXISTS `piece` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nom` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `formats_acceptes` VARCHAR(100) DEFAULT 'PDF, JPG, PNG'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Table DEMARCHE_PIECE (Relation N-N)
CREATE TABLE IF NOT EXISTS `demarche_piece` (
    `id_demarche` INT NOT NULL,
    `id_piece` INT NOT NULL,
    `obligatoire` TINYINT(1) NOT NULL DEFAULT 1,
    `instructions` TEXT,
    PRIMARY KEY (`id_demarche`, `id_piece`),
    CONSTRAINT `fk_dp_demarche` FOREIGN KEY (`id_demarche`)
        REFERENCES `demarche` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_dp_piece` FOREIGN KEY (`id_piece`)
        REFERENCES `piece` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Table DOSSIER
CREATE TABLE IF NOT EXISTS `dossier` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `numero_suivi` VARCHAR(50) NOT NULL UNIQUE,
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `date_depot` TIMESTAMP NULL,
    `statut` ENUM('BROUILLON', 'DEPOSE', 'EN_VERIFICATION', 'COMPLEMENT_DEMANDE', 'EN_TRAITEMENT', 'ACCEPTE', 'REJETE', 'CLOTURE') NOT NULL DEFAULT 'BROUILLON',
    `motif_rejet` TEXT NULL,
    `notes` TEXT NULL,
    `id_utilisateur` INT NOT NULL,
    `id_demarche` INT NOT NULL,
    `id_agent` INT NULL,
    CONSTRAINT `fk_dossier_user` FOREIGN KEY (`id_utilisateur`)
        REFERENCES `utilisateur` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_dossier_demarche` FOREIGN KEY (`id_demarche`)
        REFERENCES `demarche` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_dossier_agent` FOREIGN KEY (`id_agent`)
        REFERENCES `utilisateur` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Table DOCUMENT_FOURNI
CREATE TABLE IF NOT EXISTS `document_fourni` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `chemin_fichier` VARCHAR(255) NULL,
    `date_depot` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `statut_verification` ENUM('EN_ATTENTE', 'CONFORME', 'NON_CONFORME', 'A_COMPLETER') NOT NULL DEFAULT 'EN_ATTENTE',
    `commentaire` TEXT NULL,
    `id_dossier` INT NOT NULL,
    `id_piece` INT NOT NULL,
    CONSTRAINT `fk_doc_dossier` FOREIGN KEY (`id_dossier`)
        REFERENCES `dossier` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_doc_piece` FOREIGN KEY (`id_piece`)
        REFERENCES `piece` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Table HISTORIQUE
CREATE TABLE IF NOT EXISTS `historique` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `ancien_statut` VARCHAR(50) NULL,
    `nouveau_statut` VARCHAR(50) NOT NULL,
    `commentaire` TEXT NULL,
    `date_action` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `id_dossier` INT NOT NULL,
    `id_utilisateur` INT NOT NULL,
    CONSTRAINT `fk_hist_dossier` FOREIGN KEY (`id_dossier`)
        REFERENCES `dossier` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_hist_user` FOREIGN KEY (`id_utilisateur`)
        REFERENCES `utilisateur` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Table NOTIFICATION
CREATE TABLE IF NOT EXISTS `notification` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `titre` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `est_lue` TINYINT(1) NOT NULL DEFAULT 0,
    `id_utilisateur` INT NOT NULL,
    `id_dossier` INT NULL,
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`id_utilisateur`)
        REFERENCES `utilisateur` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notif_dossier` FOREIGN KEY (`id_dossier`)
        REFERENCES `dossier` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Table FAQ
CREATE TABLE IF NOT EXISTS `faq` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `question` TEXT NOT NULL,
    `reponse` TEXT NOT NULL,
    `publiee` TINYINT(1) NOT NULL DEFAULT 1,
    `id_service` INT NULL,
    `ordre` INT NOT NULL DEFAULT 1,
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_faq_service` FOREIGN KEY (`id_service`)
        REFERENCES `service_administratif` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
