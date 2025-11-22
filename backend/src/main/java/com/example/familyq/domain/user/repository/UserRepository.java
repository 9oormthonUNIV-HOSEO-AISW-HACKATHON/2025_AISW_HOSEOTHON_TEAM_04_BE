package com.example.familyq.domain.user.repository;

import com.example.familyq.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    @EntityGraph(attributePaths = {"family"})
    Optional<User> findWithFamilyById(Long id);
}
