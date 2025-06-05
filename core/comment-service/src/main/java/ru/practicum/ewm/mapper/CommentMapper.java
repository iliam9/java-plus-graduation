package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentRequest;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.model.Comment;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface CommentMapper {

    @Mapping(target = "eventId", source = "comment.eventId")
    CommentDto mapToCommentDto(Comment comment);

    List<CommentDto> mapToCommentDto(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    Comment mapToComment(NewCommentRequest request, UserShortDto author, Long eventId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    void update(@MappingTarget Comment comment, NewCommentRequest updateComment);

}
