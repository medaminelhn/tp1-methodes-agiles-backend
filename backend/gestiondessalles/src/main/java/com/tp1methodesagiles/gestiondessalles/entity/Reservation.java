package com.tp1methodesagiles.gestiondessalles.entity;

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

    private String demandeType;
    private String demandeStatut;
    private String demandeCommentaire;
    private Integer demandeSalleId;
    private LocalDateTime demandeDateDebut;
    private String demandeMatiere;

    @Column(insertable = false, updatable = false) // let the DB default apply
    private LocalDateTime dateCreation;
}