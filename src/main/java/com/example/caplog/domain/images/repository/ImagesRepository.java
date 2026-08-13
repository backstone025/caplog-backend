package com.example.caplog.domain.images.repository;

import com.example.caplog.domain.images.entity.Images;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagesRepository extends JpaRepository<Images, Long> {
}