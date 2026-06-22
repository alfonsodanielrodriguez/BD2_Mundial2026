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

    @GetMapping
    public ResponseEntity<?> misCompras(Principal principal) {
        return ResponseEntity.ok(compraService.misCompras(principal.getName()));
    }
}