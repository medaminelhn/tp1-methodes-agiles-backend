package com.example.salles.repository;

import com.example.salles.entity.Salle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalleRepository extends JpaRepository<Salle, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Salle s where s.id = :id")
    Optional<Salle> findByIdForUpdate(@Param("id") Integer id);
}
