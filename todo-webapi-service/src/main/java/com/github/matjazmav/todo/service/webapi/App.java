package com.github.matjazmav.todo.service.webapi;

import com.github.matjazmav.todo.common.kafka.*;
import com.github.matjazmav.todo.common.kafka.avsc.item.*;
import io.confluent.kafka.streams.serdes.avro.*;
import lombok.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;

import java.util.*;

//import com.github.matjazmav.todo.common.kafka.*;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        KafkaConfig.ensureTopics(App.class.getName());
        SpringApplication.run(App.class, args);
    }

    @Bean
    public ProducerFactory<String, ItemMutationEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>((Map)KafkaConfig.getProducerConfig(this.getClass().getName()));
    }

    @Bean
    public KafkaTemplate<String, ItemMutationEvent> kafkaTemplate() {
        return new KafkaTemplate<String, ItemMutationEvent>(producerFactory());
    }


    @Bean
    public KafkaStreams getStreams() {
        val builder = new StreamsBuilder();

        val keySerde = Serdes.String();
        val valueSerde = new SpecificAvroSerde<ItemMutationEvent>();
        valueSerde.configure((Map)KafkaConfig.getSerdesConfig(), false);

        builder.table(KafkaConfig.TOPIC_CORE_CMD_ITEM_V1,
                Consumed.with(keySerde, valueSerde),
                Materialized.as(KafkaConfig.STORE_ITEMS));

        val streams = new KafkaStreams(builder.build(), KafkaConfig.getStreamsConfig("my-app")); // error in this line
        streams.start();

        return streams;
    }
}
