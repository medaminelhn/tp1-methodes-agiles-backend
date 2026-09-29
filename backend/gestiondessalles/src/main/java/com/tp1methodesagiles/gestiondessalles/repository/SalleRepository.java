package com.example.salles.repository;

import com.example.salles.entity.Salle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalleRepository extends JpaRepository<Salle, Integer> {
}