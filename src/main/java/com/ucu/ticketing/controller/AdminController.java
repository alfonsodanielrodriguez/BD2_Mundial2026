package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EstadioRepository estadioRepo;
    private final SectorRepository sectorRepo;
    private final EncuentroRepository encuentroRepo;
    private final EquipoRepository equipoRepo;
    private final AdministradorRepository adminRepo;
    private final TieneHabilitadoRepository tieneHabilitadoRepo;
    private final EntradaRepository entradaRepo;

    public AdminController(EstadioRepository estadioRepo, SectorRepository sectorRepo,
                        EncuentroRepository encuentroRepo, EquipoRepository equipoRepo,
                        AdministradorRepository adminRepo, TieneHabilitadoRepository tieneHabilitadoRepo,
                        EntradaRepository entradaRepo) {
        this.estadioRepo = estadioRepo;
        this.sectorRepo = sectorRepo;
        this.encuentroRepo = encuentroRepo;
        this.equipoRepo = equipoRepo;
        this.adminRepo = adminRepo;
        this.tieneHabilitadoRepo = tieneHabilitadoRepo;
        this.entradaRepo = entradaRepo;
    }

   @PostMapping("/estadios")
    public ResponseEntity<?> crearEstadio(@RequestBody Map<String, Object> body) {
        Estadio e = new Estadio();
        e.setNombre((String) body.get("nombre"));
        e.setDireccion((String) body.get("direccion"));
        e.setPais((String) body.get("pais"));
        e.setAforo((Integer) body.get("aforo"));
        estadioRepo.save(e);

        // Crear sectores A, B, C, D automáticamente
        String[] letras = {"A", "B", "C", "D"};
        for (String letra : letras) {
            Sector s = new Sector();
            s.setLetra(letra);
            s.setIdEstadio(e.getIdEstadio());
            s.setAforo(e.getAforo() / 4);
            s.setPrecio(new java.math.BigDecimal("100.00"));
            sectorRepo.save(s);
        }

        return ResponseEntity.ok(e);
    }

    @PostMapping("/sectores")
    public ResponseEntity<?> crearSector(@RequestBody Map<String, Object> body) {
        Sector s = new Sector();
        s.setLetra((String) body.get("letra"));
        s.setIdEstadio((Integer) body.get("idEstadio"));
        s.setAforo((Integer) body.get("aforo"));
        s.setPrecio(new java.math.BigDecimal(body.get("precio").toString()));
        return ResponseEntity.ok(sectorRepo.save(s));
    }

    @PostMapping("/encuentros/{id}/sectores")
    public ResponseEntity<?> habilitarSector(@PathVariable Integer id,
                                            @RequestBody Map<String, Object> body) {
        Encuentro enc = encuentroRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        String letra = (String) body.get("letra");
        Sector.SectorId sectorId = new Sector.SectorId(letra, enc.getEstadio().getIdEstadio());
        Sector sector = sectorRepo.findById(sectorId)
            .orElseThrow(() -> new RuntimeException("Sector no encontrado"));

        if (body.get("precio") != null) {
            sector.setPrecio(new java.math.BigDecimal(body.get("precio").toString()));
            sectorRepo.save(sector);
        }

        TieneHabilitado th = new TieneHabilitado();
        th.setIdEncuentro(id);
        th.setLetra(letra);
        th.setIdEstadio(enc.getEstadio().getIdEstadio());
        tieneHabilitadoRepo.save(th);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/encuentros/{id}/sectores/{letra}")
    public ResponseEntity<?> deshabilitarSector(@PathVariable Integer id, @PathVariable String letra) {
        Encuentro enc = encuentroRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));
        TieneHabilitado.TieneHabilitadoId thId = new TieneHabilitado.TieneHabilitadoId();
        thId.setIdEncuentro(id);
        thId.setLetra(letra);
        thId.setIdEstadio(enc.getEstadio().getIdEstadio());
        tieneHabilitadoRepo.deleteById(thId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sectores/{idEstadio}/{letra}/precio")
    public ResponseEntity<?> editarPrecio(@PathVariable Integer idEstadio,
                                        @PathVariable String letra,
                                        @RequestBody Map<String, Object> body) {
        Sector.SectorId sectorId = new Sector.SectorId(letra, idEstadio);
        Sector sector = sectorRepo.findById(sectorId)
            .orElseThrow(() -> new RuntimeException("Sector no encontrado"));
        sector.setPrecio(new java.math.BigDecimal(body.get("precio").toString()));
        return ResponseEntity.ok(sectorRepo.save(sector));
    }

    @GetMapping("/encuentros/{id}/sectores")
    public ResponseEntity<?> sectoresHabilitados(@PathVariable Integer id) {
        return ResponseEntity.ok(tieneHabilitadoRepo.findByIdEncuentro(id));
    }

    @PostMapping("/equipos")
    public ResponseEntity<?> crearEquipo(@RequestBody Map<String, Object> body) {
        Equipo eq = new Equipo();
        eq.setPais((String) body.get("pais"));
        return ResponseEntity.ok(equipoRepo.save(eq));
    }

    @PostMapping("/encuentros")
    public ResponseEntity<?> crearEncuentro(@RequestBody Map<String, Object> body) {
        Encuentro enc = new Encuentro();
        enc.setFecha(java.time.LocalDate.parse((String) body.get("fecha")));
        enc.setHora(java.time.LocalTime.parse((String) body.get("hora")));

        Estadio estadio = estadioRepo.findById((Integer) body.get("idEstadio"))
            .orElseThrow(() -> new RuntimeException("Estadio no encontrado"));
        enc.setEstadio(estadio);

        Equipo local = equipoRepo.findById((String) body.get("paisLocal"))
            .orElseThrow(() -> new RuntimeException("Equipo local no encontrado"));
        enc.setEquipoLocal(local);

        Equipo visitante = equipoRepo.findById((String) body.get("paisVisitante"))
            .orElseThrow(() -> new RuntimeException("Equipo visitante no encontrado"));
        enc.setEquipoVisitante(visitante);

        Administrador admin = adminRepo.findById((String) body.get("emailAdmin"))
            .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        enc.setAdministrador(admin);

        if (encuentroRepo.existsByEstadioIdEstadioAndFechaAndHora(estadio.getIdEstadio(), enc.getFecha(), enc.getHora()))
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un encuentro en ese estadio a la misma fecha y hora."));

        return ResponseEntity.ok(encuentroRepo.save(enc));
    }

    @GetMapping("/estadios")
    public ResponseEntity<?> listarEstadios() {
        return ResponseEntity.ok(estadioRepo.findAll());
    }

    @GetMapping("/equipos")
    public ResponseEntity<?> listarEquipos() {
        return ResponseEntity.ok(equipoRepo.findAll());
    }

    @DeleteMapping("/estadios/{id}")
    public ResponseEntity<?> eliminarEstadio(@PathVariable Integer id) {
        if (encuentroRepo.existsByEstadioIdEstadio(id))
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar el estadio porque tiene encuentros programados. Eliminá los encuentros primero."));
        sectorRepo.deleteAll(sectorRepo.findByIdEstadio(id));
        estadioRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/encuentros/{id}")
    public ResponseEntity<?> eliminarEncuentro(@PathVariable Integer id) {
        if (entradaRepo.existsByEncuentroIdEncuentro(id))
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar el encuentro porque ya tiene entradas vendidas."));
        tieneHabilitadoRepo.deleteAll(tieneHabilitadoRepo.findByIdEncuentro(id));
        encuentroRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/equipos/{pais}")
    public ResponseEntity<?> eliminarEquipo(@PathVariable String pais) {
        if (encuentroRepo.existsByEquipoLocalPaisOrEquipoVisitantePais(pais, pais))
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede eliminar el equipo porque participa en encuentros programados. Eliminá los encuentros primero."));
        equipoRepo.deleteById(pais);
        return ResponseEntity.ok().build();
    }
}