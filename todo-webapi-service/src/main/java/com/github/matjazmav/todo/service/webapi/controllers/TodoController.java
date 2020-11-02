package com.github.matjazmav.todo.service.webapi.controllers;

import com.github.matjazmav.todo.common.kafka.*;
import com.github.matjazmav.todo.common.kafka.avsc.item.*;
import com.github.matjazmav.todo.service.webapi.entities.*;
import lombok.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.*;
import org.apache.kafka.streams.state.internals.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.*;
import org.springframework.util.concurrent.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@RestController
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private static Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final KafkaTemplate<String, ItemMutationEvent> kafkaTemplate;
    private final KafkaStreams streams;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoItem>>> list()
    {
        Iterable<KeyValue<String, ItemMutationEvent>> x = () -> getItemsStore().all();

        var list = StreamSupport
            .stream(x.spliterator(), true)
            .map(kv -> kv.value)
                .map(v -> TodoItem.of(v.getId().toString(), v.getDescription().toString(), null, false))
            .collect(Collectors.toList());

        return ApiResponse.ok(list);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> add(@RequestBody String description)
    {
        val id = UUID.randomUUID().toString();
        sendItemEvent(id, new ItemMutationEvent(){{
            setId(id);
            setDescription(description);
        }});
        return ApiResponse.ok(id);
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<ApiResponse> edit(
            @PathVariable String id,
            @RequestBody String description)
    {
        sendItemEvent(id, new ItemMutationEvent(){{
            setId(id);
            setDescription(description);
        }});
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<ApiResponse> notify(
            @PathVariable String id,
            @RequestBody LocalDateTime notifyOn)
    {
        sendItemEvent(id, new ItemMutationEvent(){{
            setId(id);
            setNotifyOn(notifyOn.format(DateTimeFormatter.ISO_DATE_TIME));
        }});
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<ApiResponse> check(@PathVariable String id)
    {
        sendItemEvent(id, new ItemMutationEvent(){{
            setId(id);
            setIsChecked(true);
        }});
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/uncheck")
    public ResponseEntity<ApiResponse> uncheck(@PathVariable String id)
    {
        sendItemEvent(id, new ItemMutationEvent(){{
            setId(id);
            setIsChecked(false);
        }});
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/remove")
    public ResponseEntity<ApiResponse> remove(@PathVariable String id)
    {
        sendItemEvent(id, new ItemMutationEvent(){{
            setId(id);
            setIsDeleted(true);
        }});
        return ApiResponse.ok();
    }

    @SneakyThrows
    private void sendItemEvent(String key, ItemMutationEvent value)
    {
        val promise = kafkaTemplate.send(KafkaConfig.TOPIC_CORE_CMD_ITEM_V1, key, value);
        promise.addCallback(new ListenableFutureCallback<SendResult<String, ItemMutationEvent>>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Event failed with: ", ex);
            }

            @Override
            public void onSuccess(SendResult<String, ItemMutationEvent> result) {
                val eventName = result.getProducerRecord().value().getClass().getSimpleName();
                val eventKey = result.getProducerRecord().key();
                logger.info("Event '" + eventName + "' with key '" + eventKey + "'...");
            }
        });
        kafkaTemplate.flush();
    }

    private ReadOnlyKeyValueStore<String, ItemMutationEvent> getItemsStore() {
        return streams
                .store(StoreQueryParameters.fromNameAndType(KafkaConfig.STORE_ITEMS, QueryableStoreTypes.keyValueStore()));
    }
}
