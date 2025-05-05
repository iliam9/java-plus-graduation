package ru.practicum.ewm.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentRequest;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface CommentMapper {

    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "authorName", source = "comment.author.name")
    CommentDto mapToCommentDto(Comment comment);

    List<CommentDto> mapToCommentDto(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    Comment mapToComment(NewCommentRequest request, User author, Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "event", ignore = true)
    void update(@MappingTarget Comment comment, NewCommentRequest updateComment);

}
