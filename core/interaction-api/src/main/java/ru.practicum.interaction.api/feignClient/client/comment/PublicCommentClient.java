package ru.practicum.interaction.api.feignClient.client.comment;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.dto.comment.CommentDto;
import ru.practicum.interaction.api.enums.comment.SortType;

import java.util.List;

@FeignClient(name = "comment-service", path = "/comments")
public interface PublicCommentClient {

    @GetMapping("/{eventId}")
    List<CommentDto> getAllCommentsByEventId(@PathVariable Long eventId,
                                             @RequestParam(defaultValue = "LIKES") SortType sort,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "20") Integer size) throws FeignException;
}