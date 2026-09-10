package gd.invernadero.backend.repository;

import gd.invernadero.backend.model.DatoTemporal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatoTemporalRepository extends JpaRepository<DatoTemporal, Long> {
}
