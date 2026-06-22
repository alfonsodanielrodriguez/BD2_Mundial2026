package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Integer> {
    List<Compra> findByUsuarioEmail(String email);

    @Query("SELECT c.usuario, SUM(c.montoTotal) as total FROM Compra c GROUP BY c.usuario ORDER BY total DESC")
    List<Object[]> mayoresCompradores();
}