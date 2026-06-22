package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.AdministradorRepository;
import com.ucu.ticketing.repository.FuncionarioRepository;
import com.ucu.ticketing.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AdministradorRepository administradorRepo;
    private final FuncionarioRepository funcionarioRepo;

    public AuthController(AuthService authService, AdministradorRepository administradorRepo,
                        FuncionarioRepository funcionarioRepo) {
        this.authService = authService;
        this.administradorRepo = administradorRepo;
        this.funcionarioRepo = funcionarioRepo;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String, Object> body) {
        UsuarioGeneral usuario = new UsuarioGeneral();
        usuario.setEmail((String) body.get("email"));
        usuario.setPaisDocumentoIdentidad((String) body.get("paisDocumentoIdentidad"));
        usuario.setTipoDocumento((String) body.get("tipoDocumento"));
        usuario.setNumeroDocumento((String) body.get("numeroDocumento"));
        usuario.setPaisDireccion((String) body.get("paisDireccion"));
        usuario.setLocalidad((String) body.get("localidad"));
        usuario.setCalle((String) body.get("calle"));
        usuario.setNumeroDireccion((String) body.get("numeroDireccion"));
        usuario.setCodigoPostal((String) body.get("codigoPostal"));
        @SuppressWarnings("unchecked")
        java.util.List<String> telefonos = (java.util.List<String>) body.get("telefonos");
        if (telefonos != null) usuario.setTelefonos(telefonos);
        String password = (String) body.get("password");
        return ResponseEntity.ok(authService.registrar(usuario, password));
    }
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(Principal principal) {
        boolean esAdmin = administradorRepo.existsById(principal.getName());
        boolean esFuncionario = funcionarioRepo.existsById(principal.getName());
        return ResponseEntity.ok(Map.of(
            "email", principal.getName(),
            "rol", esAdmin ? "ADMIN" : esFuncionario ? "FUNCIONARIO" : "USER"
        ));
    }
}