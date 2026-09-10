package gd.invernadero.backend.repository;

import gd.invernadero.backend.model.DatoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DatoHistoricoRepository extends JpaRepository<DatoHistorico, Long> {
    void deleteByFechaBefore(LocalDateTime fecha);
    List<DatoHistorico> findByFechaAfter(LocalDateTime fecha);
    List<DatoHistorico> findAllByOrderByFechaDesc();
}
