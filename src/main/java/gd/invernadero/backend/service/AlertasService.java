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
    private final Map<String, Instant> ultimaEmision = new ConcurrentHashMap<>();

    public List<AlertaDTO> evaluarDatosConUmbrales(DatosInvernaderoDTO d, ConfiguracionUmbralesDTO u) {
        List<AlertaDTO> out = new ArrayList<>();
        double t = d.getTemperatura(), hs = d.getHumedadSuelo(), ha = d.getHumedadAire();
        int luz = d.getLuminosidad();

        if (t < u.getTempMin() || t > u.getTempMax())
            emitir(out, "UMBRAL", "Sensor Temperatura", "Temperatura fuera de rango: " + t + "°C", "ADVERTENCIA");
        if (hs < u.getHumSueloMin())
            emitir(out, "UMBRAL", "Sensor Humedad Suelo", "Déficit hídrico en sustrato: " + hs + "%", "CRITICO");
        if (hs > u.getHumSueloMax())
            emitir(out, "UMBRAL", "Sensor Humedad Suelo", "Exceso de agua en sustrato: " + hs + "%", "ADVERTENCIA");
        if (ha < u.getHumAireMin())
            emitir(out, "UMBRAL", "Sensor Humedad Aire", "Aire demasiado seco: " + ha + "%", "ADVERTENCIA");
        if (ha > u.getHumAireMax())
            emitir(out, "UMBRAL", "Sensor Humedad Aire", "Exceso de humedad ambiente: " + ha + "%", "ADVERTENCIA");
        if (luz < u.getLuzMin() || luz > u.getLuzMax())
            emitir(out, "UMBRAL", "Sensor Luminosidad", "Nivel lumínico fuera de consigna: " + luz + " lux", "ADVERTENCIA");
        if (hs < u.getHumSueloMin() && !d.isBombaActiva())
            emitir(out, "HARDWARE", "Bomba de Agua", "Suelo seco con la bomba apagada (¿modo manual o falla?).", "CRITICO");
        return out;
    }

    private void emitir(List<AlertaDTO> destino, String tipo, String comp, String msg, String sev) {
        String clave = tipo + '|' + comp + '|' + sev;
        Instant ahora = Instant.now(), previa = ultimaEmision.get(clave);
        if (previa != null && Duration.between(previa, ahora).getSeconds() < cooldownSeg) return;
        ultimaEmision.put(clave, ahora);
        destino.add(new AlertaDTO(tipo, comp, msg, sev, LocalDateTime.now()));
    }
}
