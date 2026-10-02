package com.tp1methodesagiles.gestiondessalles;

import com.example.salles.dto.ReservationActionResponse;
import com.example.salles.dto.SalleAffectationRequest;
import com.example.salles.entity.Reservation;
import com.example.salles.entity.Salle;
import com.example.salles.entity.Utilisateur;
import com.example.salles.exception.BusinessException;
import com.example.salles.repository.ReservationRepository;
import com.example.salles.repository.SalleRepository;
import com.example.salles.repository.SemestreRepository;
import com.example.salles.repository.UtilisateurRepository;
import com.example.salles.repository.VolumeHoraireRepository;
import com.example.salles.service.ReservationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private final UtilisateurRepository utilisateurRepo = mock(UtilisateurRepository.class);
    private final SalleRepository salleRepo = mock(SalleRepository.class);
    private final SemestreRepository semestreRepo = mock(SemestreRepository.class);
    private final VolumeHoraireRepository volumeRepo = mock(VolumeHoraireRepository.class);
    private final ReservationRepository reservationRepo = mock(ReservationRepository.class);
    private final ReservationService service = new ReservationService(
            utilisateurRepo, salleRepo, semestreRepo, volumeRepo, reservationRepo);

    @Test
    void accepteReservationSansConflit() {
        Reservation reservation = reservation(1, 10, "EN_ATTENTE");
        Salle salle = salle(10, "A1", true);
        when(reservationRepo.findByIdForUpdate(1)).thenReturn(Optional.of(reservation));
        when(salleRepo.findByIdForUpdate(10)).thenReturn(Optional.of(salle));
        when(reservationRepo.existsConflit(10, reservation.getDateDebut(), reservation.getDateFin())).thenReturn(false);
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salleRepo.save(any(Salle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockViewLookups(reservation, salle);

        ReservationActionResponse response = service.accepter(1);

        assertEquals("CONFIRMEE", response.reservation().statut());
        assertEquals(false, salle.getDisponible());
        assertNull(response.alternative());
    }

    @Test
    void refuseReservationAvecAlternativeDisponible() {
        Reservation reservation = reservation(1, 10, "EN_ATTENTE");
        Salle salle = salle(10, "A1", true);
        Salle alternative = salle(11, "B1", true);
        when(reservationRepo.findByIdForUpdate(1)).thenReturn(Optional.of(reservation));
        when(salleRepo.findAll()).thenReturn(List.of(salle, alternative));
        when(reservationRepo.existsConflit(11, reservation.getDateDebut(), reservation.getDateFin())).thenReturn(false);
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockViewLookups(reservation, salle);

        ReservationActionResponse response = service.refuser(1);

        assertEquals("REFUSEE", response.reservation().statut());
        assertNotNull(response.alternative());
        assertEquals(11, response.alternative().salleId());
    }

    @Test
    void rejetteConfirmationEnCasDeConflitSansAlternative() {
        Reservation reservation = reservation(1, 10, "EN_ATTENTE");
        Salle salle = salle(10, "A1", true);
        when(reservationRepo.findByIdForUpdate(1)).thenReturn(Optional.of(reservation));
        when(salleRepo.findByIdForUpdate(10)).thenReturn(Optional.of(salle));
        when(reservationRepo.existsConflit(eq(10), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(true);
        when(salleRepo.findAll()).thenReturn(List.of(salle));

        assertThrows(BusinessException.class, () -> service.accepter(1));
    }

    @Test
    void refuseReservationSansAlternativeDisponible() {
        Reservation reservation = reservation(1, 10, "EN_ATTENTE");
        Salle salle = salle(10, "A1", false);
        when(reservationRepo.findByIdForUpdate(1)).thenReturn(Optional.of(reservation));
        when(salleRepo.findAll()).thenReturn(List.of(salle));
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mockViewLookups(reservation, salle);

        ReservationActionResponse response = service.refuser(1);

        assertEquals("REFUSEE", response.reservation().statut());
        assertNull(response.alternative());
    }

    @Test
    void modifieSalleAttribueeSansConflit() {
        Reservation reservation = reservation(1, 10, "CONFIRMEE");
        Salle ancienneSalle = salle(10, "A1", false);
        Salle nouvelleSalle = salle(11, "B1", true);
        when(reservationRepo.findByIdForUpdate(1)).thenReturn(Optional.of(reservation));
        when(salleRepo.findByIdForUpdate(11)).thenReturn(Optional.of(nouvelleSalle));
        when(salleRepo.findByIdForUpdate(10)).thenReturn(Optional.of(ancienneSalle));
        when(reservationRepo.existsConflitExcluding(11, reservation.getDateDebut(),
                reservation.getDateFin(), reservation.getId())).thenReturn(false);
        when(reservationRepo.existsBySalleIdAndStatut(10, "CONFIRMEE")).thenReturn(false);
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salleRepo.save(any(Salle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salleRepo.findById(11)).thenReturn(Optional.of(nouvelleSalle));
        mockUtilisateurLookup(reservation);

        ReservationActionResponse response = service.modifierSalle(1, new SalleAffectationRequest(11));

        assertEquals(11, response.reservation().salleId());
        assertEquals(false, nouvelleSalle.getDisponible());
        assertEquals(true, ancienneSalle.getDisponible());
    }

    @Test
    void rejetteModificationSalleEnCasDeConflit() {
        Reservation reservation = reservation(1, 10, "CONFIRMEE");
        Salle nouvelleSalle = salle(11, "B1", true);
        when(reservationRepo.findByIdForUpdate(1)).thenReturn(Optional.of(reservation));
        when(salleRepo.findByIdForUpdate(11)).thenReturn(Optional.of(nouvelleSalle));
        when(reservationRepo.existsConflitExcluding(11, reservation.getDateDebut(),
                reservation.getDateFin(), reservation.getId())).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.modifierSalle(1, new SalleAffectationRequest(11)));
    }

    private void mockViewLookups(Reservation reservation, Salle salle) {
        when(salleRepo.findById(reservation.getSalleId())).thenReturn(Optional.of(salle));
        mockUtilisateurLookup(reservation);
    }

    private void mockUtilisateurLookup(Reservation reservation) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(reservation.getUtilisateurId());
        utilisateur.setPrenom("Ada");
        utilisateur.setNom("Lovelace");
        when(utilisateurRepo.findById(reservation.getUtilisateurId())).thenReturn(Optional.of(utilisateur));
    }

    private Reservation reservation(Integer id, Integer salleId, String statut) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setSalleId(salleId);
        reservation.setUtilisateurId(20);
        reservation.setSemestreId(30);
        reservation.setDateDebut(LocalDateTime.of(2026, 10, 5, 10, 0));
        reservation.setDateFin(LocalDateTime.of(2026, 10, 5, 12, 0));
        reservation.setMatiere("Architecture");
        reservation.setStatut(statut);
        return reservation;
    }

    private Salle salle(Integer id, String nom, Boolean disponible) {
        Salle salle = new Salle();
        salle.setId(id);
        salle.setNom(nom);
        salle.setCapacite(30);
        salle.setDisponible(disponible);
        return salle;
    }
}
