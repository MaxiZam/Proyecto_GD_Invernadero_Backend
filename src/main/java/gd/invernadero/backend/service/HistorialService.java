package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialService {

    private final DatoHistoricoRepository datoHistoricoRepository;

    // Búfer en memoria thread-safe para acumular las lecturas del día
    private final Queue<DatosInvernaderoDTO> bufferMuestras = new ConcurrentLinkedQueue<>();

    // Variable inyectada para consultar o reprogramar el intervalo dinámicamente
    @Value("${invernadero.historico.intervalo-ms}")
    private long intervaloHistoricoMs;

    public void registrarMuestra(DatosInvernaderoDTO telemetria) {
        bufferMuestras.add(telemetria);
    }

    @Scheduled(fixedDelayString = "${invernadero.historico.intervalo-ms}")
    public void consolidarDatosHistoricos() {
        if (bufferMuestras.isEmpty()) {
            log.info("No hay lecturas registradas en el búfer para el período actual.");
            return;
        }

        // 1. Drenar todas las muestras acumuladas durante el intervalo
        List<DatosInvernaderoDTO> muestras = new ArrayList<>();
        DatosInvernaderoDTO dato;
        while ((dato = bufferMuestras.poll()) != null) {
            muestras.add(dato);
        }

        int totalMuestras = muestras.size();

        // 2. Cálculo de Promedios, Mínimos y Máximos de Sensores
        DoubleSummaryStatistics statsTemp = muestras.stream()
                .mapToDouble(DatosInvernaderoDTO::getTemperatura)
                .summaryStatistics();

        DoubleSummaryStatistics statsHumSuelo = muestras.stream()
                .mapToDouble(DatosInvernaderoDTO::getHumedadSuelo)
                .summaryStatistics();

        DoubleSummaryStatistics statsHumAire = muestras.stream()
                .mapToDouble(DatosInvernaderoDTO::getHumedadAire)
                .summaryStatistics();

        IntSummaryStatistics statsLuz = muestras.stream()
                .mapToInt(DatosInvernaderoDTO::getLuminosidad)
                .summaryStatistics();

        // 3. Cálculo de Porcentajes de Uso de Actuadores ((activos / totalMuestras) * 100)
        double pctCaloventor = (muestras.stream().filter(DatosInvernaderoDTO::isCaloventorActivo).count() * 100.0) / totalMuestras;
        double pctHumidificador = (muestras.stream().filter(DatosInvernaderoDTO::isHumidificadorActivo).count() * 100.0) / totalMuestras;
        double pctVentanales = (muestras.stream().filter(DatosInvernaderoDTO::isVentanalesAbiertos).count() * 100.0) / totalMuestras;
        double pctPersianas = (muestras.stream().filter(DatosInvernaderoDTO::isPersianasEnrolladas).count() * 100.0) / totalMuestras;
        double pctBomba = (muestras.stream().filter(DatosInvernaderoDTO::isBombaActiva).count() * 100.0) / totalMuestras;

        // 4. Construcción y persistencia de la fila diaria consolidada
        DatoHistorico historico = new DatoHistorico();
        historico.setFechaHora(LocalDateTime.now());

        // Promedios
        historico.setTemperaturaMedia(redondear(statsTemp.getAverage()));
        historico.setHumedadSueloMedia(redondear(statsHumSuelo.getAverage()));
        historico.setHumedadAireMedia(redondear(statsHumAire.getAverage()));
        historico.setLuminosidadMedia(redondear(statsLuz.getAverage()));

        // Mínimos y Máximos
        historico.setTemperaturaMin(redondear(statsTemp.getMin()));
        historico.setTemperaturaMax(redondear(statsTemp.getMax()));
        historico.setHumedadSueloMin(redondear(statsHumSuelo.getMin()));
        historico.setHumedadSueloMax(redondear(statsHumSuelo.getMax()));
        historico.setHumedadAireMin(redondear(statsHumAire.getMin()));
        historico.setHumedadAireMax(redondear(statsHumAire.getMax()));
        historico.setLuminosidadMin(statsLuz.getMin());
        historico.setLuminosidadMax(statsLuz.getMax());

        // Porcentajes de actuación
        historico.setPorcentajeCaloventor(redondear(pctCaloventor));
        historico.setPorcentajeHumidificador(redondear(pctHumidificador));
        historico.setPorcentajeVentanales(redondear(pctVentanales));
        historico.setPorcentajePersianas(redondear(pctPersianas));
        historico.setPorcentajeBomba(redondear(pctBomba));

        datoHistoricoRepository.save(historico);

        log.info("Consolidación histórica completada: {} muestras. TempMedia: {}°C (Min: {}, Max: {}), Uso Bomba: {}%",
                totalMuestras, historico.getTemperaturaMedia(), historico.getTemperaturaMin(),
                historico.getTemperaturaMax(), historico.getPorcentajeBomba());
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}