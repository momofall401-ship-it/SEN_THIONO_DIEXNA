-- =====================================================================
-- JEU DE DONNÉES INITIAL (SEED DATA) — SEN THIOONO DIEXNA
-- Démarches administratives réelles de la République du Sénégal
-- =====================================================================

USE `sen_thioono_diexna`;

-- 1. ADMINISTRATIONS
INSERT INTO `administration` (`id`, `nom`, `description`, `adresse`, `telephone`, `email`, `site_web`) VALUES
(1, 'Ministère de la Justice', 'Gestion des juridictions, des registres judiciaires et des actes de nationalité.', 'Avenue Jean Jaurès x Rue Le Dantec, Dakar', '+221 33 823 10 50', 'contact@justice.sec.gouv.sn', 'https://www.justice.gouv.sn'),
(2, 'Ministère de l\'Intérieur', 'Sécurité publique, délivrance des passeports et gestion de l\'identité nationale.', 'Place Washington, Dakar', '+221 33 889 91 00', 'contact@interieur.sec.gouv.sn', 'https://interieur.sec.gouv.sn'),
(3, 'Ministère des Infrastructures et des Transports', 'Régulation des transports terrestres, délivrance des permis de conduire et cartes grises.', 'Diamniadio, Sphère Ministérielle Habib Thiam', '+221 33 879 20 00', 'transports@mitt.gouv.sn', 'https://transports.gouv.sn'),
(4, 'Mairie de la Ville de Dakar', 'Gestion des centres d\'état civil et des services municipaux de proximité.', 'Hôtel de Ville de Dakar, Boulevard Djily Mbaye', '+221 33 849 05 55', 'contact@villededakar.sn', 'https://villededakar.sn');

-- 2. SERVICES ADMINISTRATIFS
INSERT INTO `service_administratif` (`id`, `nom`, `description`, `telephone`, `email`, `id_administration`) VALUES
(1, 'Greffe du Tribunal d\'Instance de Dakar', 'Délivrance des extraits de casier judiciaire et certificats de nationalité.', '+221 33 821 12 45', 'tribunal.dakar@justice.sn', 1),
(2, 'Direction de la Police de l\'Air et des Frontières (DPAF)', 'Service d\'enrôlement et de délivrance des passeports biométriques.', '+221 33 869 30 00', 'dpaf.passeport@interieur.sn', 2),
(3, 'Direction de l\'Automatisation des Fichiers (DAF)', 'Production et distribution de la Carte Nationale d\'Identité CEDEAO.', '+221 33 864 80 00', 'daf@interieur.sn', 2),
(4, 'Division Régionale des Transports Terrestres', 'Examens et délivrance des permis de conduire et titres de transport.', '+221 33 832 54 10', 'permis.dakar@transports.sn', 3),
(5, 'Centre Principal d\'État Civil de Dakar', 'Enregistrement des naissances, mariages, décès et jugements d\'état civil.', '+221 33 822 41 90', 'etatcivil@villededakar.sn', 4);

-- 3. UTILISATEURS (Mots de passe hachés avec BCrypt)
-- admin@senthioono.sn / Admin@2026
-- responsable.dpaf@senthioono.sn / Pass@2026
-- agent.dpaf@senthioono.sn / Agent@2026
-- agent.justice@senthioono.sn / Agent@2026
-- citoyen@gmail.com / Citoyen@2026
INSERT INTO `utilisateur` (`id`, `nom`, `prenom`, `email`, `telephone`, `mot_de_passe_hash`, `role`, `statut_compte`, `id_service`) VALUES
(1, 'Diop', 'Amadou', 'admin@senthioono.sn', '+221 77 100 20 30', '$2a$10$Lj34TbC.0JcAicskX9m6IOQuQTbWYJZ1bdm8rBp7rq1M4VQaPPm62', 'ADMIN', 'ACTIF', NULL),
(2, 'Ndiaye', 'Fatou', 'responsable.dpaf@senthioono.sn', '+221 77 200 30 40', '$2a$10$0hBqNnewyivky.8XVSBCZu7ZwpQUNQOTBZGwyizLKNmYJZasd7YEy', 'RESPONSABLE', 'ACTIF', 2),
(3, 'Fall', 'Moussa', 'agent.dpaf@senthioono.sn', '+221 78 300 40 50', '$2a$10$x979zkjGAJwuWPGELtm/3O84CCvHZAnirVR.0VAvuKHpouYXyi3Hy', 'AGENT', 'ACTIF', 2),
(4, 'Sow', 'Aïssatou', 'agent.justice@senthioono.sn', '+221 76 400 50 60', '$2a$10$x979zkjGAJwuWPGELtm/3O84CCvHZAnirVR.0VAvuKHpouYXyi3Hy', 'AGENT', 'ACTIF', 1),
(5, 'Ba', 'Ousmane', 'citoyen@gmail.com', '+221 77 555 12 34', '$2a$10$ptnNV49/qwUZ1oh6rg9lTe0KtMedLo4Vnnb.d8QUa0cMiRk/0IgTu', 'CITOYEN', 'ACTIF', NULL);

