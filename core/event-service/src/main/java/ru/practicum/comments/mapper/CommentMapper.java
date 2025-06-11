package ru.practicum.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.dto.events.CommentDto;
import ru.practicum.comments.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "eventId", source = "eventId")
    Comment toComment(NewCommentDto newCommentDto, Long authorId, Long eventId);

    @Mapping(target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventId", expression = "java(comment.getEventId())")
    @Mapping(target = "authorId", expression = "java(comment.getAuthorId())")
    @Mapping(target = "status", expression = "java(comment.getStatus().name())")
    CommentDto toDto(Comment comment);

}
