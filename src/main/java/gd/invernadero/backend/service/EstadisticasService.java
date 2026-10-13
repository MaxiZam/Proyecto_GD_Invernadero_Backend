package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.EstadisticasMensualesDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasService {

    private static final double UMBRAL_RIESGO_FUNGICO = 80.0;
    private final DatoHistoricoRepository repo;

    @Transactional(readOnly = true)
    public EstadisticasMensualesDTO ultimos30Dias() {
        List<DatoHistorico> d = repo.findByFechaHoraAfterOrderByFechaHoraAsc(LocalDateTime.now().minusDays(30));
        EstadisticasMensualesDTO dto = new EstadisticasMensualesDTO();
        if (d.isEmpty()) return dto;

        dto.setTemperaturaMediaMensual(avg(d, DatoHistorico::getTemperaturaMedia));
        dto.setTemperaturaMaximaMensual(d.stream().mapToDouble(DatoHistorico::getTemperaturaMax).max().orElse(0));
        dto.setTemperaturaMinimaMensual(d.stream().mapToDouble(DatoHistorico::getTemperaturaMin).min().orElse(0));
        dto.setHumedadSueloMediaMensual(avg(d, DatoHistorico::getHumedadSueloMedia));
        dto.setHumedadAireMediaMensual(avg(d, DatoHistorico::getHumedadAireMedia));
        dto.setLuminosidadMediaMensual(avg(d, DatoHistorico::getLuminosidadMedia));
        dto.setUsoPromedioCaloventor(avg(d, DatoHistorico::getPorcentajeCaloventor));
        dto.setUsoPromedioHumidificador(avg(d, DatoHistorico::getPorcentajeHumidificador));
        dto.setUsoPromedioVentanales(avg(d, DatoHistorico::getPorcentajeVentanales));
        dto.setUsoPromedioPersianas(avg(d, DatoHistorico::getPorcentajePersianas));
        dto.setUsoPromedioBomba(avg(d, DatoHistorico::getPorcentajeBomba));

        // Día con riesgo fúngico = humedad media del aire de ese día >= umbral
        long dias = d.stream()
                .collect(Collectors.groupingBy(x -> x.getFechaHora().toLocalDate(),
                        Collectors.averagingDouble(DatoHistorico::getHumedadAireMedia)))
                .values().stream().filter(h -> h >= UMBRAL_RIESGO_FUNGICO).count();
        dto.setDiasRiesgoFungico((int) dias);
        return dto;
    }

    private double avg(List<DatoHistorico> l, ToDoubleFunction<DatoHistorico> f) {
        return Math.round(l.stream().mapToDouble(f).average().orElse(0) * 100.0) / 100.0;
    }
}
