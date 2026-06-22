package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {}