package com.tp1methodesagiles.gestiondessalles.repository;

import com.tp1methodesagiles.gestiondessalles.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUtilisateurIdOrderByDateDebutAsc(Integer utilisateurId);

    List<Reservation> findByUtilisateurIdAndSemestreIdOrderByDateDebutAsc(
            Integer utilisateurId, Integer semestreId);

    long countByUtilisateurIdAndSemestreIdAndStatut(Integer utilisateurId, Integer semestreId, String statut);

    @Query("""
        select count(r) > 0 from Reservation r
        where r.salleId = :salleId and r.statut = 'CONFIRMEE'
          and r.dateDebut < :fin and r.dateFin > :debut
        """)
    boolean existsConflit(@Param("salleId") Integer salleId,
                          @Param("debut") LocalDateTime debut,
                          @Param("fin") LocalDateTime fin);
}