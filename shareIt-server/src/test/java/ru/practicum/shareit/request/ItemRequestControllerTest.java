package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    @Mock private ItemRequestService requestService;
    @InjectMocks private ItemRequestController controller;

    @Test
    void delegatesRequestOperationsToService() {
        ItemRequestDto dto = ItemRequestDto.builder().id(5L).description("Need drill").build();
        when(requestService.create(1L, dto)).thenReturn(dto);
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(dto));
        when(requestService.getOtherRequests(1L)).thenReturn(List.of(dto));
        when(requestService.getById(5L)).thenReturn(dto);

        assertThat(controller.create(1L, dto)).isEqualTo(dto);
        assertThat(controller.getOwn(1L)).containsExactly(dto);
        assertThat(controller.getOthers(1L)).containsExactly(dto);
        assertThat(controller.getById(5L)).isEqualTo(dto);

        verify(requestService).create(1L, dto);
        verify(requestService).getOwnRequests(1L);
        verify(requestService).getOtherRequests(1L);
        verify(requestService).getById(5L);
    }
}
