package com.example.familyq.domain.question.repository;

import com.example.familyq.domain.question.entity.Answer;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByFamilyQuestionAndUser(FamilyQuestion familyQuestion, User user);

    long countByFamilyQuestion(FamilyQuestion familyQuestion);

    List<Answer> findByFamilyQuestion(FamilyQuestion familyQuestion);
}
