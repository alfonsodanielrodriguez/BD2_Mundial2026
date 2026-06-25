package com.ucu.ticketing.service;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ValidacionService {

    private final EntradaRepository entradaRepo;
    private final ValidaRepository validaRepo;
    private final FuncionarioRepository funcionarioRepo;
    private final TieneAsignadoRepository tieneAsignadoRepo;
    private final AsignadoARepository asignadoARepo;

    public ValidacionService(EntradaRepository entradaRepo, ValidaRepository validaRepo,
                             FuncionarioRepository funcionarioRepo, TieneAsignadoRepository tieneAsignadoRepo,
                             AsignadoARepository asignadoARepo) {
        this.entradaRepo = entradaRepo;
        this.validaRepo = validaRepo;
        this.funcionarioRepo = funcionarioRepo;
        this.tieneAsignadoRepo = tieneAsignadoRepo;
        this.asignadoARepo = asignadoARepo;
    }

    @Transactional
    public String escanear(String codigoQr, String emailFuncionario) {
        Entrada entrada = entradaRepo.findByQrTokenActual(codigoQr)
            .orElseThrow(() -> new RuntimeException("QR inválido — entrada no encontrada"));

        if (entrada.getEstado() == Entrada.Estado.consumida)
            throw new RuntimeException("La entrada ya fue consumida");

        if (entrada.getEstado() == Entrada.Estado.transferida_pendiente)
            throw new RuntimeException("La entrada tiene una transferencia pendiente");

        if (entrada.getEstado() == Entrada.Estado.reservada)
            throw new RuntimeException("La entrada no ha sido confirmada (compra pendiente)");

        if (entrada.getQrTokenExpiraEn() != null && LocalDateTime.now().isAfter(entrada.getQrTokenExpiraEn()))
            throw new RuntimeException("QR expirado");

        Integer idEncuentro = entrada.getEncuentro().getIdEncuentro();
        String letraSector = entrada.getLetraSector();
        Integer idEstadio = entrada.getIdEstadio();
        if (!asignadoARepo.existsByEmailFuncionarioAndIdEncuentroAndLetraSectorAndIdEstadio(
                emailFuncionario, idEncuentro, letraSector, idEstadio))
            throw new RuntimeException("El funcionario no está asignado al sector " + letraSector + " de este encuentro");

        List<TieneAsignado> asignados = tieneAsignadoRepo.findByEmailFuncionario(emailFuncionario);
        if (asignados.isEmpty())
            throw new RuntimeException("El funcionario no tiene un dispositivo asignado");
        String idDispositivo = asignados.get(0).getIdDispositivo();

        FuncionarioValidacion funcionario = funcionarioRepo.findById(emailFuncionario)
            .orElseThrow(() -> new RuntimeException("Funcionario no encontrado"));

        entrada.setEstado(Entrada.Estado.consumida);
        entradaRepo.save(entrada);

        Valida valida = new Valida();
        valida.setIdDispositivo(idDispositivo);
        valida.setIdEntrada(entrada.getIdEntrada());
        valida.setEncuentro(entrada.getEncuentro());
        valida.setFuncionario(funcionario);
        valida.setCodigoAceptado(codigoQr);
        valida.setHora(LocalDateTime.now());
        validaRepo.save(valida);

        return "Entrada validada correctamente";
    }
}
