package com.github.matjazmav.todo.service.webapi.controllers;

import com.github.matjazmav.todo.common.kafka.*;
import com.github.matjazmav.todo.common.kafka.avsc.item.*;
import com.github.matjazmav.todo.service.webapi.entities.*;
import lombok.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.*;
import org.springframework.util.concurrent.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

@RestController
@RequestMapping("/todo")
public class TodoController {

    private static Logger logger = LoggerFactory.getLogger(TodoController.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoItem>>> list()
    {
        return ApiResponse.ok(Arrays.asList(
                TodoItem.of("id0", "Wash dishes", null, false),
                TodoItem.of("id1", "Study", null, false),
                TodoItem.of("id2", "Work on this app", LocalDateTime.now().plusDays(5), false),
                TodoItem.of("id3", "Go for run", null, true),
                TodoItem.of("id4", "Call home", null, true),
                TodoItem.of("id5", null, null, false),
                TodoItem.of("id6", "...", null, false)
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> add(@RequestBody String description)
    {
        val id = UUID.randomUUID().toString();
        sendItemEvent(id, new ItemAddedEvent(id, description));
        return ApiResponse.ok(id);
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<ApiResponse> edit(
            @PathVariable String id,
            @RequestBody String description)
    {
        sendItemEvent(id, new ItemEditedEvent(id, description));
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<ApiResponse> notify(
            @PathVariable String id,
            @RequestBody LocalDateTime notifyOn)
    {
        sendItemEvent(id, new ItemNotificationSetEvent(id, notifyOn.format(DateTimeFormatter.ISO_DATE_TIME)));
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<ApiResponse> check(@PathVariable String id)
    {
        sendItemEvent(id, new ItemCheckedEvent(id));
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/uncheck")
    public ResponseEntity<ApiResponse> uncheck(@PathVariable String id)
    {
        sendItemEvent(id, new ItemUncheckedEvent(id));
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/remove")
    public ResponseEntity<ApiResponse> remove(@PathVariable String id)
    {
        sendItemEvent(id, new ItemRemovedEvent(id));
        return ApiResponse.ok();
    }

    @SneakyThrows
    private void sendItemEvent(String key, Object value)
    {
        val promise = kafkaTemplate.send(KafkaConfig.TOPIC_CORE_CMD_ITEM_V1, key, value);
        promise.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Event failed with: ", ex);
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                val eventName = result.getProducerRecord().value().getClass().getSimpleName();
                val eventKey = result.getProducerRecord().key();
                logger.info("Event '" + eventName + "' with key '" + eventKey + "'...");
            }
        });
        kafkaTemplate.flush();
    }
}
