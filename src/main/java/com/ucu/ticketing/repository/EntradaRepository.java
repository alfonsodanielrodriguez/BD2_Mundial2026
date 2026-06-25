package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntradaRepository extends JpaRepository<Entrada, Integer> {
    List<Entrada> findByPropietarioActualEmail(String email);
    int countByEncuentroIdEncuentroAndLetraSector(Integer idEncuentro, String letraSector);
    java.util.Optional<Entrada> findByQrTokenActual(String qrTokenActual);
    List<Entrada> findByIdCompra(Integer idCompra);

    boolean existsByEncuentroIdEncuentro(Integer idEncuentro);

    @Query("SELECT e FROM Entrada e JOIN FETCH e.encuentro enc JOIN FETCH enc.estadio JOIN FETCH enc.equipoLocal JOIN FETCH enc.equipoVisitante WHERE e.propietarioActual.email = :email")
    List<Entrada> findByPropietarioActualEmailConEncuentro(@Param("email") String email);

    @Query("SELECT e.encuentro, COUNT(e) as total FROM Entrada e GROUP BY e.encuentro ORDER BY total DESC")
    List<Object[]> encuentrosConMasVentas();
}