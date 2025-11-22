package com.example.familyq.domain.batch;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.question.QuestionPolicy;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.entity.FamilyQuestionStatus;
import com.example.familyq.domain.question.entity.Question;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.question.repository.QuestionRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import com.example.familyq.global.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyQuestionScheduler {

    private final FamilyRepository familyRepository;
    private final FamilyQuestionRepository familyQuestionRepository;
    private final QuestionRepository questionRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void assignNextQuestions() {
        int totalQuestionCount = (int) questionRepository.count();
        if (totalQuestionCount == 0) {
            log.warn("질문이 존재하지 않아 배치를 건너뜁니다.");
            return;
        }

        LocalDate today = DateTimeUtils.today();
        List<Family> families = familyRepository.findAll();
        for (Family family : families) {
            if (!Boolean.TRUE.equals(family.getQuestionsStarted())) {
                continue;
            }

            Family managedFamily = familyRepository.findWithMembersById(family.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));
            if (managedFamily.getMembers().size() < QuestionPolicy.MIN_MEMBERS_TO_START) {
                continue;
            }

            familyQuestionRepository.findTopByFamilyOrderBySequenceNumberDesc(managedFamily)
                    .ifPresent(latest -> createNextIfNecessary(managedFamily, latest, today, totalQuestionCount));
        }
    }

    private void createNextIfNecessary(Family family,
                                       FamilyQuestion latest,
                                       LocalDate today,
                                       int totalQuestionCount) {
        if (!latest.isCompleted()) {
            return;
        }
        if (!today.isAfter(latest.getAssignedDate())) {
            return;
        }
        if (latest.getSequenceNumber() >= totalQuestionCount) {
            return;
        }

        int nextSequence = latest.getSequenceNumber() + 1;
        Question nextQuestion = questionRepository.findByOrderIndex(nextSequence)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        FamilyQuestion familyQuestion = FamilyQuestion.builder()
                .family(family)
                .question(nextQuestion)
                .sequenceNumber(nextSequence)
                .assignedDate(today)
                .status(FamilyQuestionStatus.IN_PROGRESS)
                .requiredMemberCount(Math.max(QuestionPolicy.MIN_MEMBERS_TO_START, family.getMembers().size()))
                .build();
        familyQuestionRepository.save(familyQuestion);
        log.info("Assigned question {} to family {}", nextSequence, family.getId());
    }
}
