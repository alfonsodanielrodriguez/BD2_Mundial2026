package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.DispositivoEscaneo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoRepository extends JpaRepository<DispositivoEscaneo, String> {}