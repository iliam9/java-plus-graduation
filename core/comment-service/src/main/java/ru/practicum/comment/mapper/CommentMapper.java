package ru.practicum.comment.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Mapper
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "eventId", source = "comment.event")
    @Mapping(target = "parentCommentId", source = "comment.parentComment.id")
    @Mapping(target = "user", source = "userShortDto")
    CommentDto toDto(Comment comment, UserShortDto userShortDto);

    @Mapping(target = "eventId", source = "comment.event")
    @Mapping(target = "parentCommentId", source = "comment.parentComment.id")
    @Mapping(target = "user", ignore = true)
    CommentDto toDto(Comment comment);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    List<CommentDto> toDtos(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", source = "eventId")
    Comment toEntity(CommentDto commentDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "parentComment.id", source = "parentCommentId")
    void updateDto(CommentDto commentDto, @MappingTarget Comment comment);
}