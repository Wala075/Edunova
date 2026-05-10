-- Vérifier la structure de la table risk_analysis
DESCRIBE risk_analysis;

-- Voir les colonnes
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'risk_analysis' AND TABLE_SCHEMA = 'edunova';

-- Voir quelques données
SELECT * FROM risk_analysis LIMIT 5;

-- Compter les enregistrements
SELECT COUNT(*) as total FROM risk_analysis;
