package com.ucu.ticketing.controller;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
    private final FuncionarioRepository funcionarioRepo;
    private final UsuarioRepository usuarioRepo;
    private final DispositivoRepository dispositivoRepo;
    private final TieneAsignadoRepository tieneAsignadoRepo;
    private final PasswordEncoder passwordEncoder;
    private final AsignadoARepository asignadoARepo;

    public AdminController(EstadioRepository estadioRepo, SectorRepository sectorRepo,
                        EncuentroRepository encuentroRepo, EquipoRepository equipoRepo,
                        AdministradorRepository adminRepo, TieneHabilitadoRepository tieneHabilitadoRepo,
                        EntradaRepository entradaRepo, FuncionarioRepository funcionarioRepo,
                        UsuarioRepository usuarioRepo, DispositivoRepository dispositivoRepo,
                        TieneAsignadoRepository tieneAsignadoRepo, PasswordEncoder passwordEncoder,
                        AsignadoARepository asignadoARepo) {
        this.estadioRepo = estadioRepo;
        this.sectorRepo = sectorRepo;
        this.encuentroRepo = encuentroRepo;
        this.equipoRepo = equipoRepo;
        this.adminRepo = adminRepo;
        this.tieneHabilitadoRepo = tieneHabilitadoRepo;
        this.entradaRepo = entradaRepo;
        this.funcionarioRepo = funcionarioRepo;
        this.usuarioRepo = usuarioRepo;
        this.dispositivoRepo = dispositivoRepo;
        this.tieneAsignadoRepo = tieneAsignadoRepo;
        this.passwordEncoder = passwordEncoder;
        this.asignadoARepo = asignadoARepo;
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
            java.math.BigDecimal precio = new java.math.BigDecimal(body.get("precio").toString());
            if (precio.compareTo(java.math.BigDecimal.ZERO) < 0)
                return ResponseEntity.badRequest().body(Map.of("error", "El precio no puede ser negativo"));
            sector.setPrecio(precio);
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

    @PutMapping("/estadios/{id}")
    public ResponseEntity<?> editarEstadio(@PathVariable Integer id,
                                           @RequestBody Map<String, Object> body) {
        Estadio e = estadioRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Estadio no encontrado"));
        if (body.get("nombre") != null) e.setNombre((String) body.get("nombre"));
        if (body.get("direccion") != null) e.setDireccion((String) body.get("direccion"));
        if (body.get("pais") != null) e.setPais((String) body.get("pais"));
        return ResponseEntity.ok(estadioRepo.save(e));
    }

    @PutMapping("/encuentros/{id}")
    public ResponseEntity<?> editarEncuentro(@PathVariable Integer id,
                                             @RequestBody Map<String, Object> body) {
        Encuentro enc = encuentroRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));
        if (body.get("fecha") != null) enc.setFecha(java.time.LocalDate.parse((String) body.get("fecha")));
        if (body.get("hora") != null) enc.setHora(java.time.LocalTime.parse((String) body.get("hora")));
        return ResponseEntity.ok(encuentroRepo.save(enc));
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

    @GetMapping("/funcionarios")
    public ResponseEntity<?> listarFuncionarios() {
        return ResponseEntity.ok(funcionarioRepo.findAll());
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<?> crearFuncionario(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        if (usuarioRepo.existsById(email))
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un usuario con ese email"));

        // Generar legajo autoincremental LEG-001, LEG-002, ...
        int nextNum = funcionarioRepo.findAll().stream()
            .filter(fn -> fn.getNumeroLegajo() != null && fn.getNumeroLegajo().startsWith("LEG-"))
            .mapToInt(fn -> {
                try { return Integer.parseInt(fn.getNumeroLegajo().substring(4)); }
                catch (NumberFormatException e) { return 0; }
            })
            .max().orElse(0) + 1;
        String numeroLegajo = String.format("LEG-%03d", nextNum);

        FuncionarioValidacion f = new FuncionarioValidacion();
        f.setEmail(email);
        f.setPaisDocumentoIdentidad((String) body.getOrDefault("paisDocumentoIdentidad", "N/A"));
        f.setTipoDocumento((String) body.getOrDefault("tipoDocumento", "N/A"));
        f.setNumeroDocumento((String) body.getOrDefault("numeroDocumento", "N/A"));
        f.setPaisDireccion((String) body.getOrDefault("paisDireccion", "N/A"));
        f.setLocalidad((String) body.getOrDefault("localidad", "N/A"));
        f.setCalle((String) body.getOrDefault("calle", "N/A"));
        f.setNumeroDireccion((String) body.getOrDefault("numeroDireccion", "N/A"));
        f.setCodigoPostal((String) body.getOrDefault("codigoPostal", "N/A"));
        f.setNumeroLegajo(numeroLegajo);
        f.setPasswordHash(passwordEncoder.encode((String) body.get("password")));
        funcionarioRepo.save(f);

        // Crear y asignar dispositivo automáticamente
        String idDispositivo = "DISP-" + numeroLegajo.substring(4); // DISP-001
        if (!dispositivoRepo.existsById(idDispositivo)) {
            DispositivoEscaneo disp = new DispositivoEscaneo();
            disp.setIdDispositivo(idDispositivo);
            dispositivoRepo.save(disp);
        }
        TieneAsignado ta = new TieneAsignado();
        ta.setEmailFuncionario(email);
        ta.setIdDispositivo(idDispositivo);
        tieneAsignadoRepo.save(ta);

        return ResponseEntity.ok(Map.of(
            "email", f.getEmail(),
            "numeroLegajo", f.getNumeroLegajo(),
            "idDispositivo", idDispositivo
        ));
    }

    @GetMapping("/funcionarios/{email}/encuentros")
    public ResponseEntity<?> encuentrosFuncionario(@PathVariable String email) {
        return ResponseEntity.ok(asignadoARepo.findByEmailFuncionario(email));
    }

    @PostMapping("/funcionarios/{email}/encuentros/{idEncuentro}")
    public ResponseEntity<?> asignarEncuentro(@PathVariable String email, @PathVariable Integer idEncuentro) {
        if (!funcionarioRepo.existsById(email))
            return ResponseEntity.badRequest().body(Map.of("error", "Funcionario no encontrado"));
        if (!encuentroRepo.existsById(idEncuentro))
            return ResponseEntity.badRequest().body(Map.of("error", "Encuentro no encontrado"));
        if (asignadoARepo.existsByEmailFuncionarioAndIdEncuentro(email, idEncuentro))
            return ResponseEntity.badRequest().body(Map.of("error", "Ya está asignado a este encuentro"));

        AsignadoA a = new AsignadoA();
        a.setEmailFuncionario(email);
        a.setIdEncuentro(idEncuentro);
        return ResponseEntity.ok(asignadoARepo.save(a));
    }

    @Transactional
    @DeleteMapping("/funcionarios/{email}/encuentros/{idEncuentro}")
    public ResponseEntity<?> desasignarEncuentro(@PathVariable String email, @PathVariable Integer idEncuentro) {
        asignadoARepo.deleteByEmailFuncionarioAndIdEncuentro(email, idEncuentro);
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