package com.example.familyq.domain.question.entity;

import com.example.familyq.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "questions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_question_order", columnNames = "order_index")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Builder
    public Question(String text, Integer orderIndex) {
        this.text = text;
        this.orderIndex = orderIndex;
    }

    public void updateText(String text) {
        this.text = text;
    }

    public void updateOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
