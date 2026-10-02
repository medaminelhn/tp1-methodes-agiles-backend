package com.example.salles.controller;

import com.example.salles.entity.Salle;
import com.example.salles.entity.Semestre;
import com.example.salles.entity.Utilisateur;
import com.example.salles.repository.SalleRepository;
import com.example.salles.repository.SemestreRepository;
import com.example.salles.repository.UtilisateurRepository;
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