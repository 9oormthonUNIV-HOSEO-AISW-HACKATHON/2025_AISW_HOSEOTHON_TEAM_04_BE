package com.example.familyq.domain.question.service;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.domain.family.repository.FamilyRepository;
import com.example.familyq.domain.question.dto.QuestionCreateRequest;
import com.example.familyq.domain.question.entity.Question;
import com.example.familyq.domain.question.repository.QuestionRepository;
import com.example.familyq.domain.user.entity.RoleType;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.domain.user.repository.UserRepository;
import com.example.familyq.global.exception.BusinessException;
import com.example.familyq.support.IntegrationTestSupport;
import com.example.familyq.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionAdminServiceTest extends IntegrationTestSupport {

    @Autowired
    private QuestionAdminService questionAdminService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 질문은_추가된_순서대로_순번이_증가한다() {
        QuestionCreateRequest first = new QuestionCreateRequest();
        first.setText("첫 번째 질문");
        questionAdminService.createQuestion(first);

        QuestionCreateRequest second = new QuestionCreateRequest();
        second.setText("두 번째 질문");
        questionAdminService.createQuestion(second);

        assertThat(questionRepository.findAllByOrderByOrderIndexAsc())
                .extracting(Question::getOrderIndex)
                .containsExactly(1, 2);
    }

    @Test
    void 특정_순번에_삽입시_이후_질문_순번이_이동한다() {
        seedQuestions("질문1", "질문2");

        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setText("새 질문");
        request.setOrderIndex(1);
        questionAdminService.createQuestion(request);

        assertThat(questionRepository.findAllByOrderByOrderIndexAsc())
                .extracting(Question::getText)
                .containsExactly("새 질문", "질문1", "질문2");
    }

    @Test
    void 사용중인_질문은_삭제할_수_없다() {
        Question question = seedQuestions("질문1")[0];
        Family family = familyRepository.save(TestDataFactory.family("QADMIN"));
        User user = userRepository.save(TestDataFactory.user("admin_user", RoleType.FATHER, 1980));
        family.addMember(user);
        questionService.getTodayQuestion(user.getId()); // 첫 질문 생성

        assertThatThrownBy(() -> questionAdminService.deleteQuestion(question.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 질문을_삭제하면_이후_순번이_앞으로_땡겨진다() {
        Question[] questions = seedQuestions("질문1", "질문2", "질문3");

        questionAdminService.deleteQuestion(questions[0].getId());

        assertThat(questionRepository.findAllByOrderByOrderIndexAsc())
                .extracting(Question::getOrderIndex)
                .containsExactly(1, 2);
        assertThat(questionRepository.findAllByOrderByOrderIndexAsc())
                .extracting(Question::getText)
                .containsExactly("질문2", "질문3");
    }

    private Question[] seedQuestions(String... texts) {
        questionRepository.deleteAll();
        Question[] questions = new Question[texts.length];
        for (int i = 0; i < texts.length; i++) {
            questions[i] = questionRepository.save(Question.builder()
                    .text(texts[i])
                    .orderIndex(i + 1)
                    .build());
        }
        return questions;
    }
}
