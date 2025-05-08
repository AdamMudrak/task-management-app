package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.comment.request.ColorDto;
import com.example.taskmanagementapp.dtos.label.request.AddLabelDto;
import com.example.taskmanagementapp.dtos.label.request.UpdateLabelDto;
import com.example.taskmanagementapp.dtos.label.response.LabelDto;
import com.example.taskmanagementapp.entities.Label;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface LabelMapper {
    default Label toAddLabel(User user, AddLabelDto addLabelDto, ColorDto colorDto) {
        Label label = new Label();
        label.setUser(user);
        label.setName(addLabelDto.name());
        label.setColor(Label.Color.valueOf(colorDto.name()));
        return label;
    }

    default Label toUpdateLabel(Label thisLabel, UpdateLabelDto updateLabelDto, ColorDto colorDto) {
        if (updateLabelDto.name() != null
                && !updateLabelDto.name().isBlank()) {
            thisLabel.setName(updateLabelDto.name());
        }
        if (colorDto != null) {
            thisLabel.setColor(Label.Color.valueOf(colorDto.name()));
        }
        return thisLabel;
    }

    default LabelDto toLabelDto(Label label) {
        LabelDto labelDto = new LabelDto();
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

    List<LabelDto> toLabelDtoList(List<Label> labels);
}
