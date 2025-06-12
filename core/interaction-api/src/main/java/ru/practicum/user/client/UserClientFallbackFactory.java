package ru.practicum.user.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceTemporarilyUnavailable;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.dto.UsersDtoGetParam;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {

            @Override
            public ResponseEntity<List<UserDto>> getUsers(UsersDtoGetParam usersDtoGetParam) {
                if (cause instanceof FeignException e) {
                    if (e.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
                    }
                }
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public ResponseEntity<UserDto> createUser(UserDto userDto) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public ResponseEntity<String> deleteUser(Long userId) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public ResponseEntity<UserShortDto> getUser(Long userId) {
                if (cause instanceof FeignException e) {
                    if (e.status() == 404) {
                        throw new NotFoundException(e.getMessage());
                    }
                    if (e.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        return ResponseEntity.status(HttpStatus.OK).build();
                    }
                }
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }
        };
    }
}
