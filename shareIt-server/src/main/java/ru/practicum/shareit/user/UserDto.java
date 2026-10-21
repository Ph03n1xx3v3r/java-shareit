package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    public interface Create {
    }

    public interface Update {
    }

    private Long id;

    @NotBlank(message = "Имя не может быть пустым", groups = Create.class)
    @Pattern(regexp = "^(?=.*\\S).+$", message = "Имя не может быть пустым", groups = Update.class)
    @Size(max = 255, message = "Имя не должно превышать 255 символов", groups = {Create.class, Update.class})
    private String name;

    @NotBlank(message = "Email не может быть пустым", groups = Create.class)
    @Pattern(regexp = "^(?=.*\\S).+$", message = "Email не может быть пустым", groups = Update.class)
    @Email(message = "Некорректный формат email", groups = {Create.class, Update.class})
    @Size(max = 512, message = "Email не должен превышать 512 символов", groups = {Create.class, Update.class})
    private String email;
}
