package gd.invernadero.backend.controller;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.ComandoActuadorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/actuadores")
@RequiredArgsConstructor
public class ControlActuadoresController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/control")
    public ResponseEntity<String> enviarOrdenActuador(@RequestBody ComandoActuadorDTO comando) {
        log.info("Comando web despachado a la cola: Actuador={}, Auto={}, Manual={}",
                comando.getNombreActuador(), comando.isModoAutomatico(), comando.isEstadoManual());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INTERCAMBIO_INVERNADERO,
                RabbitMQConfig.RUTA_COMANDOS,
                comando
        );

        return ResponseEntity.ok("Orden transferida al simulador para " + comando.getNombreActuador());
    }
}
