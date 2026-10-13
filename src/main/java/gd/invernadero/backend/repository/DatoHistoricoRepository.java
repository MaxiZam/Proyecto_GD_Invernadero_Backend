package gd.invernadero.backend.repository;

import gd.invernadero.backend.model.DatoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DatoHistoricoRepository extends JpaRepository<DatoHistorico, Long> {
    List<DatoHistorico> findByFechaHoraAfterOrderByFechaHoraAsc(LocalDateTime desde);
    List<DatoHistorico> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime desde, LocalDateTime hasta);
    Optional<DatoHistorico> findTopByOrderByFechaHoraDesc();

    @Modifying
    @Transactional
    @Query("delete from DatoHistorico d where d.fechaHora < :limite")
    int eliminarAnterioresA(@Param("limite") LocalDateTime limite);
}
