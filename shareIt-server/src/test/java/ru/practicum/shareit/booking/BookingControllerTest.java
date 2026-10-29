package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @Mock private BookingService bookingService;
    @InjectMocks private BookingController controller;

    @Test
    void delegatesAllBookingOperationsToService() {
        NewBookingDto newBooking = new NewBookingDto();
        BookingDto booking = BookingDto.builder().id(4L).build();
        when(bookingService.create(1L, newBooking)).thenReturn(booking);
        when(bookingService.approve(2L, 4L, true)).thenReturn(booking);
        when(bookingService.getById(1L, 4L)).thenReturn(booking);
        when(bookingService.getByBooker(1L, "ALL")).thenReturn(List.of(booking));
        when(bookingService.getByOwner(2L, "WAITING")).thenReturn(List.of(booking));

        assertThat(controller.create(1L, newBooking)).isEqualTo(booking);
        assertThat(controller.approve(2L, 4L, true)).isEqualTo(booking);
        assertThat(controller.getById(1L, 4L)).isEqualTo(booking);
        assertThat(controller.getByBooker(1L, "ALL")).containsExactly(booking);
        assertThat(controller.getByOwner(2L, "WAITING")).containsExactly(booking);

        verify(bookingService).create(1L, newBooking);
        verify(bookingService).approve(2L, 4L, true);
        verify(bookingService).getById(1L, 4L);
        verify(bookingService).getByBooker(1L, "ALL");
        verify(bookingService).getByOwner(2L, "WAITING");
    }
}
