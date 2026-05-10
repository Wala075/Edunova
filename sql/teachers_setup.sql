-- ============================================================
-- Script de configuration pour la gestion des enseignants
-- ============================================================

-- 1. Créer la table 'roles' si elle n'existe pas
CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50) UNIQUE NOT NULL
);

-- 2. Insérer les rôles de base
INSERT IGNORE INTO roles (nom) VALUES ('Admin');
INSERT IGNORE INTO roles (nom) VALUES ('Enseignant');
INSERT IGNORE INTO roles (nom) VALUES ('Étudiant');
INSERT IGNORE INTO roles (nom) VALUES ('Parent');

-- 3. Créer la table 'users' si elle n'existe pas
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    actif BOOLEAN DEFAULT TRUE,
    role_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 4. Créer la table 'teachers' pour les informations spécifiques aux enseignants
CREATE TABLE IF NOT EXISTS teachers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    specialite VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Créer des index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_actif ON users(actif);
CREATE INDEX IF NOT EXISTS idx_teachers_user_id ON teachers(user_id);

-- 6. Insérer un enseignant d'exemple (optionnel)
-- Décommentez les lignes suivantes pour ajouter un enseignant de test
-- INSERT INTO users (email, password, nom, prenom, telephone, actif, role_id)
-- VALUES ('prof@example.com', 'password123', 'Dupont', 'Jean', '0123456789', TRUE, 
--         (SELECT id FROM roles WHERE nom = 'Enseignant'));
-- 
-- INSERT INTO teachers (user_id, specialite)
-- VALUES (LAST_INSERT_ID(), 'Mathématiques');

-- ============================================================
-- Fin du script
-- ============================================================
