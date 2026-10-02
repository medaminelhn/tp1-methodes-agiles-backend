package com.tp1methodesagiles.gestiondessalles.repository;

import com.tp1methodesagiles.gestiondessalles.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
}