package com.example.familyq.domain.question.service;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.insight.dto.InsightResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.familyq.domain.question.QuestionPolicy;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final QuestionRepository questionRepository;
    private final FamilyQuestionRepository familyQuestionRepository;
    private final AnswerRepository answerRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DailyQuestionResponse getTodayQuestion(Long userId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = loadFamilyWithMembers(getFamily(user));
        validateFamilyReadyForQuestions(family);

        FamilyQuestion familyQuestion = resolveCurrentFamilyQuestion(family);

        // 질문이 없을 때 처리
        if (familyQuestion == null) {
            return DailyQuestionResponse.empty();
        }

        int requiredMemberCount = updateRequiredMemberCount(family, familyQuestion);

        long answeredCount = answerRepository.countByFamilyQuestion(familyQuestion);
        InsightResponse insight = parseInsight(familyQuestion);
        AnswerResponse myAnswer = answerRepository.findByFamilyQuestionAndUser(familyQuestion, user)
                .map(answer -> AnswerResponse.of(answer, userId))
                .orElse(null);

        return DailyQuestionResponse.of(
                familyQuestion,
                (int) answeredCount,
                requiredMemberCount,
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
        AnswerResponse myAnswer = answers.stream()
                .filter(answer -> answer.getUser().getId().equals(userId))
                .findFirst()
                .map(answer -> AnswerResponse.of(answer, userId))
                .orElse(null);
        List<AnswerResponse> answerResponses = familyQuestion.isCompleted()
                ? answers.stream().map(answer -> AnswerResponse.of(answer, userId)).toList()
                : answers.stream()
                .filter(answer -> answer.getUser().getId().equals(userId))
                .map(answer -> AnswerResponse.of(answer, userId))
                .toList();

        InsightResponse insight = familyQuestion.isCompleted() ? parseInsight(familyQuestion) : null;

        return QuestionDetailResponse.of(familyQuestion, myAnswer, answerResponses, insight);
    }

    private FamilyQuestion resolveCurrentFamilyQuestion(Family family) {
        int totalQuestionCount = (int) questionRepository.count();
        if (totalQuestionCount == 0) {
            // 질문이 없을 때는 null 반환 (에러 대신 graceful 처리)
            return null;
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

        int requiredMemberCount = Math.max(QuestionPolicy.MIN_MEMBERS_TO_START, family.getMembers().size());

        FamilyQuestion familyQuestion = FamilyQuestion.builder()
                .family(family)
                .question(question)
                .sequenceNumber(sequenceNumber)
                .assignedDate(DateTimeUtils.today())
                .status(FamilyQuestionStatus.IN_PROGRESS)
                .requiredMemberCount(requiredMemberCount)
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
        try {
            return objectMapper.readValue(familyQuestion.getInsightJson(), InsightResponse.class);
        } catch (JsonProcessingException e) {
            // 파싱 실패 시 null 반환 (에러 로그는 선택사항)
            log.warn("Failed to parse insight JSON for familyQuestionId={}: {}", familyQuestion.getId(), e.getMessage());
            return null;
        }
    }

    @Transactional
    public DailyQuestionResponse refreshQuestion(Long userId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = loadFamilyWithMembers(getFamily(user));
        validateFamilyReadyForQuestions(family);

        // 기존 IN_PROGRESS 질문 삭제
        Optional<FamilyQuestion> existingQuestion = familyQuestionRepository
                .findByFamilyAndStatus(family, FamilyQuestionStatus.IN_PROGRESS);
        existingQuestion.ifPresent(familyQuestionRepository::delete);

        // 최신 질문 번호 확인
        Optional<FamilyQuestion> latestOptional = familyQuestionRepository
                .findTopByFamilyOrderBySequenceNumberDesc(family);
        int nextSequenceNumber = latestOptional
                .map(fq -> fq.getSequenceNumber() + 1)
                .orElse(1);

        // 새로운 FamilyQuestion 생성 시도
        int totalQuestionCount = (int) questionRepository.count();
        if (totalQuestionCount == 0) {
            return DailyQuestionResponse.empty();
        }

        // 다음 질문이 있는지 확인
        if (nextSequenceNumber > totalQuestionCount) {
            // 모든 질문을 완료했으면 처음부터 다시 시작
            nextSequenceNumber = 1;
        }

        // 동일 sequence가 이미 있으면 삭제하여 중복 키 방지 (디버그/재시작 시나리오 보호)
        familyQuestionRepository.findByFamilyAndSequenceNumber(family, nextSequenceNumber)
                .ifPresent(familyQuestionRepository::delete);

        FamilyQuestion newQuestion = createFamilyQuestion(family, nextSequenceNumber);

        int requiredMemberCount = updateRequiredMemberCount(family, newQuestion);

        long answeredCount = answerRepository.countByFamilyQuestion(newQuestion);
        InsightResponse insight = parseInsight(newQuestion);
        AnswerResponse myAnswer = answerRepository.findByFamilyQuestionAndUser(newQuestion, user)
                .map(answer -> AnswerResponse.of(answer, userId))
                .orElse(null);

        return DailyQuestionResponse.of(newQuestion, (int) answeredCount, requiredMemberCount, myAnswer, insight);
    }

    @Transactional
    public DailyQuestionResponse skipToNextQuestion(Long userId) {
        User user = userRepository.findWithFamilyById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Family family = loadFamilyWithMembers(getFamily(user));
        validateFamilyReadyForQuestions(family);

        // 현재 IN_PROGRESS 질문을 COMPLETED로 변경
        Optional<FamilyQuestion> currentQuestion = familyQuestionRepository
                .findByFamilyAndStatus(family, FamilyQuestionStatus.IN_PROGRESS);

        if (currentQuestion.isPresent()) {
            FamilyQuestion current = currentQuestion.get();
            current.markCompleted(LocalDateTime.now());
            familyQuestionRepository.save(current);
        }

        // 다음 질문 번호 계산
        Optional<FamilyQuestion> latestOptional = familyQuestionRepository
                .findTopByFamilyOrderBySequenceNumberDesc(family);
        int nextSequenceNumber = latestOptional
                .map(fq -> fq.getSequenceNumber() + 1)
                .orElse(1);

        int totalQuestionCount = (int) questionRepository.count();
        if (totalQuestionCount == 0) {
            return DailyQuestionResponse.empty();
        }

        // 다음 질문이 없으면 처음부터 다시
        if (nextSequenceNumber > totalQuestionCount) {
            nextSequenceNumber = 1;
        }

        // 동일 sequence가 이미 있으면 삭제하여 중복 키 방지 (디버그/재시작 시나리오 보호)
        familyQuestionRepository.findByFamilyAndSequenceNumber(family, nextSequenceNumber)
                .ifPresent(familyQuestionRepository::delete);

        // 새로운 FamilyQuestion 생성
        FamilyQuestion newQuestion = createFamilyQuestion(family, nextSequenceNumber);

        int requiredMemberCount = updateRequiredMemberCount(family, newQuestion);

        long answeredCount = answerRepository.countByFamilyQuestion(newQuestion);
        InsightResponse insight = parseInsight(newQuestion);
        AnswerResponse myAnswer = answerRepository.findByFamilyQuestionAndUser(newQuestion, user)
                .map(answer -> AnswerResponse.of(answer, userId))
                .orElse(null);

        return DailyQuestionResponse.of(newQuestion, (int) answeredCount, requiredMemberCount, myAnswer, insight);
    }

    private void validateFamilyReadyForQuestions(Family family) {
        if (!Boolean.TRUE.equals(family.getQuestionsStarted())) {
            throw new BusinessException(
                    ErrorCode.FAMILY_NOT_READY_FOR_QUESTIONS,
                    "가족이 모두 모이면 '질문 받기 시작하기'를 눌러주세요."
            );
        }

        if (family.getMembers().size() < QuestionPolicy.MIN_MEMBERS_TO_START) {
            throw new BusinessException(
                    ErrorCode.FAMILY_NOT_READY_FOR_QUESTIONS,
                    "가족 구성원이 최소 " + QuestionPolicy.MIN_MEMBERS_TO_START + "명 이상일 때 질문이 시작됩니다."
            );
        }
    }

    private Family loadFamilyWithMembers(Family family) {
        return familyRepository.findWithMembersById(family.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FAMILY_NOT_FOUND));
    }

    private int updateRequiredMemberCount(Family family, FamilyQuestion familyQuestion) {
        int currentMemberCount = family.getMembers().size();
        int storedRequiredCount = Optional.ofNullable(familyQuestion.getRequiredMemberCount()).orElse(0);
        int baseRequiredCount = Math.max(QuestionPolicy.MIN_MEMBERS_TO_START, storedRequiredCount);
        int adjustedRequiredCount = Math.max(1, Math.min(currentMemberCount, baseRequiredCount));
        familyQuestion.updateRequiredMemberCount(adjustedRequiredCount);
        return adjustedRequiredCount;
    }
}