-- 4. PIÈCES REQUISES
INSERT INTO `piece` (`id`, `nom`, `description`, `formats_acceptes`) VALUES
(1, 'Copie littérale d\'acte de naissance', 'Datant de moins de trois (3) mois, délivrée par le centre d\'état civil de naissance.', 'PDF, JPG, PNG'),
(2, 'Carte Nationale d\'Identité Biométrique CEDEAO', 'Copie recto-verso lisible de la carte d\'identité en cours de validité.', 'PDF, JPG, PNG'),
(3, 'Certificat de nationalité sénégalaise', 'Délivré par le tribunal d\'instance compétent.', 'PDF, JPG'),
(4, 'Quittance de paiement des droits de timbre', 'Reçu de paiement délivré par le Trésor Public ou la plateforme habilitée.', 'PDF, JPG'),
(5, 'Deux (2) photos d\'identité couleur récentes', 'Format 4x4 ou 3.5x4.5 sur fond blanc neutre, visage découvert.', 'JPG, PNG'),
(6, 'Certificat de résidence', 'Délivré par le commissariat de police ou la brigade de gendarmerie du domicile.', 'PDF, JPG'),
(7, 'Certificat médical d\'aptitude physique et visuelle', 'Établi par un médecin agréé pour la conduite automobile.', 'PDF, JPG'),
(8, 'Ancien passeport (le cas échéant)', 'En cas de renouvellement de passeport.', 'PDF, JPG');

-- 5. DÉMARCHES ADMINISTRATIVES
INSERT INTO `demarche` (`id`, `titre`, `description`, `conditions`, `delai_estime`, `frais_indicatifs`, `id_service`, `statut_publication`) VALUES
(1, 'Extrait de Casier Judiciaire (Bulletin n°3)',
 'Le casier judiciaire atteste de l\'absence ou de l\'existence de condamnations pénales. Le bulletin n°3 ne mentionne que les condamnations les plus graves et peut être demandé pour un concours, un emploi ou un visa.',
 'Être de nationalité sénégalaise ou résider au Sénégal. La demande doit être introduite auprès du tribunal d\'instance du lieu de naissance.',
 '24 à 48 heures', '500 FCFA (timbre fiscal)', 1, 'PUBLIEE'),

(2, 'Passeport biométrique ordinaire',
 'Document de voyage officiel délivré aux citoyens sénégalais pour voyager à l\'étranger. Sa validité est de 5 ans.',
 'Être de nationalité sénégalaise. Présence physique obligatoire le jour de l\'enrôlement biométrique (empreintes et photo).',
 '5 à 10 jours ouvrés', '20 500 FCFA', 2, 'PUBLIEE'),

(3, 'Carte Nationale d\'Identité Biométrique CEDEAO',
 'Titre d\'identité sécurisé servant également de document de voyage dans tout l\'espace CEDEAO. Validité : 10 ans.',
 'Avoir au moins 5 ans (obligatoire dès 15 ans). Présence physique requise lors de l\'enrôlement.',
 '15 à 30 jours', 'Gratuit (première demande) / 10 000 FCFA (duplicata)', 3, 'PUBLIEE'),

(4, 'Permis de conduire sénégalais (Catégorie B)',
 'Autorisation de conduire des véhicules automobiles légers de transport de personnes ou de marchandises (poids < 3,5 tonnes).',
 'Avoir au moins 18 ans révolus. Avoir réussi les examens théorique (Code) et pratique (Conduite).',
 '1 à 2 semaines après validation', '10 000 FCFA (droits de chancellerie et examen)', 4, 'PUBLIEE'),

(5, 'Certificat de nationalité sénégalaise',
 'Acte officiel délivré par le Président du Tribunal d\'Instance attestant de la possession de la nationalité sénégalaise.',
 'Être né de père ou mère sénégalais(e) ou avoir acquis la nationalité par décret/mariage.',
 '48 à 72 heures', '1 500 FCFA', 1, 'PUBLIEE');

-- 6. DEMARCHE_PIECE (Association Démarche - Pièces)
-- Casier Judiciaire (1)
INSERT INTO `demarche_piece` (`id_demarche`, `id_piece`, `obligatoire`, `instructions`) VALUES
(1, 1, 1, 'Acte de naissance de moins de 3 mois.'),
(1, 2, 1, 'Photocopie légalisée de la CNI CEDEAO.'),
(1, 4, 1, 'Timbre fiscal de 500 FCFA.');

