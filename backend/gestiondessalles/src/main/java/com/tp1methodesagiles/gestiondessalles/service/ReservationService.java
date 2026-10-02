package com.example.salles.service;

import com.example.salles.dto.Creneau;
import com.example.salles.dto.AlternativeReservation;
import com.example.salles.dto.ReservationActionResponse;
import com.example.salles.dto.ReservationRequest;
import com.example.salles.dto.ReservationView;
import com.example.salles.dto.Solde;
import com.example.salles.entity.Reservation;
import com.example.salles.entity.Salle;
import com.example.salles.entity.Semestre;
import com.example.salles.entity.Utilisateur;
import com.example.salles.entity.VolumeHoraire;
import com.example.salles.exception.BusinessException;
import com.example.salles.repository.ReservationRepository;
import com.example.salles.repository.SalleRepository;
import com.example.salles.repository.SemestreRepository;
import com.example.salles.repository.UtilisateurRepository;
import com.example.salles.repository.VolumeHoraireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int DUREE = 2;
    private static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    private static final String STATUT_CONFIRMEE = "CONFIRMEE";
    private static final String STATUT_REFUSEE = "REFUSEE";

    private final UtilisateurRepository utilisateurRepo;
    private final SalleRepository salleRepo;
    private final SemestreRepository semestreRepo;
    private final VolumeHoraireRepository volumeRepo;
    private final ReservationRepository reservationRepo;

    public Solde solde(Integer utilisateurId, Integer semestreId) {
        int consomme = (int) reservationRepo
                .countByUtilisateurIdAndSemestreIdAndStatut(utilisateurId, semestreId, STATUT_CONFIRMEE) * DUREE;
        return volumeRepo.findByUtilisateurIdAndSemestreId(utilisateurId, semestreId)
                .map(v -> new Solde(v.getVolumeHoraireTotal(), consomme, v.getVolumeHoraireTotal() - consomme))
                .orElse(new Solde(null, consomme, null));
    }

    public List<ReservationView> rechercher(String recherche, String statut, Integer salleId,
                                            Integer utilisateurId, Integer semestreId) {
        return reservationRepo.findAll().stream()
                .filter(r -> statut == null || statut.isBlank() || statut.equalsIgnoreCase(r.getStatut()))
                .filter(r -> salleId == null || Objects.equals(salleId, r.getSalleId()))
                .filter(r -> utilisateurId == null || Objects.equals(utilisateurId, r.getUtilisateurId()))
                .filter(r -> semestreId == null || Objects.equals(semestreId, r.getSemestreId()))
                .map(this::toView)
                .filter(v -> correspondRecherche(v, recherche))
                .toList();
    }

    public ReservationView detail(Integer id) {
        return reservationRepo.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new BusinessException("Reservation introuvable."));
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
            r.setStatut(STATUT_EN_ATTENTE);
            aCreer.add(r);
        }
        return reservationRepo.saveAll(aCreer);
    }

    @Transactional
    public ReservationActionResponse accepter(Integer reservationId) {
        Reservation reservation = reservationRepo.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new BusinessException("Reservation introuvable."));
        if (!STATUT_EN_ATTENTE.equalsIgnoreCase(reservation.getStatut())) {
            throw new BusinessException("Seules les reservations en attente peuvent etre acceptees.");
        }

        Salle salle = salleRepo.findByIdForUpdate(reservation.getSalleId())
                .orElseThrow(() -> new BusinessException("Salle introuvable."));

        if (!Boolean.TRUE.equals(salle.getDisponible()) || conflit(reservation)) {
            AlternativeReservation alternative = trouverAlternative(reservation);
            if (alternative == null) {
                throw new BusinessException("Aucune salle disponible pour ce creneau.");
            }
            salle = salleRepo.findByIdForUpdate(alternative.salleId())
                    .orElseThrow(() -> new BusinessException("Salle introuvable."));
            if (!Boolean.TRUE.equals(salle.getDisponible())) {
                throw new BusinessException("La salle alternative n'est plus disponible.");
            }
            reservation.setSalleId(salle.getId());
            reservation.setDateDebut(alternative.debut());
            reservation.setDateFin(alternative.fin());
        }

        if (conflit(reservation)) {
            throw new BusinessException("Cette confirmation chevauche deja une reservation confirmee.");
        }

        reservation.setStatut(STATUT_CONFIRMEE);
        salle.setDisponible(false);
        salleRepo.save(salle);
        return new ReservationActionResponse(
                toView(reservationRepo.save(reservation)),
                null,
                "Reservation confirmee.");
    }

    @Transactional
    public ReservationActionResponse refuser(Integer reservationId) {
        Reservation reservation = reservationRepo.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new BusinessException("Reservation introuvable."));
        if (STATUT_CONFIRMEE.equalsIgnoreCase(reservation.getStatut())) {
            throw new BusinessException("Une reservation confirmee ne peut pas etre refusee.");
        }
        AlternativeReservation alternative = trouverAlternative(reservation);
        reservation.setStatut(STATUT_REFUSEE);
        Reservation saved = reservationRepo.save(reservation);
        String message = alternative == null
                ? "Reservation refusee. Aucun alternatif disponible avec les donnees actuelles."
                : "Reservation refusee. Un alternatif est disponible.";
        return new ReservationActionResponse(toView(saved), alternative, message);
    }

    private boolean conflit(Reservation reservation) {
        return reservationRepo.existsConflit(
                reservation.getSalleId(),
                reservation.getDateDebut(),
                reservation.getDateFin());
    }

    private AlternativeReservation trouverAlternative(Reservation reservation) {
        for (Salle salle : salleRepo.findAll()) {
            if (!Boolean.TRUE.equals(salle.getDisponible())) {
                continue;
            }
            if (Objects.equals(salle.getId(), reservation.getSalleId())) {
                continue;
            }
            if (creneauLibre(salle.getId(), reservation.getDateDebut(), reservation.getDateFin())) {
                return new AlternativeReservation(salle.getId(), salle.getNom(),
                        reservation.getDateDebut(), reservation.getDateFin());
            }
        }

        LocalDateTime debut = reservation.getDateDebut().plusHours(DUREE);
        for (int i = 0; i < 8; i++) {
            LocalDateTime fin = debut.plusHours(DUREE);
            for (Salle salle : salleRepo.findAll()) {
                if (Boolean.TRUE.equals(salle.getDisponible()) && creneauLibre(salle.getId(), debut, fin)) {
                    return new AlternativeReservation(salle.getId(), salle.getNom(), debut, fin);
                }
            }
            debut = debut.plusHours(DUREE);
        }
        return null;
    }

    private boolean creneauLibre(Integer salleId, LocalDateTime debut, LocalDateTime fin) {
        return !reservationRepo.existsConflit(salleId, debut, fin);
    }

    private boolean correspondRecherche(ReservationView reservation, String recherche) {
        if (recherche == null || recherche.isBlank()) {
            return true;
        }
        String texte = recherche.toLowerCase(Locale.ROOT);
        return contient(reservation.professeurNom(), texte)
                || contient(reservation.salleNom(), texte)
                || contient(reservation.matiere(), texte)
                || contient(reservation.motif(), texte)
                || contient(reservation.statut(), texte);
    }

    private boolean contient(String valeur, String recherche) {
        return valeur != null && valeur.toLowerCase(Locale.ROOT).contains(recherche);
    }

    private ReservationView toView(Reservation reservation) {
        Salle salle = salleRepo.findById(reservation.getSalleId()).orElse(null);
        Utilisateur utilisateur = utilisateurRepo.findById(reservation.getUtilisateurId()).orElse(null);
        String professeur = utilisateur == null
                ? "Professeur #" + reservation.getUtilisateurId()
                : utilisateur.getPrenom() + " " + utilisateur.getNom();
        return new ReservationView(
                reservation.getId(),
                reservation.getSalleId(),
                salle == null ? "Salle #" + reservation.getSalleId() : salle.getNom(),
                reservation.getUtilisateurId(),
                professeur,
                reservation.getSemestreId(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                0,
                reservation.getMatiere(),
                reservation.getMotif(),
                reservation.getStatut(),
                reservation.getDateCreation());
    }
}
