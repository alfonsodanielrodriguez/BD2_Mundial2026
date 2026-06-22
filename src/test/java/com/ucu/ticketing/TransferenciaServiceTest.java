package com.ucu.ticketing;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import com.ucu.ticketing.service.TransferenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransferenciaServiceTest {

    @Mock TransferenciaRepository transferenciaRepo;
    @Mock EntradaRepository entradaRepo;
    @Mock UsuarioGeneralRepository usuarioRepo;

    @InjectMocks TransferenciaService transferenciaService;

    private UsuarioGeneral emisor;
    private UsuarioGeneral receptor;
    private Entrada entrada;

    @BeforeEach
    void setUp() {
        emisor = new UsuarioGeneral();
        emisor.setEmail("emisor@test.com");

        receptor = new UsuarioGeneral();
        receptor.setEmail("receptor@test.com");

        entrada = new Entrada();
        entrada.setIdEntrada(1);
        entrada.setPropietarioActual(emisor);
        entrada.setEstado(Entrada.Estado.activa);
        entrada.setCantidadTransferencias((byte) 0);

        when(entradaRepo.findById(1)).thenReturn(Optional.of(entrada));
        when(usuarioRepo.findById("receptor@test.com")).thenReturn(Optional.of(receptor));
        when(entradaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transferenciaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---- iniciarTransferencia ----

    @Test
    void iniciar_exitoso_marcaEntradaComoPendiente() {
        Transferencia t = transferenciaService.iniciarTransferencia("emisor@test.com", "receptor@test.com", 1);

        assertEquals(Transferencia.Estado.pendiente, t.getEstado());
        assertEquals(Entrada.Estado.transferida_pendiente, entrada.getEstado());
        assertEquals(emisor, t.getEmisor());
        assertEquals(receptor, t.getReceptor());
    }

    @Test
    void iniciar_noEsPropietario_lanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> transferenciaService.iniciarTransferencia("otro@test.com", "receptor@test.com", 1));
        assertTrue(ex.getMessage().contains("propietario"));
    }

    @Test
    void iniciar_entradaConsumida_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.consumida);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> transferenciaService.iniciarTransferencia("emisor@test.com", "receptor@test.com", 1));
        assertTrue(ex.getMessage().contains("disponible"));
    }

    @Test
    void iniciar_entradaConTransferenciaPendiente_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.transferida_pendiente);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> transferenciaService.iniciarTransferencia("emisor@test.com", "receptor@test.com", 1));
        assertTrue(ex.getMessage().contains("disponible"));
    }

    @Test
    void iniciar_limiteTransferenciasAlcanzado_lanzaExcepcion() {
        entrada.setCantidadTransferencias((byte) 3);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> transferenciaService.iniciarTransferencia("emisor@test.com", "receptor@test.com", 1));
        assertTrue(ex.getMessage().contains("3 transferencias"));
    }

    @Test
    void iniciar_dosTransferencias_aunPermitido() {
        entrada.setCantidadTransferencias((byte) 2);

        assertDoesNotThrow(
            () -> transferenciaService.iniciarTransferencia("emisor@test.com", "receptor@test.com", 1));
    }

    // ---- responderTransferencia ----

    @Test
    void responder_aceptar_cambiaPropietarioEIncrementaContador() {
        Transferencia t = crearTransferenciaPendiente();
        when(transferenciaRepo.findById(99)).thenReturn(Optional.of(t));

        transferenciaService.responderTransferencia(99, "receptor@test.com", true);

        assertEquals(receptor, entrada.getPropietarioActual());
        assertEquals((byte) 1, entrada.getCantidadTransferencias());
        assertEquals(Entrada.Estado.activa, entrada.getEstado());
        assertEquals(Transferencia.Estado.aceptada, t.getEstado());
    }

    @Test
    void responder_rechazar_devuelveEntradaAlEmisorSinCambiarPropietario() {
        Transferencia t = crearTransferenciaPendiente();
        when(transferenciaRepo.findById(99)).thenReturn(Optional.of(t));

        transferenciaService.responderTransferencia(99, "receptor@test.com", false);

        assertEquals(emisor, entrada.getPropietarioActual());
        assertEquals((byte) 0, entrada.getCantidadTransferencias());
        assertEquals(Entrada.Estado.activa, entrada.getEstado());
        assertEquals(Transferencia.Estado.rechazada, t.getEstado());
    }

    @Test
    void responder_noEsElReceptor_lanzaExcepcion() {
        Transferencia t = crearTransferenciaPendiente();
        when(transferenciaRepo.findById(99)).thenReturn(Optional.of(t));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> transferenciaService.responderTransferencia(99, "otro@test.com", true));
        assertTrue(ex.getMessage().contains("receptor"));
    }

    @Test
    void responder_transferenciaYaProcesada_lanzaExcepcion() {
        Transferencia t = crearTransferenciaPendiente();
        t.setEstado(Transferencia.Estado.aceptada);
        when(transferenciaRepo.findById(99)).thenReturn(Optional.of(t));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> transferenciaService.responderTransferencia(99, "receptor@test.com", true));
        assertTrue(ex.getMessage().contains("procesada"));
    }

    private Transferencia crearTransferenciaPendiente() {
        entrada.setEstado(Entrada.Estado.transferida_pendiente);
        Transferencia t = new Transferencia();
        t.setIdTransferencia(99);
        t.setEmisor(emisor);
        t.setReceptor(receptor);
        t.setEntrada(entrada);
        t.setEstado(Transferencia.Estado.pendiente);
        return t;
    }
}
