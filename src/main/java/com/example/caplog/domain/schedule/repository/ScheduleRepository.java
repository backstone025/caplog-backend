package com.example.caplog.domain.schedule.repository;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
    Page<Schedule> findByGroups(Groups groups, Pageable pageable);
}
