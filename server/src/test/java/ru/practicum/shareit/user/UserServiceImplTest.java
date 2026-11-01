package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void createUser() {
        UserDto dto = UserDto.builder().name("Alex").email("alex@example.com").build();
        when(repository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        assertThat(service.create(dto).getId()).isEqualTo(1L);
        verify(repository).save(any(User.class));
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("alex@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.create(UserDto.builder().name("Alex")
                .email("alex@example.com").build())).isInstanceOf(ConflictException.class);
    }

    @Test
    void updateNameAndEmail() {
        User user = User.builder().id(2L).name("Old").email("old@example.com").build();
        when(repository.findById(2L)).thenReturn(Optional.of(user));
        when(repository.existsByEmailIgnoreCaseAndIdNot("new@example.com", 2L)).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDto result = service.update(2L, UserDto.builder().name("New")
                .email("new@example.com").build());
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateWithoutFieldsLeavesUserUnchanged() {
        User user = User.builder().id(2L).name("Alex").email("a@example.com").build();
        when(repository.findById(2L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.update(2L, new UserDto()).getName()).isEqualTo("Alex");
    }

    @Test
    void updateRejectsDuplicateEmail() {
        when(repository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).name("Alex")
                .email("a@example.com").build()));
        when(repository.existsByEmailIgnoreCaseAndIdNot("b@example.com", 2L)).thenReturn(true);
        assertThatThrownBy(() -> service.update(2L, UserDto.builder().email("b@example.com").build()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getUserAndDtoById() {
        User user = User.builder().id(1L).name("Alex").email("a@example.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        assertThat(service.getUser(1L)).isEqualTo(user);
        assertThat(service.getById(1L).getEmail()).isEqualTo("a@example.com");
    }

    @Test
    void missingUserThrows() {
        when(repository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(404L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllAndDelete() {
        when(repository.findAll()).thenReturn(List.of(User.builder().id(1L).name("Alex")
                .email("a@example.com").build()));
        assertThat(service.getAll()).hasSize(1);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
