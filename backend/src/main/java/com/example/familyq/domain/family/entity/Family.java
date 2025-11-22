package com.example.familyq.domain.family.entity;

import com.example.familyq.domain.question.entity.FamilyQuestion;
import com.example.familyq.domain.user.entity.User;
import com.example.familyq.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "families",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_family_code", columnNames = "family_code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Family extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_code", nullable = false, length = 8)
    private String familyCode;

    @Builder.Default
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FamilyQuestion> familyQuestions = new ArrayList<>();

    public void updateFamilyCode(String familyCode) {
        this.familyCode = familyCode;
    }

    public void addMember(User user) {
        this.members.add(user);
        user.assignFamily(this);
    }

    public void removeMember(User user) {
        this.members.remove(user);
        user.leaveFamily();
    }
}
