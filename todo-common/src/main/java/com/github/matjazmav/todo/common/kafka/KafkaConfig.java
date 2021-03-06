package com.github.matjazmav.todo.common.kafka;

import com.fasterxml.jackson.databind.*;
import com.github.matjazmav.todo.common.kafka.avsc.item.*;
import io.confluent.kafka.serializers.*;
import io.confluent.kafka.serializers.subject.*;
import io.confluent.kafka.streams.serdes.avro.*;
import lombok.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.*;
import org.apache.kafka.common.metrics.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.*;
import java.util.stream.*;

public class KafkaConfig {

    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

    public static final String TOPIC_CORE_CMD_ITEM_V1 = "coreCmdItemV1";

    public static final String STORE_ITEMS = "items";

    public static Properties getAdminClientConfig(@NonNull String clientId) {
        return new Properties(){{
            put(AdminClientConfig.CLIENT_ID_CONFIG, clientId);
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        }};
    }

    public static Properties getProducerConfig(@NonNull String clientId) {
        return new Properties(){{
            put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

            // Safe producer
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
            put(ProducerConfig.ACKS_CONFIG, "all");
            put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

            put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
            put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, io.confluent.kafka.serializers.subject.TopicNameStrategy.class);
        }};
    }

    public static Properties getConsumerConfig(@NonNull String groupId) {
        return new Properties(){{
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        }};
    }

    public static Properties getStreamsConfig(@NonNull String applicationId) {
        return new Properties(){{
            put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        }};
    }

    public static Properties getSerdesConfig() {
        return new Properties(){{
            put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        }};
    }

    public static <T> KafkaProducer<String, T> getProducer(String clientId) {
        return new KafkaProducer<String, T>(getProducerConfig(clientId));
    }

    public static <T> KafkaConsumer<String, T> getConsumer(String groupId) {
        return new KafkaConsumer<String, T>(getConsumerConfig(groupId));
    }

    @SneakyThrows
    public static void ensureTopics(String clientId) {
        val adminClient = AdminClient.create(getAdminClientConfig(clientId));
        val existingTopics = adminClient.listTopics().names().get();
        val requiredTopics = Arrays.asList(
                new NewTopic(TOPIC_CORE_CMD_ITEM_V1, 1, (short)1).configs(new HashMap<String, String>() {{
                    put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
                    put(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip");
                    put(TopicConfig.RETENTION_MS_CONFIG, "-1");
                }})
        );
        val result = adminClient.createTopics(requiredTopics.stream()
                .filter(x -> !existingTopics.contains(x.name()))
                .collect(Collectors.toList()));
        result.all().get();
        adminClient.close();
    }


}
