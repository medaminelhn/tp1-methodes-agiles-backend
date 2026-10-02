package com.example.salles.repository;

import com.example.salles.entity.VolumeHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VolumeHoraireRepository extends JpaRepository<VolumeHoraire, Integer> {
    Optional<VolumeHoraire> findByUtilisateurIdAndSemestreId(Integer utilisateurId, Integer semestreId);
}