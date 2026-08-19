package com.example.caplog.domain.groups.repository;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupsRepository extends JpaRepository<Groups, Long> {
    boolean existsByTitleAndGroupIdNot(String title, Long groupId);

    Page<Groups> findAllByUser(Users user, Pageable pageable);

    Optional<Groups> findByGroupIdAndUser(
            Long groupId,
            Users user
    );
}
