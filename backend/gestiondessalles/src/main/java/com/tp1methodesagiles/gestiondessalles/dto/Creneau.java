package com.example.salles.dto;

import java.time.LocalDateTime;

public record Creneau(Integer salleId, LocalDateTime debut) {
}