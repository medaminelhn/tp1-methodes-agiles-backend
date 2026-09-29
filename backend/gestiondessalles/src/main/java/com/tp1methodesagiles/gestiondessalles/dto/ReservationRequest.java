package com.example.salles.dto;

import java.util.List;

public record ReservationRequest(
        Integer utilisateurId,
        Integer semestreId,
        String matiere,
        Integer volumeHoraireTotal,
        String commentaire,
        List<Creneau> creneaux) {
}