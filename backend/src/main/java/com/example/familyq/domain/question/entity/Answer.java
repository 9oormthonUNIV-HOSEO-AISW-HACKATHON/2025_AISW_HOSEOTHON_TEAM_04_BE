package com.example.familyq.domain.question.entity;

import com.example.familyq.domain.user.entity.User;
import com.example.familyq.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "answers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_family_question_user",
                        columnNames = {"family_question_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Answer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_question_id", nullable = false)
    private FamilyQuestion familyQuestion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Builder
    public Answer(FamilyQuestion familyQuestion, User user, String content) {
        this.familyQuestion = familyQuestion;
        this.user = user;
        this.content = content;
        if (familyQuestion != null) {
            familyQuestion.addAnswer(this);
        }
        if (user != null) {
            user.getAnswers().add(this);
        }
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
