package com.example.familyq.domain.question.service;

import com.example.familyq.domain.question.dto.QuestionAdminResponse;
import com.example.familyq.domain.question.dto.QuestionCreateRequest;
import com.example.familyq.domain.question.entity.Question;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.question.repository.QuestionRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionAdminService {

    private final QuestionRepository questionRepository;
    private final FamilyQuestionRepository familyQuestionRepository;

    @Transactional(readOnly = true)
    public List<QuestionAdminResponse> getQuestions() {
        return questionRepository.findAllByOrderByOrderIndexAsc()
                .stream()
                .map(QuestionAdminResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionAdminResponse createQuestion(QuestionCreateRequest request) {
        long count = questionRepository.count();
        int nextOrderIndex = (int) count + 1;
        int orderIndex = request.getOrderIndex() == null ? nextOrderIndex : request.getOrderIndex();

        validateOrderIndex(orderIndex, nextOrderIndex);

        if (request.getOrderIndex() != null) {
            shiftOrderIndexesForInsert(orderIndex);
        }

        Question question = Question.builder()
                .text(request.getText())
                .orderIndex(orderIndex)
                .build();
        Question saved = questionRepository.save(question);
        return QuestionAdminResponse.from(saved);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        if (familyQuestionRepository.existsByQuestion(question)) {
            throw new BusinessException(ErrorCode.QUESTION_IN_USE);
        }

        int deletedOrderIndex = question.getOrderIndex();
        questionRepository.delete(question);
        shiftOrderIndexesForDelete(deletedOrderIndex);
    }

    private void validateOrderIndex(int orderIndex, int nextOrderIndex) {
        if (orderIndex < 1 || orderIndex > nextOrderIndex) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "순번은 1 이상 " + nextOrderIndex + " 이하의 값이어야 합니다."
            );
        }
    }

    private void shiftOrderIndexesForInsert(int orderIndex) {
        questionRepository.incrementOrderIndexesFrom(orderIndex);
    }

    private void shiftOrderIndexesForDelete(int deletedOrderIndex) {
        questionRepository.decrementOrderIndexesFrom(deletedOrderIndex);
    }
}
