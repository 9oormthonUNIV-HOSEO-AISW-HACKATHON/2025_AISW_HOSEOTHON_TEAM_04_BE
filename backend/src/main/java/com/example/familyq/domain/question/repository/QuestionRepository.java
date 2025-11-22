package com.example.familyq.domain.question.repository;

import com.example.familyq.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByOrderIndex(Integer orderIndex);

    List<Question> findAllByOrderByOrderIndexAsc();

    List<Question> findByOrderIndexGreaterThanEqualOrderByOrderIndexDesc(Integer orderIndex);

    List<Question> findByOrderIndexGreaterThanOrderByOrderIndexAsc(Integer orderIndex);

    @Modifying(clearAutomatically = true)
    @Query("update Question q set q.orderIndex = q.orderIndex + 1 where q.orderIndex >= :orderIndex")
    void incrementOrderIndexesFrom(@Param("orderIndex") int orderIndex);

    @Modifying(clearAutomatically = true)
    @Query("update Question q set q.orderIndex = q.orderIndex - 1 where q.orderIndex > :orderIndex")
    void decrementOrderIndexesFrom(@Param("orderIndex") int orderIndex);
}
