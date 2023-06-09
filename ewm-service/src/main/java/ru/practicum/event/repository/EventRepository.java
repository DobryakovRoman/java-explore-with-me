package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT event FROM Event event " +
            "WHERE event.id in :events")
    List<Event> findEventsByIds(List<Long> events);

    @Query("SELECT event FROM Event event WHERE event.initiator.id IN :users " +
            "AND event.state in :states " +
            "AND event.category.id in :categories " +
            "AND event.eventDate between :rangeStart AND :rangeEnd " +
            "ORDER BY event.eventDate DESC")
    List<Event> findAllEventsWithDates(List<Long> users,
                                       List<EventState> states,
                                       List<Long> categories,
                                       LocalDateTime rangeStart,
                                       LocalDateTime rangeEnd,
                                       Pageable page);

    @Query("SELECT event FROM Event event ")
    List<Event> findEventsByDefault(Pageable page);

    @Query("SELECT event FROM Event event WHERE event.initiator in :user")
    List<Event> findAllByUserId(User user, Pageable page);

    @Query("SELECT event FROM Event event " +
            "WHERE (lower(event.annotation) LIKE %:text% " +
            "OR lower(event.description) LIKE %:text%) " +
            "ORDER BY event.eventDate DESC")
    List<Event> findEventsByText(String text, Pageable page);

    @Query("SELECT event FROM Event event " +
            "WHERE (lower(event.annotation) LIKE %:text% " +
            "OR lower(event.description) LIKE %:text%) " +
            "AND event.eventDate >= :startDate " +
            "AND event.eventDate <= :endDate " +
            "ORDER BY event.eventDate DESC")
    List<Event> findAllByTextAndDateRange(String text, LocalDateTime startDate, LocalDateTime endDate, Pageable page);

    List<Event> findAllByCategoryId(Long catId);

    @Query("SELECT event FROM Event event " +
            "WHERE event.category.id in :categories ")
    List<Event> findAllByCategoryIdPageable(List<Long> categories, Pageable page);
}