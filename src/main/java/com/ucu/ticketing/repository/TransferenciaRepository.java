package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Integer> {
    List<Transferencia> findByEmisorEmail(String email);
    List<Transferencia> findByReceptorEmail(String email);
}