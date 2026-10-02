package com.tp1methodesagiles.gestiondessalles;

import com.tp1methodesagiles.gestiondessalles.controller.ReservationController;
import com.tp1methodesagiles.gestiondessalles.dto.ReservationActionRequest;
import com.tp1methodesagiles.gestiondessalles.entity.Reservation;
import com.tp1methodesagiles.gestiondessalles.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ReservationService service;

    @Test
    void calendrierRetourneLesReservationsDuProfesseur() throws Exception {
        Reservation r = reservation(10, 7);
        when(service.calendrier(7, null)).thenReturn(List.of(r));

        mockMvc.perform(get("/api/calendrier").param("utilisateurId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].utilisateurId").value(7))
                .andExpect(jsonPath("$[0].statut").value("CONFIRMEE"));
    }

    @Test
    void annulationCreeUneDemande() throws Exception {
        Reservation r = reservation(10, 7);
        r.setDemandeType("ANNULATION");
        r.setDemandeStatut("EN_ATTENTE");
        when(service.demanderAnnulation(eq(7), eq(10), any())).thenReturn(r);

        mockMvc.perform(post("/api/reservations/10/annulation")
                        .param("utilisateurId", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"commentaire":"Indisponibilité"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.demandeType").value("ANNULATION"))
                .andExpect(jsonPath("$.demandeStatut").value("EN_ATTENTE"));
    }

    @Test
    void modificationCreeUneDemande() throws Exception {
        Reservation r = reservation(10, 7);
        r.setDemandeType("MODIFICATION");
        r.setDemandeStatut("EN_ATTENTE");
        r.setDemandeSalleId(3);
        r.setDemandeDateDebut(LocalDateTime.of(2026, 10, 12, 10, 0));
        when(service.demanderModification(eq(7), eq(10), any(ReservationActionRequest.class)))
                .thenReturn(r);

        mockMvc.perform(post("/api/reservations/10/modification")
                        .param("utilisateurId", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"salleId":3,"debut":"2026-10-12T10:00:00","commentaire":"Changement de cours"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demandeType").value("MODIFICATION"))
                .andExpect(jsonPath("$.demandeStatut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.demandeSalleId").value(3));
    }

    private Reservation reservation(Integer id, Integer utilisateurId) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setUtilisateurId(utilisateurId);
        r.setSalleId(1);
        r.setSemestreId(1);
        r.setDateDebut(LocalDateTime.of(2026, 10, 10, 8, 0));
        r.setDateFin(LocalDateTime.of(2026, 10, 10, 10, 0));
        r.setMatiere("Agile");
        r.setStatut("CONFIRMEE");
        return r;
    }
}
