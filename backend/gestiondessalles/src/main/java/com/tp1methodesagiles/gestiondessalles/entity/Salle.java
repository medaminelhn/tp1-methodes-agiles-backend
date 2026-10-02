package com.example.salles.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salles")
@Getter @Setter
public class Salle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nom;
    private Integer capacite;
    private Boolean disponible;
}