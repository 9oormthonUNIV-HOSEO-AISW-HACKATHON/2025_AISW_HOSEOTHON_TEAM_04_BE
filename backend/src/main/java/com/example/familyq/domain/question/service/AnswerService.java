package com.example.familyq.domain.question.service;

import com.example.familyq.domain.insight.service.AiService;
import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.question.QuestionPolicy;
import com.example.familyq.domain.question.dto.AnswerRequest;
import com.example.familyq.domain.question.dto.AnswerResponse;
import com.example.familyq.domain.question.entity.Answer;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.repository.AnswerRepository;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import com.example.familyq.global.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final FamilyQuestionRepository familyQuestionRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final AiService aiService;

    @Transactional
    public AnswerResponse submitAnswer(Long userId, Long familyQuestionId, AnswerRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = user.getFamily();
        if (family == null) {
            throw new BusinessException(ErrorCode.USER_NOT_IN_FAMILY);
        }

        FamilyQuestion familyQuestion = familyQuestionRepository.findByIdWithLock(familyQuestionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_QUESTION_NOT_FOUND));

        if (!familyQuestion.getFamily().getId().equals(family.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (familyQuestion.isCompleted()) {
            throw new BusinessException(ErrorCode.FAMILY_QUESTION_ALREADY_COMPLETED);
        }

        Family managedFamily = familyRepository.findWithMembersById(family.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));
        int requiredMemberCount = resolveRequiredMemberCount(managedFamily, familyQuestion);
        familyQuestion.updateRequiredMemberCount(requiredMemberCount);

        Answer answer = answerRepository.findByFamilyQuestionAndUser(familyQuestion, user)
                .map(existing -> {
                    existing.updateContent(request.getContent());
                    return existing;
                })
                .orElseGet(() ->
                        answerRepository.save(Answer.builder()
                                .familyQuestion(familyQuestion)
                                .user(user)
                                .content(request.getContent())
                                .build())
                );

        evaluateCompletion(familyQuestion, requiredMemberCount);
        return AnswerResponse.of(answer, userId);
    }

    private void evaluateCompletion(FamilyQuestion familyQuestion, int requiredMemberCount) {
        long answerCount = answerRepository.countByFamilyQuestion(familyQuestion);
        if (familyQuestion.isCompleted() || answerCount < requiredMemberCount) {
            return;
        }

        familyQuestion.markCompleted(DateTimeUtils.now());
        familyQuestionRepository.save(familyQuestion);

        // AI 서비스를 통해 인사이트 생성
        try {
            String insightJson = aiService.getCounselingByFamilyQuestion(familyQuestion.getId());
            familyQuestion.saveInsightJson(insightJson);
            familyQuestionRepository.save(familyQuestion);
        } catch (Exception e) {
            // 인사이트 생성 실패는 답변 완료를 막지 않도록 무시하고 로그만 남김
            log.warn("Failed to generate insight for familyQuestionId={}: {}", familyQuestion.getId(), e.getMessage());
        }
    }

    private int resolveRequiredMemberCount(Family family, FamilyQuestion familyQuestion) {
        int currentMemberCount = family.getMembers().size();
        int storedRequiredCount = familyQuestion.getRequiredMemberCount() == null ? 0 : familyQuestion.getRequiredMemberCount();
        int baseRequiredCount = Math.max(QuestionPolicy.MIN_MEMBERS_TO_START, storedRequiredCount);
        return Math.max(1, Math.min(currentMemberCount, baseRequiredCount));
    }
}
