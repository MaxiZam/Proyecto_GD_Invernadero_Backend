package gd.invernadero.backend.listener;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.DatosInvernaderoDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class InvernaderoListener {

    private final SimpMessagingTemplate messagingTemplate;

    public InvernaderoListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void recibirDatosDeSensores(DatosInvernaderoDTO datos) {
        System.out.println("Dato recibido de RabbitMQ: " + datos);

        // Envía el dato por WebSocket a todos los clientes conectados
        messagingTemplate.convertAndSend("/topic/sensores", datos);
    }
}