package com.ucu.ticketing.service;

import com.ucu.ticketing.model.*;
import com.ucu.ticketing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CompraService {

    private final CompraRepository compraRepo;
    private final EntradaRepository entradaRepo;
    private final UsuarioGeneralRepository usuarioRepo;
    private final SectorRepository sectorRepo;
    private final EncuentroRepository encuentroRepo;

    public CompraService(CompraRepository compraRepo, EntradaRepository entradaRepo,
                         UsuarioGeneralRepository usuarioRepo, SectorRepository sectorRepo,
                         EncuentroRepository encuentroRepo) {
        this.compraRepo = compraRepo;
        this.entradaRepo = entradaRepo;
        this.usuarioRepo = usuarioRepo;
        this.sectorRepo = sectorRepo;
        this.encuentroRepo = encuentroRepo;
    }

    @Transactional
    public Compra comprar(String emailUsuario, Integer idEncuentro, String letraSector, int cantidad) {
        if (cantidad < 1 || cantidad > 5)
            throw new RuntimeException("Solo se pueden comprar entre 1 y 5 entradas por transacción");

        UsuarioGeneral usuario = usuarioRepo.findById(emailUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Encuentro encuentro = encuentroRepo.findById(idEncuentro)
            .orElseThrow(() -> new RuntimeException("Encuentro no encontrado"));

        Sector.SectorId sectorId = new Sector.SectorId(letraSector, encuentro.getEstadio().getIdEstadio());
        Sector sector = sectorRepo.findById(sectorId)
            .orElseThrow(() -> new RuntimeException("Sector no encontrado"));

        int entradasVendidas = entradaRepo.countByEncuentroIdEncuentroAndLetraSector(idEncuentro, letraSector);
        if (entradasVendidas + cantidad > sector.getAforo())
            throw new RuntimeException("No hay suficiente aforo disponible en el sector");

        BigDecimal subtotal = sector.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        BigDecimal comision = new BigDecimal("5.00");
        BigDecimal total = subtotal.multiply(BigDecimal.ONE.add(comision.divide(BigDecimal.valueOf(100))));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setFecha(LocalDate.now());
        compra.setHora(LocalTime.now());
        compra.setEstado(Compra.Estado.paga);
        compra.setComisionAplicada(comision);
        compra.setMontoTotal(total);
        compraRepo.save(compra);

        for (int i = 0; i < cantidad; i++) {
            Entrada entrada = new Entrada();
            entrada.setEncuentro(encuentro);
            entrada.setLetraSector(letraSector);
            entrada.setIdEstadio(encuentro.getEstadio().getIdEstadio());
            entrada.setMontoSector(sector.getPrecio());
            entrada.setPropietarioActual(usuario);
            entrada.setEstado(Entrada.Estado.activa);
            entrada.setCantidadTransferencias((byte) 0);
            entradaRepo.save(entrada);
        }

        return compra;
    }

    public List<Compra> misCompras(String email) {
        return compraRepo.findByUsuarioEmail(email);
    }
}