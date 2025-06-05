package ru.practicum.ewm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewUserRequest {

    @NotBlank
    @Size(min = 6, max = 254)
    String email;

    @NotBlank
    @Size(min = 2, max = 250)
    String name;
}
