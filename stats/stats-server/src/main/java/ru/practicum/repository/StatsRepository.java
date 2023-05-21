package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {

    @Query(value = "SELECT new ru.practicum.model.Stats(" +
            "hit.app as app, hit.uri as uri, COUNT(DISTINCT hit.ip) as counter) " +
            "FROM EndpointHit hit " +
            "WHERE hit.timestamp between :start AND :end " +
            "GROUP BY hit.app, hit.uri " +
            "ORDER BY counter DESC")
    List<Stats> findUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT new ru.practicum.model.Stats(" +
            "hit.app as app, hit.uri as uri, COUNT(hit.ip) as counter) " +
            "FROM EndpointHit hit " +
            "WHERE hit.timestamp between :start AND :end " +
            "GROUP BY hit.app, hit.uri " +
            "ORDER BY counter DESC")
    List<Stats> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT new ru.practicum.model.Stats(" +
            "hit.app as app, hit.uri as uri, COUNT(DISTINCT hit.ip) as counter) " +
            "FROM EndpointHit hit " +
            "WHERE hit.timestamp between :start AND :end " +
            "AND uri in ( :uris) " +
            "GROUP BY hit.app, hit.uri " +
            "ORDER BY counter DESC")
    List<Stats> findUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query(value = "SELECT new ru.practicum.model.Stats(" +
            "hit.app as app, hit.uri as uri, COUNT(hit.ip) as counter) " +
            "FROM EndpointHit hit " +
            "WHERE hit.timestamp between :start AND :end " +
            "AND uri in ( :uris) " +
            "GROUP BY hit.app, hit.uri " +
            "ORDER BY counter DESC")
    List<Stats> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}
