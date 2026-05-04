-- ============================================================
-- EduNova - Migration : Système d'Alertes + Emails (SAFE)
-- ============================================================
-- Version compatible MySQL 5.7+ et 8.x
-- Si tu as déjà exécuté la 01_alertes_emails.sql, ignore ce fichier.
-- Si une commande échoue car la colonne/table existe déjà,
-- c'est OK : continue jusqu'au bout.
-- ============================================================

USE edunova;

-- ----------------------------------------------------------
-- 1. Colonnes parent dans student
-- (Si erreur "Duplicate column name" => colonne déjà ajoutée, ignorer)
-- ----------------------------------------------------------
ALTER TABLE student ADD COLUMN email_parent     VARCHAR(150) NULL;
ALTER TABLE student ADD COLUMN telephone_parent VARCHAR(30)  NULL;

-- ----------------------------------------------------------
-- 2. Table des alertes intelligentes
-- ----------------------------------------------------------
DROP TABLE IF EXISTS alerte;
CREATE TABLE alerte (
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
-- 3. Log des emails (pour la Feature 3 - Brevo)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS email_log;
CREATE TABLE email_log (
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
-- 4. Vérification
-- ----------------------------------------------------------
SELECT 'Migration OK ✅' AS resultat,
       (SELECT COUNT(*) FROM information_schema.tables
        WHERE table_schema='edunova' AND table_name='alerte') AS table_alerte_creee,
       (SELECT COUNT(*) FROM information_schema.tables
        WHERE table_schema='edunova' AND table_name='email_log') AS table_email_log_creee;
