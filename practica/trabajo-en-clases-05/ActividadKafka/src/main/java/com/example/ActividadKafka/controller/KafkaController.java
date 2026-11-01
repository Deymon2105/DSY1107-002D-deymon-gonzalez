package com.example.ActividadKafka.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que envía mensajes a Kafka
 * 
 * Demuestra dos conceptos:
 * -Sin Key: Round Robin (distribuye entre particiones 0, 1, 2...)
 * -Con Key: Sticky Partitioning (todos a la misma partición)
 */

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Endpoint para enviar mensajes a Kafka
     *
     * Ejemplos de uso:
     *
     * SIN KEY (Round Robin - distribuye entre particiones):
     * GET http://localhost:8888/api/kafka/send?message=Mensaje1
     *
     * CON KEY (Sticky Partitioning - misma partición):
     * GET http://localhost:8888/api/kafka/send?message=Mensaje1&key=orden-555
     */

    @RequestMapping(value = "/send", method = {RequestMethod.GET, RequestMethod.POST})
    public String sendMessage(@RequestParam String message, @RequestParam(required = false) String key) {
        // Enviamos al topic 'tutorial-particiones'
        // Si hay key, Kafka asegura el orden para esa key enviandola siempre a la misma
        // particion
        kafkaTemplate.send("tutorial-particiones", key, message);
        return "Mensaje enviado:" + message + "Key:" + key;
    }

    /**
     * Endpoint de verificación
     */
    @GetMapping("/health")
    public String health() {
        return "Aplicación de Kafka Tutorial está corriendo en puerto 8888";
    }

}
