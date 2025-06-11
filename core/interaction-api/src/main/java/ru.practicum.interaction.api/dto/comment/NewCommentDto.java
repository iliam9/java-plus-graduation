package ru.practicum.interaction.api.dto.comment;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NewCommentDto {
    @NotBlank
    @Size(min = 3, max = 512)
    String text;
}
