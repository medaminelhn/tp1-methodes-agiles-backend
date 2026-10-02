package com.tp1methodesagiles.gestiondessalles.controller;

import com.tp1methodesagiles.gestiondessalles.entity.Salle;
import com.tp1methodesagiles.gestiondessalles.entity.Semestre;
import com.tp1methodesagiles.gestiondessalles.entity.Utilisateur;
import com.tp1methodesagiles.gestiondessalles.repository.SalleRepository;
import com.tp1methodesagiles.gestiondessalles.repository.SemestreRepository;
import com.tp1methodesagiles.gestiondessalles.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferentielController {

    private final UtilisateurRepository utilisateurRepo;
    private final SalleRepository salleRepo;
    private final SemestreRepository semestreRepo;

    @GetMapping("/utilisateurs")
    public List<Utilisateur> utilisateurs() {
        return utilisateurRepo.findAll();
    }

    @GetMapping("/salles")
    public List<Salle> salles() {
        return salleRepo.findAll();
    }

    @GetMapping("/semestres")
    public List<Semestre> semestres() {
        return semestreRepo.findAll();
    }
}