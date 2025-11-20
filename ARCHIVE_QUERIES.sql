-- Script SQL pour analyser les archives et les purges
-- À utiliser en H2 Console ou via votre client SQL

-- ============================================================================
-- SECTION 1 : VÉRIFICATION DE LA CONFIGURATION
-- ============================================================================

-- Vérifier que les tables existent
SELECT table_name
FROM information_schema.tables
WHERE table_name IN ('MEETING_ARCHIVE', 'MEETINGS', 'PARTICIPANTS', 'MEETING_ASSISTANCE')
ORDER BY table_name;

-- ============================================================================
-- SECTION 2 : STATISTIQUES GLOBALES
-- ============================================================================

-- Nombre total de réunions actives vs archivées
SELECT
  'ACTIVES' as type_reunion,
  COUNT(*) as count,
  COUNT(CASE WHEN purge_date IS NULL THEN 1 END) as non_purgees,
  COUNT(CASE WHEN purge_date IS NOT NULL THEN 1 END) as purgees
FROM meetings
UNION ALL
SELECT
  'ARCHIVES' as type_reunion,
  COUNT(*) as count,
  COUNT(*) as non_purgees,
  0 as purgees
FROM meeting_archive;

-- Nombre total de participants par type de réunion
SELECT
  'ACTIFS' as type,
  COUNT(*) as participants
FROM participants
UNION ALL
SELECT
  'ARCHIVES_PRESENTIELS' as type,
  SUM(in_person_total) as participants
FROM meeting_archive
UNION ALL
SELECT
  'ARCHIVES_VISIO' as type,
  SUM(remote_total) as participants
FROM meeting_archive;

-- ============================================================================
-- SECTION 3 : ARCHIVES RÉCENTES
-- ============================================================================

-- Les 10 dernières réunions archivées
SELECT
  id,
  original_meeting_id,
  topic,
  start_time,
  end_time,
  total_participants,
  in_person_total,
  remote_total,
  archived_at
FROM meeting_archive
ORDER BY archived_at DESC
LIMIT 10;

-- Réunions archivées ce mois-ci
SELECT
  original_meeting_id,
  topic,
  start_time,
  end_time,
  total_participants,
  in_person_total,
  remote_total,
  archived_at
FROM meeting_archive
WHERE EXTRACT(YEAR FROM archived_at) = EXTRACT(YEAR FROM CURRENT_DATE)
  AND EXTRACT(MONTH FROM archived_at) = EXTRACT(MONTH FROM CURRENT_DATE)
ORDER BY archived_at DESC;

-- ============================================================================
-- SECTION 4 : STATISTIQUES DE PARTICIPATION
-- ============================================================================

-- Ratio présentiels vs visio par semaine (archives)
SELECT
  TRUNC(start_time, 'WW') as semaine,
  SUM(total_participants) as total,
  SUM(in_person_total) as presentiels,
  SUM(remote_total) as visio,
  ROUND(100.0 * SUM(in_person_total) / SUM(total_participants), 2) as pourcent_presentiels
FROM meeting_archive
GROUP BY TRUNC(start_time, 'WW')
ORDER BY semaine DESC;

-- Ratio présentiels vs visio par mois (archives)
SELECT
  EXTRACT(YEAR FROM start_time) as annee,
  EXTRACT(MONTH FROM start_time) as mois,
  SUM(total_participants) as total,
  SUM(in_person_total) as presentiels,
  SUM(remote_total) as visio,
  ROUND(100.0 * SUM(in_person_total) / SUM(total_participants), 2) as pourcent_presentiels
FROM meeting_archive
GROUP BY EXTRACT(YEAR FROM start_time), EXTRACT(MONTH FROM start_time)
ORDER BY annee DESC, mois DESC;

-- Ratio présentiels vs visio par trimestre
SELECT
  EXTRACT(YEAR FROM start_time) as annee,
  CEIL(EXTRACT(MONTH FROM start_time) / 3.0) as trimestre,
  SUM(total_participants) as total,
  SUM(in_person_total) as presentiels,
  SUM(remote_total) as visio,
  ROUND(100.0 * SUM(in_person_total) / SUM(total_participants), 2) as pourcent_presentiels
FROM meeting_archive
GROUP BY EXTRACT(YEAR FROM start_time), CEIL(EXTRACT(MONTH FROM start_time) / 3.0)
ORDER BY annee DESC, trimestre DESC;

-- ============================================================================
-- SECTION 5 : RAPPORT DE PURGE
-- ============================================================================

