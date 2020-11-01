package com.github.matjazmav.todo.service.webapi.entities;

import lombok.*;

import java.time.*;

@Value
@AllArgsConstructor(staticName = "of")
public class TodoItem {
    @NonNull
    private String id;
    private String description;
    private LocalDateTime notifyOn;
    private boolean isChecked;
}
