package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.FuncionarioValidacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<FuncionarioValidacion, String> {}