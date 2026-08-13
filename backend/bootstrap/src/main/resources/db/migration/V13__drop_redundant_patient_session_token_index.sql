-- La contrainte UNIQUE sur token_hash (V10:7) crée déjà, implicitement, un
-- index B-tree qui sert toute recherche « WHERE token_hash = ? » (validation
-- de session à chaque requête patient authentifiée). idx_patient_session_token
-- (V10:12) est un doublon EXACT de cet index : aucun gain en lecture, coût
-- réel en écriture (maintenance de deux index identiques à chaque insertion/
-- suppression de session) et en espace disque.
DROP INDEX idx_patient_session_token;
