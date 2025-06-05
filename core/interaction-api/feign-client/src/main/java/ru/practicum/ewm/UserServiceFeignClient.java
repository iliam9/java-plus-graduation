package ru.practicum.ewm;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "admin/users")
public interface UserServiceFeignClient {

    @GetMapping("/short")
    public List<UserShortDto> findShortUsers(@RequestParam List<Long> ids) throws FeignException;
}
