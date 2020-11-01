package com.github.matjazmav.todo.common.kafka;

import com.fasterxml.jackson.databind.*;
import io.confluent.kafka.serializers.*;
import io.confluent.kafka.serializers.subject.*;
import lombok.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;

import java.util.*;

public class KafkaConfig {

    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

    // kafka-topics --bootstrap-server broker:9092 --create --partitions 5 --topic <topic name>
    public static final String TOPIC_CORE_CMD_ITEM_V1 = "core.cmd.item.v1";

    public static Map<String, Object> getProducerConfig(@NonNull String clientId) {
        return new HashMap<String, Object>(){{
            put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
            put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
            put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class); // allow multiple event types in a single topic
        }};
    }

    public static Map<String, Object> getConsumerConfig(@NonNull String groupId) {
        return new HashMap<String, Object>(){{
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
            put(KafkaAvroDeserializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class); // allow multiple event types in a single topic
        }};
    }

    public static Map<String, Object> getStreamsConfig(@NonNull String applicationId) {
        return new HashMap<String, Object>(){{
            put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.);
//            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerializer.class);
        }};
    }

    public static <T> KafkaProducer<String, T> getProducer(String clientId) {
        return new KafkaProducer<String, T>(getProducerConfig(clientId));
    }

    public static <T> KafkaConsumer<String, T> getConsumer(String groupId) {
        return new KafkaConsumer<String, T>(getConsumerConfig(groupId));
    }
}
