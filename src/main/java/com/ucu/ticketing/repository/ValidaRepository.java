package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Valida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidaRepository extends JpaRepository<Valida, Valida.ValidaId> {}