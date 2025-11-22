package com.example.familyq.domain.question.entity;

import com.example.familyq.domain.family.entity.Family;
import com.example.familyq.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "family_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_family_question_sequence",
                        columnNames = {"family_id", "sequence_number"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FamilyQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FamilyQuestionStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "required_member_count", nullable = false)
    private Integer requiredMemberCount;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "insight_json", columnDefinition = "LONGTEXT")
    private String insightJson;

    @Builder.Default
    @OneToMany(mappedBy = "familyQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    public boolean isCompleted() {
        return FamilyQuestionStatus.COMPLETED.equals(this.status);
    }

    public void markCompleted(LocalDateTime completedAt) {
        this.status = FamilyQuestionStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void saveInsightJson(String insightJson) {
        this.insightJson = insightJson;
    }

    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }
}
