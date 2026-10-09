package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewBookingDto {
    @NotNull(message = "Необходимо указать идентификатор вещи")
    private Long itemId;

    @NotNull(message = "Необходимо указать дату начала")
    private LocalDateTime start;

    @NotNull(message = "Необходимо указать дату окончания")
    private LocalDateTime end;
}
