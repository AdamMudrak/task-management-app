package com.example.taskmanagementapp.config;

import com.example.taskmanagementapp.constants.config.ConfigConstants;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.NullValueCheckStrategy;

@org.mapstruct.MapperConfig(
        componentModel = ConfigConstants.COMPONENT_MODEL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public class MapperConfig {
}
