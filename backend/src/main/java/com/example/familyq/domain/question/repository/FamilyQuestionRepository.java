package com.example.familyq.domain.question.repository;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.entity.FamilyQuestionStatus;
import com.example.familyq.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FamilyQuestionRepository extends JpaRepository<FamilyQuestion, Long> {

    Optional<FamilyQuestion> findTopByFamilyOrderBySequenceNumberDesc(Family family);

    Optional<FamilyQuestion> findByFamilyAndStatus(Family family, FamilyQuestionStatus status);

    Optional<FamilyQuestion> findByFamilyAndAssignedDate(Family family, LocalDate assignedDate);

    List<FamilyQuestion> findByFamilyOrderBySequenceNumberDesc(Family family);

    Optional<FamilyQuestion> findByFamilyAndSequenceNumber(Family family, Integer sequenceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fq from FamilyQuestion fq where fq.id = :id")
    Optional<FamilyQuestion> findByIdWithLock(@Param("id") Long id);

    boolean existsByQuestion(Question question);
}
