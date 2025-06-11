package ru.practicum.interaction.api.feignClient.client.comment;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.dto.comment.CommentDto;
import ru.practicum.interaction.api.dto.user.UserDtoForAdmin;

@FeignClient(name = "comment-service", path = "/admin/comments")
public interface AdminCommentClient {

    @PutMapping("/ban/{userId}")
    UserDtoForAdmin addBanCommited(@PathVariable Long userId, @RequestParam(defaultValue = "0") Long eventId) throws FeignException;

    @DeleteMapping("/ban/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBanCommited(@PathVariable Long userId, @RequestParam(defaultValue = "0") Long eventId) throws FeignException;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long commentId, @RequestParam(defaultValue = "0") Long eventId) throws FeignException;

    @GetMapping
    CommentDto getComment(@RequestParam(defaultValue = "0") Long id) throws FeignException;
}