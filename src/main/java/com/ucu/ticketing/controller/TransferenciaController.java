package com.ucu.ticketing.controller;

import com.ucu.ticketing.service.TransferenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    public ResponseEntity<?> iniciar(@RequestBody Map<String, Object> body, Principal principal) {
        return ResponseEntity.ok(transferenciaService.iniciarTransferencia(
            principal.getName(),
            (String) body.get("emailReceptor"),
            (Integer) body.get("idEntrada")
        ));
    }

    @PutMapping("/{id}/responder")
    public ResponseEntity<?> responder(@PathVariable Integer id,
                                       @RequestBody Map<String, Object> body,
                                       Principal principal) {
        return ResponseEntity.ok(transferenciaService.responderTransferencia(
            id,
            principal.getName(),
            (Boolean) body.get("aceptar")
        ));
    }

    @GetMapping
    public ResponseEntity<?> misTransferencias(Principal principal) {
        return ResponseEntity.ok(transferenciaService.misTransferencias(principal.getName()));
    }
}