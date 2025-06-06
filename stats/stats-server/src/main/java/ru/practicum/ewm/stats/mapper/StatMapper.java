package ru.practicum.ewm.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.dto.ParamDto;
import ru.practicum.ewm.dto.StatDto;
import ru.practicum.ewm.stats.model.ParamStat;
import ru.practicum.ewm.stats.model.Stat;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring")
public interface StatMapper {
    StatDto map(Stat entity);

    @Mapping(source = "start", target = "start", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "end", target = "end", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ParamStat map(ParamDto paramDto);
}
