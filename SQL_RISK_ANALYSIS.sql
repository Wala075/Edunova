-- ============================================================================
-- Table pour l'analyse de risque avec IA
-- ============================================================================

CREATE TABLE IF NOT EXISTS risk_analysis (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    country VARCHAR(100),
    device VARCHAR(100),
    login_time DATETIME,
    failed_attempts INT DEFAULT 0,
    typing_speed DOUBLE DEFAULT 0,
    risk_score INT DEFAULT 0,
    risk_level VARCHAR(50),
    blocked BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user(id_u),
    INDEX idx_user_id (user_id),
    INDEX idx_risk_score (risk_score),
    INDEX idx_blocked (blocked),
    INDEX idx_created_at (created_at)
);

-- ============================================================================
-- Vues pour les statistiques de risque
-- ============================================================================

-- Vue: Statistiques de risque par utilisateur
CREATE OR REPLACE VIEW vw_user_risk_stats AS
SELECT 
    u.id_u,
    u.email_u,
    u.nom_u,
    u.prenom_u,
    COUNT(ra.id) as total_logins,
    SUM(CASE WHEN ra.blocked = TRUE THEN 1 ELSE 0 END) as blocked_logins,
    AVG(ra.risk_score) as avg_risk_score,
    MAX(ra.risk_score) as max_risk_score,
    MIN(ra.risk_score) as min_risk_score,
    MAX(ra.created_at) as last_login
FROM user u
LEFT JOIN risk_analysis ra ON u.id_u = ra.user_id
GROUP BY u.id_u, u.email_u, u.nom_u, u.prenom_u;

-- Vue: Connexions bloquées récentes
CREATE OR REPLACE VIEW vw_blocked_connections AS
SELECT 
    ra.id,
    ra.user_id,
    u.email_u,
    u.nom_u,
    u.prenom_u,
    ra.ip_address,
    ra.country,
    ra.device,
    ra.risk_score,
    ra.risk_level,
    ra.created_at
FROM risk_analysis ra
JOIN user u ON ra.user_id = u.id_u
WHERE ra.blocked = TRUE
ORDER BY ra.created_at DESC;

-- Vue: Analyse de risque par pays
CREATE OR REPLACE VIEW vw_risk_by_country AS
SELECT 
    country,
    COUNT(*) as total_attempts,
    SUM(CASE WHEN blocked = TRUE THEN 1 ELSE 0 END) as blocked_attempts,
    AVG(risk_score) as avg_risk_score,
    MAX(risk_score) as max_risk_score
FROM risk_analysis
GROUP BY country
ORDER BY avg_risk_score DESC;

-- Vue: Analyse de risque par appareil
CREATE OR REPLACE VIEW vw_risk_by_device AS
SELECT 
    device,
    COUNT(*) as total_attempts,
    SUM(CASE WHEN blocked = TRUE THEN 1 ELSE 0 END) as blocked_attempts,
    AVG(risk_score) as avg_risk_score,
    MAX(risk_score) as max_risk_score
FROM risk_analysis
GROUP BY device
ORDER BY avg_risk_score DESC;

-- ============================================================================
-- Procédures stockées
-- ============================================================================

-- Procédure: Obtenir le score de risque moyen du système
DELIMITER //
CREATE PROCEDURE sp_get_system_risk_stats()
BEGIN
    SELECT 
        COUNT(*) as total_logins,
        SUM(CASE WHEN blocked = TRUE THEN 1 ELSE 0 END) as blocked_logins,
        AVG(risk_score) as avg_risk_score,
        MAX(risk_score) as max_risk_score,
        MIN(risk_score) as min_risk_score
    FROM risk_analysis;
END //
DELIMITER ;

-- Procédure: Obtenir les connexions suspectes
DELIMITER //
CREATE PROCEDURE sp_get_suspicious_connections(IN p_limit INT)
BEGIN
    SELECT 
        ra.id,
        ra.user_id,
        u.email_u,
        ra.ip_address,
        ra.country,
        ra.device,
        ra.risk_score,
        ra.risk_level,
        ra.blocked,
        ra.created_at
    FROM risk_analysis ra
    JOIN user u ON ra.user_id = u.id_u
    WHERE ra.risk_score >= 60
    ORDER BY ra.created_at DESC
    LIMIT p_limit;
END //
DELIMITER ;

-- Procédure: Obtenir l'historique de risque d'un utilisateur
DELIMITER //
CREATE PROCEDURE sp_get_user_risk_history(IN p_user_id INT, IN p_limit INT)
BEGIN
    SELECT 
        id,
        user_id,
        ip_address,
        country,
        device,
        login_time,
        failed_attempts,
        typing_speed,
        risk_score,
        risk_level,
        blocked,
        created_at
    FROM risk_analysis
    WHERE user_id = p_user_id
    ORDER BY created_at DESC
    LIMIT p_limit;
END //
DELIMITER ;

-- ============================================================================
-- Exemples de requêtes utiles
-- ============================================================================

-- Obtenir les 10 connexions les plus suspectes
-- SELECT * FROM risk_analysis WHERE risk_score >= 60 ORDER BY risk_score DESC LIMIT 10;

-- Obtenir les connexions bloquées par jour
-- SELECT DATE(created_at) as date, COUNT(*) as blocked_count FROM risk_analysis WHERE blocked = TRUE GROUP BY DATE(created_at);

-- Obtenir les pays avec le plus de connexions bloquées
-- SELECT country, COUNT(*) as blocked_count FROM risk_analysis WHERE blocked = TRUE GROUP BY country ORDER BY blocked_count DESC;

-- Obtenir les appareils suspects
-- SELECT device, AVG(risk_score) as avg_score, COUNT(*) as attempts FROM risk_analysis GROUP BY device HAVING avg_score > 50;

-- ============================================================================
