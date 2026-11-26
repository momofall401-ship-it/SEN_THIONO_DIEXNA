# 🇸🇳 SEN THIOONO DIEXNA — Assistant Numérique des Démarches Administratives

> **Version 1.0 — Novembre 2026**  
> *Développement Java Avancé • Travail de groupe*  
> **Technologies :** Java 21/24, JavaFX, FXML, CSS, MySQL 8/9, JDBC, Apache PDFBox, Maven, JUnit 5.

---

## 📌 Présentation du Projet

**Sen Thioono Diexna** (*« Fin des tracasseries »* en Wolof) est une application desktop de gestion et d'accompagnement des citoyens dans leurs démarches administratives au Sénégal (Ministère de la Justice, Ministère de l'Intérieur / DPAF / DAF, Ministère des Transports, Mairie de Dakar).

L'application centralise :
- Le **catalogue officiel des démarches administratives** (conditions, étapes, frais indicatifs, délais estimés, coordonnées).
- Un **assistant de parcours avec checklist interactive** pour s'assurer que le dossier physique est complet avant tout déplacement.
- L'**enregistrement et le suivi des dossiers** avec un numéro de suivi unique (`STD-2026-XXXXX`).
- Un **espace dédié aux agents administratifs** pour instruire les dossiers de leur service, vérifier les pièces fournies (conforme, non conforme, à compléter) et notifier l'usager.
- Un **générateur de fiches et récépissés officiels en PDF** (Apache PDFBox).
- Un **tableau de bord décisionnel** avec métriques et graphiques pour les responsables et administrateurs.
- Un module de **Foire Aux Questions (FAQ)** et un **centre de notifications internes**.

---

## 🏗️ Architecture Technique en Couches

L'application suit une stricte séparation des responsabilités (architecture 3-tiers / MVC JavaFX) :

```
src/
├── main/
│   ├── java/sn/senthioonodiexna/
│   │   ├── MainApp.java                    # Point d'entrée JavaFX
│   │   ├── config/                         # Configuration base de données & session active
│   │   │   ├── DatabaseConfig.java         # Pool HikariCP & migration automatique
│   │   │   └── AppSession.java             # Session utilisateur connecté (Thread-safe)
│   │   ├── model/                          # Entités métier & Énumérations
│   │   │   ├── Role.java                   # CITOYEN, AGENT, RESPONSABLE, ADMIN
│   │   │   ├── StatutCompte.java           # ACTIF, DESACTIVE
│   │   │   ├── StatutDossier.java          # BROUILLON, DEPOSE, EN_VERIFICATION...
│   │   │   ├── StatutVerification.java     # CONFORME, NON_CONFORME, A_COMPLETER...
│   │   │   └── Utilisateur.java, Demarche.java, Dossier.java, etc.
│   │   ├── dao/                            # Interfaces et implémentations JDBC
│   │   │   ├── UtilisateurDao.java
│   │   │   ├── DemarcheDao.java
│   │   │   ├── DossierDao.java
│   │   │   └── impl/                       # Implémentations PreparedStatements MySQL
│   │   ├── service/                        # Logique métier & Règles de gestion (RG01 - RG12)
│   │   │   ├── AuthService.java            # Hachage BCrypt, contrôle de compte actif
│   │   │   ├── DemarcheService.java        # Filtrage démarches publiées (RG05)
│   │   │   ├── DossierService.java         # Numéro unique (RG06), workflow, RG04, RG08, RG09
│   │   │   ├── NotificationService.java    # Notifications internes
│   │   │   ├── FaqService.java
│   │   │   ├── DashboardService.java       # Statistiques et KPIs
│   │   │   └── PdfExportService.java       # Génération PDFBox
│   │   ├── controller/                     # Contrôleurs JavaFX FXML
│   │   └── util/                           # Hachage BCrypt, PDFBox, Validations, Alertes
│   └── resources/
│       ├── fxml/                           # Interfaces graphiques FXML
│       ├── css/style.css                   # Charte graphique (Vert institutionnel, Doré, Slate)
│       └── db/                             # Scripts SQL (schema.sql, seed_data.sql)
└── test/
    └── java/sn/senthioonodiexna/service/   # Tests unitaires JUnit 5
```

---

## 🔒 Respect des Règles de Gestion (RG01 - RG12)

| Règle | Libellé | Implémentation technique |
|---|---|---|
| **RG01** | Accès aux espaces privés sous authentification | Contrôle via `AppSession` et redirection obligatoire |
| **RG02** | Adresse e-mail unique | Contrainte `UNIQUE` MySQL et validation dans `AuthService.register()` |
| **RG03** | Un citoyen ne consulte que ses propres dossiers | Filtrage SQL `WHERE id_utilisateur = ?` et vérification dans `getAuthorizedDossierForCitizen()` |
| **RG04** | Un agent ne traite que les dossiers de son service | `DossierService.checkAgentServiceAuthorization()` bloque tout accès inter-services |
| **RG05** | Seules les démarches publiées sont visibles aux citoyens | `demarcheDao.findPublished()` avec statut `PUBLIEE` |
| **RG06** | Numéro de suivi unique par dossier | Générateur `generateUniqueNumeroSuivi()` format `STD-2026-XXXXX` |
| **RG07** | Toute modification de statut produit un historique | Insertion systématique dans la table `HISTORIQUE` (date, auteur, motif, ancien/nouveau statut) |
| **RG08** | Un rejet doit obligatoirement avoir un motif | `dossierService.rejectDossier()` exige un motif non vide |
| **RG09** | Demande de complément précise les pièces/infos attendues | `dossierService.requestComplement()` exige les détails requis |
| **RG10** | Pièces obligatoires clairement identifiées | Flag `obligatoire` et badges visuels distincts dans la checklist |
| **RG11** | Un compte désactivé ne peut pas se connecter | Contrôle `statut_compte == 'ACTIF'` dans `AuthService.login()` |
| **RG12** | Actions sensibles associées à l'utilisateur auteur | Traçabilité avec clé étrangère `id_utilisateur` |

---

## 🔑 Comptes de démonstration pré-configurés

| Profil | Rôle | Adresse e-mail | Mot de passe |
|---|---|---|---|
| **Administrateur** | `ADMIN` | `admin@senthioono.sn` | `Admin@2026` |
| **Responsable Service** | `RESPONSABLE` | `responsable.dpaf@senthioono.sn` | `Pass@2026` |
| **Agent DPAF (Passeports)** | `AGENT` | `agent.dpaf@senthioono.sn` | `Agent@2026` |
| **Agent Justice (Casier Judiciaire)** | `AGENT` | `agent.justice@senthioono.sn` | `Agent@2026` |
| **Citoyen usager** | `CITOYEN` | `citoyen@gmail.com` | `Citoyen@2026` |

---

## 🚀 Lancement et Exécution

### 1. Démarrer MySQL
Vérifier que le service MySQL 8/9 écoute sur le port `3306` (avec utilisateur `root`).
Au premier démarrage, l'application crée automatiquement la base `sen_thioono_diexna` et insère le schéma et les données de test.

### 2. Compiler et exécuter les tests
```powershell
.\mvnw.cmd clean test
```

### 3. Lancer l'application desktop JavaFX
```powershell
.\mvnw.cmd javafx:run
```
