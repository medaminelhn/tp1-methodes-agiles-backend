package com.example.salles.dto;

import java.time.LocalDateTime;

public record AlternativeReservation(Integer salleId, String salleNom, LocalDateTime debut, LocalDateTime fin) {
}
