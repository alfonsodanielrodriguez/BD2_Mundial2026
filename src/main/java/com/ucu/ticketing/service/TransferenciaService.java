package com.ucu.ticketing.service;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepo;
    private final EntradaRepository entradaRepo;
    private final UsuarioGeneralRepository usuarioRepo;

    public TransferenciaService(TransferenciaRepository transferenciaRepo,
                                EntradaRepository entradaRepo,
                                UsuarioGeneralRepository usuarioRepo) {
        this.transferenciaRepo = transferenciaRepo;
        this.entradaRepo = entradaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public Transferencia iniciarTransferencia(String emailEmisor, String emailReceptor, Integer idEntrada) {
        Entrada entrada = entradaRepo.findById(idEntrada)
            .orElseThrow(() -> new RuntimeException("Entrada no encontrada"));

        if (!entrada.getPropietarioActual().getEmail().equals(emailEmisor))
            throw new RuntimeException("No sos el propietario de esta entrada");

        if (entrada.getEstado() != Entrada.Estado.activa)
            throw new RuntimeException("La entrada no está disponible para transferir");

        if (entrada.getCantidadTransferencias() >= 3)
            throw new RuntimeException("La entrada alcanzó el límite de 3 transferencias");

        UsuarioGeneral receptor = usuarioRepo.findById(emailReceptor)
            .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));

        entrada.setEstado(Entrada.Estado.transferida_pendiente);
        entradaRepo.save(entrada);

        Transferencia t = new Transferencia();
        t.setEmisor(entrada.getPropietarioActual());
        t.setReceptor(receptor);
        t.setEntrada(entrada);
        t.setFecha(LocalDate.now());
        t.setHora(LocalTime.now());
        t.setEstado(Transferencia.Estado.pendiente);
        return transferenciaRepo.save(t);
    }

    @Transactional
    public Transferencia responderTransferencia(Integer idTransferencia, String emailReceptor, boolean aceptar) {
        Transferencia t = transferenciaRepo.findById(idTransferencia)
            .orElseThrow(() -> new RuntimeException("Transferencia no encontrada"));

        if (!t.getReceptor().getEmail().equals(emailReceptor))
            throw new RuntimeException("No sos el receptor de esta transferencia");

        if (t.getEstado() != Transferencia.Estado.pendiente)
            throw new RuntimeException("La transferencia ya fue procesada");

        t.setFechaRespuesta(LocalDateTime.now());

        if (aceptar) {
            t.setEstado(Transferencia.Estado.aceptada);
            Entrada entrada = t.getEntrada();
            entrada.setPropietarioActual(t.getReceptor());
            entrada.setCantidadTransferencias((byte)(entrada.getCantidadTransferencias() + 1));
            entrada.setEstado(Entrada.Estado.activa);
            entradaRepo.save(entrada);
        } else {
            t.setEstado(Transferencia.Estado.rechazada);
            Entrada entrada = t.getEntrada();
            entrada.setEstado(Entrada.Estado.activa);
            entradaRepo.save(entrada);
        }

        return transferenciaRepo.save(t);
    }

    public List<Transferencia> misTransferencias(String email) {
        List<Transferencia> enviadas = transferenciaRepo.findByEmisorEmail(email);
        List<Transferencia> recibidas = transferenciaRepo.findByReceptorEmail(email);
        enviadas.addAll(recibidas);
        return enviadas;
    }
}