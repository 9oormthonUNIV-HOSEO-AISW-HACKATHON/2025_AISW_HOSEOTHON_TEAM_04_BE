package com.example.familyq.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id; // 사용자 고유 ID

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // 로그인용 아이디

    @Column(name = "name", nullable = false)
    private String name; // 이름 (프로필 표시 이름)

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "day", nullable = false)
    private Integer day;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, nullable = true)
    private LocalDateTime updatedAt; // 정보 수정일시

    @Builder
    public User(String userId, String name, String password, String role, Integer year, Integer month, Integer day) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.password = password;
        this.year = year;
        this.month = month;
        this.day = day;
    }
}