-- Passeport Biométrique (2)
INSERT INTO `demarche_piece` (`id_demarche`, `id_piece`, `obligatoire`, `instructions`) VALUES
(2, 2, 1, 'Original et photocopie de la CNI CEDEAO.'),
(2, 1, 1, 'Acte de naissance légalisé datant de moins de 3 mois.'),
(2, 4, 1, 'Quittance de paiement de 20 000 FCFA + 500 FCFA de frais de quittance.'),
(2, 5, 1, 'Photos récentes sur fond blanc conformes aux normes ICAO.'),
(2, 8, 0, 'Obligatoire uniquement en cas de renouvellement.');

-- Carte d'Identité CEDEAO (3)
INSERT INTO `demarche_piece` (`id_demarche`, `id_piece`, `obligatoire`, `instructions`) VALUES
(3, 1, 1, 'Original de l\'acte de naissance ou certificat de nationalité.'),
(3, 6, 1, 'Certificat de résidence pour justifier de l\'adresse.');

-- Permis de conduire (4)
INSERT INTO `demarche_piece` (`id_demarche`, `id_piece`, `obligatoire`, `instructions`) VALUES
(4, 2, 1, 'Copie de la CNI en cours de validité.'),
(4, 7, 1, 'Certificat médical visuel et d\'aptitude physique.'),
(4, 5, 1, '3 photos d\'identité d\'identité récentes.'),
(4, 4, 1, 'Reçu des droits d\'examen.');

-- Certificat de nationalité (5)
INSERT INTO `demarche_piece` (`id_demarche`, `id_piece`, `obligatoire`, `instructions`) VALUES
(5, 1, 1, 'Extrait d\'acte de naissance du demandeur.'),
(5, 2, 1, 'Copie CNI du père ou de la mère.'),
(5, 4, 1, 'Timbre de chancellerie de 1 500 FCFA.');

-- 7. ÉTAPES DES DÉMARCHES
-- Casier Judiciaire
INSERT INTO `etape` (`id`, `titre`, `description`, `ordre`, `id_demarche`) VALUES
(1, 'Préparation des pièces', 'Se munir d\'un acte de naissance récent et d\'un timbre fiscal de 500 FCFA.', 1, 1),
(2, 'Dépôt au greffe', 'Déposer la demande au guichet du Tribunal d\'Instance compétent.', 2, 1),
(3, 'Traitement et vérification', 'Le greffe vérifie le casier judiciaire centralisé.', 3, 1),
(4, 'Retrait du bulletin', 'Récupération du document signé au guichet de délivrance.', 4, 1);

-- Passeport Biométrique
INSERT INTO `etape` (`id`, `titre`, `description`, `ordre`, `id_demarche`) VALUES
(5, 'Achat de la quittance', 'Payer les droits de timbre (20 000 FCFA) à la banque ou en ligne.', 1, 2),
(6, 'Constitution du dossier', 'Rassembler CNI, acte de naissance et photos réglementaires.', 2, 2),
(7, 'Enrôlement biométrique', 'Se présenter au centre DPAF pour la prise d\'empreintes et photo.', 3, 2),
(8, 'Production et délivrance', 'Notification de disponibilité et retrait du passeport.', 4, 2);

-- 8. DOSSIERS DE DÉMONSTRATION
INSERT INTO `dossier` (`id`, `numero_suivi`, `date_creation`, `date_depot`, `statut`, `motif_rejet`, `notes`, `id_utilisateur`, `id_demarche`, `id_agent`) VALUES
(1, 'STD-2026-00001', '2026-11-20 09:30:00', '2026-11-20 10:15:00', 'ACCEPTE', NULL, 'Dossier complet, timbre validé.', 5, 1, 4),
(2, 'STD-2026-00002', '2026-11-22 14:00:00', '2026-11-22 15:30:00', 'COMPLEMENT_DEMANDE', NULL, 'Photo floue et acte de naissance de plus de 3 mois.', 5, 2, 3),
(3, 'STD-2026-00003', '2026-11-24 11:00:00', '2026-11-24 11:20:00', 'EN_VERIFICATION', NULL, 'Dépôt physique enregistré au guichet.', 5, 3, NULL);

