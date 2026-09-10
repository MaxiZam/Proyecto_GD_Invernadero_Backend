package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.EstadisticasMensualesDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EstadisticasService {

    @Autowired
    private DatoHistoricoRepository historicoRepo;

    public EstadisticasMensualesDTO calcularEstadisticasMensuales() {
        // Buscar los datos de los últimos 30 días
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        List<DatoHistorico> historial = historicoRepo.findByFechaAfter(hace30Dias);

        EstadisticasMensualesDTO stats = new EstadisticasMensualesDTO();

        if (historial.isEmpty()) {
            return stats;
        }

        // Calculos termicos
        double tempMedia = historial.stream().mapToDouble(DatoHistorico::getTempPromedio).average().orElse(0.0);
        double tempMax = historial.stream().mapToDouble(DatoHistorico::getTempPromedio).max().orElse(0.0);
        double tempMin = historial.stream().mapToDouble(DatoHistorico::getTempPromedio).min().orElse(0.0);

        // Calculos de humedad y riesgo
        double humSueloMedia = historial.stream().mapToDouble(DatoHistorico::getHumSueloPromedio).average().orElse(0.0);
        double humAireMedia = historial.stream().mapToDouble(DatoHistorico::getHumAirePromedio).average().orElse(0.0);
        long diasHongos = historial.stream().filter(d -> d.getHumAirePromedio() > 80.0).count();

        // Eficiencia de luz y actuadores
        double lumMedia = historial.stream().mapToDouble(DatoHistorico::getLumPromedio).average().orElse(0.0);
        double usoCaloventor = historial.stream().mapToDouble(DatoHistorico::getCaloventorUsoPorcentaje).average().orElse(0.0);
        double usoHumidificador = historial.stream().mapToDouble(DatoHistorico::getHumidificadorUsoPorcentaje).average().orElse(0.0);
        double usoVentanales = historial.stream().mapToDouble(DatoHistorico::getVentanalesUsoPorcentaje).average().orElse(0.0);
        double usoBomba = historial.stream().mapToDouble(DatoHistorico::getBombaUsoPorcentaje).average().orElse(0.0);

        // Ensamble de DTO
        stats.setTemperaturaMediaMensual(tempMedia);
        stats.setTemperaturaMaximaMensual(tempMax);
        stats.setTemperaturaMinimaMensual(tempMin);
        stats.setHumedadSueloMediaMensual(humSueloMedia);
        stats.setHumedadAireMediaMensual(humAireMedia);
        stats.setDiasRiesgoFungico((int) diasHongos);
        stats.setLuminosidadMediaMensual(lumMedia);
        stats.setUsoPromedioCaloventor(usoCaloventor);
        stats.setUsoPromedioHumidificador(usoHumidificador);
        stats.setUsoPromedioVentanales(usoVentanales);
        stats.setUsoPromedioBomba(usoBomba);

        return stats;
    }
}
