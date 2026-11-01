package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStateTest {
    @Test
    void parsesEveryStateAndIgnoresCase() {
        for (BookingState state : BookingState.values()) {
            assertThat(BookingState.from(state.name())).isEqualTo(state);
            assertThat(BookingState.from(state.name().toLowerCase())).isEqualTo(state);
        }
    }

    @Test
    void rejectsUnknownAndNullState() {
        assertThatThrownBy(() -> BookingState.from("unknown")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> BookingState.from(null)).isInstanceOf(ValidationException.class);
    }
}
