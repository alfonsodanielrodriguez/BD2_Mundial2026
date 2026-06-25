package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.TieneAsignado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TieneAsignadoRepository extends JpaRepository<TieneAsignado, TieneAsignado.TieneAsignadoId> {
    java.util.List<TieneAsignado> findByEmailFuncionario(String emailFuncionario);
}
