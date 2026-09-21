package gd.invernadero.backend.controller;

import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/invernadero")
@CrossOrigin(origins = "*")
public class InvernaderoController {

    @Autowired
    private DatoHistoricoRepository datoHistoricoRepository;

    /**
     * Retorna el histórico consolidado.
     * Si se envían 'fechaInicio' y 'fechaFin', filtra por ese rango.
     * Si no se envían parámetros, devuelve la lista completa.
     *
     * Ejemplo de uso: GET /api/invernadero/historial/rango?fechaInicio=2026-03-01T00:00:00&fechaFin=2026-03-15T23:59:59
     */
    @GetMapping("/historial/rango")
    public ResponseEntity<List<DatoHistorico>> obtenerHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        if (fechaInicio != null && fechaFin != null) {
            List<DatoHistorico> datosFiltrados = datoHistoricoRepository
                    .findByFechaHoraBetweenOrderByFechaHoraAsc(fechaInicio, fechaFin);
            return ResponseEntity.ok(datosFiltrados);
        }

        return ResponseEntity.ok(datoHistoricoRepository.findAll());
    }

    /**
     * Retorna únicamente el último registro consolidado disponible.
     * Ideal para mostrar promedios del día previo en tarjetas del dashboard.
     *
     * Ejemplo de uso: GET /api/invernadero/historial/ultimo
     */
    @GetMapping("/historial/ultimo")
    public ResponseEntity<DatoHistorico> obtenerUltimoRegistro() {
        return datoHistoricoRepository.findTopByOrderByFechaHoraDesc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Devuelve la lista cruda de los ultimos 30 días.
     * Ruta: GET http://localhost:8080/api/invernadero/historial
     */
    @GetMapping("/historial")
    public List<DatoHistorico> obtenerHistorialGraficos() {
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        return datoHistoricoRepository.findByFechaAfter(hace30Dias);
    }

    /**
     * Devuelve todo el historial almacenado.
     * Ruta: GET http://localhost:8080/api/invernadero/historial-completo
     */
    @GetMapping("/historial-completo")
    public List<DatoHistorico> obtenerHistorialTabla() {
        return datoHistoricoRepository.findAllByOrderByFechaDesc();
    }
}
