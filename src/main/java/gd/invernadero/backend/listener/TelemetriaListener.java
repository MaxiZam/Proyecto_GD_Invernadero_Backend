package gd.invernadero.backend.listener;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import gd.invernadero.backend.service.AlertasService;
import gd.invernadero.backend.service.GestorUmbralesService;
import gd.invernadero.backend.service.HistorialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetriaListener {

    private final HistorialService historialService;
    private final AlertasService alertasService;
    private final GestorUmbralesService gestorUmbralesService;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.COLA_TELEMETRIA)
    public void procesarTelemetria(DatosInvernaderoDTO t) {
        if (t == null || !t.esValida()) { log.warn("Telemetría descartada: {}", t); return; }

        messagingTemplate.convertAndSend("/topic/sensores", t);
        try {
            alertasService.evaluarDatosConUmbrales(t, gestorUmbralesService.obtenerUmbralesActivos())
                    .forEach(a -> messagingTemplate.convertAndSend("/topic/alertas", a));
        } catch (Exception e) {
            log.error("Error evaluando alertas", e);
        }
        historialService.registrarMuestra(t);
    }
}