-- 9. DOCUMENTS FOURNIS ASSOCIÉS AUX DOSSIERS
INSERT INTO `document_fourni` (`id`, `chemin_fichier`, `date_depot`, `statut_verification`, `commentaire`, `id_dossier`, `id_piece`) VALUES
(1, 'acte_naissance_ob.pdf', '2026-11-20 10:15:00', 'CONFORME', 'Acte conforme et lisible.', 1, 1),
(2, 'cni_ob.pdf', '2026-11-20 10:15:00', 'CONFORME', 'CNI valide jusqu\'en 2030.', 1, 2),
(3, 'timbre_500.pdf', '2026-11-20 10:15:00', 'CONFORME', 'Timbre vérifié.', 1, 4),
(4, 'acte_ancien.pdf', '2026-11-22 15:30:00', 'NON_CONFORME', 'Acte périmé datant de 2024. Veuillez fournir un acte de moins de 3 mois.', 2, 1),
(5, 'photo_ob.jpg', '2026-11-22 15:30:00', 'A_COMPLETER', 'Fond non blanc, visage partiellement ombré.', 2, 5);

-- 10. HISTORIQUE DES DOSSIERS
INSERT INTO `historique` (`id`, `ancien_statut`, `nouveau_statut`, `commentaire`, `date_action`, `id_dossier`, `id_utilisateur`) VALUES
(1, 'BROUILLON', 'DEPOSE', 'Dépôt du dossier par l\'usager.', '2026-11-20 10:15:00', 1, 5),
(2, 'DEPOSE', 'EN_VERIFICATION', 'Prise en charge par le greffe.', '2026-11-20 11:00:00', 1, 4),
(3, 'EN_VERIFICATION', 'ACCEPTE', 'Casier vierge, document délivré au citoyen.', '2026-11-21 16:30:00', 1, 4),
(4, 'BROUILLON', 'DEPOSE', 'Dépôt de la demande de passeport.', '2026-11-22 15:30:00', 2, 5),
(5, 'DEPOSE', 'EN_VERIFICATION', 'Début de l\'instruction du dossier.', '2026-11-23 09:15:00', 2, 3),
(6, 'EN_VERIFICATION', 'COMPLEMENT_DEMANDE', 'Pièces rejetées : Acte de naissance périmé et photo non conforme.', '2026-11-23 10:30:00', 2, 3),
(7, 'BROUILLON', 'DEPOSE', 'Dossier CNI déposé en ligne.', '2026-11-24 11:20:00', 3, 5),
(8, 'DEPOSE', 'EN_VERIFICATION', 'Transmission au centre d\'enrôlement.', '2026-11-24 14:00:00', 3, 1);

-- 11. NOTIFICATIONS
INSERT INTO `notification` (`id`, `titre`, `message`, `date_creation`, `est_lue`, `id_utilisateur`, `id_dossier`) VALUES
(1, 'Dossier accepté', 'Votre demande de Casier Judiciaire (STD-2026-00001) a été acceptée. Vous pouvez retirer votre document.', '2026-11-21 16:30:00', 1, 5, 1),
(2, 'Complément de pièces requis', 'Des pièces de votre dossier de Passeport (STD-2026-00002) sont non conformes. Veuillez consulter le détail.', '2026-11-23 10:30:00', 0, 5, 2),
(3, 'Dossier en cours d\'examen', 'Votre demande de CNI CEDEAO (STD-2026-00003) est passée au statut En vérification.', '2026-11-24 14:00:00', 0, 5, 3);

-- 12. FOIRE AUX QUESTIONS (FAQ)
INSERT INTO `faq` (`id`, `question`, `reponse`, `publiee`, `id_service`, `ordre`) VALUES
(1, 'Quelle est la durée de validité d\'un extrait de naissance pour un passeport ou un casier judiciaire ?',
 'Pour la quasi-totalité des démarches officielles au Sénégal, l\'extrait ou copie littérale d\'acte de naissance doit dater de moins de trois (3) mois au moment du dépôt physique.', 1, NULL, 1),
(2, 'Comment obtenir un duplicata en cas de perte de la CNI CEDEAO ?',
 'Il faut d\'abord faire une déclaration de perte auprès d\'un commissariat de police ou d\'une brigade de gendarmerie, puis se présenter dans un centre de la DAF muni du certificat de perte et de 10 000 FCFA de quittance.', 1, 3, 2),
(3, 'Puis-je me faire représenter pour l\'enrôlement du passeport biométrique ?',
 'Non, la présence physique du demandeur est strictement obligatoire lors du dépôt pour la capture des empreintes digitales et la prise de photo biométrique aux normes internationales.', 1, 2, 3),
(4, 'Quels sont les horaires d\'ouverture des guichets administratifs ?',
 'Les services publics sénégalais sont généralement ouverts du lundi au vendredi, de 08h00 à 13h30 et de 14h30 à 17h00 (vendredi : fermeture à 13h30 pour la prière, réouverture à 15h00).', 1, NULL, 4);
