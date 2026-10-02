package com.tp1methodesagiles.gestiondessalles.dto;

import java.time.LocalDateTime;

public record Creneau(Integer salleId, LocalDateTime debut) {
}