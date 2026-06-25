package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.Encuentro;
import com.ucu.ticketing.model.Sector;
import com.ucu.ticketing.model.TieneHabilitado;
import com.ucu.ticketing.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/encuentros")
public class EncuentroController {

    private final EncuentroRepository encuentroRepo;
    private final TieneHabilitadoRepository tieneHabilitadoRepo;
    private final SectorRepository sectorRepo;
    private final EntradaRepository entradaRepo;

    public EncuentroController(EncuentroRepository encuentroRepo,
                               TieneHabilitadoRepository tieneHabilitadoRepo,
                               SectorRepository sectorRepo,
                               EntradaRepository entradaRepo) {
        this.encuentroRepo = encuentroRepo;
        this.tieneHabilitadoRepo = tieneHabilitadoRepo;
        this.sectorRepo = sectorRepo;
        this.entradaRepo = entradaRepo;
    }

    @GetMapping
    public ResponseEntity<List<Encuentro>> listar() {
        return ResponseEntity.ok(encuentroRepo.findAll());
    }

    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<?> disponibilidad(@PathVariable Integer id) {
        Encuentro enc = encuentroRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        List<TieneHabilitado> habilitados = tieneHabilitadoRepo.findByIdEncuentro(id);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TieneHabilitado th : habilitados) {
            Sector sector = sectorRepo.findById(new Sector.SectorId(th.getLetra(), enc.getEstadio().getIdEstadio()))
                .orElse(null);
            if (sector == null) continue;

            int vendidas = entradaRepo.countByEncuentroIdEncuentroAndLetraSector(id, th.getLetra());
            int disponibles = sector.getAforo() - vendidas;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("letra", th.getLetra());
            m.put("precio", sector.getPrecio());
            m.put("aforo", sector.getAforo());
            m.put("vendidas", vendidas);
            m.put("disponibles", disponibles);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
}