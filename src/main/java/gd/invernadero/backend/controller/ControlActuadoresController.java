package gd.invernadero.backend.controller;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.ComandoActuadorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RestController
@RequestMapping("/api/actuadores")
@RequiredArgsConstructor
public class ControlActuadoresController {

    private final RabbitTemplate rabbitTemplate;

    private static final Set<String> VALIDOS = Set.of("caloventor","humidificador","ventanales","persianas","bomba");
    private final ConcurrentMap<String, ComandoActuadorDTO> estados = new ConcurrentHashMap<>();

    @PostMapping("/control")
    public ResponseEntity<String> enviar(@RequestBody ComandoActuadorDTO c) {
        String nombre = c.getNombreActuador() == null ? "" : c.getNombreActuador().trim().toLowerCase();
        if (!VALIDOS.contains(nombre))
            return ResponseEntity.badRequest().body("Actuador desconocido: " + c.getNombreActuador());
        c.setNombreActuador(nombre);
        estados.put(nombre, c);
        rabbitTemplate.convertAndSend(RabbitMQConfig.INTERCAMBIO_INVERNADERO, RabbitMQConfig.RUTA_COMANDOS, c);
        return ResponseEntity.ok("Orden transferida a " + nombre);
    }

    @GetMapping("/estado")
    public Collection<ComandoActuadorDTO> estado() { return estados.values(); }
}
