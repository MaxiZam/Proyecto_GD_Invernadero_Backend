package gd.invernadero.backend.service;

import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertasService {

    private DatosInvernaderoDTO lecturaAnterior;
    private long ultimoTiempoRecepcion = System.currentTimeMillis();
    private int ciclosHumedadEstancada = 0;

    public List<AlertaDTO> evaluarDatos(DatosInvernaderoDTO datoActual) {
        List<AlertaDTO> nuevasAlertas = new ArrayList<>();
        ultimoTiempoRecepcion = System.currentTimeMillis(); // Actualizamos el "ping"

        // ALERTAS DE UMBRAL
        if (datoActual.getTemperatura() < 20.0 || datoActual.getTemperatura() > 28.0) {
            nuevasAlertas.add(new AlertaDTO("UMBRAL", "Sensor de Temperatura",
                    "Temperatura fuera de rango óptimo: " + datoActual.getTemperatura() + "°C", "ADVERTENCIA", LocalDateTime.now()));
        }

        if (datoActual.getHumedadSuelo() < 30.0) {
            nuevasAlertas.add(new AlertaDTO("UMBRAL", "Sensor de Humedad Suelo",
                    "Déficit hídrico crítico. Nivel al " + datoActual.getHumedadSuelo() + "%", "CRITICO", LocalDateTime.now()));
        }

        if (datoActual.getHumedadAire() > 80.0) {
            nuevasAlertas.add(new AlertaDTO("UMBRAL", "Sensor Humedad Aire",
                    "Riesgo Fúngico: Humedad ambiente excesiva (" + datoActual.getHumedadAire() + "%)", "ADVERTENCIA", LocalDateTime.now()));
        }

        if (datoActual.getLuminosidad() < 20000 || datoActual.getLuminosidad() > 50000) {
            nuevasAlertas.add(new AlertaDTO("UMBRAL", "Sensor de Luminosidad",
                    "Luz fuera del fotoperiodo óptimo: " + datoActual.getLuminosidad() + " lux", "ADVERTENCIA", LocalDateTime.now()));
        }

        // ALERTAS DE ANOMALIA
        if (datoActual.getHumedadSuelo() <= 0.0) {
            nuevasAlertas.add(new AlertaDTO("ANOMALIA", "Sensor de Humedad Suelo",
                    "Dato inválido: Caída abrupta a cero o negativo.", "CRITICO", LocalDateTime.now()));
        }

        if (lecturaAnterior != null) {
            // Fluctuación irreal: Salto de 15 grados de un ciclo a otro
            if (Math.abs(datoActual.getTemperatura() - lecturaAnterior.getTemperatura()) >= 15.0) {
                nuevasAlertas.add(new AlertaDTO("ANOMALIA", "Sensor de Temperatura",
                        "Fluctuación irreal: Salto térmico abrupto detectado. Posible falso contacto.", "CRITICO", LocalDateTime.now()));
            }

            if (datoActual.getHumedadAire() == lecturaAnterior.getHumedadAire()) {
                ciclosHumedadEstancada++;
                if (ciclosHumedadEstancada > 50) {
                    nuevasAlertas.add(new AlertaDTO("ANOMALIA", "Sensor Humedad Aire",
                            "Valor estancado: Sin variaciones prolongadas. Posible bloqueo físico.", "ADVERTENCIA", LocalDateTime.now()));
                    ciclosHumedadEstancada = 0;
                }
            } else {
                ciclosHumedadEstancada = 0;
            }
        }

        // ALERTAS DE FALLA DE HARDWARE
        if (datoActual.getHumedadSuelo() < 30.0 && !datoActual.isBombaActiva()) {
            nuevasAlertas.add(new AlertaDTO("HARDWARE", "Bomba de 12V",
                    "Falla de respuesta automática: La bomba no inició operación con humedad < 30%.", "CRITICO", LocalDateTime.now()));
        }

        this.lecturaAnterior = datoActual;

        return nuevasAlertas;
    }

    // 4. TIMEOUT
    @Scheduled(fixedRate = 10000)
    public void verificarConexionSensores() {
        long tiempoActual = System.currentTimeMillis();
        long tiempoSinDatos = tiempoActual - ultimoTiempoRecepcion;

        if (tiempoSinDatos > 60000) {
            System.out.println("ALERTA HARDWARE - Pérdida de telemetría: Tiempo de espera agotado.");
        }
    }
}
