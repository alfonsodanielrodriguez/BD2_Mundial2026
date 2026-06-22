package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Encuentro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncuentroRepository extends JpaRepository<Encuentro, Integer> {
    boolean existsByEstadioIdEstadio(Integer idEstadio);
    boolean existsByEquipoLocalPaisOrEquipoVisitantePais(String paisLocal, String paisVisitante);
    boolean existsByEstadioIdEstadioAndFechaAndHora(Integer idEstadio, java.time.LocalDate fecha, java.time.LocalTime hora);
}