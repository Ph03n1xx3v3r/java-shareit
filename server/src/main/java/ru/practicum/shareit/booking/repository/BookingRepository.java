package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where b.user.id = :userId order by b.start desc")
    List<Booking> findAllBookingByUserId(@Param("userId") Long userId);

    @Query("select b from Booking b where b.user.id = :userId and b.status = :status and" +
            " (b.start<= :now and b.end>= :now) order by b.start desc")
    List<Booking> findCurrentBookingByUserId(@Param("userId") Long userId,
                                             @Param("now") LocalDateTime now,
                                             @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.user.id = :userId and b.status = :status and" +
            " b.end<= :now order by b.start desc")
    List<Booking> findPastBookingByUserId(@Param("userId") Long userId,
                                          @Param("now") LocalDateTime now,
                                          @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.user.id = :userId and b.status = :status and" +
            " b.start>= :now order by b.start desc")
    List<Booking> findFutureBookingByUserId(@Param("userId") Long userId,
                                            @Param("now") LocalDateTime now,
                                            @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.user.id = :userId and b.status = :status " +
            " order by b.start desc")
    List<Booking> findWaitingBookingByUserId(@Param("userId") Long userId,
                                             @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.user.id = :userId and (b.status = :firstStatus " +
            " or b.status = :secondStatus ) order by b.start desc")
    List<Booking> findRejectedBookingByUserId(@Param("userId") Long userId,
                                              @Param("firstStatus") StatusBooking firstStatus,
                                              @Param("secondStatus") StatusBooking secondStatus);

    //    __________________
    @Query("select b from Booking b where b.item.user.id = :userId order by b.start desc")
    List<Booking> findAllBookingByOwnerId(@Param("userId") Long userId);

    @Query("select b from Booking b where b.item.user.id = :userId and b.status = :status and" +
            " (b.start<= :now and b.end>= :now) order by b.start desc")
    List<Booking> findCurrentBookingByOwnerId(@Param("userId") Long userId,
                                              @Param("now") LocalDateTime now,
                                              @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.item.user.id = :userId and b.status = :status and" +
            " b.end<= :now order by b.start desc")
    List<Booking> findPastBookingByOwnerId(@Param("userId") Long userId,
                                           @Param("now") LocalDateTime now,
                                           @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.item.user.id = :userId and b.status = :status and" +
            " b.start>= :now order by b.start desc")
    List<Booking> findFutureBookingByOwnerId(@Param("userId") Long userId,
                                             @Param("now") LocalDateTime now,
                                             @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.item.user.id = :userId and b.status = :status " +
            " order by b.start desc")
    List<Booking> findWaitingBookingByOwnerId(@Param("userId") Long userId,
                                              @Param("status") StatusBooking status);

    @Query("select b from Booking b where b.item.user.id = :userId and (b.status = :firstStatus " +
            " or b.status = :secondStatus ) order by b.start desc")
    List<Booking> findRejectedBookingByOwnerId(@Param("userId") Long userId,
                                               @Param("firstStatus") StatusBooking firstStatus,
                                               @Param("secondStatus") StatusBooking secondStatus);

    @Query("select b from Booking b where b.item.id = :itemId and " +
            " b.start>= :now order by b.start desc")
    List<Booking> findBookingByItemAndLocalDateTime(@Param("itemId") Long itemId, LocalDateTime now);

    @Query("select b from Booking b where b.item.id = :itemId and b.user.id = :userId" +
            " and b.end<= :now and b.status = :status order by b.start desc")
    List<Booking> findBookingByItemAndUser(@Param("userId") Long userId,
                                           @Param("itemId") Long itemId,
                                           @Param("now") LocalDateTime now,
                                           @Param("status") StatusBooking status);

    @Query("select b from Booking b " +
            "where b.item.id in :itemIds and b.status = :status " +
            "order by b.start desc")
    List<Booking> findAllByItemIdsAndStatus(@Param("itemIds") List<Long> itemIds,
                                            @Param("status") StatusBooking status);
}
