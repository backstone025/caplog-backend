package com.example.caplog.domain.schedule.repository;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
    Page<Schedule> findByGroups(Groups groups, Pageable pageable);

    // 특정 사용자의 전체 일정 개수
    @Query("""
           SELECT COUNT(s)
           FROM Schedule s
           WHERE s.groups.user = :user
           """)
    Integer countAllByUser(@Param("user") Users user);

    // 특정 사용자의 특정 기간 내 생성된 일정 개수
    @Query("""
           SELECT COUNT(s)
           FROM Schedule s
           WHERE s.groups.user = :user
           AND s.createdAt >= :startDate
           AND s.createdAt <= :endDate
           """)
    Integer countByUserAndCreatedAtBetween(
            @Param("user") Users user,
            @Param("startDate")LocalDateTime startDate,
            @Param("endDate")LocalDateTime endDate
            );
}
