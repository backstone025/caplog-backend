package com.example.caplog.domain.notification.repository;

import com.example.caplog.domain.notification.entity.Notification;
import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByUser(Users user, Pageable pageable);

    Page<Notification> findAllByUserAndType(Users user, NotificationType type, Pageable pageable);
}
