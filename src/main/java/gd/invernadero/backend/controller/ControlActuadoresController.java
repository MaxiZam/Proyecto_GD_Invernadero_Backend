package gd.invernadero.backend.controller;

import gd.invernadero.backend.dto.ComandoActuadorDTO;
import gd.invernadero.backend.service.EstadoActuadoresService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/actuadores")
@RequiredArgsConstructor
public class ControlActuadoresController {

    private final EstadoActuadoresService servicio;

    @PostMapping("/control")
    public ResponseEntity<String> enviar(@RequestBody ComandoActuadorDTO c) {
        String nombre = servicio.normalizar(c.getNombreActuador());
        if (!servicio.esValido(nombre))
            return ResponseEntity.badRequest().body("Actuador desconocido: " + c.getNombreActuador());
        c.setNombreActuador(nombre);
        try {
            servicio.enviar(c);
        } catch (AmqpException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Broker no disponible: la orden quedó registrada y se reenviará automáticamente.");
        }
        return ResponseEntity.ok("Orden transferida a " + nombre);
    }

    @GetMapping("/estado")
    public Collection<ComandoActuadorDTO> estado() {
        return servicio.estados();
    }
}