package com.example.caplog.domain.notification.repository;

import com.example.caplog.domain.notification.entity.Notification;
import com.example.caplog.domain.users.entity.Users;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
