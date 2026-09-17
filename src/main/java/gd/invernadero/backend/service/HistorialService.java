package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistorialService {

    private final DatoHistoricoRepository datoHistoricoRepository;

    // Búfer en memoria para acumular muestras recibidas del simulador
    private final Queue<DatosInvernaderoDTO> bufferMuestras = new ConcurrentLinkedQueue<>();

    // Agrega cada lectura entrante al búfer sin bloquear la ejecución de RabbitMQ
    public void registrarMuestra(DatosInvernaderoDTO telemetria) {
        bufferMuestras.add(telemetria);
    }

    // Tarea periódica parametrizada por application.properties
    @Scheduled(fixedDelayString = "${invernadero.historico.intervalo-ms}")
    public void consolidarDatosHistoricos() {
        if (bufferMuestras.isEmpty()) {
            log.info("No hay muestras acumuladas en este intervalo para generar DatoHistorico.");
            return;
        }

        // 1. Extraer todas las lecturas acumuladas en este período
        List<DatosInvernaderoDTO> muestras = new ArrayList<>();
        DatosInvernaderoDTO dato;
        while ((dato = bufferMuestras.poll()) != null) {
            muestras.add(dato);
        }

        int totalMuestras = muestras.size();

        // 2. Cálculo de Promedios de Sensores (Media aritmética)
        double promTemperatura = muestras.stream()
                .mapToDouble(DatosInvernaderoDTO::getTemperatura)
                .average()
                .orElse(0.0);

        double promHumedadSuelo = muestras.stream()
                .mapToDouble(DatosInvernaderoDTO::getHumedadSuelo)
                .average()
                .orElse(0.0);

        double promHumedadAire = muestras.stream()
                .mapToDouble(DatosInvernaderoDTO::getHumedadAire)
                .average()
                .orElse(0.0);

        double promLuminosidad = muestras.stream()
                .mapToInt(DatosInvernaderoDTO::getLuminosidad)
                .average()
                .orElse(0.0);

        // 3. Cálculo de Porcentajes de Activación de Actuadores ((activos / total) * 100)
        double pctCaloventor = (muestras.stream().filter(DatosInvernaderoDTO::isCaloventorActivo).count() * 100.0) / totalMuestras;
        double pctHumidificador = (muestras.stream().filter(DatosInvernaderoDTO::isHumidificadorActivo).count() * 100.0) / totalMuestras;
        double pctVentanales = (muestras.stream().filter(DatosInvernaderoDTO::isVentanalesAbiertos).count() * 100.0) / totalMuestras;
        double pctPersianas = (muestras.stream().filter(DatosInvernaderoDTO::isPersianasEnrolladas).count() * 100.0) / totalMuestras;
        double pctBomba = (muestras.stream().filter(DatosInvernaderoDTO::isBombaActiva).count() * 100.0) / totalMuestras;

        // 4. Construcción y persistencia de la entidad histórica consolidada
        DatoHistorico historico = new DatoHistorico();
        historico.setFechaHora(LocalDateTime.now());
        historico.setTemperatura(Math.round(promTemperatura * 100.0) / 100.0);
        historico.setHumedadSuelo(Math.round(promHumedadSuelo * 100.0) / 100.0);
        historico.setHumedadAire(Math.round(promHumedadAire * 100.0) / 100.0);
        historico.setLuminosidad((int) Math.round(promLuminosidad));

        // Porcentajes de uso (almacenados como valores numéricos de 0 a 100%)
        historico.setPorcentajeCaloventor(Math.round(pctCaloventor * 100.0) / 100.0);
        historico.setPorcentajeHumidificador(Math.round(pctHumidificador * 100.0) / 100.0);
        historico.setPorcentajeVentanales(Math.round(pctVentanales * 100.0) / 100.0);
        historico.setPorcentajePersianas(Math.round(pctPersianas * 100.0) / 100.0);
        historico.setPorcentajeBomba(Math.round(pctBomba * 100.0) / 100.0);

        datoHistoricoRepository.save(historico);

        log.info("DatoHistorico consolidado exitosamente con {} muestras. TempMedia: {}°C, Bomba: {}%",
                totalMuestras, historico.getTemperatura(), historico.getPorcentajeBomba());
    }
}