package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertasService {

    public List<AlertaDTO> evaluarDatosConUmbrales(DatosInvernaderoDTO datos, ConfiguracionUmbralesDTO u) {
        List<AlertaDTO> alertas = new ArrayList<>();

        if (datos.getTemperatura() < u.getTempMin() || datos.getTemperatura() > u.getTempMax()) {
            alertas.add(new AlertaDTO("UMBRAL", "Sensor Temperatura",
                    "Temperatura fuera de rango: " + datos.getTemperatura() + "°C",
                    "ADVERTENCIA", LocalDateTime.now()));
        }

        if (datos.getHumedadSuelo() < u.getHumSueloMin()) {
            alertas.add(new AlertaDTO("UMBRAL", "Sensor Humedad Suelo",
                    "Déficit hídrico crítico en sustrato: " + datos.getHumedadSuelo() + "%",
                    "CRITICO", LocalDateTime.now()));
        }

        if (datos.getHumedadAire() > u.getHumAireMax()) {
            alertas.add(new AlertaDTO("UMBRAL", "Sensor Humedad Aire",
                    "Exceso de humedad ambiente: " + datos.getHumedadAire() + "%",
                    "ADVERTENCIA", LocalDateTime.now()));
        }

        if (datos.getLuminosidad() < u.getLuzMin() || datos.getLuminosidad() > u.getLuzMax()) {
            alertas.add(new AlertaDTO("UMBRAL", "Sensor Luminosidad",
                    "Nivel lumínico fuera de consigna: " + datos.getLuminosidad() + " lux",
                    "ADVERTENCIA", LocalDateTime.now()));
        }

        if (datos.getHumedadSuelo() < u.getHumSueloMin() && !datos.isBombaActiva()) {
            alertas.add(new AlertaDTO("HARDWARE", "Bomba de Agua",
                    "Estrés hídrico detectado sin accionamiento de bomba.",
                    "CRITICO", LocalDateTime.now()));
        }

        return alertas;
    }
}
