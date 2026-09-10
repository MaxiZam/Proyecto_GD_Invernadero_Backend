package gd.invernadero.backend.service;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.AlertaDTO;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class SimuladorSensores {

    private final RabbitTemplate rabbitTemplate;
    private final AlertasService alertasService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();

    public SimuladorSensores(RabbitTemplate rabbitTemplate, AlertasService alertasService, SimpMessagingTemplate messagingTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.alertasService = alertasService;
        this.messagingTemplate = messagingTemplate;
    }

    // Se ejecuta cada 3000 milisegundos (3 segundos)
    @Scheduled(fixedRate = 3000)
    public void simularEnvio() {
        // valores simulados de sensores
        Double temp = 20.0 + (random.nextDouble() * 10); // Entre 20 y 30
        Double humSuelo = 20.0 + (random.nextDouble() * 40);  // Entre 20 y 60
        Double humAire = 50.0 + (random.nextDouble() * 30);  // Entre 50 y 80
        Integer luz = 400 + random.nextInt(600);  // Entre 400 y 1000

        // Logica actuadores
        boolean caloventorActivo = temp < 22.0;
        boolean humidificadorActivo = humAire < 60.0;
        boolean ventanalesAbiertos = temp > 28.0 || humAire > 75.0;
        boolean persianasEnrolladas = luz <= 900;
        boolean bombaActiva = humSuelo < 30;

        DatosInvernaderoDTO nuevoDato = new DatosInvernaderoDTO(
                temp,
                humSuelo,
                humAire,
                luz,
                caloventorActivo,
                humidificadorActivo,
                ventanalesAbiertos,
                persianasEnrolladas,
                bombaActiva
        );

        // Alertas
        List<AlertaDTO> alertasGeneradas = alertasService.evaluarDatos(nuevoDato);

        if (!alertasGeneradas.isEmpty()) {
            enviarAlertas(alertasGeneradas);
        }

        // Envio de dato a la cola de RabbitMQ principal
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, nuevoDato);
        System.out.println("Simulador envió: " + nuevoDato);
    }

    // funcion de gestion de alertas
    private void enviarAlertas(List<AlertaDTO> alertas) {
        for (AlertaDTO alerta : alertas) {
            System.out.println("Enviando alerta a Vue: " + alerta.getMensaje());

            // Envio del objeto DTO al canal "/topic/alertas"
            messagingTemplate.convertAndSend("/topic/alertas", alerta);
        }
    }
}
