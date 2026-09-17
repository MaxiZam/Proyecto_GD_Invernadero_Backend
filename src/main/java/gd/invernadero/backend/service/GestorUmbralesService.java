package gd.invernadero.backend.service;

import gd.invernadero.backend.config.RabbitMQConfig;
import gd.invernadero.backend.dto.ConfiguracionUmbralesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
        log.info("Sincronizando nuevos umbrales hacia RabbitMQ: {}", nuevosUmbrales);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.INTERCAMBIO_INVERNADERO,
                RabbitMQConfig.RUTA_UMBRALES,
                nuevosUmbrales
        );
    }
}