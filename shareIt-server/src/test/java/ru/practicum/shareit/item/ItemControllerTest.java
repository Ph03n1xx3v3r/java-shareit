package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @Mock private ItemService itemService;
    @InjectMocks private ItemController controller;

    @Test
    void delegatesAllItemOperationsToService() {
        ItemDto item = ItemDto.builder().id(3L).name("Drill").build();
        ItemDto input = ItemDto.builder().name("Drill").build();
        CommentDto comment = CommentDto.builder().text("Useful").build();
        CommentDto savedComment = CommentDto.builder().id(8L).text("Useful").build();
        when(itemService.create(1L, input)).thenReturn(item);
        when(itemService.update(1L, 3L, input)).thenReturn(item);
        when(itemService.getById(3L)).thenReturn(item);
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(item));
        when(itemService.search("drill")).thenReturn(List.of(item));
        when(itemService.addComment(1L, 3L, comment)).thenReturn(savedComment);

        assertThat(controller.create(1L, input)).isEqualTo(item);
        assertThat(controller.update(1L, 3L, input)).isEqualTo(item);
        assertThat(controller.getById(3L)).isEqualTo(item);
        assertThat(controller.getAllByOwner(1L)).containsExactly(item);
        assertThat(controller.search("drill")).containsExactly(item);
        assertThat(controller.addComment(1L, 3L, comment)).isEqualTo(savedComment);

        verify(itemService).create(1L, input);
        verify(itemService).update(1L, 3L, input);
        verify(itemService).getById(3L);
        verify(itemService).getAllByOwner(1L);
        verify(itemService).search("drill");
        verify(itemService).addComment(1L, 3L, comment);
    }
}
