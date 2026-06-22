package com.ucu.ticketing;

import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import com.ucu.ticketing.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioGeneralRepository usuarioRepo;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    @Test
    void registrar_emailNuevo_guardaUsuario() {
        UsuarioGeneral usuario = new UsuarioGeneral();
        usuario.setEmail("nuevo@test.com");

        when(usuarioRepo.existsById("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hash");
        when(usuarioRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioGeneral resultado = authService.registrar(usuario, "password123");

        assertEquals("$2a$hash", resultado.getPasswordHash());
        verify(usuarioRepo).save(usuario);
    }

    @Test
    void registrar_passwordNoSeGuardaEnTextoplano() {
        UsuarioGeneral usuario = new UsuarioGeneral();
        usuario.setEmail("nuevo@test.com");

        when(usuarioRepo.existsById("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(usuarioRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioGeneral resultado = authService.registrar(usuario, "miPassword");

        assertNotEquals("miPassword", resultado.getPasswordHash());
    }

    @Test
    void registrar_emailDuplicado_lanzaExcepcion() {
        UsuarioGeneral usuario = new UsuarioGeneral();
        usuario.setEmail("existente@test.com");

        when(usuarioRepo.existsById("existente@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.registrar(usuario, "password123"));
        assertTrue(ex.getMessage().contains("Ya existe"));
        verify(usuarioRepo, never()).save(any());
    }
}
