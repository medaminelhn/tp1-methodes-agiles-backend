package com.tp1methodesagiles.gestiondessalles;

import com.tp1methodesagiles.gestiondessalles.entity.Reservation;
import com.tp1methodesagiles.gestiondessalles.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class H2DatabaseIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void reservationRepositoryFonctionneAvecH2() {
        Reservation reservation = new Reservation();
        reservation.setSalleId(1);
        reservation.setUtilisateurId(7);
        reservation.setSemestreId(1);
        reservation.setDateDebut(LocalDateTime.of(2026, 10, 10, 8, 0));
        reservation.setDateFin(LocalDateTime.of(2026, 10, 10, 10, 0));
        reservation.setMatiere("Agile");
        reservation.setStatut("CONFIRMEE");

        Reservation saved = reservationRepository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(reservationRepository.findByUtilisateurIdOrderByDateDebutAsc(7))
                .extracting(Reservation::getMatiere)
                .containsExactly("Agile");
    }
}
