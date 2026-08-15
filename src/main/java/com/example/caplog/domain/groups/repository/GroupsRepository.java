package com.example.caplog.domain.groups.repository;

import com.example.caplog.domain.groups.entity.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsRepository extends JpaRepository<Groups, Long> {
    boolean existsByTitleAndGroupIdNot(String title, Long groupId);
}
