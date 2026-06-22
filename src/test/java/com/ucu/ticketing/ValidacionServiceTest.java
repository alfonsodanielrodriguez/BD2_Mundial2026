package com.ucu.ticketing;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import com.ucu.ticketing.service.ValidacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidacionServiceTest {

    @Mock EntradaRepository entradaRepo;
    @Mock ValidaRepository validaRepo;
    @Mock FuncionarioRepository funcionarioRepo;
    @Mock DispositivoRepository dispositivoRepo;

    @InjectMocks ValidacionService validacionService;

    private Entrada entrada;
    private FuncionarioValidacion funcionario;
    private static final String TOKEN = "token-valido-123";
    private static final String DISPOSITIVO = "DISP-001";
    private static final String EMAIL_FUNCIONARIO = "funcionario@test.com";

    @BeforeEach
    void setUp() {
        entrada = new Entrada();
        entrada.setIdEntrada(1);
        entrada.setEstado(Entrada.Estado.activa);
        entrada.setQrTokenActual(TOKEN);
        entrada.setQrTokenExpiraEn(LocalDateTime.now().plusMinutes(5));
        entrada.setEncuentro(new Encuentro());

        funcionario = new FuncionarioValidacion();
        funcionario.setEmail(EMAIL_FUNCIONARIO);

        when(entradaRepo.findById(1)).thenReturn(Optional.of(entrada));
        when(dispositivoRepo.existsById(DISPOSITIVO)).thenReturn(true);
        when(funcionarioRepo.findById(EMAIL_FUNCIONARIO)).thenReturn(Optional.of(funcionario));
        when(entradaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(validaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void escanear_exitoso_marcaEntradaConsumidaYRegistraAuditoria() {
        String resultado = validacionService.escanear(1, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO);

        assertEquals("Entrada validada correctamente", resultado);
        assertEquals(Entrada.Estado.consumida, entrada.getEstado());
        verify(validaRepo).save(argThat(v ->
            v.getCodigoAceptado().equals(TOKEN) &&
            v.getFuncionario().equals(funcionario)
        ));
    }

    @Test
    void escanear_entradaYaConsumida_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.consumida);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(1, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("consumida"));
    }

    @Test
    void escanear_entradaConTransferenciaPendiente_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.transferida_pendiente);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(1, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("transferencia pendiente"));
    }

    @Test
    void escanear_tokenIncorrecto_lanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(1, DISPOSITIVO, "token-falso", EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("inválido"));
    }

    @Test
    void escanear_tokenExpirado_lanzaExcepcion() {
        entrada.setQrTokenExpiraEn(LocalDateTime.now().minusSeconds(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(1, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("expirado"));
    }

    @Test
    void escanear_dispositivoNoAutorizado_lanzaExcepcion() {
        when(dispositivoRepo.existsById("DISP-FALSO")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(1, "DISP-FALSO", TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("no autorizado"));
    }

    @Test
    void escanear_entradaNoExiste_lanzaExcepcion() {
        when(entradaRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> validacionService.escanear(999, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO));
    }

    @Test
    void escanear_exitoso_noVuelveAValidarMismaEntrada() {
        validacionService.escanear(1, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO);

        // Simular segundo intento: la entrada ya está consumida
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(1, DISPOSITIVO, TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("consumida"));
    }
}
