package com.github.matjazmav.todo.service.webapi;

import com.github.matjazmav.todo.common.kafka.*;
import com.github.matjazmav.todo.common.kafka.avsc.item.*;
import org.apache.kafka.clients.producer.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;

//import com.github.matjazmav.todo.common.kafka.*;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        KafkaConfig.ensureTopics(App.class.getName());
        SpringApplication.run(App.class, args);
    }

    @Bean
    public ProducerFactory<String, ItemMutationEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(KafkaConfig.getProducerConfig(this.getClass().getName()));
    }

    @Bean
    public KafkaTemplate<String, ItemMutationEvent> kafkaTemplate() {
        return new KafkaTemplate<String, ItemMutationEvent>(producerFactory());
    }
}
