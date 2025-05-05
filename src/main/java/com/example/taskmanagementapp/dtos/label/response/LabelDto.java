package com.example.taskmanagementapp.dtos.label.response;

import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelDto {
    private Long id;
    private String name;
    private ColorDto colorDto;
    private Long userId;
    private Set<Long> taskIds = new HashSet<>();
}

