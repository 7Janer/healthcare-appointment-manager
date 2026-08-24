package com.clinic.ai.service;

import com.clinic.ai.repository.AiSummaryCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiSummaryServiceTest {
    @Test void usesDeterministicFallbackWhenGeminiAndCacheAreUnavailable() {
        GeminiClient gemini = mock(GeminiClient.class);
        AiSummaryCacheRepository cache = mock(AiSummaryCacheRepository.class);
        when(gemini.primary(anyString())).thenThrow(new IllegalStateException("offline"));
        when(gemini.secondary(anyString())).thenThrow(new IllegalStateException("offline"));
        when(cache.findByPromptTypeAndInputHash(anyString(), anyString())).thenReturn(Optional.empty());
        AiSummaryService service = new AiSummaryService(gemini, cache, new ObjectMapper());

        var result = service.preVisit("Severe chest pain and difficulty breathing");

        assertThat(result.urgencyLevel()).isEqualTo("HIGH");
        assertThat(result.suggestedQuestions()).hasSize(3);
        assertThat(result.generatedBy()).isEqualTo("RULES");
    }
}
