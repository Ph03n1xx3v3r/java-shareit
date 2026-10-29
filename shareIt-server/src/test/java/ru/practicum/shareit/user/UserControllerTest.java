package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock private UserService userService;
    @InjectMocks private UserController controller;

    @Test
    void delegatesUserOperationsToService() {
        UserDto dto = UserDto.builder().id(1L).name("Alex").email("alex@example.com").build();
        when(userService.create(dto)).thenReturn(dto);
        when(userService.update(1L, dto)).thenReturn(dto);
        when(userService.getById(1L)).thenReturn(dto);
        when(userService.getAll()).thenReturn(List.of(dto));

        assertThat(controller.create(dto)).isEqualTo(dto);
        assertThat(controller.update(1L, dto)).isEqualTo(dto);
        assertThat(controller.getById(1L)).isEqualTo(dto);
        assertThat(controller.getAll()).containsExactly(dto);
        controller.delete(1L);

        verify(userService).create(dto);
        verify(userService).update(1L, dto);
        verify(userService).getById(1L);
        verify(userService).getAll();
        verify(userService).delete(1L);
    }
}
