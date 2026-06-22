package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.Entrada;
import com.ucu.ticketing.repository.EntradaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaRepository entradaRepo;

    public EntradaController(EntradaRepository entradaRepo) {
        this.entradaRepo = entradaRepo;
    }

    @GetMapping
    public ResponseEntity<?> misEntradas(Principal principal) {
        return ResponseEntity.ok(entradaRepo.findByPropietarioActualEmailConEncuentro(principal.getName()));
    }

    @PostMapping("/{id}/generar-qr")
    public ResponseEntity<?> generarQr(@PathVariable Integer id, Principal principal) {
        Entrada entrada = entradaRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));

        if (!entrada.getPropietarioActual().getEmail().equals(principal.getName()))
            return ResponseEntity.badRequest().body(Map.of("error", "No sos el propietario"));

        if (entrada.getEstado() == Entrada.Estado.consumida)
            return ResponseEntity.badRequest().body(Map.of("error", "Entrada ya consumida"));

        String token = java.util.UUID.randomUUID().toString();
        entrada.setQrTokenActual(token);
        entrada.setQrTokenExpiraEn(java.time.LocalDateTime.now().plusMinutes(5));
        entradaRepo.save(entrada);

        return ResponseEntity.ok(Map.of("token", token, "expiraEn", entrada.getQrTokenExpiraEn()));
    }

    @GetMapping("/{id}/qr-token")
    public ResponseEntity<?> obtenerToken(@PathVariable Integer id, Principal principal) {
        Entrada entrada = entradaRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));
        return ResponseEntity.ok(Map.of(
            "token", entrada.getQrTokenActual() != null ? entrada.getQrTokenActual() : "",
            "expiraEn", entrada.getQrTokenExpiraEn()
        ));
    }
}