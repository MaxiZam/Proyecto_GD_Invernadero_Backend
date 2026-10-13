package gd.invernadero.backend.controller;

import gd.invernadero.backend.dto.EstadisticasMensualesDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import gd.invernadero.backend.service.EstadisticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/invernadero")
@RequiredArgsConstructor
public class InvernaderoController {

    private final DatoHistoricoRepository repo;
    private final EstadisticasService estadisticasService;

    @GetMapping("/historial/rango")
    public List<DatoHistorico> porRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return repo.findByFechaHoraBetweenOrderByFechaHoraAsc(fechaInicio, fechaFin);
    }

    @GetMapping("/historial/ultimo")
    public ResponseEntity<DatoHistorico> ultimo() {
        return repo.findTopByOrderByFechaHoraDesc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/historial")                       // gráficos: últimos 30 días
    public List<DatoHistorico> ultimos30Dias() {
        return repo.findByFechaHoraAfterOrderByFechaHoraAsc(LocalDateTime.now().minusDays(30));
    }

    @GetMapping("/historial-completo")              // tabla: paginada, más reciente primero
    public List<DatoHistorico> tabla(
            @PageableDefault(size = 100, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable p) {
        return repo.findAll(p).getContent();
    }

    @GetMapping("/estadisticas")
    public EstadisticasMensualesDTO estadisticas() {
        return estadisticasService.ultimos30Dias();
    }
}
