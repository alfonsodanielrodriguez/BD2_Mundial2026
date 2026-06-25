package com.ucu.ticketing.controller;

import com.ucu.ticketing.repository.ValidaRepository;
import com.ucu.ticketing.service.ValidacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/validacion")
public class ValidacionController {

    private final ValidacionService validacionService;
    private final ValidaRepository validaRepo;

    public ValidacionController(ValidacionService validacionService, ValidaRepository validaRepo) {
        this.validacionService = validacionService;
        this.validaRepo = validaRepo;
    }

    @GetMapping("/historial")
    public ResponseEntity<?> historial(Principal principal) {
        return ResponseEntity.ok(validaRepo.findByFuncionarioEmail(principal.getName()));
    }

    @GetMapping("/historial/todos")
    public ResponseEntity<?> historialTodos() {
        return ResponseEntity.ok(validaRepo.findAll());
    }

    @PostMapping("/escanear")
    public ResponseEntity<?> escanear(@RequestBody Map<String, Object> body, Principal principal) {
        String codigoQr = (String) body.get("codigoQr");
        try {
            String mensaje = validacionService.escanear(codigoQr, principal.getName());
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
