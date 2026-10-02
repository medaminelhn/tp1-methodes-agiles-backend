-- Execute once on the existing database before using action-request endpoints.
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS demande_type VARCHAR(30);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS demande_statut VARCHAR(30);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS demande_commentaire TEXT;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS demande_salle_id INTEGER;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS demande_date_debut TIMESTAMP;
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS demande_matiere VARCHAR(255);
