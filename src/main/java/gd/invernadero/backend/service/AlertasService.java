package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertasService {

    @Value("${invernadero.alertas.cooldown-seg:60}")
    private long cooldownSeg;

    /** Última emisión por código de alerta, para no repetir la misma condición en cada muestra. */
    private final Map<String, Instant> ultimaEmision = new ConcurrentHashMap<>();

    public List<AlertaDTO> evaluarDatosConUmbrales(DatosInvernaderoDTO d, ConfiguracionUmbralesDTO u) {
        List<AlertaDTO> out = new ArrayList<>();
        double t = d.getTemperatura(), hs = d.getHumedadSuelo(), ha = d.getHumedadAire();
        int luz = d.getLuminosidad();

        if (t < u.getTempMin() || t > u.getTempMax())
            emitir(out, "UMBRAL", "TEMPERATURA_FUERA_RANGO", "Sensor Temperatura",
                    "Temperatura fuera de rango: " + t + "°C", "ADVERTENCIA");
        if (hs < u.getHumSueloMin())
            emitir(out, "UMBRAL", "SUELO_DEFICIT", "Sensor Humedad Suelo",
                    "Déficit hídrico en sustrato: " + hs + "%", "CRITICO");
        if (hs > u.getHumSueloMax())
            emitir(out, "UMBRAL", "SUELO_EXCESO", "Sensor Humedad Suelo",
                    "Exceso de agua en sustrato: " + hs + "%", "ADVERTENCIA");
        if (ha < u.getHumAireMin())
            emitir(out, "UMBRAL", "AIRE_SECO", "Sensor Humedad Aire",
                    "Aire demasiado seco: " + ha + "%", "ADVERTENCIA");
        if (ha > u.getHumAireMax())
            emitir(out, "UMBRAL", "AIRE_HUMEDO", "Sensor Humedad Aire",
                    "Exceso de humedad ambiente: " + ha + "%", "ADVERTENCIA");
        // La luz mínima solo se evalúa de día
        if ((d.isEsDeDia() && luz < u.getLuzMin()) || luz > u.getLuzMax())
            emitir(out, "UMBRAL", "LUZ_FUERA_RANGO", "Sensor Luminosidad",
                    "Nivel lumínico fuera de consigna: " + luz + " lux", "ADVERTENCIA");
        if (hs < u.getHumSueloMin() && !d.isBombaActiva())
            emitir(out, "HARDWARE", "BOMBA_SIN_RIEGO", "Bomba de Agua",
                    "Suelo seco con la bomba apagada (¿modo manual o falla?).", "CRITICO");
        return out;
    }

    private void emitir(List<AlertaDTO> destino, String tipo, String codigo,
                        String componente, String mensaje, String severidad) {
        Instant ahora = Instant.now();
        Instant previa = ultimaEmision.get(codigo);
        if (previa != null && Duration.between(previa, ahora).getSeconds() < cooldownSeg) return;
        ultimaEmision.put(codigo, ahora);
        destino.add(new AlertaDTO(tipo, codigo, componente, mensaje, severidad, LocalDateTime.now()));
    }
}
