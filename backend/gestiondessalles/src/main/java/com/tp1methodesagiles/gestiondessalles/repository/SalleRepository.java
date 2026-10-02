package com.tp1methodesagiles.gestiondessalles.repository;

import com.tp1methodesagiles.gestiondessalles.entity.Salle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalleRepository extends JpaRepository<Salle, Integer> {
}