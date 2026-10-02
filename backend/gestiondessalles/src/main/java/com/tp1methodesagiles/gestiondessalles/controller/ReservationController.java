package com.tp1methodesagiles.gestiondessalles.controller;

import com.tp1methodesagiles.gestiondessalles.dto.CalendrierItem;
import com.tp1methodesagiles.gestiondessalles.dto.ReservationActionRequest;
import com.tp1methodesagiles.gestiondessalles.dto.ReservationRequest;
import com.tp1methodesagiles.gestiondessalles.dto.Solde;
import com.tp1methodesagiles.gestiondessalles.entity.Reservation;
import com.tp1methodesagiles.gestiondessalles.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping("/solde")
    public Solde solde(@RequestParam Integer utilisateurId, @RequestParam Integer semestreId) {
        return service.solde(utilisateurId, semestreId);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Reservation> creer(@RequestBody ReservationRequest req) {
        return service.creer(req);
    }

    @GetMapping("/calendrier")
    public List<CalendrierItem> calendrier(@RequestParam Integer utilisateurId,
                                           @RequestParam(required = false) Integer semestreId) {
        return service.calendrier(utilisateurId, semestreId)
                .stream()
                .map(CalendrierItem::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/reservations/{reservationId}")
    public Reservation consulter(@PathVariable Integer reservationId,
                                  @RequestParam Integer utilisateurId) {
        return service.consulter(utilisateurId, reservationId);
    }

    @PostMapping("/reservations/{reservationId}/annulation")
    public Reservation demanderAnnulation(@PathVariable Integer reservationId,
                                          @RequestParam Integer utilisateurId,
                                          @RequestBody(required = false) ReservationActionRequest req) {
        return service.demanderAnnulation(
                utilisateurId, reservationId, req == null ? null : req.commentaire());
    }

    @PostMapping("/reservations/{reservationId}/modification")
    public Reservation demanderModification(@PathVariable Integer reservationId,
                                            @RequestParam Integer utilisateurId,
                                            @RequestBody ReservationActionRequest req) {
        return service.demanderModification(utilisateurId, reservationId, req);
    }
}
