-- ============================================================
-- EduNova - Migration : Système d'Alertes + Emails
-- ============================================================
-- À exécuter sur la base `edunova` (phpMyAdmin / MySQL CLI).
-- Idempotent : peut être relancé sans casser l'existant.
-- ============================================================

USE edunova;

-- ----------------------------------------------------------
-- 1. Ajouter email parent + téléphone parent à la table student
-- ----------------------------------------------------------
ALTER TABLE student
    ADD COLUMN IF NOT EXISTS email_parent     VARCHAR(150) NULL,
    ADD COLUMN IF NOT EXISTS telephone_parent VARCHAR(30)  NULL;

-- ----------------------------------------------------------
-- 2. Table des alertes intelligentes
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS alerte (
    id_a            INT AUTO_INCREMENT PRIMARY KEY,
    type_alerte     VARCHAR(50)  NOT NULL,
    severite        ENUM('INFO','MOYENNE','CRITIQUE','POSITIVE') NOT NULL DEFAULT 'INFO',
    titre           VARCHAR(255) NOT NULL,
    message         TEXT,
    student_id      INT          NULL,
    matiere_id      INT          NULL,
    trimestre       INT          NULL,
    annee_scolaire  VARCHAR(20)  NULL,
    date_creation   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut          ENUM('NOUVELLE','LUE','TRAITEE','IGNOREE') NOT NULL DEFAULT 'NOUVELLE',
    email_envoye    BOOLEAN      NOT NULL DEFAULT FALSE,
    sms_envoye      BOOLEAN      NOT NULL DEFAULT FALSE,

    INDEX idx_alerte_student   (student_id),
    INDEX idx_alerte_severite  (severite),
    INDEX idx_alerte_statut    (statut),
    INDEX idx_alerte_date      (date_creation),

    CONSTRAINT fk_alerte_student
        FOREIGN KEY (student_id) REFERENCES student(id_s) ON DELETE CASCADE,
    CONSTRAINT fk_alerte_matiere
        FOREIGN KEY (matiere_id) REFERENCES matiere(id_m) ON DELETE SET NULL
);

-- ----------------------------------------------------------
-- 3. Log des emails envoyés (utilisé par Brevo en F3)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS email_log (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    destinataire  VARCHAR(150) NOT NULL,
    sujet         VARCHAR(255) NOT NULL,
    statut        ENUM('ENVOYE','ECHEC') NOT NULL,
    erreur        TEXT NULL,
    date_envoi    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    alerte_id     INT NULL,
    student_id    INT NULL,

    INDEX idx_email_date    (date_envoi),
    INDEX idx_email_statut  (statut),

    CONSTRAINT fk_email_alerte
        FOREIGN KEY (alerte_id) REFERENCES alerte(id_a) ON DELETE SET NULL,
    CONSTRAINT fk_email_student
        FOREIGN KEY (student_id) REFERENCES student(id_s) ON DELETE SET NULL
);

-- ----------------------------------------------------------
-- 4. (Optionnel) Données de démo pour le jury
-- ----------------------------------------------------------
-- Décommenter pour avoir 2 alertes de test au démarrage.
--
-- INSERT INTO alerte (type_alerte, severite, titre, message, student_id, annee_scolaire)
-- VALUES
--   ('EXCELLENCE', 'POSITIVE', 'Élève excellent', 'Moyenne 17.5/20 - félicitations.', 1, '2025-2026'),
--   ('MOYENNE_FAIBLE', 'CRITIQUE', 'Moyenne en difficulté', 'Moyenne 6.5/20 - intervention recommandée.', 2, '2025-2026');
