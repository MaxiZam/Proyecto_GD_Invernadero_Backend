package gd.invernadero.backend.listener;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import gd.invernadero.backend.service.AlertasService;
import gd.invernadero.backend.service.GestorUmbralesService;
import gd.invernadero.backend.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TelemetriaListener {

    private final HistorialService historialService;
    private final AlertasService alertasService;
    private final GestorUmbralesService gestorUmbralesService;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.COLA_TELEMETRIA)
    public void procesarTelemetria(DatosInvernaderoDTO telemetria) {
        // 1. Envío directo a WebSocket (TresJS y KPIs en tiempo real)
        messagingTemplate.convertAndSend("/topic/sensores", telemetria);

        // 2. Comprobación y despacho de alertas agronómicas
        List<AlertaDTO> alertas = alertasService.evaluarDatosConUmbrales(
                telemetria,
                gestorUmbralesService.obtenerUmbralesActivos()
        );

        for (AlertaDTO alerta : alertas) {
            messagingTemplate.convertAndSend("/topic/alertas", alerta);
        }

        // 3. Encolar la muestra para el cálculo consolidado del intervalo
        historialService.registrarMuestra(telemetria);
    }
}