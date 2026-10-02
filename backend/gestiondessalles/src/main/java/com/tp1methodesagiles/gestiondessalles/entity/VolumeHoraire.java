package com.example.salles.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "volumes_horaires")
@Getter @Setter
public class VolumeHoraire {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer utilisateurId;
    private Integer semestreId;
    private Integer volumeHoraireTotal;
}