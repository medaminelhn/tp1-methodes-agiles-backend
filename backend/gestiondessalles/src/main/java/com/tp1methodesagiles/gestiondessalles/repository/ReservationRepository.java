package com.example.salles.repository;

import com.example.salles.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    long countByUtilisateurIdAndSemestreIdAndStatut(Integer utilisateurId, Integer semestreId, String statut);

    @Query("""
        select count(r) > 0 from Reservation r
        where r.salleId = :salleId and r.statut = 'CONFIRMEE'
          and r.dateDebut < :fin and r.dateFin > :debut
        """)
    boolean existsConflit(@Param("salleId") Integer salleId,
                          @Param("debut") LocalDateTime debut,
                          @Param("fin") LocalDateTime fin);

    @Query("""
        select count(r) > 0 from Reservation r
        where r.salleId = :salleId and r.statut = 'CONFIRMEE' and r.id <> :reservationId
          and r.dateDebut < :fin and r.dateFin > :debut
        """)
    boolean existsConflitExcluding(@Param("salleId") Integer salleId,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin,
                                   @Param("reservationId") Integer reservationId);

    boolean existsBySalleIdAndStatut(Integer salleId, String statut);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Integer id);
}
