package gd.invernadero.backend.controller;

import gd.invernadero.backend.dto.EstadisticasMensualesDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import gd.invernadero.backend.service.EstadisticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/invernadero")
@CrossOrigin(origins = "*")
public class InvernaderoController {

    @Autowired
    private EstadisticasService estadisticasService;

    @Autowired
    private DatoHistoricoRepository historicoRepo;

    /**
     * Endpoint 1: Devuelve los calculos agronomicos mensuales.
     * Ruta: GET http://localhost:8080/api/invernadero/estadisticas
     */
    @GetMapping("/estadisticas")
    public EstadisticasMensualesDTO obtenerEstadisticas() {
        return estadisticasService.calcularEstadisticasMensuales();
    }

    /**
     * Endpoint 2: Devuelve la lista cruda de los ultimos 30 días.
     * Ruta: GET http://localhost:8080/api/invernadero/historial
     */
    @GetMapping("/historial")
    public List<DatoHistorico> obtenerHistorialGraficos() {
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        return historicoRepo.findByFechaAfter(hace30Dias);
    }

    /**
     * Endpoint 3: Devuelve todo el historial almacenado.
     * Ruta: GET http://localhost:8080/api/invernadero/historial-completo
     */
    @GetMapping("/historial-completo")
    public List<DatoHistorico> obtenerHistorialTabla() {
        return historicoRepo.findAllByOrderByFechaDesc();
    }
}
