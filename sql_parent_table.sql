-- =====================================================================
-- À exécuter dans phpMyAdmin (base edunova) avant d'utiliser la feature
-- =====================================================================

CREATE TABLE IF NOT EXISTS parent (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    classe_id INT NOT NULL,
    CONSTRAINT fk_parent_classe FOREIGN KEY (classe_id)
        REFERENCES classe(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Optionnel : ajouter un index sur classe_id pour optimiser les filtres
CREATE INDEX IF NOT EXISTS idx_parent_classe ON parent(classe_id);
