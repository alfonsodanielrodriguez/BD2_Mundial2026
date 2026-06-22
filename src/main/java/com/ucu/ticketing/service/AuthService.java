package com.ucu.ticketing.service;

import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioGeneralRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioGeneralRepository usuarioRepo, PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioGeneral registrar(UsuarioGeneral usuario, String passwordPlana) {
        if (usuarioRepo.existsById(usuario.getEmail()))
            throw new RuntimeException("Ya existe un usuario con ese email");
        usuario.setPasswordHash(passwordEncoder.encode(passwordPlana));
        return usuarioRepo.save(usuario);
    }
}