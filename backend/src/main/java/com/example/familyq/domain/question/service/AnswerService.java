package com.example.familyq.domain.question.service;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.insight.dto.InsightGenerateRequest;
import com.example.familyq.domain.insight.dto.InsightResponse;
import com.example.familyq.domain.insight.service.InsightService;
import com.example.familyq.domain.question.dto.AnswerRequest;
import com.example.familyq.domain.question.dto.AnswerResponse;
import com.example.familyq.domain.question.entity.Answer;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.repository.AnswerRepository;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.global.exception.ErrorCode;
import com.example.familyq.global.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final FamilyQuestionRepository familyQuestionRepository;
    private final UserRepository userRepository;
    private final InsightService insightService;

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

        evaluateCompletion(familyQuestion);
        return AnswerResponse.of(answer, userId);
    }

    private void evaluateCompletion(FamilyQuestion familyQuestion) {
        long answerCount = answerRepository.countByFamilyQuestion(familyQuestion);
        if (familyQuestion.isCompleted() || answerCount < familyQuestion.getRequiredMemberCount()) {
            return;
        }

        familyQuestion.markCompleted(DateTimeUtils.now());

        List<Answer> answers = answerRepository.findByFamilyQuestion(familyQuestion);
        InsightGenerateRequest request = buildInsightRequest(familyQuestion, answers);
        InsightResponse response = insightService.generateInsight(request);
        familyQuestion.saveInsightJson(insightService.serialize(response));
    }

    private InsightGenerateRequest buildInsightRequest(FamilyQuestion familyQuestion, List<Answer> answers) {
        Family family = familyQuestion.getFamily();
        List<User> members = family.getMembers();
        Map<Long, String> birthOrderTagMap = buildBirthOrderMap(members);

        return InsightGenerateRequest.builder()
                .questionText(familyQuestion.getQuestion().getText())
                .familyMembers(
                        members.stream()
                                .map(member -> InsightGenerateRequest.FamilyMemberContext.builder()
                                        .name(member.getName())
                                        .roleType(member.getRoleType())
                                        .birthYear(member.getBirthYear())
                                        .ageGroupTag(calculateAgeGroup(member.getBirthYear()))
                                        .birthOrderTag(birthOrderTagMap.get(member.getId()))
                                        .build())
                                .toList()
                )
                .answers(
                        answers.stream()
                                .map(answer -> InsightGenerateRequest.AnswerContext.builder()
                                        .userName(answer.getUser().getName())
                                        .roleType(answer.getUser().getRoleType())
                                        .ageGroupTag(calculateAgeGroup(answer.getUser().getBirthYear()))
                                        .birthOrderTag(birthOrderTagMap.get(answer.getUser().getId()))
                                        .content(answer.getContent())
                                        .build())
                                .toList()
                )
                .build();
    }

    private Map<Long, String> buildBirthOrderMap(List<User> members) {
        AtomicInteger order = new AtomicInteger(0);
        Map<Long, String> result = new HashMap<>();
        members.stream()
                .filter(member -> member.getRoleType() == RoleType.CHILD)
                .sorted(Comparator.comparing(User::getBirthYear))
                .forEach(child -> result.put(child.getId(), mapOrder(order.getAndIncrement())));
        return result;
    }

    private String mapOrder(int order) {
        return switch (order) {
            case 0 -> "firstChild";
            case 1 -> "secondChild";
            case 2 -> "thirdChild";
            case 3 -> "fourthChild";
            default -> (order + 1) + "thChild";
        };
    }

    private String calculateAgeGroup(Integer birthYear) {
        int currentYear = DateTimeUtils.today().getYear();
        int age = currentYear - birthYear;
        if (age < 10) {
            return "10대 미만";
        }
        int decade = (age / 10) * 10;
        if (decade >= 60) {
            return "60대 이상";
        }
        return decade + "대";
    }
}
