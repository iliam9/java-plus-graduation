package ru.practicum.comments.dto;

import lombok.Data;
import ru.practicum.comments.enums.AdminUpdateCommentStatusAction;

@Data
public class AdminUpdateCommentStatusDto {
    private AdminUpdateCommentStatusAction action;
}
