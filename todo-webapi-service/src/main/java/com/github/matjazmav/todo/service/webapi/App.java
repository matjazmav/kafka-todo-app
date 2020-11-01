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
        SpringApplication.run(App.class, args);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(KafkaConfig.getProducerConfig(this.getClass().getName()));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<String, Object>(producerFactory());
    }
}
