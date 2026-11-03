package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    Long id;
    @NotBlank
    String name; // имя или логин пользователя
    @NotBlank
    @Email
    String email; // адрес электронной почты (учтите, что два пользователя не могут иметь одинаковый адрес электронной почты)
}
