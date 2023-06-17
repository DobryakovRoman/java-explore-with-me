package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.requester.id = :userId " +
            "AND r.event.initiator.id <> :userId")
    List<ParticipationRequest> findByUserId(Long userId);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.event.initiator.id = :userId")
    List<ParticipationRequest> findByEventInitiatorId(Long userId);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId")
    List<ParticipationRequest> findByEventId(Long eventId);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.event.id in :eventIds ")
    List<ParticipationRequest> findByEventIds(List<Long> eventIds);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.event.id in :eventIds " +
            "AND r.status = :status")
    List<ParticipationRequest> findByEventIdsAndStatus(List<Long> eventIds, String status);

    List<ParticipationRequest> findRequestByEventIdAndStatus(Long eventId, String status);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId " +
            "AND r.status in :statuses")
    Long countByEventAndStatuses(Long eventId, List<String> statuses);
}
