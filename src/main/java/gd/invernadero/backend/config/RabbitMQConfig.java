package gd.invernadero.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Identificadores de intercambio y enrutamiento
    public static final String INTERCAMBIO_INVERNADERO = "invernadero.exchange";

    public static final String COLA_TELEMETRIA = "invernadero.telemetria.cola";
    public static final String RUTA_TELEMETRIA = "invernadero.telemetria.key";

    public static final String COLA_COMANDOS = "invernadero.comandos.cola";
    public static final String RUTA_COMANDOS = "invernadero.comandos.key";

    public static final String COLA_UMBRALES = "invernadero.umbrales.cola";
    public static final String RUTA_UMBRALES = "invernadero.umbrales.key";

    // 1. Declaración de Exchange tipo Topic
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(INTERCAMBIO_INVERNADERO);
    }

    // 2. Declaración de Colas duraderas
    @Bean
    public Queue colaTelemetria() {
        return QueueBuilder.nonDurable(COLA_TELEMETRIA).ttl(10_000).maxLength(100).build();
    }

    @Bean
    public Queue colaComandos() {
        return QueueBuilder.durable(COLA_COMANDOS).build();
    }

    @Bean
    public Queue colaUmbrales() {
        return QueueBuilder.durable(COLA_UMBRALES).build();
    }

    // 3. Enlaces (Bindings)
    @Bean
    public Binding bindingTelemetria(Queue colaTelemetria, TopicExchange exchange) {
        return BindingBuilder.bind(colaTelemetria).to(exchange).with(RUTA_TELEMETRIA);
    }

    @Bean
    public Binding bindingComandos(Queue colaComandos, TopicExchange exchange) {
        return BindingBuilder.bind(colaComandos).to(exchange).with(RUTA_COMANDOS);
    }

    @Bean
    public Binding bindingUmbrales(Queue colaUmbrales, TopicExchange exchange) {
        return BindingBuilder.bind(colaUmbrales).to(exchange).with(RUTA_UMBRALES);
    }

    @Bean
    public MessageConverter conversorJson() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter conversorJson) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(conversorJson);
        return template;
    }
}