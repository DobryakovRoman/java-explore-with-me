package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByIdIn(List<Long> ids);

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

    Page<Event> findAll(Pageable page);

    List<Event> findAllByInitiator(User user, Pageable page);

    @Query("SELECT event FROM Event event " +
            "WHERE (lower(event.annotation) LIKE %:text% " +
            "OR lower(event.description) LIKE %:text%) " +
            "AND event.state = :state " +
            "ORDER BY event.eventDate DESC")
    List<Event> findEventsByText(String text, EventState state, Pageable page);

    @Query("SELECT event FROM Event event " +
            "WHERE (lower(event.annotation) LIKE %:text% " +
            "OR lower(event.description) LIKE %:text%) " +
            "AND event.eventDate >= :startDate " +
            "AND event.eventDate <= :endDate " +
            "AND event.state = :state " +
            "ORDER BY event.eventDate DESC")
    List<Event> findAllByTextAndDateRange(String text, LocalDateTime startDate, LocalDateTime endDate, EventState state, Pageable page);

    List<Event> findAllByCategoryId(Long catId);

    Boolean existsByCategoryId(Long catId);

    @Query("SELECT event FROM Event event " +
            "WHERE event.category.id in :categories " +
            "AND event.state = :state " +
            "ORDER BY event.eventDate DESC")
    List<Event> findAllByCategoryIdPageable(List<Long> categories, EventState state, Pageable page);

    @Query("SELECT DISTINCT event FROM Event event " +
            "WHERE (event.annotation LIKE COALESCE(:text, event.annotation) OR event.description LIKE COALESCE(:text, event.description)) " +
            "AND (:categories IS NULL OR event.category.id IN :categories) " +
            "AND (:paid IS NULL OR event.paid = :paid) " +
            "AND event.eventDate >= COALESCE(:rangeStart, event.eventDate) " +
            "AND event.eventDate <= COALESCE(:rangeEnd, event.eventDate) " +
            "AND event.state = :state " +
            "ORDER BY event.eventDate DESC")
    List<Event> findEventList(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, EventState state);

}