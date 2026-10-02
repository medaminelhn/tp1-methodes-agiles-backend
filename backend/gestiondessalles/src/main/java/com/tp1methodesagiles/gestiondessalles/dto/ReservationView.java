package com.example.salles.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record ReservationView(
        Integer id,
        Integer salleId,
        String salleNom,
        Integer utilisateurId,
        String professeurNom,
        Integer semestreId,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        long dureeHeures,
        String matiere,
        String motif,
        String statut,
        LocalDateTime dateCreation) {

    public ReservationView {
        dureeHeures = dateDebut != null && dateFin != null ? Duration.between(dateDebut, dateFin).toHours() : 0;
    }
}
