package gd.invernadero.backend.service;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.ComandoActuadorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstadoActuadoresService {

    private static final Set<String> VALIDOS =
            Set.of("caloventor", "humidificador", "ventanales", "persianas", "bomba");

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentMap<String, ComandoActuadorDTO> estados = new ConcurrentHashMap<>();

    public String normalizar(String nombre) {
        return nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
    }

    public boolean esValido(String nombreNormalizado) {
        return VALIDOS.contains(nombreNormalizado);
    }

    /** Registra el estado deseado y lo publica. Si el broker falla, queda registrado y se reenvía solo. */
    public void enviar(ComandoActuadorDTO comando) {
        estados.put(comando.getNombreActuador(), comando);
        publicar(comando);
    }

    public Collection<ComandoActuadorDTO> estados() {
        return estados.values();
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void resincronizar() {
        try {
            estados.values().forEach(this::publicar);
        } catch (AmqpException e) {
            log.warn("No se pudieron re-publicar los comandos (se reintentará): {}", e.getMessage());
        }
    }

    private void publicar(ComandoActuadorDTO c) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.INTERCAMBIO_INVERNADERO, RabbitMQConfig.RUTA_COMANDOS, c);
    }
}
