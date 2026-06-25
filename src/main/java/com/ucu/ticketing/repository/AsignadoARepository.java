package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.AsignadoA;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsignadoARepository extends JpaRepository<AsignadoA, AsignadoA.AsignadoAId> {
    List<AsignadoA> findByEmailFuncionario(String emailFuncionario);
    boolean existsByEmailFuncionarioAndIdEncuentro(String emailFuncionario, Integer idEncuentro);
    void deleteByEmailFuncionarioAndIdEncuentro(String emailFuncionario, Integer idEncuentro);
}
