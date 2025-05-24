package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.LabelRequest;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelRequest;
import com.example.taskmanagementapp.dtos.label.response.LabelResponse;
import com.example.taskmanagementapp.entities.Label;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface LabelMapper {
    default Label toAddLabel(User user, LabelRequest addLabelDto, ColorDto colorDto) {
        Label label = new Label();
        label.setUser(user);
        label.setName(addLabelDto.name());
        label.setColor(Label.Color.valueOf(colorDto.name()));
        return label;
    }

    default Label toUpdateLabel(Label thisLabel,
                                UpdateLabelRequest updateLabelDto, ColorDto colorDto) {
        if (updateLabelDto.name() != null
                && !updateLabelDto.name().isBlank()) {
            thisLabel.setName(updateLabelDto.name());
        }
        if (colorDto != null) {
            thisLabel.setColor(Label.Color.valueOf(colorDto.name()));
        }
        return thisLabel;
    }

    default LabelResponse toLabelDto(Label label) {
        LabelResponse labelDto = new LabelResponse();
        labelDto.setId(label.getId());
        labelDto.setName(label.getName());
        labelDto.setColorDto(ColorDto.valueOf(label.getColor().name()));
        labelDto.setUserId(label.getUser().getId());
        labelDto.getTaskIds().addAll(toTaskIds(label));
        return labelDto;
    }

    default Set<Long> toTaskIds(Label label) {
        return label.getTasks().stream()
                .map(Task::getId)
                .collect(Collectors.toSet());
    }

    List<LabelResponse> toLabelDtoList(List<Label> labels);
}
