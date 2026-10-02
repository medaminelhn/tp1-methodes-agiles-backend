package com.tp1methodesagiles.gestiondessalles.service;

import com.tp1methodesagiles.gestiondessalles.dto.Creneau;
import com.tp1methodesagiles.gestiondessalles.dto.ReservationRequest;
import com.tp1methodesagiles.gestiondessalles.dto.Solde;
import com.tp1methodesagiles.gestiondessalles.entity.Reservation;
import com.tp1methodesagiles.gestiondessalles.entity.Salle;
import com.tp1methodesagiles.gestiondessalles.entity.Semestre;
import com.tp1methodesagiles.gestiondessalles.entity.VolumeHoraire;
import com.tp1methodesagiles.gestiondessalles.exception.BusinessException;
import com.tp1methodesagiles.gestiondessalles.repository.ReservationRepository;
import com.tp1methodesagiles.gestiondessalles.repository.SalleRepository;
import com.tp1methodesagiles.gestiondessalles.repository.SemestreRepository;
import com.tp1methodesagiles.gestiondessalles.repository.UtilisateurRepository;
import com.tp1methodesagiles.gestiondessalles.repository.VolumeHoraireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int DUREE = 2;

    private final UtilisateurRepository utilisateurRepo;
    private final SalleRepository salleRepo;
    private final SemestreRepository semestreRepo;
    private final VolumeHoraireRepository volumeRepo;
    private final ReservationRepository reservationRepo;

    public Solde solde(Integer utilisateurId, Integer semestreId) {
        int consomme = (int) reservationRepo
                .countByUtilisateurIdAndSemestreIdAndStatut(utilisateurId, semestreId, "CONFIRMEE") * DUREE;
        return volumeRepo.findByUtilisateurIdAndSemestreId(utilisateurId, semestreId)
                .map(v -> new Solde(v.getVolumeHoraireTotal(), consomme, v.getVolumeHoraireTotal() - consomme))
                .orElse(new Solde(null, consomme, null));
    }

    @Transactional
    public List<Reservation> creer(ReservationRequest req) {
        if (req.creneaux() == null || req.creneaux().isEmpty())
            throw new BusinessException("Ajoutez au moins un créneau.");
        if (req.matiere() == null || req.matiere().isBlank())
            throw new BusinessException("La matière est obligatoire.");
        utilisateurRepo.findById(req.utilisateurId())
                .orElseThrow(() -> new BusinessException("Professeur introuvable."));
        Semestre sem = semestreRepo.findById(req.semestreId())
                .orElseThrow(() -> new BusinessException("Semestre introuvable."));

        int demandees = req.creneaux().size() * DUREE;

        // First request for this semester: create the volume, slots must match it exactly
        if (volumeRepo.findByUtilisateurIdAndSemestreId(req.utilisateurId(), req.semestreId()).isEmpty()) {
            if (req.volumeHoraireTotal() == null || demandees != req.volumeHoraireTotal())
                throw new BusinessException("La somme des créneaux (" + demandees
                        + "h) doit être égale au volume horaire déclaré (" + req.volumeHoraireTotal() + "h).");
            VolumeHoraire v = new VolumeHoraire();
            v.setUtilisateurId(req.utilisateurId());
            v.setSemestreId(req.semestreId());
            v.setVolumeHoraireTotal(req.volumeHoraireTotal());
            volumeRepo.save(v);
        }

        Solde solde = solde(req.utilisateurId(), req.semestreId());
        if (demandees > solde.restant())
            throw new BusinessException("Volume horaire dépassé : il reste " + solde.restant()
                    + "h, " + demandees + "h demandées.");

        List<Reservation> aCreer = new ArrayList<>();
        for (Creneau c : req.creneaux()) {
            Salle salle = salleRepo.findById(c.salleId())
                    .orElseThrow(() -> new BusinessException("Salle introuvable."));
            if (!Boolean.TRUE.equals(salle.getDisponible()))
                throw new BusinessException("La salle " + salle.getNom() + " n'est pas disponible.");

            LocalDateTime debut = c.debut();
            LocalDateTime fin = debut.plusHours(DUREE);
            if (debut.toLocalDate().isBefore(sem.getDateDebut()) || debut.toLocalDate().isAfter(sem.getDateFin()))
                throw new BusinessException("Le créneau du " + debut + " est hors du semestre.");

            boolean conflit = reservationRepo.existsConflit(c.salleId(), debut, fin)
                    || aCreer.stream().anyMatch(r -> r.getSalleId().equals(c.salleId())
                    && r.getDateDebut().isBefore(fin) && r.getDateFin().isAfter(debut));
            if (conflit)
                throw new BusinessException("La salle " + salle.getNom() + " est déjà réservée le " + debut + ".");

            Reservation r = new Reservation();
            r.setSalleId(c.salleId());
            r.setUtilisateurId(req.utilisateurId());
            r.setSemestreId(req.semestreId());
            r.setDateDebut(debut);
            r.setDateFin(fin);   // always +2h, matches check_duree_2h
            r.setMotif(req.commentaire());
            r.setMatiere(req.matiere());
            r.setStatut("CONFIRMEE");
            aCreer.add(r);
        }
        return reservationRepo.saveAll(aCreer);
    }
    public List<Reservation> calendrier(Integer utilisateurId, Integer semestreId) {
        utilisateurRepo.findById(utilisateurId)
                .orElseThrow(() -> new BusinessException("Professeur introuvable."));
        if (semestreId == null) {
            return reservationRepo.findByUtilisateurIdOrderByDateDebutAsc(utilisateurId);
        }
        return reservationRepo.findByUtilisateurIdAndSemestreIdOrderByDateDebutAsc(
                utilisateurId, semestreId);
    }

    public Reservation consulter(Integer utilisateurId, Integer reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new BusinessException("Réservation introuvable."));
        verifierProprietaire(reservation, utilisateurId);
        return reservation;
    }

    @Transactional
    public Reservation demanderAnnulation(Integer utilisateurId, Integer reservationId,
                                           String commentaire) {
        Reservation reservation = consulter(utilisateurId, reservationId);
        verifierAucuneDemandeEnCours(reservation);
        reservation.setDemandeType("ANNULATION");
        reservation.setDemandeStatut("EN_ATTENTE");
        reservation.setDemandeCommentaire(commentaire);
        reservation.setDemandeSalleId(null);
        reservation.setDemandeDateDebut(null);
        reservation.setDemandeMatiere(null);
        return reservationRepo.save(reservation);
    }

    @Transactional
    public Reservation demanderModification(Integer utilisateurId, Integer reservationId,
                                            com.tp1methodesagiles.gestiondessalles.dto.ReservationActionRequest req) {
        Reservation reservation = consulter(utilisateurId, reservationId);
        verifierAucuneDemandeEnCours(reservation);
        if (req == null || req.debut() == null || req.salleId() == null) {
            throw new BusinessException("La salle et la nouvelle date de début sont obligatoires.");
        }
        Salle salle = salleRepo.findById(req.salleId())
                .orElseThrow(() -> new BusinessException("Salle introuvable."));
        if (!Boolean.TRUE.equals(salle.getDisponible())) {
            throw new BusinessException("La salle " + salle.getNom() + " n'est pas disponible.");
        }
        LocalDateTime fin = req.debut().plusHours(DUREE);
        boolean sameSlot = reservation.getSalleId().equals(req.salleId())
                && reservation.getDateDebut().equals(req.debut());
        if (!sameSlot && reservationRepo.existsConflit(req.salleId(), req.debut(), fin)) {
            throw new BusinessException("La salle " + salle.getNom()
                    + " est déjà réservée sur le créneau demandé.");
        }
        reservation.setDemandeType("MODIFICATION");
        reservation.setDemandeStatut("EN_ATTENTE");
        reservation.setDemandeCommentaire(req.commentaire());
        reservation.setDemandeSalleId(req.salleId());
        reservation.setDemandeDateDebut(req.debut());
        reservation.setDemandeMatiere(req.matiere());
        return reservationRepo.save(reservation);
    }

    private void verifierProprietaire(Reservation reservation, Integer utilisateurId) {
        if (!reservation.getUtilisateurId().equals(utilisateurId)) {
            throw new BusinessException("Vous ne pouvez consulter ou modifier que vos propres réservations.");
        }
    }

    private void verifierAucuneDemandeEnCours(Reservation reservation) {
        if ("EN_ATTENTE".equals(reservation.getDemandeStatut())) {
            throw new BusinessException("Une demande est déjà en attente pour cette réservation.");
        }
    }

}
