--table des users 
CREATE TABLE utilisateurs (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--table des salles
CREATE TABLE salles (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    capacite INT NOT NULL,
    disponible BOOLEAN DEFAULT TRUE
);

--table semestre
CREATE TABLE semestres (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    CONSTRAINT check_semestre_dates CHECK (date_fin > date_debut)
);

--table volume horaire
CREATE TABLE volumes_horaires (
    id SERIAL PRIMARY KEY,
    utilisateur_id INT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    semestre_id INT NOT NULL REFERENCES semestres(id) ON DELETE CASCADE,
    volume_horaire_total INT NOT NULL,
    CONSTRAINT check_volume_positif CHECK (volume_horaire_total > 0),
    CONSTRAINT unique_prof_semestre UNIQUE (utilisateur_id, semestre_id)
);

--table des réservations
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    salle_id INT NOT NULL REFERENCES salles(id) ON DELETE CASCADE,
    utilisateur_id INT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    semestre_id INT NOT NULL REFERENCES semestres(id),
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    motif VARCHAR(255),
    matiere VARCHAR(150),
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_dates CHECK (date_fin > date_debut),
    CONSTRAINT check_duree_2h CHECK (date_fin = date_debut + INTERVAL '2 hours')
);



