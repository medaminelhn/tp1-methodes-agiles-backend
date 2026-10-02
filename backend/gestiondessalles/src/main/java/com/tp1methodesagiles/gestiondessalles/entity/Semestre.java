package com.tp1methodesagiles.gestiondessalles.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "semestres")
@Getter @Setter
public class Semestre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String libelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}