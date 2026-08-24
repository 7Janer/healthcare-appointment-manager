package com.clinic.ai.repository;
import com.clinic.ai.domain.AiSummaryCache;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface AiSummaryCacheRepository extends JpaRepository<AiSummaryCache,UUID>{Optional<AiSummaryCache> findByPromptTypeAndInputHash(String promptType,String inputHash);}