-- Date de la dernière purge
SELECT
  MAX(archived_at) as derniere_purge,
  MIN(archived_at) as premiere_archive,
  COUNT(*) as nombre_reunions_archivees
FROM meeting_archive;

-- Réunions purgées mais non archivées (anomalie)
SELECT
  id,
  topic,
  purge_date,
  start,
  end
FROM meetings
WHERE purge_date IS NOT NULL
  AND id NOT IN (SELECT original_meeting_id FROM meeting_archive);

-- ============================================================================
-- SECTION 6 : ANALYSE DE TAILLE
-- ============================================================================

-- Taille estimée de la table meeting_archive
SELECT
  'meeting_archive' as table_name,
  COUNT(*) as nb_lignes,
  ROUND(((COUNT(*) * 8) / 1024.0 / 1024.0), 2) as taille_estimee_mb
FROM meeting_archive
UNION ALL
-- Taille estimée de la table meetings
SELECT
  'meetings' as table_name,
  COUNT(*) as nb_lignes,
  ROUND(((COUNT(*) * 8) / 1024.0 / 1024.0), 2) as taille_estimee_mb
FROM meetings;

-- ============================================================================
-- SECTION 7 : DONNÉES MANQUANTES (AUDIT)
-- ============================================================================

-- Réunions sans participants et sans archive (données perdues)
SELECT
  m.id,
  m.topic,
  m.start,
  m.end,
  'PAS_DE_PARTICIPANTS_PAS_DE_ARCHIVE' as type_anomalie
FROM meetings m
WHERE id NOT IN (SELECT DISTINCT meeting_id FROM participants)
  AND id NOT IN (SELECT original_meeting_id FROM meeting_archive)
ORDER BY m.start DESC;

-- Réunions archivées avec 0 participants (anomalie)
SELECT
  id,
  original_meeting_id,
  topic,
  total_participants,
  archived_at
FROM meeting_archive
WHERE total_participants = 0;

-- ============================================================================
-- SECTION 8 : PURGE MANUELLE (À UTILISER AVEC PRUDENCE)
-- ============================================================================

-- ATTENTION : Ces requêtes SUPPRIMENT des données

-- Supprimer les archives plus vieilles que 1 an
-- DELETE FROM meeting_archive
-- WHERE archived_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 365 DAY);

-- Supprimer les archives d'une année spécifique (ex: 2023)
-- DELETE FROM meeting_archive
-- WHERE EXTRACT(YEAR FROM archived_at) = 2023;

-- Supprimer les archives d'un intervalle de dates
-- DELETE FROM meeting_archive
-- WHERE archived_at BETWEEN '2023-01-01' AND '2023-12-31';

-- ============================================================================
-- SECTION 9 : VÉRIFICATION DE L'INTÉGRITÉ
-- ============================================================================

-- Vérifier les clés étrangères dans les données actives
SELECT
  COUNT(*) as participants_sans_meeting
FROM participants
WHERE meeting_id NOT IN (SELECT id FROM meetings);

-- Vérifier les archives orphelines
SELECT
  COUNT(*) as archives_sans_meeting_original
FROM meeting_archive
WHERE original_meeting_id NOT IN (SELECT id FROM meetings);

-- ============================================================================
-- SECTION 10 : RAPPORT DE SYNTHÈSE
-- ============================================================================

SELECT
  'MEETINGS' as type_data,
  COUNT(*) as total,
  SUM(CASE WHEN purge_date IS NULL THEN 1 ELSE 0 END) as actives,
  SUM(CASE WHEN purge_date IS NOT NULL THEN 1 ELSE 0 END) as purgees
FROM meetings
UNION ALL
SELECT
  'PARTICIPANTS' as type_data,
  COUNT(*) as total,
  COUNT(*) as actives,
  0 as purgees
FROM participants
UNION ALL
SELECT
  'ARCHIVES' as type_data,
  COUNT(*) as total,
  COUNT(*) as actives,
  0 as purgees
FROM meeting_archive;

-- ============================================================================
-- NOTES
-- ============================================================================
--
-- Ces requêtes vous permettent de :
-- 1. Vérifier que la purge fonctionne correctement
-- 2. Générer des rapports sur les réunions archivées
-- 3. Analyser les ratios présentiels/visio
-- 4. Détecter les anomalies ou incohérences
-- 5. Gérer les archives manuellement si nécessaire
--
-- La section 8 (purge manuelle) doit être utilisée avec PRUDENCE
-- Vérifiez toujours les SELECT avant d'exécuter les DELETE correspondants
--
