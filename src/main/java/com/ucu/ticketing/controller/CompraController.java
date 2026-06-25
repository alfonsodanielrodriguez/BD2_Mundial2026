package com.ucu.ticketing.controller;

import com.ucu.ticketing.service.CompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    public ResponseEntity<?> comprar(@RequestBody Map<String, Object> body, Principal principal) {
        return ResponseEntity.ok(compraService.comprar(
            principal.getName(),
            (Integer) body.get("idEncuentro"),
            (String) body.get("letraSector"),
            (Integer) body.get("cantidad")
        ));
    }

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciar(@RequestBody Map<String, Object> body, Principal principal) {
        try {
            return ResponseEntity.ok(compraService.iniciar(
                principal.getName(),
                (Integer) body.get("idEncuentro"),
                (String) body.get("letraSector"),
                (Integer) body.get("cantidad")
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable Integer id, Principal principal) {
        try {
            return ResponseEntity.ok(compraService.confirmar(id, principal.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> misCompras(Principal principal) {
        return ResponseEntity.ok(compraService.misCompras(principal.getName()));
    }
}