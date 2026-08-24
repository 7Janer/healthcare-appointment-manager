package com.clinic.ai.controller;
import com.clinic.ai.dto.AiDtos.*;import com.clinic.ai.service.AiSummaryService;import jakarta.validation.Valid;import org.springframework.beans.factory.annotation.Value;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/internal/ai") public class AiController{
 private final AiSummaryService service;private final String key;public AiController(AiSummaryService service,@Value("${app.internal-api-key}")String key){this.service=service;this.key=key;}
 @PostMapping("/pre-visit") public PreVisitResponse pre(@RequestHeader("X-Internal-Key")String provided,@Valid @RequestBody PreVisitRequest r){check(provided);return service.preVisit(r.symptoms());}
 @PostMapping("/post-visit") public PostVisitResponse post(@RequestHeader("X-Internal-Key")String provided,@Valid @RequestBody PostVisitRequest r){check(provided);return service.postVisit(r.clinicalNotes(),r.prescription());}
 private void check(String p){if(!key.equals(p))throw new SecurityException("Invalid internal credential");}
}
