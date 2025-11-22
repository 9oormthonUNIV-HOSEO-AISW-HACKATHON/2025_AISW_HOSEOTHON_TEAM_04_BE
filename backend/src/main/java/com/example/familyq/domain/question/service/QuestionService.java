package com.example.familyq.domain.question.service;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.insight.dto.InsightResponse;
import com.example.familyq.domain.insight.service.InsightService;
import com.example.familyq.domain.question.dto.AnswerResponse;
import com.example.familyq.domain.question.dto.DailyQuestionResponse;
import com.example.familyq.domain.question.dto.QuestionDetailResponse;
import com.example.familyq.domain.question.dto.QuestionHistoryItemResponse;
import com.example.familyq.domain.question.entity.Answer;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.entity.FamilyQuestionStatus;
import com.example.familyq.domain.question.entity.Question;
import com.example.familyq.domain.question.repository.AnswerRepository;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.question.repository.QuestionRepository;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import com.example.familyq.global.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final QuestionRepository questionRepository;
    private final FamilyQuestionRepository familyQuestionRepository;
    private final AnswerRepository answerRepository;
    private final InsightService insightService;

    @Transactional
    public DailyQuestionResponse getTodayQuestion(Long userId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = getFamily(user);

        FamilyQuestion familyQuestion = resolveCurrentFamilyQuestion(family);
        long answeredCount = answerRepository.countByFamilyQuestion(familyQuestion);
        InsightResponse insight = parseInsight(familyQuestion);
        AnswerResponse myAnswer = answerRepository.findByFamilyQuestionAndUser(familyQuestion, user)
                .map(answer -> AnswerResponse.of(answer, userId))
                .orElse(null);

        return DailyQuestionResponse.of(
                familyQuestion,
                (int) answeredCount,
                myAnswer,
                insight
        );
    }

    @Transactional(readOnly = true)
    public List<QuestionHistoryItemResponse> getHistory(Long userId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = getFamily(user);

        return familyQuestionRepository.findByFamilyOrderBySequenceNumberDesc(family)
                .stream()
                .sorted(Comparator.comparing(FamilyQuestion::getSequenceNumber).reversed())
                .map(QuestionHistoryItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionDetailResponse getQuestionDetail(Long userId, Long familyQuestionId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = getFamily(user);

        FamilyQuestion familyQuestion = familyQuestionRepository.findById(familyQuestionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_QUESTION_NOT_FOUND));

        if (!familyQuestion.getFamily().getId().equals(family.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<Answer> answers = answerRepository.findByFamilyQuestion(familyQuestion);
        List<AnswerResponse> answerResponses = familyQuestion.isCompleted()
                ? answers.stream().map(answer -> AnswerResponse.of(answer, userId)).toList()
                : answers.stream()
                .filter(answer -> answer.getUser().getId().equals(userId))
                .map(answer -> AnswerResponse.of(answer, userId))
                .toList();

        InsightResponse insight = familyQuestion.isCompleted() ? parseInsight(familyQuestion) : null;

        return QuestionDetailResponse.of(familyQuestion, answerResponses, insight);
    }

    private FamilyQuestion resolveCurrentFamilyQuestion(Family family) {
        int totalQuestionCount = (int) questionRepository.count();
        if (totalQuestionCount == 0) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND, "등록된 질문이 없습니다.");
        }

        Optional<FamilyQuestion> inProgress = familyQuestionRepository.findByFamilyAndStatus(family, FamilyQuestionStatus.IN_PROGRESS);
        if (inProgress.isPresent()) {
            return inProgress.get();
        }

        Optional<FamilyQuestion> latestOptional = familyQuestionRepository.findTopByFamilyOrderBySequenceNumberDesc(family);
        if (latestOptional.isEmpty()) {
            return createFamilyQuestion(family, 1);
        }

        FamilyQuestion latest = latestOptional.get();
        LocalDate today = DateTimeUtils.today();
        boolean canCreateNext = latest.isCompleted()
                && latest.getSequenceNumber() < totalQuestionCount
                && today.isAfter(latest.getAssignedDate());

        if (canCreateNext) {
            return createFamilyQuestion(family, latest.getSequenceNumber() + 1);
        }

        return latest;
    }

    private FamilyQuestion createFamilyQuestion(Family family, int sequenceNumber) {
        Question question = questionRepository.findByOrderIndex(sequenceNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        Family managedFamily = familyRepository.findWithMembersById(family.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));

        FamilyQuestion familyQuestion = FamilyQuestion.builder()
                .family(managedFamily)
                .question(question)
                .sequenceNumber(sequenceNumber)
                .assignedDate(DateTimeUtils.today())
                .status(FamilyQuestionStatus.IN_PROGRESS)
                .requiredMemberCount(managedFamily.getMembers().size())
                .build();
        return familyQuestionRepository.save(familyQuestion);
    }

    private Family getFamily(User user) {
        Family family = user.getFamily();
        if (family == null) {
            throw new BusinessException(ErrorCode.USER_NOT_IN_FAMILY);
        }
        return family;
    }

    private InsightResponse parseInsight(FamilyQuestion familyQuestion) {
        if (familyQuestion.getInsightJson() == null) {
            return null;
        }
        return insightService.deserialize(familyQuestion.getInsightJson());
    }
}
