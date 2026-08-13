package com.example.caplog.domain.schedule;

import com.example.caplog.domain.schedule.entity.DemoSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoScheduleRepository extends JpaRepository<DemoSchedule,Long> {
}
