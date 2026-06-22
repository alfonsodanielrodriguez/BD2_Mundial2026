package com.ucu.ticketing.controller;

import com.ucu.ticketing.repository.CompraRepository;
import com.ucu.ticketing.repository.EntradaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final EntradaRepository entradaRepo;
    private final CompraRepository compraRepo;

    public RankingController(EntradaRepository entradaRepo, CompraRepository compraRepo) {
        this.entradaRepo = entradaRepo;
        this.compraRepo = compraRepo;
    }

    @GetMapping("/eventos")
    public ResponseEntity<?> eventosMasVentas() {
        List<Object[]> rows = entradaRepo.encuentrosConMasVentas();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            var enc = (com.ucu.ticketing.model.Encuentro) row[0];
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idEncuentro", enc.getIdEncuentro());
            m.put("local", enc.getEquipoLocal().getPais());
            m.put("visitante", enc.getEquipoVisitante().getPais());
            m.put("fecha", enc.getFecha());
            m.put("estadio", enc.getEstadio().getNombre());
            m.put("totalEntradas", row[1]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/compradores")
    public ResponseEntity<?> mayoresCompradores() {
        List<Object[]> rows = compraRepo.mayoresCompradores();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            var usuario = (com.ucu.ticketing.model.UsuarioGeneral) row[0];
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("email", usuario.getEmail());
            m.put("totalGastado", row[1]);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
}