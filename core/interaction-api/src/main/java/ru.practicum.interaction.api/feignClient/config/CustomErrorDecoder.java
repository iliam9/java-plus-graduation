package ru.practicum.interaction.api.feignClient.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.api.exception.BadRequestException;
import ru.practicum.interaction.api.exception.NotFoundException;
import ru.practicum.interaction.api.exception.ServerErrorException;

@Component
public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();

    final int BAD_REQUEST = 400;
    final int NOT_FOUND = 404;
    final int SERVER_ERROR = 500;

    @Override
    public Exception decode(String s, Response response) {

        if (response.status() == BAD_REQUEST) {
            throw new BadRequestException("Bad request");
        } else if (response.status() == NOT_FOUND) {
            throw new NotFoundException("Not Found");
        } else if (response.status() == SERVER_ERROR) {
            throw new ServerErrorException("Internal server error");
        } else {
            return errorDecoder.decode(s, response);
        }
    }
}
