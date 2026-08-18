package com.example.caplog.domain.groups.repository;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupsRepository extends JpaRepository<Groups, Long> {
    boolean existsByTitleAndGroupIdNot(String title, Long groupId);

    Page<Groups> findByUserAndCategory(Users user, Category category, Pageable pageable);

    // TOTAL + 검색어
    @Query("""
            SELECT g
            FROM Groups g
            WHERE g.user = :user
            AND (
                :searchWords = ''
                OR LOWER(g.title) LIKE LOWER(CONCAT('%', :searchWords, '%'))
            )
            """)
    List<Groups> findGroups(
            @Param("user") Users user,
            @Param("searchWords") String searchWords
    );

    // 카테고리 + 검색어
    @Query("""
            SELECT g
            FROM Groups g
            WHERE g.user = :user
            AND g.category = :category
            AND (
                :searchWords = ''
                OR LOWER(g.title) LIKE LOWER(CONCAT('%', :searchWords, '%'))
            )
            """)


    List<Groups> findGroupsByCategory(
            @Param("user") Users user,
            @Param("category") Category category,
            @Param("searchWords") String searchWords
    );
}
