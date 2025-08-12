package com.example.taskmanagementapp.dto.label.response;

import com.example.taskmanagementapp.dto.comment.request.ColorDto;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelResponse {
    private Long id;
    private String name;
    private ColorDto colorDto;
    private Long userId;
    private Set<Long> taskIds = new HashSet<>();
}

