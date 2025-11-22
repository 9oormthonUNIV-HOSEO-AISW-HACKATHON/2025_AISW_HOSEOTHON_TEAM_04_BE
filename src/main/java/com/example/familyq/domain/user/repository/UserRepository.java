package com.example.familyq.domain.user.repository;

import com.example.familyq.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String nickname); // 닉네임으로 유저 검색
    Optional<User> findByUserId(String userLoginId);
    Optional<User> findByRole(String email);
}