package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.EventSimilarity;
import ru.yandex.practicum.model.UserAction;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.yandex.practicum.dto.RecommendedEvent;

@Mapper(componentModel = "spring")
public interface AnalyzerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weight", expression = "java(WeightMapper.mapToWeight(avro.getActionType()))")
    UserAction map(UserActionAvro avro);

    EventSimilarity map(EventSimilarityAvro eventSimilarityAvro);

    RecommendedEventProto map(RecommendedEvent eventRating);
}
