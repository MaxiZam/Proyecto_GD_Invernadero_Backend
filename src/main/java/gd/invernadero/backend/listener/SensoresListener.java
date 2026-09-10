package gd.invernadero.backend.listener;

import gd.invernadero.backend.model.DatoTemporal;
import gd.invernadero.backend.service.HistorialService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SensoresListener {

    @Autowired
    private HistorialService historialService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "invernadero.datos")
    public void procesarMensaje(DatoTemporal dato) {
        System.out.println("Mensaje recibido en RabbitMQ -> Temp: "
                + dato.getTemperatura() + "°C, Hum: " + dato.getHumedadSuelo() + "%");

        historialService.registrarDato(dato);

        messagingTemplate.convertAndSend("/topic/sensores", dato);
    }
}
