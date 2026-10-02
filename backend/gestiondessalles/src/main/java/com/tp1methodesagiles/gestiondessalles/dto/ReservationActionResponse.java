package com.example.salles.dto;

public record ReservationActionResponse(ReservationView reservation, AlternativeReservation alternative, String message) {
}
