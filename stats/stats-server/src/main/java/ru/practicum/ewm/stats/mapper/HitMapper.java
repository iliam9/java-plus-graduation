package ru.practicum.ewm.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.stats.model.Hit;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring")
public interface HitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "timestamp", target = "time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Hit map(HitDto hitDto);
}
