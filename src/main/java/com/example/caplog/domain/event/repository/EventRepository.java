package com.example.caplog.domain.event.repository;

import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // 각 일정별 첫 번째 Event와 Images를 한 번에 가져오는 Fetch Join 쿼리(N+1 문제 방지)
    @Query("""
            SELECT e 
            FROM Event e LEFT JOIN FETCH e.images
            WHERE e.schedule IN :scheduleList
                AND e.eventId = (
                    SELECT MIN(e2.eventId)
                    FROM Event e2
                    WHERE e2.schedule = e.schedule
                )
            """)
    List<Event> findFirstEventsWithImageByScheduleIn(@Param("scheduleList") List<Schedule> scheduleList);
}
