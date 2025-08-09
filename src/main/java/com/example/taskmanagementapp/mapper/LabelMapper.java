package com.example.taskmanagementapp.mapper;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dto.comment.request.ColorDto;
import com.example.taskmanagementapp.dto.label.request.LabelRequest;
import com.example.taskmanagementapp.dto.label.response.LabelResponse;
import com.example.taskmanagementapp.entity.Label;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface LabelMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "name", source = "addLabelDto.name")
    @Mapping(target = "color", expression = "java(com.example.taskmanagementapp.entity."
            + "Label.Color.valueOf(colorDto.name()))")
    Label toAddLabel(User user, LabelRequest addLabelDto, ColorDto colorDto);

    @Mapping(target = "colorDto", expression =
            "java(com.example.taskmanagementapp.dto.comment.request."
            + "ColorDto.valueOf(label.getColor().name()))")
    @Mapping(target = "userId", source = "user.id")
    LabelResponse toLabelDto(Label label);

    @AfterMapping
    default void setTaskIds(@MappingTarget LabelResponse labelResponse, Label label) {
        labelResponse.getTaskIds().addAll(label.getTasks().stream()
                .map(Task::getId)
                .collect(Collectors.toSet()));
    }

    List<LabelResponse> toLabelDtoList(List<Label> labels);
}
