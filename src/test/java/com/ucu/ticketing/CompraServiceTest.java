package com.ucu.ticketing;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import com.ucu.ticketing.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompraServiceTest {

    @Mock CompraRepository compraRepo;
    @Mock EntradaRepository entradaRepo;
    @Mock UsuarioGeneralRepository usuarioRepo;
    @Mock SectorRepository sectorRepo;
    @Mock EncuentroRepository encuentroRepo;

    @InjectMocks CompraService compraService;

    private UsuarioGeneral usuario;
    private Encuentro encuentro;
    private Estadio estadio;
    private Sector sector;

    @BeforeEach
    void setUp() {
        estadio = new Estadio();
        estadio.setIdEstadio(1);

        encuentro = new Encuentro();
        encuentro.setIdEncuentro(10);
        encuentro.setEstadio(estadio);

        usuario = new UsuarioGeneral();
        usuario.setEmail("user@test.com");

        sector = new Sector();
        sector.setLetra("A");
        sector.setIdEstadio(1);
        sector.setAforo(100);
        sector.setPrecio(new BigDecimal("200.00"));

        when(usuarioRepo.findById("user@test.com")).thenReturn(Optional.of(usuario));
        when(encuentroRepo.findById(10)).thenReturn(Optional.of(encuentro));
        when(sectorRepo.findById(new Sector.SectorId("A", 1))).thenReturn(Optional.of(sector));
        when(compraRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entradaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void comprar_cantidadValida_creaCompraYEntradas() {
        when(entradaRepo.countByEncuentroIdEncuentroAndLetraSector(10, "A")).thenReturn(0);

        Compra compra = compraService.comprar("user@test.com", 10, "A", 3);

        assertNotNull(compra);
        // 3 entradas × $200 = $600, + 5% = $630
        assertEquals(0, new BigDecimal("630.00").compareTo(compra.getMontoTotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(compra.getComisionAplicada()));
        verify(entradaRepo, times(3)).save(any(Entrada.class));
    }

    @Test
    void comprar_cantidadCero_lanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> compraService.comprar("user@test.com", 10, "A", 0));
        assertTrue(ex.getMessage().contains("1 y 5"));
    }

    @Test
    void comprar_cantidadMayorACinco_lanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> compraService.comprar("user@test.com", 10, "A", 6));
        assertTrue(ex.getMessage().contains("1 y 5"));
    }

    @Test
    void comprar_aforoInsuficiente_lanzaExcepcion() {
        // Sector con aforo 100, ya vendidas 98, intenta comprar 3 → no alcanza
        when(entradaRepo.countByEncuentroIdEncuentroAndLetraSector(10, "A")).thenReturn(98);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> compraService.comprar("user@test.com", 10, "A", 3));
        assertTrue(ex.getMessage().contains("aforo"));
    }

    @Test
    void comprar_aforoJusto_permiteCompra() {
        // Sector con aforo 100, ya vendidas 97, intenta comprar 3 → exacto
        when(entradaRepo.countByEncuentroIdEncuentroAndLetraSector(10, "A")).thenReturn(97);

        assertDoesNotThrow(() -> compraService.comprar("user@test.com", 10, "A", 3));
    }

    @Test
    void comprar_montoTotalConComision_calculoCorrecto() {
        // 1 entrada × $200 + 5% = $210
        when(entradaRepo.countByEncuentroIdEncuentroAndLetraSector(10, "A")).thenReturn(0);

        Compra compra = compraService.comprar("user@test.com", 10, "A", 1);

        assertEquals(0, new BigDecimal("210.00").compareTo(compra.getMontoTotal()));
    }

    @Test
    void comprar_usuarioNoEncontrado_lanzaExcepcion() {
        when(usuarioRepo.findById("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> compraService.comprar("noexiste@test.com", 10, "A", 1));
    }

    @Test
    void comprar_encuentroNoEncontrado_lanzaExcepcion() {
        when(encuentroRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> compraService.comprar("user@test.com", 999, "A", 1));
    }
}
