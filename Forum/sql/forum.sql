USE edunova;

-- ============================================================
--  1. COMPTES DE TEST
-- ============================================================
INSERT IGNORE INTO `user`
    (email_u, password_u, nom_u, prenom_u, telephone_u, actif_u, role_id, reputation)
VALUES
    ('admin@edunova.com',        'admin123',    'EduNova',  'Admin',  '00000000', 1, 1, 0),
    ('etudiant@edunova.com',     'etudiant123', 'Benali',   'Karim',  '55000001', 1, 2, 0),
    ('parent@edunova.com',       'parent123',   'Trabelsi', 'Fatma',  '55000002', 1, 3, 0),
    ('sophie.martin@edunova.com','etudiant123', 'Martin',   'Sophie', '55000003', 1, 2, 0),
    ('marc.dupont@edunova.com',  'parent123',   'Dupont',   'Marc',   '55000004', 1, 3, 0);
    ('hamza@edunova.com',        'hamza123',    'EduNova',  'hamza',  '00000000', 1, 1, 0);


-- ============================================================
--  2. TABLE POSTS
-- ============================================================
CREATE TABLE IF NOT EXISTS forum_post (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    titre         VARCHAR(200) NOT NULL,
    contenu       TEXT         NOT NULL,
    photo_path    VARCHAR(500),
    auteur_id     INT          NOT NULL,
    auteur_nom    VARCHAR(150) NOT NULL,
    auteur_role   VARCHAR(30)  NOT NULL,
    statut        ENUM('EN_ATTENTE','ACCEPTE','REFUSE') DEFAULT 'EN_ATTENTE',
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modif    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  3. TABLE LIKES
-- ============================================================
CREATE TABLE IF NOT EXISTS forum_like (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    post_id       INT NOT NULL,
    user_id       INT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_like (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  4. TABLE COMMENTAIRES
-- ============================================================
CREATE TABLE IF NOT EXISTS forum_comment (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    post_id       INT          NOT NULL,
    auteur_id     INT          NOT NULL,
    auteur_nom    VARCHAR(150) NOT NULL,
    contenu       TEXT         NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  5. POSTS DE TEST
--  Verifie tes id_u avec : SELECT id_u, email_u FROM user;
--  Remplace les auteur_id si necessaire.
-- ============================================================
INSERT IGNORE INTO forum_post
    (id, titre, contenu, auteur_id, auteur_nom, auteur_role, statut, date_creation)
VALUES
(1,'Bienvenue sur le forum EduNova !',
 'Bonjour a tous ! Ce forum est un espace d''echange pour etudiants et parents.',
 2,'Karim Benali','student','ACCEPTE', NOW() - INTERVAL 3 DAY),
(2,'Resultats des examens du trimestre',
 'Quelqu''un sait quand seront publies les resultats des examens ?',
 2,'Karim Benali','student','ACCEPTE', NOW() - INTERVAL 2 DAY),
(3,'Probleme acces e-learning',
 'Depuis hier je n''arrive plus a me connecter. Meme probleme ?',
 2,'Karim Benali','student','EN_ATTENTE', NOW() - INTERVAL 1 HOUR),
(4,'Reunion parents-professeurs',
 'La reunion du 2e trimestre aura lieu le 15 du mois prochain.',
 3,'Fatma Trabelsi','parent','ACCEPTE', NOW() - INTERVAL 2 DAY),
(5,'Aide devoirs mathematiques',
 'Mon fils a du mal avec les fractions. Un tuteur peut l''aider ?',
 3,'Fatma Trabelsi','parent','EN_ATTENTE', NOW() - INTERVAL 30 MINUTE),
(6,'Notes cours Physique chapitre 3',
 'Je partage mes notes du chapitre 3. J''espere que ca aide !',
 4,'Sophie Martin','student','ACCEPTE', NOW() - INTERVAL 1 DAY),
(7,'Concours de programmation',
 'Inscriptions ouvertes jusqu''au 20. Qui forme une equipe avec moi ?',
 4,'Sophie Martin','student','ACCEPTE', NOW() - INTERVAL 12 HOUR),
(8,'Transport scolaire nouvel horaire',
 'Le bus change d''horaire lundi. Depart 7h30 au lieu de 7h45.',
 5,'Marc Dupont','parent','ACCEPTE', NOW() - INTERVAL 6 HOUR);

-- ============================================================
--  TABLE NOTIFICATIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS forum_notification (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    message       VARCHAR(500) NOT NULL,
    type          VARCHAR(30)  NOT NULL,
    lue           TINYINT(1)   DEFAULT 0,
    date_creation DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_notif_user ON forum_notification(user_id);

-- ============================================================
--  TABLE SIGNALEMENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS forum_signalement (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    post_id       INT          NOT NULL,
    user_id       INT          NOT NULL,
    raison        VARCHAR(200) NOT NULL,
    date_creation DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_signalement (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_sig_post ON forum_signalement(post_id);

-- ============================================================
--  FOREIGN KEY CONSTRAINTS (added to enforce referential integrity)
--  Run these after the tables above are created.
--  If your `user` table uses a different PK name, adjust accordingly.
-- ============================================================

-- forum_like → forum_post
ALTER TABLE forum_like
    ADD CONSTRAINT fk_like_post
        FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE;

-- forum_comment → forum_post
ALTER TABLE forum_comment
    ADD CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE;

-- forum_notification → user
-- (cross-database FK omitted — user table is in a separate DB)

-- forum_signalement → user
-- (cross-database FK omitted — user table is in a separate DB)

-- ============================================================
--  MEDIUM FEATURES — added tables
-- ============================================================

-- Comment likes (reactions on comments)
-- NOTE: fk_clike_user is omitted intentionally — the `user` table lives in a
-- separate database managed by a colleague. MySQL does not support cross-database
-- foreign keys. Referential integrity for user_id is enforced at the application
-- layer (UserService / SessionManager guarantee a valid logged-in user).
CREATE TABLE IF NOT EXISTS forum_comment_like (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    comment_id    INT NOT NULL,
    user_id       INT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_comment_like (comment_id, user_id),
    CONSTRAINT fk_clike_comment FOREIGN KEY (comment_id) REFERENCES forum_comment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  CHAT FEATURE
-- ============================================================

-- User presence: tracks online status and last login time
CREATE TABLE IF NOT EXISTS user_presence (
    user_id       INT PRIMARY KEY,
    last_seen     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_online     TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Private chat messages between two users
CREATE TABLE IF NOT EXISTS chat_message (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    sender_id     INT          NOT NULL,
    receiver_id   INT          NOT NULL,
    contenu       TEXT         NOT NULL,
    statut        ENUM('EN_ATTENTE','ACCEPTE','REFUSE') DEFAULT 'ACCEPTE',
    lu            TINYINT(1)   DEFAULT 0,
    date_creation DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_sender   (sender_id),
    INDEX idx_chat_receiver (receiver_id),
    INDEX idx_chat_pair     (sender_id, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Drop the invalid FK above and recreate without cross-table reference
ALTER TABLE chat_message DROP FOREIGN KEY fk_chat_msg_post;
