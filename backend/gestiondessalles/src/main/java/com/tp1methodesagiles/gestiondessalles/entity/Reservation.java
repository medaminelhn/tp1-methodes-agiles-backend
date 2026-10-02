package com.example.salles.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter @Setter
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer salleId;
    private Integer utilisateurId;
    private Integer semestreId;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String motif;      // comment
    private String matiere;
    private String statut;

    @Column(insertable = false, updatable = false) // let the DB default apply
    private LocalDateTime dateCreation;
}