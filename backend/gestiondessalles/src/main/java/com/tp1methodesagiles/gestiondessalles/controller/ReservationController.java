package com.example.salles.controller;

import com.example.salles.dto.ReservationRequest;
import com.example.salles.dto.ReservationActionResponse;
import com.example.salles.dto.ReservationView;
import com.example.salles.dto.Solde;
import com.example.salles.entity.Reservation;
import com.example.salles.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping("/solde")
    public Solde solde(@RequestParam Integer utilisateurId, @RequestParam Integer semestreId) {
        return service.solde(utilisateurId, semestreId);
    }

    @GetMapping("/reservations")
    public List<ReservationView> reservations(@RequestParam(required = false) String recherche,
                                              @RequestParam(required = false) String statut,
                                              @RequestParam(required = false) Integer salleId,
                                              @RequestParam(required = false) Integer utilisateurId,
                                              @RequestParam(required = false) Integer semestreId) {
        return service.rechercher(recherche, statut, salleId, utilisateurId, semestreId);
    }

    @GetMapping("/reservations/{id}")
    public ReservationView reservation(@PathVariable Integer id) {
        return service.detail(id);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Reservation> creer(@RequestBody ReservationRequest req) {
        return service.creer(req);
    }

    @PatchMapping("/reservations/{id}/accepter")
    public ReservationActionResponse accepter(@PathVariable Integer id) {
        return service.accepter(id);
    }

    @PatchMapping("/reservations/{id}/refuser")
    public ReservationActionResponse refuser(@PathVariable Integer id) {
        return service.refuser(id);
    }
}
