package com.example.familyq.domain.family.repository;

import com.example.familyq.domain.family.entity.Family;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findByFamilyCode(String familyCode);

    boolean existsByFamilyCode(String familyCode);

    @EntityGraph(attributePaths = {"members"})
    Optional<Family> findWithMembersById(Long id);
}
