package ru.practicum.interaction.api.feignClient.client.comment;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.dto.comment.CommentDto;
import ru.practicum.interaction.api.dto.comment.NewCommentDto;

@FeignClient(name = "comment-service", path = "/users/{userId}/comments")
public interface PrivateCommentClient {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto createComment(@PathVariable Long userId, @RequestParam(defaultValue = "0") Long eventId,
                             @RequestBody NewCommentDto newCommentDto) throws FeignException;

    @PatchMapping("/{commentId}")
    CommentDto updateComment(@PathVariable Long userId, @PathVariable Long commentId,
                                    @RequestParam(defaultValue = "0") Long eventId,
                                    @RequestBody NewCommentDto newCommentDto) throws FeignException;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long userId, @PathVariable Long commentId,
                              @RequestParam(defaultValue = "0") Long eventId) throws FeignException;

    @PutMapping("/{commentId}/like")
    CommentDto addLike(@PathVariable Long userId, @PathVariable Long commentId) throws FeignException;

    @DeleteMapping("/{commentId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLike(@PathVariable Long userId, @PathVariable Long commentId) throws FeignException;
}