-- ============================================
-- Création de la Table RISK
-- ============================================

CREATE TABLE IF NOT EXISTS risk (
    id_ra INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    date_analyse DATETIME DEFAULT CURRENT_TIMESTAMP,
    adresse_ip VARCHAR(45),
    pays_ip VARCHAR(100),
    heure_connexion DATETIME,
    nb_tentatives_echouees INT DEFAULT 0,
    score_risque INT DEFAULT 0,
    niveau_risque VARCHAR(50),
    raisons TEXT,
    action_prise VARCHAR(50),
    
    -- Nouveaux champs pour le rapport amélioré
    temps_connexion_ms INT DEFAULT 0,
    vitesse_ecriture DOUBLE DEFAULT 0.0,
    device_type VARCHAR(100),
    navigateur VARCHAR(100),
    systeme_exploitation VARCHAR(100),
    localisation_precise VARCHAR(255),
    
    FOREIGN KEY (user_id) REFERENCES user(id_u) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_date_analyse (date_analyse),
    INDEX idx_score_risque (score_risque),
    INDEX idx_action_prise (action_prise)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Vérification de la création
-- ============================================

SHOW TABLES LIKE 'risk';
DESCRIBE risk;

-- ============================================
-- Données de test (optionnel)
-- ============================================

-- INSERT INTO risk (
--     user_id, date_analyse, adresse_ip, pays_ip, heure_connexion,
--     nb_tentatives_echouees, score_risque, niveau_risque, raisons, action_prise,
--     temps_connexion_ms, vitesse_ecriture, device_type, navigateur, systeme_exploitation
-- ) VALUES (
--     1, NOW(), '127.0.0.1', 'Tunisia', NOW(),
--     0, 15, '✅ FAIBLE', 'Heure normale (14h)', 'AUTORISÉ',
--     245, 50.5, 'Desktop', 'Chrome', 'Windows 10'
-- );
