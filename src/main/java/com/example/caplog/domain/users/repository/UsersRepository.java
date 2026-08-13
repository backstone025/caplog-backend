package com.example.caplog.domain.users.repository;

import com.example.caplog.domain.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByLoginId(String loginId);
}
