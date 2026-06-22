package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.Encuentro;
import com.ucu.ticketing.repository.EncuentroRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/encuentros")
public class EncuentroController {

    private final EncuentroRepository encuentroRepo;

    public EncuentroController(EncuentroRepository encuentroRepo) {
        this.encuentroRepo = encuentroRepo;
    }

    @GetMapping
    public ResponseEntity<List<Encuentro>> listar() {
        return ResponseEntity.ok(encuentroRepo.findAll());
    }
}