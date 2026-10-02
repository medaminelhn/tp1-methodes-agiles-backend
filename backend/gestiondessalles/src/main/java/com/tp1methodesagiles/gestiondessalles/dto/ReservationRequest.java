package com.tp1methodesagiles.gestiondessalles.dto;

import java.util.List;

public record ReservationRequest(
        Integer utilisateurId,
        Integer semestreId,
        String matiere,
        Integer volumeHoraireTotal,
        String commentaire,
        List<Creneau> creneaux) {
}