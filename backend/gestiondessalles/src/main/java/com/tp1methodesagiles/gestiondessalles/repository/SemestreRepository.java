package com.tp1methodesagiles.gestiondessalles.repository;

import com.tp1methodesagiles.gestiondessalles.entity.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemestreRepository extends JpaRepository<Semestre, Integer> {
}