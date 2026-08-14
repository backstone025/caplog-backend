package com.example.caplog.domain.users.repository;

import com.example.caplog.domain.users.entity.UsersDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersDetailsRepository extends JpaRepository<UsersDetails, Long> {
}
