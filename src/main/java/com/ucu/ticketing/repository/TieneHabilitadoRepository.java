package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.TieneHabilitado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TieneHabilitadoRepository extends JpaRepository<TieneHabilitado, TieneHabilitado.TieneHabilitadoId> {
    List<TieneHabilitado> findByIdEncuentro(Integer idEncuentro);
}