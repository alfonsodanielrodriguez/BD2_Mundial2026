package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Sector.SectorId> {
    List<Sector> findByIdEstadio(Integer idEstadio);
}