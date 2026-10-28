package gd.invernadero.backend.service;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestorUmbralesService {

    private final RabbitTemplate rabbitTemplate;

    private volatile ConfiguracionUmbralesDTO umbralesActivos =
            new ConfiguracionUmbralesDTO(18.0, 28.0, 50.0, 80.0, 50.0, 75.0, 400, 900);

    public ConfiguracionUmbralesDTO obtenerUmbralesActivos() {
        return umbralesActivos;
    }

    public void actualizarUmbrales(ConfiguracionUmbralesDTO nuevosUmbrales) {
        this.umbralesActivos = nuevosUmbrales;
        log.info("Nuevos umbrales activos: {}", nuevosUmbrales);
        publicar();
    }

    /** El mensaje es idempotente: si el simulador se reinició, se autocorrige en <= 30 s. */
    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void resincronizar() {
        publicar();
    }

    private void publicar() {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INTERCAMBIO_INVERNADERO, RabbitMQConfig.RUTA_UMBRALES, umbralesActivos);
        } catch (AmqpException e) {
            log.warn("No se pudieron publicar los umbrales (se reintentará): {}", e.getMessage());
        }
    }
}