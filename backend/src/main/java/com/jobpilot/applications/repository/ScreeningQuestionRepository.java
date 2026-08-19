package com.jobpilot.applications.repository;

import com.jobpilot.applications.entity.ScreeningQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScreeningQuestionRepository extends JpaRepository<ScreeningQuestion, UUID> {
    List<ScreeningQuestion> findByApplicationId(UUID applicationId);
}
