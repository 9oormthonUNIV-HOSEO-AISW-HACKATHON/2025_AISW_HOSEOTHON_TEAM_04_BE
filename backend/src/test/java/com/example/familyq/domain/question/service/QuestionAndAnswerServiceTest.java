package com.example.familyq.domain.question.service;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.question.dto.AnswerRequest;
import com.example.familyq.domain.question.dto.DailyQuestionResponse;
import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.question.entity.Question;
import com.example.familyq.domain.question.repository.AnswerRepository;
import com.example.familyq.domain.question.repository.FamilyQuestionRepository;
import com.example.familyq.domain.question.repository.QuestionRepository;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.support.IntegrationTestSupport;
import com.example.familyq.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionAndAnswerServiceTest extends IntegrationTestSupport {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyQuestionRepository familyQuestionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUpQuestions() {
        if (questionRepository.count() == 0) {
            questionRepository.save(
                    Question.builder()
                            .text("테스트 질문 1")
                            .orderIndex(1)
                            .build()
            );
        }
    }

    @Test
    void 첫_질문_요청시_family_question_생성() {
        Family family = familyRepository.save(TestDataFactory.family("FAM001"));
        User father = saveUser(family, "father", RoleType.FATHER, 1980);

        DailyQuestionResponse response = questionService.getTodayQuestion(father.getId());

        assertThat(response.getSequenceNumber()).isEqualTo(1);
        assertThat(response.getRequiredMemberCount()).isEqualTo(1);
        FamilyQuestion saved = familyQuestionRepository
                .findByFamilyAndSequenceNumber(family, 1)
                .orElseThrow();
        assertThat(saved.getQuestion()).isNotNull();
        assertThat(saved.getRequiredMemberCount()).isEqualTo(1);
    }

    @Test
    void 전원_답변_시_질문_완료후_인사이트_생성() {
        Family family = familyRepository.save(TestDataFactory.family("FAM002"));
        User father = saveUser(family, "father2", RoleType.FATHER, 1980);
        User child = saveUser(family, "child1", RoleType.CHILD, 2010);

        DailyQuestionResponse today = questionService.getTodayQuestion(father.getId());
        FamilyQuestion familyQuestion = familyQuestionRepository
                .findById(today.getFamilyQuestionId())
                .orElseThrow();

        submitAnswer(father.getId(), familyQuestion.getId(), "아버지 답변");
        submitAnswer(child.getId(), familyQuestion.getId(), "자녀 답변");

        FamilyQuestion completed = familyQuestionRepository.findById(familyQuestion.getId())
                .orElseThrow();
        assertThat(completed.isCompleted()).isTrue();
        assertThat(completed.getInsightJson()).isNotBlank();
        assertThat(answerRepository.countByFamilyQuestion(completed))
                .isEqualTo(completed.getRequiredMemberCount().longValue());
    }

    private User saveUser(Family family, String loginId, RoleType roleType, int birthYear) {
        User user = TestDataFactory.user(loginId, roleType, birthYear);
        family.addMember(user);
        User saved = userRepository.save(user);
        userRepository.flush();
        return saved;
    }

    private void submitAnswer(Long userId, Long familyQuestionId, String content) {
        AnswerRequest request = new AnswerRequest();
        request.setContent(content);
        answerService.submitAnswer(userId, familyQuestionId, request);
    }
}
