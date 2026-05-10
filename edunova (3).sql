-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : mer. 29 avr. 2026 à 18:45
-- Version du serveur : 8.2.0
-- Version de PHP : 8.2.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `edunova`
--

-- --------------------------------------------------------

--
-- Structure de la table `bulletin`
--

DROP TABLE IF EXISTS `bulletin`;
CREATE TABLE IF NOT EXISTS `bulletin` (
  `id_b` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `trimestre` int NOT NULL,
  `moyenne_generale` decimal(4,2) DEFAULT NULL,
  `rang` int DEFAULT NULL,
  `mention` varchar(20) DEFAULT NULL,
  `appreciation` text,
  `pdf_path` varchar(255) DEFAULT NULL,
  `date_generation` date DEFAULT NULL,
  `annee_scolaire` varchar(9) DEFAULT NULL,
  PRIMARY KEY (`id_b`),
  UNIQUE KEY `unique_bulletin` (`student_id`,`trimestre`,`annee_scolaire`),
  KEY `idx_student_bull` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `classe`
--

DROP TABLE IF EXISTS `classe`;
CREATE TABLE IF NOT EXISTS `classe` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `niveau` varchar(255) DEFAULT NULL,
  `capacite` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `classe`
--

INSERT INTO `classe` (`id`, `nom`, `niveau`, `capacite`) VALUES
(1, '6A', '6ème', 30),
(2, '5B', 'Terminale', 28),
(6, '6b', '5ème', 19),
(7, '6A', '6ème', 32);

-- --------------------------------------------------------

--
-- Structure de la table `live_session`
--

DROP TABLE IF EXISTS `live_session`;
CREATE TABLE IF NOT EXISTS `live_session` (
  `id_ls` int NOT NULL AUTO_INCREMENT,
  `titre_ls` varchar(100) NOT NULL,
  `date_session_ls` date NOT NULL,
  `heure_debut_ls` time NOT NULL,
  `heure_fin_ls` time NOT NULL,
  `lien_meet_ls` varchar(255) DEFAULT NULL,
  `statut_ls` varchar(20) DEFAULT 'PROGRAMMEE',
  `seance_id` int DEFAULT NULL,
  `teacher_id` int NOT NULL,
  `classe_id` int NOT NULL,
  `matiere_id` int NOT NULL,
  PRIMARY KEY (`id_ls`),
  KEY `idx_ls_seance` (`seance_id`),
  KEY `idx_ls_teacher` (`teacher_id`),
  KEY `idx_ls_classe` (`classe_id`),
  KEY `idx_ls_matiere` (`matiere_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `login_history`
--

DROP TABLE IF EXISTS `login_history`;
CREATE TABLE IF NOT EXISTS `login_history` (
  `id_lh` int NOT NULL AUTO_INCREMENT,
  `date_connexion_lh` datetime DEFAULT CURRENT_TIMESTAMP,
  `adresse_ip_lh` varchar(45) DEFAULT NULL,
  `succes_lh` tinyint(1) DEFAULT '1',
  `user_id` int NOT NULL,
  PRIMARY KEY (`id_lh`),
  KEY `idx_lh_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `matiere`
--

DROP TABLE IF EXISTS `matiere`;
CREATE TABLE IF NOT EXISTS `matiere` (
  `id_m` int NOT NULL AUTO_INCREMENT,
  `nom_m` varchar(255) NOT NULL,
  `coefficient_m` int DEFAULT NULL,
  PRIMARY KEY (`id_m`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `matiere`
--

INSERT INTO `matiere` (`id_m`, `nom_m`, `coefficient_m`) VALUES
(1, 'Mathématiques', 4),
(2, 'Français', 4),
(3, 'Anglais', 3),
(4, 'Histoire-Géographie', 2),
(5, 'Sciences Physiques', 3);

-- --------------------------------------------------------

--
-- Structure de la table `note`
--

DROP TABLE IF EXISTS `note`;
CREATE TABLE IF NOT EXISTS `note` (
  `id_n` int NOT NULL AUTO_INCREMENT,
  `valeur` decimal(4,2) NOT NULL,
  `coefficient` int DEFAULT '1',
  `type_eval` varchar(20) DEFAULT NULL,
  `trimestre` int NOT NULL,
  `date_saisie` date DEFAULT NULL,
  `student_id` int NOT NULL,
  `matiere_id` int NOT NULL,
  `teacher_id` int DEFAULT NULL,
  `annee_scolaire` varchar(9) DEFAULT NULL,
  PRIMARY KEY (`id_n`),
  KEY `idx_student` (`student_id`),
  KEY `idx_matiere` (`matiere_id`),
  KEY `idx_note_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `paiement`
--

DROP TABLE IF EXISTS `paiement`;
CREATE TABLE IF NOT EXISTS `paiement` (
  `id_p` int NOT NULL AUTO_INCREMENT,
  `montant` decimal(10,2) NOT NULL,
  `type_paiement` varchar(30) DEFAULT NULL,
  `methode` varchar(20) DEFAULT NULL,
  `date_paiement` date NOT NULL,
  `date_echeance` date DEFAULT NULL,
  `statut` varchar(20) DEFAULT 'EN_ATTENTE',
  `reference` varchar(50) DEFAULT NULL,
  `mois_concerne` varchar(20) DEFAULT NULL,
  `student_id` int NOT NULL,
  `annee_scolaire` varchar(9) DEFAULT NULL,
  `commentaire` text,
  PRIMARY KEY (`id_p`),
  UNIQUE KEY `reference` (`reference`),
  KEY `idx_student_pay` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `parent`
--

DROP TABLE IF EXISTS `parent`;
CREATE TABLE IF NOT EXISTS `parent` (
  `id_par` int NOT NULL AUTO_INCREMENT,
  `nom_par` varchar(50) NOT NULL,
  `prenom_par` varchar(50) NOT NULL,
  `email_par` varchar(100) DEFAULT NULL,
  `telephone_par` varchar(20) DEFAULT NULL,
  `profession_par` varchar(100) DEFAULT NULL,
  `adresse_par` text,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id_par`),
  UNIQUE KEY `email_par` (`email_par`),
  KEY `idx_parent_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `parent_student`
--

DROP TABLE IF EXISTS `parent_student`;
CREATE TABLE IF NOT EXISTS `parent_student` (
  `id_ps` int NOT NULL AUTO_INCREMENT,
  `relation_ps` varchar(20) DEFAULT NULL,
  `parent_id` int NOT NULL,
  `student_id` int NOT NULL,
  PRIMARY KEY (`id_ps`),
  KEY `idx_ps_parent` (`parent_id`),
  KEY `idx_ps_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `presence`
--

DROP TABLE IF EXISTS `presence`;
CREATE TABLE IF NOT EXISTS `presence` (
  `id_pr` int NOT NULL AUTO_INCREMENT,
  `statut_pr` varchar(20) NOT NULL,
  `date_presence_pr` date NOT NULL,
  `heure_arrivee_pr` time DEFAULT NULL,
  `justificatif_path_pr` varchar(255) DEFAULT NULL,
  `commentaire_pr` text,
  `student_id` int NOT NULL,
  `seance_id` int DEFAULT NULL,
  `live_session_id` int DEFAULT NULL,
  PRIMARY KEY (`id_pr`),
  KEY `idx_presence_student` (`student_id`),
  KEY `idx_presence_seance` (`seance_id`),
  KEY `idx_presence_live` (`live_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `role`
--

DROP TABLE IF EXISTS `role`;
CREATE TABLE IF NOT EXISTS `role` (
  `id_r` int NOT NULL AUTO_INCREMENT,
  `nom_r` varchar(30) NOT NULL,
  `description_r` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_r`),
  UNIQUE KEY `nom_r` (`nom_r`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `role`
--

INSERT INTO `role` (`id_r`, `nom_r`, `description_r`) VALUES
(1, 'ADMIN', 'Administrateur du système'),
(2, 'PROVISEUR', 'Proviseur de l\'établissement'),
(3, 'ENSEIGNANT', 'Enseignant'),
(4, 'ELEVE', 'Élève'),
(5, 'PARENT', 'Parent d\'élève');

-- --------------------------------------------------------

--
-- Structure de la table `seance`
--

DROP TABLE IF EXISTS `seance`;
CREATE TABLE IF NOT EXISTS `seance` (
  `id_se` int NOT NULL AUTO_INCREMENT,
  `jour_se` varchar(10) NOT NULL,
  `heure_debut_se` time NOT NULL,
  `heure_fin_se` time NOT NULL,
  `salle_se` varchar(20) DEFAULT NULL,
  `type_cours_se` varchar(20) DEFAULT 'PRESENTIEL',
  `annee_scolaire` varchar(9) DEFAULT NULL,
  `classe_id` int NOT NULL,
  `matiere_id` int NOT NULL,
  `teacher_id` int NOT NULL,
  PRIMARY KEY (`id_se`),
  KEY `idx_seance_classe` (`classe_id`),
  KEY `idx_seance_matiere` (`matiere_id`),
  KEY `idx_seance_teacher` (`teacher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `seance`
--

INSERT INTO `seance` (`id_se`, `jour_se`, `heure_debut_se`, `heure_fin_se`, `salle_se`, `type_cours_se`, `annee_scolaire`, `classe_id`, `matiere_id`, `teacher_id`) VALUES
(2, 'LUNDI', '08:30:00', '14:30:00', 's101', 'DISTANCIEL', '2024-2025', 6, 2, 4);

-- --------------------------------------------------------

--
-- Structure de la table `student`
--

DROP TABLE IF EXISTS `student`;
CREATE TABLE IF NOT EXISTS `student` (
  `id_s` int NOT NULL AUTO_INCREMENT,
  `nom_s` varchar(255) NOT NULL,
  `prenom_s` varchar(255) NOT NULL,
  `date_naissance_s` date NOT NULL,
  `classe_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id_s`),
  KEY `idx_classe` (`classe_id`),
  KEY `idx_student_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
CREATE TABLE IF NOT EXISTS `teacher` (
  `id_t` int NOT NULL AUTO_INCREMENT,
  `nom_t` varchar(50) NOT NULL,
  `prenom_t` varchar(50) NOT NULL,
  `email_t` varchar(100) DEFAULT NULL,
  `telephone_t` varchar(20) DEFAULT NULL,
  `specialite_t` varchar(100) DEFAULT NULL,
  `diplome_t` varchar(100) DEFAULT NULL,
  `date_embauche_t` date DEFAULT NULL,
  `cv_path_t` varchar(255) DEFAULT NULL,
  `actif_t` tinyint(1) DEFAULT '1',
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id_t`),
  UNIQUE KEY `email_t` (`email_t`),
  KEY `idx_teacher_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `teacher`
--

INSERT INTO `teacher` (`id_t`, `nom_t`, `prenom_t`, `email_t`, `telephone_t`, `specialite_t`, `diplome_t`, `date_embauche_t`, `cv_path_t`, `actif_t`, `user_id`) VALUES
(1, 'Ben Ali', 'Mohamed', 'm.benali@edunova.tn', '20123456', 'Mathématiques', NULL, NULL, NULL, 1, NULL),
(2, 'Trabelsi', 'Sonia', 's.trabelsi@edunova.tn', '20234567', 'Français', NULL, NULL, NULL, 1, NULL),
(3, 'Khémiri', 'Samir', 's.khemiri@edunova.tn', '20345678', 'Histoire-Géo', NULL, NULL, NULL, 1, NULL),
(4, 'Mansouri', 'Lina', 'l.mansouri@edunova.tn', '20456789', 'Anglais', NULL, NULL, NULL, 1, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `teacher_matiere`
--

DROP TABLE IF EXISTS `teacher_matiere`;
CREATE TABLE IF NOT EXISTS `teacher_matiere` (
  `id_tm` int NOT NULL AUTO_INCREMENT,
  `teacher_id` int NOT NULL,
  `matiere_id` int NOT NULL,
  PRIMARY KEY (`id_tm`),
  KEY `idx_tm_teacher` (`teacher_id`),
  KEY `idx_tm_matiere` (`matiere_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id_u` int NOT NULL AUTO_INCREMENT,
  `email_u` varchar(100) NOT NULL,
  `password_u` varchar(255) NOT NULL,
  `nom_u` varchar(50) NOT NULL,
  `prenom_u` varchar(50) NOT NULL,
  `telephone_u` varchar(20) DEFAULT NULL,
  `photo_u` varchar(255) DEFAULT NULL,
  `actif_u` tinyint(1) DEFAULT '1',
  `date_creation_u` datetime DEFAULT CURRENT_TIMESTAMP,
  `role_id` int NOT NULL,
  PRIMARY KEY (`id_u`),
  UNIQUE KEY `email_u` (`email_u`),
  KEY `idx_user_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
