package ru.practicum.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@AllArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {

    /*@GrpcClient("collector")
    private final UserActionControllerGrpc.UserActionControllerBlockingStub client;*/

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        /*(new HitDto("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now()));*/
        return true;
    }
}