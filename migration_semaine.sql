-- ============================================================
-- Migration : ajout de la colonne semaine_debut dans seance
-- À exécuter UNE SEULE FOIS dans phpMyAdmin sur la base edunova
-- ============================================================

-- 1. Ajouter la colonne
ALTER TABLE seance
ADD COLUMN semaine_debut DATE NOT NULL DEFAULT '2024-01-01'
AFTER annee_scolaire;

-- 2. Mettre à jour les séances existantes avec la semaine actuelle
--    (lundi de la semaine courante)
UPDATE seance
SET semaine_debut = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
WHERE semaine_debut = '2024-01-01';
