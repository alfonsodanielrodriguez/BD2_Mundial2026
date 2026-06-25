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
import java.util.List;
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
    @Mock TieneAsignadoRepository tieneAsignadoRepo;
    @Mock AsignadoARepository asignadoARepo;

    @InjectMocks ValidacionService validacionService;

    private Entrada entrada;
    private FuncionarioValidacion funcionario;
    private static final String TOKEN = "token-valido-123";
    private static final String DISPOSITIVO = "DISP-001";
    private static final String EMAIL_FUNCIONARIO = "funcionario@test.com";
    private static final int ID_ENCUENTRO = 1;

    @BeforeEach
    void setUp() {
        Encuentro encuentro = new Encuentro();
        encuentro.setIdEncuentro(ID_ENCUENTRO);

        entrada = new Entrada();
        entrada.setIdEntrada(1);
        entrada.setEstado(Entrada.Estado.activa);
        entrada.setQrTokenActual(TOKEN);
        entrada.setQrTokenExpiraEn(LocalDateTime.now().plusMinutes(5));
        entrada.setEncuentro(encuentro);

        funcionario = new FuncionarioValidacion();
        funcionario.setEmail(EMAIL_FUNCIONARIO);

        TieneAsignado ta = new TieneAsignado();
        ta.setIdDispositivo(DISPOSITIVO);

        when(entradaRepo.findByQrTokenActual(TOKEN)).thenReturn(Optional.of(entrada));
        when(entradaRepo.findByQrTokenActual(argThat(t -> !TOKEN.equals(t)))).thenReturn(Optional.empty());
        when(asignadoARepo.existsByEmailFuncionarioAndIdEncuentro(EMAIL_FUNCIONARIO, ID_ENCUENTRO)).thenReturn(true);
        when(tieneAsignadoRepo.findByEmailFuncionario(EMAIL_FUNCIONARIO)).thenReturn(List.of(ta));
        when(funcionarioRepo.findById(EMAIL_FUNCIONARIO)).thenReturn(Optional.of(funcionario));
        when(entradaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(validaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void escanear_exitoso_marcaEntradaConsumidaYRegistraAuditoria() {
        String resultado = validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO);

        assertEquals("Entrada validada correctamente", resultado);
        assertEquals(Entrada.Estado.consumida, entrada.getEstado());
        verify(validaRepo).save(argThat(v ->
            v.getCodigoAceptado().equals(TOKEN) &&
            v.getFuncionario().equals(funcionario) &&
            v.getIdDispositivo().equals(DISPOSITIVO)
        ));
    }

    @Test
    void escanear_entradaYaConsumida_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.consumida);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("consumida"));
    }

    @Test
    void escanear_entradaConTransferenciaPendiente_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.transferida_pendiente);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("transferencia pendiente"));
    }

    @Test
    void escanear_entradaReservada_lanzaExcepcion() {
        entrada.setEstado(Entrada.Estado.reservada);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("confirmada"));
    }

    @Test
    void escanear_tokenInvalido_lanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear("token-falso", EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("inválido"));
    }

    @Test
    void escanear_tokenExpirado_lanzaExcepcion() {
        entrada.setQrTokenExpiraEn(LocalDateTime.now().minusSeconds(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("expirado"));
    }

    @Test
    void escanear_funcionarioNoAsignadoAlEncuentro_lanzaExcepcion() {
        when(asignadoARepo.existsByEmailFuncionarioAndIdEncuentro(EMAIL_FUNCIONARIO, ID_ENCUENTRO)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("asignado"));
    }

    @Test
    void escanear_funcionarioSinDispositivo_lanzaExcepcion() {
        when(tieneAsignadoRepo.findByEmailFuncionario(EMAIL_FUNCIONARIO)).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("dispositivo"));
    }

    @Test
    void escanear_exitoso_noVuelveAValidarMismaEntrada() {
        validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> validacionService.escanear(TOKEN, EMAIL_FUNCIONARIO));
        assertTrue(ex.getMessage().contains("consumida"));
    }
}
