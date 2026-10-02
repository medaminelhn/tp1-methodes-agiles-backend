package com.tp1methodesagiles.gestiondessalles.dto;

import com.tp1methodesagiles.gestiondessalles.entity.Reservation;
import java.time.LocalDateTime;

public record CalendrierItem(
        Integer id,
        Integer salleId,
        Integer utilisateurId,
        Integer semestreId,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        String motif,
        String matiere,
        String statut,
        String demandeType,
        String demandeStatut,
        String demandeCommentaire,
        Integer demandeSalleId,
        LocalDateTime demandeDateDebut,
        String demandeMatiere) {

    public static CalendrierItem from(Reservation r) {
        return new CalendrierItem(
                r.getId(), r.getSalleId(), r.getUtilisateurId(), r.getSemestreId(),
                r.getDateDebut(), r.getDateFin(), r.getMotif(), r.getMatiere(),
                r.getStatut(), r.getDemandeType(), r.getDemandeStatut(),
                r.getDemandeCommentaire(), r.getDemandeSalleId(),
                r.getDemandeDateDebut(), r.getDemandeMatiere());
    }
}
