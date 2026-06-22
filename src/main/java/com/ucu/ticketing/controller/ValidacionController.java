package com.ucu.ticketing.controller;

import com.ucu.ticketing.service.ValidacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/validacion")
public class ValidacionController {

    private final ValidacionService validacionService;

    public ValidacionController(ValidacionService validacionService) {
        this.validacionService = validacionService;
    }

    @PostMapping("/escanear")
    public ResponseEntity<?> escanear(@RequestBody Map<String, Object> body, Principal principal) {
        Integer idEntrada = (Integer) body.get("idEntrada");
        String idDispositivo = (String) body.get("idDispositivo");
        String codigoQr = (String) body.get("codigoQr");
        try {
            String mensaje = validacionService.escanear(idEntrada, idDispositivo, codigoQr, principal.getName());
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
