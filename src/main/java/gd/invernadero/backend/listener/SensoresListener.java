package gd.invernadero.backend.listener;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import gd.invernadero.backend.model.DatoHistorico;
import gd.invernadero.backend.repository.DatoHistoricoRepository;
import gd.invernadero.backend.service.AlertasService;
import gd.invernadero.backend.service.GestorUmbralesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensoresListener {

    private final DatoHistoricoRepository datoHistoricoRepository;
    private final AlertasService alertasService;
    private final GestorUmbralesService gestorUmbrales;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.COLA_TELEMETRIA)
    public void procesarTelemetriaSimulador(DatosInvernaderoDTO datos) {
        log.info("Telemetría entrante de RabbitMQ -> Temp: {}°C, Hum: {}%",
                datos.getTemperatura(), datos.getHumedadAire());

        // 1. Difusión en tiempo real hacia Vue.js (TresJS y KPIs)
        messagingTemplate.convertAndSend("/topic/sensores", datos);

        // 2. Evaluación de alertas basada en los umbrales activos
        List<AlertaDTO> alertas = alertasService.evaluarDatosConUmbrales(
                datos,
                gestorUmbrales.obtenerUmbralesActivos()
        );
        alertas.forEach(alerta -> messagingTemplate.convertAndSend("/topic/alertas", alerta));

        // 3. Persistencia en PostgreSQL
        DatoHistorico historico = new DatoHistorico();
        historico.setFecha(LocalDateTime.now());
        historico.setTempPromedio(datos.getTemperatura());
        historico.setHumSueloPromedio(datos.getHumedadSuelo());
        historico.setHumAirePromedio(datos.getHumedadAire());
        historico.setLumPromedio(datos.getLuminosidad());
        historico.setCaloventorUsoPorcentaje(datos.isCaloventorActivo());
        historico.setHumidificadorActivo(datos.isHumidificadorActivo());
        historico.setVentanalesAbiertos(datos.isVentanalesAbiertos());
        historico.setPersianasEnrolladas(datos.isPersianasEnrolladas());
        historico.setBombaActiva(datos.isBombaActiva());

        datoHistoricoRepository.save(historico);
    }
}
