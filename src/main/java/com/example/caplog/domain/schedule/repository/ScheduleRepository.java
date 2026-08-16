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
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Page<Schedule> findByGroups(Groups groups, Pageable pageable);

    // 특정 그룹의 일정 목록 조회 (JPQL)
    @Query("SELECT s FROM Schedule s WHERE s.groups.groupId = :groupId")
    List<Schedule> findByGroupsGroupId(@Param("groupId") Long groupId);

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
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 특정 사용자의 미확인 일정 조회(viewedAt IS NULL)
    @Query("""
            SELECT s
            FROM Schedule s
            JOIN FETCH s.groups g
            WHERE g.user = :user
            AND s.viewedAt IS NULL
            AND s.createdAt <= :thresholdDate
            ORDER BY s.createdAt DESC
            """)
    List<Schedule> findUnviewedSchedulesByUser(
            @Param("user") Users user,
            @Param("thresholdDate") LocalDateTime thresholdDate,
            Pageable pageable
    );
}
