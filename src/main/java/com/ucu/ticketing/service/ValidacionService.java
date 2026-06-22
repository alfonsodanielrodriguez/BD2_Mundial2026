package com.ucu.ticketing.service;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ValidacionService {

    private final EntradaRepository entradaRepo;
    private final ValidaRepository validaRepo;
    private final FuncionarioRepository funcionarioRepo;
    private final DispositivoRepository dispositivoRepo;

    public ValidacionService(EntradaRepository entradaRepo, ValidaRepository validaRepo,
                             FuncionarioRepository funcionarioRepo, DispositivoRepository dispositivoRepo) {
        this.entradaRepo = entradaRepo;
        this.validaRepo = validaRepo;
        this.funcionarioRepo = funcionarioRepo;
        this.dispositivoRepo = dispositivoRepo;
    }

    @Transactional
    public String escanear(Integer idEntrada, String idDispositivo, String codigoQr, String emailFuncionario) {
        Entrada entrada = entradaRepo.findById(idEntrada)
            .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));

        if (entrada.getEstado() == Entrada.Estado.consumida)
            throw new RuntimeException("La entrada ya fue consumida");

        if (entrada.getEstado() == Entrada.Estado.transferida_pendiente)
            throw new RuntimeException("La entrada tiene una transferencia pendiente");

        if (!codigoQr.equals(entrada.getQrTokenActual()))
            throw new RuntimeException("QR inválido o expirado");

        if (entrada.getQrTokenExpiraEn() != null && LocalDateTime.now().isAfter(entrada.getQrTokenExpiraEn()))
            throw new RuntimeException("QR expirado");

        if (!dispositivoRepo.existsById(idDispositivo))
            throw new RuntimeException("Dispositivo no autorizado");

        FuncionarioValidacion funcionario = funcionarioRepo.findById(emailFuncionario)
            .orElseThrow(() -> new RuntimeException("Funcionario no encontrado"));

        entrada.setEstado(Entrada.Estado.consumida);
        entradaRepo.save(entrada);

        Valida valida = new Valida();
        valida.setIdDispositivo(idDispositivo);
        valida.setIdEntrada(idEntrada);
        valida.setEncuentro(entrada.getEncuentro());
        valida.setFuncionario(funcionario);
        valida.setCodigoAceptado(codigoQr);
        valida.setHora(LocalDateTime.now());
        validaRepo.save(valida);

        return "Entrada validada correctamente";
    }
}
