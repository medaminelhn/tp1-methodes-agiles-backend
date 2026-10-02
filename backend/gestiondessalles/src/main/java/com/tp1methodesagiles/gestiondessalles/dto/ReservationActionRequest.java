package com.tp1methodesagiles.gestiondessalles.dto;

import java.time.LocalDateTime;

public record ReservationActionRequest(
        String commentaire,
        Integer salleId,
        LocalDateTime debut,
        String matiere) {
}
