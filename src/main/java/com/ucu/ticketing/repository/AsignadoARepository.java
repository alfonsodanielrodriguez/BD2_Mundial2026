package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.AsignadoA;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsignadoARepository extends JpaRepository<AsignadoA, AsignadoA.AsignadoAId> {
    List<AsignadoA> findByEmailFuncionario(String emailFuncionario);
    List<AsignadoA> findByEmailFuncionarioAndIdEncuentro(String emailFuncionario, Integer idEncuentro);
    boolean existsByEmailFuncionarioAndIdEncuentroAndLetraSectorAndIdEstadio(
        String emailFuncionario, Integer idEncuentro, String letraSector, Integer idEstadio);
    void deleteByEmailFuncionarioAndIdEncuentroAndLetraSectorAndIdEstadio(
        String emailFuncionario, Integer idEncuentro, String letraSector, Integer idEstadio);
}
