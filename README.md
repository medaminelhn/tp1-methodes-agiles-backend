# Gestion des reservations - Documentation du developpement

Ce README resume le travail realise pour developper la partie **gestion des reservations** du projet de gestion des salles.

## Objectif

Permettre a une secretaire de consulter, filtrer, accepter ou refuser les demandes de reservation, tout en evitant les doubles reservations et en proposant une alternative lorsque c'est possible.

## Fonctionnalites developpees

- Affichage des reservations dans un tableau.
- Recherche par professeur, salle, matiere, motif ou statut.
- Filtrage par statut et par salle.
- Affichage des details d'une reservation selectionnee.
- Affichage clair des statuts :
  - `EN_ATTENTE`
  - `CONFIRMEE`
  - `REFUSEE`
  - `ANNULEE`
- Actions disponibles selon le statut.
- Acceptation d'une reservation en attente.
- Refus d'une reservation en attente.
- Verification backend des conflits de creneaux.
- Proposition d'une salle ou d'un creneau alternatif si possible.
- Affichage des messages de succes et d'erreur cote interface.

## Backend Spring Boot

### Endpoints ajoutes ou completes

- `GET /api/reservations`
  - Liste les reservations.
  - Accepte des filtres optionnels : `recherche`, `statut`, `salleId`, `utilisateurId`, `semestreId`.

- `GET /api/reservations/{id}`
  - Retourne les details d'une reservation.

- `POST /api/reservations`
  - Cree une demande de reservation.
  - Les reservations sont maintenant creees avec le statut `EN_ATTENTE`.

- `PATCH /api/reservations/{id}/accepter`
  - Accepte une reservation.
  - Verifie les conflits de creneaux.
  - Assigne une salle disponible.
  - Passe la reservation en `CONFIRMEE`.
  - Met la salle a `disponible = false`.

- `PATCH /api/reservations/{id}/refuser`
  - Refuse une reservation.
  - Passe la reservation en `REFUSEE`.
  - Retourne une alternative si elle existe.

### Regles metier

- Une reservation ne peut etre acceptee que si elle est en `EN_ATTENTE`.
- Une reservation confirmee ne peut pas etre refusee directement.
- Une reservation confirmee bloque le creneau de sa salle.
- Le backend verifie toujours les conflits avant confirmation.
- Le controle frontend ne remplace pas la validation backend.
- Les creneaux restent fixes a 2 heures, comme prevu par le schema SQL.

### Concurrence

Des verrous pessimistes ont ete ajoutes pour reduire le risque de double confirmation simultanee :

- `ReservationRepository.findByIdForUpdate`
- `SalleRepository.findByIdForUpdate`

Ces verrous sont utilises pendant les actions d'acceptation et de refus.

### DTO ajoutes

- `ReservationView`
  - Vue detaillee envoyee au frontend.
  - Contient le nom du professeur, le nom de la salle, la duree et les informations principales.

- `AlternativeReservation`
  - Represente une proposition alternative.

- `ReservationActionResponse`
  - Reponse commune aux actions accepter/refuser.
  - Contient la reservation, un message et une alternative eventuelle.

## Frontend Angular

Le placeholder Angular initial a ete remplace par une interface de gestion des reservations.

### Elements de l'interface

- Barre de titre.
- Filtres de recherche.
- Tableau des reservations.
- Badges de statut.
- Panneau de details.
- Boutons `Accepter` et `Refuser`.
- Zone d'affichage des messages.
- Bloc d'affichage d'une alternative proposee.

### Appels HTTP utilises

- `GET /api/salles`
- `GET /api/reservations`
- `PATCH /api/reservations/{id}/accepter`
- `PATCH /api/reservations/{id}/refuser`

Le `HttpClient` Angular a ete active dans `app.config.ts`.

## Base de donnees

Le fichier `DB.sql` a ete mis a jour :

- ajout de la colonne `matiere` dans la table `reservations` ;
- statut par defaut change en `EN_ATTENTE`.

Une migration SQL a aussi ete ajoutee :

- `migration_reservations_admin.sql`

Elle permet de mettre a jour une base deja existante :

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS matiere VARCHAR(150);

ALTER TABLE reservations
    ALTER COLUMN statut SET DEFAULT 'EN_ATTENTE';
```

## Tests ajoutes

Un test unitaire a ete ajoute pour le service de reservation :

- `ReservationServiceTest`

Cas couverts :

- acceptation d'une reservation sans conflit ;
- refus d'une reservation ;
- rejet d'une confirmation en cas de conflit ;
- disponibilite ou absence d'une alternative.

## Fichiers modifies ou ajoutes

### Backend

- `pom.xml`
- `mvnw.cmd`
- `GestiondessallesApplication.java`
- `ReservationController.java`
- `ReservationService.java`
- `ReservationRepository.java`
- `SalleRepository.java`
- `ReservationView.java`
- `AlternativeReservation.java`
- `ReservationActionResponse.java`
- `ReservationServiceTest.java`

### Frontend

- `app.component.ts`
- `app.component.html`
- `app.component.scss`
- `app.component.spec.ts`
- `app.config.ts`

### SQL

- `DB.sql`
- `migration_reservations_admin.sql`

## Verifications tentees

Les commandes suivantes ont ete tentees :

```bash
mvnw.cmd test
npm run build
npm install
```

Elles n'ont pas pu etre terminees dans l'environnement de developpement :

- Maven devait telecharger sa distribution et la connexion a ete interrompue.
- Angular ne pouvait pas compiler car `node_modules` n'etait pas installe.
- `npm install` a echoue par manque d'espace disque avec l'erreur `ENOSPC`.

Les dossiers temporaires generes par ces essais ont ete nettoyes.

## Limites connues

- Le modele existant utilise `salles.disponible` comme booleen global. Une salle confirmee passe donc a `false`, mais aucune liberation automatique apres la fin du creneau n'est encore prevue.
- La recherche d'alternative reste simple : meme creneau dans une autre salle, puis creneaux suivants de 2 heures.
- Les statuts sont encore des chaines de caracteres et non une enum Java.

## Commandes utiles

Backend :

```bash
cd "tp1-methodes-agiles-backend/backend/gestiondessalles"
mvnw.cmd test
```

Frontend :

```bash
cd "tp1-methodes-agiles-frontend/tp1-methodes-agiles-front"
npm install
npm run build
```

