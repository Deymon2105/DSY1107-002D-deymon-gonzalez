package com.example.loggingsystem;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "logs_direct_exchange";
    public static final String ALL_LOGS_QUEUE = "all_logs_queue";
    public static final String ERRORS_ONLY_QUEUE = "errors_only_queue";

    // 1. Declarar el Exchange
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // 2. Declarar las Queues
    @Bean
    public Queue allLogsQueue() {
        return new Queue(ALL_LOGS_QUEUE, true); // durable
    }

    @Bean
    public Queue errorsOnlyQueue() {
        return new Queue(ERRORS_ONLY_QUEUE, true); // durable
    }

    // 3. Declarar los Bindings
    // Cada binding une una cola a un exchange con una routing key.

    @Bean
    public Binding bindAllLogsForInfo(DirectExchange exchange, Queue allLogsQueue) {
        return BindingBuilder.bind(allLogsQueue).to(exchange).with("INFO");
    }

    @Bean
    public Binding bindAllLogsForWarning(DirectExchange exchange, Queue allLogsQueue) {
        return BindingBuilder.bind(allLogsQueue).to(exchange).with("WARNING");
    }

    @Bean
    public Binding bindAllLogsForError(DirectExchange exchange, Queue allLogsQueue) {
        return BindingBuilder.bind(allLogsQueue).to(exchange).with("ERROR");
    }

    @Bean
    public Binding bindErrorsOnly(DirectExchange exchange, Queue errorsOnlyQueue) {
        return BindingBuilder.bind(errorsOnlyQueue).to(exchange).with("ERROR");

    }
}
